package com.lostf1sh.pixelplayeross.data.service.player

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.AudioProcessor.AudioFormat
import androidx.media3.common.util.UnstableApi
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * AuraPlay's audiophile DSP stage: **preamp → true-peak limiter → PCM tap**.
 *
 * Sits at the end of the Media3 audio processor chain (after the hi-res sample
 * rate cap and surround downmix) and offers three things:
 *
 *  1. **Preamp** — a clean gain trim (dB) applied in the float domain, used to
 *     make headroom for EQ boosts or to correct quiet recordings.
 *  2. **Brickwall limiter** — a stereo-linked, 2.5 ms-lookahead limiter with a
 *     fast attack and a ~60 ms exponential release, ceiling at −1 dBFS. It
 *     prevents the clipping that bass boost / EQ presets can otherwise cause,
 *     without pumping on normal programme material.
 *  3. **PCM tap** — publishes the post-DSP mono mixdown to
 *     [AudiophileDspState] so the Now-Playing spectrum visualizer can render
 *     the exact signal the listener hears (no RECORD_AUDIO permission needed).
 *
 * The processor is always active in the chain so the tap works even when the
 * DSP itself is idle; when there is nothing to do it falls back to a bulk
 * byte-copy passthrough. All parameters are read from the shared
 * [AudiophileDspState] on every buffer, so UI changes apply instantly without
 * rebuilding the player.
 */
@androidx.annotation.OptIn(UnstableApi::class)
class AudiophileAudioProcessor(
    private val dsp: AudiophileDspState
) : AudioProcessor {

    private companion object {
        private val NATIVE_ORDER = ByteOrder.nativeOrder()

        /** Limiter ceiling: −1 dBFS. */
        private const val LIMITER_CEILING_DB = -1.0
        private const val LOOKAHEAD_MS = 2.5
        private const val RELEASE_MS = 60.0
        private const val MAX_CHANNELS = 8
    }

    private var inputFormat: AudioFormat = AudioFormat.NOT_SET
    private var outputBuffer: ByteBuffer = AudioProcessor.EMPTY_BUFFER
    private var inputEnded = false

    // ---- DSP runtime state (rebuilt on configure) ----
    private var channels = 0
    private var sampleRate = 0
    private var preampGain = 1f
    private var lookaheadFrames = 0
    private var delayLines = Array(MAX_CHANNELS) { FloatArray(0) }
    private var delayWriteIndex = 0
    private var gainHistoryIndex = 0
    private var envelopeGain = 1f
    private var releaseCoef = 0.94f
    private val limiterCeiling = 10.0.pow(LIMITER_CEILING_DB / 20.0).toFloat()

    // ---- Scratch buffers ----
    private val frameSamples = FloatArray(MAX_CHANNELS)
    private val delayedSamples = FloatArray(MAX_CHANNELS)
    private val monoScratch = FloatArray(1)
    private val tapBatch = FloatArray(AudiophileDspState.TAP_BATCH)
    private var tapBatchCount = 0

    // ---- Sliding-window minimum over the gain history (lookahead window) ----
    private var windowMinBuffer = FloatArray(0)
    private var windowMinIndex = IntArray(0)
    private var windowMinHead = 0
    private var windowMinTail = 0
    private var windowMinCount = 0

    override fun configure(inputAudioFormat: AudioFormat): AudioFormat {
        inputFormat = inputAudioFormat
        channels = inputAudioFormat.channelCount
        sampleRate = inputAudioFormat.sampleRate
        preampGain = dbToGain(dsp.preampDb)
        lookaheadFrames = max(1, (sampleRate * LOOKAHEAD_MS / 1000.0).toInt())
        envelopeGain = 1f
        delayWriteIndex = 0
        gainHistoryIndex = 0
        releaseCoef = exp(-1.0 / (RELEASE_MS / 1000.0 * sampleRate)).toFloat()
        for (ch in 0 until MAX_CHANNELS) {
            delayLines[ch] = FloatArray(lookaheadFrames)
        }
        windowMinBuffer = FloatArray(lookaheadFrames + 1)
        windowMinIndex = IntArray(lookaheadFrames + 1)
        windowMinHead = 0
        windowMinTail = 0
        windowMinCount = 0
        tapBatchCount = 0
        // Format passes through unchanged.
        return inputAudioFormat
    }

    override fun isActive(): Boolean = true

    /** DSP (and sample parsing) is only supported for plain 16-bit/float PCM within [MAX_CHANNELS]. */
    private val formatSupported: Boolean
        get() = channels in 1..MAX_CHANNELS && (
            inputFormat.encoding == C.ENCODING_PCM_16BIT ||
                inputFormat.encoding == C.ENCODING_PCM_FLOAT
            )

    override fun queueInput(inputBuffer: ByteBuffer) {
        if (!isActive()) return

        val passthrough = !formatSupported ||
            dsp.pureDirect ||
            (!dsp.limiterEnabled && dsp.preampDb == 0f)

        if (passthrough && !dsp.tapActive) {
            // Nothing to do: bulk copy the bytes through.
            val size = inputBuffer.remaining()
            outputBuffer = ensureOutputBuffer(size)
            outputBuffer.put(inputBuffer)
            outputBuffer.flip()
            return
        }

        if (!formatSupported) {
            // Exotic layout: never touch the samples; the tap stays silent.
            val size = inputBuffer.remaining()
            outputBuffer = ensureOutputBuffer(size)
            outputBuffer.put(inputBuffer)
            outputBuffer.flip()
            inputBuffer.position(inputBuffer.limit())
            return
        }

        if (inputFormat.encoding == C.ENCODING_PCM_FLOAT) {
            processFloat(inputBuffer, passthrough)
        } else {
            process16Bit(inputBuffer, passthrough)
        }
        inputBuffer.position(inputBuffer.limit())
    }

    private fun processFloat(inputBuffer: ByteBuffer, passthrough: Boolean) {
        val floatInput = inputBuffer.duplicate().order(NATIVE_ORDER)
        val totalFrames = floatInput.remaining() / (channels * Float.SIZE_BYTES)
        outputBuffer = ensureOutputBuffer(totalFrames * channels * Float.SIZE_BYTES)

        if (passthrough) {
            // DSP idle, but the tap still needs the samples.
            repeat(totalFrames) {
                var mono = 0f
                for (ch in 0 until channels) {
                    val value = floatInput.float
                    frameSamples[ch] = value
                    mono += value
                }
                monoScratch[0] = mono / channels
                pushTap(monoScratch, 0, 1)
                for (ch in 0 until channels) {
                    outputBuffer.putFloat(frameSamples[ch])
                }
            }
        } else {
            repeat(totalFrames) {
                val frame = processFrame { _ -> floatInput.float }
                for (ch in 0 until channels) {
                    outputBuffer.putFloat(frame[ch])
                }
            }
        }
        outputBuffer.flip()
    }

    private fun process16Bit(inputBuffer: ByteBuffer, passthrough: Boolean) {
        val shortInput = inputBuffer.duplicate().order(NATIVE_ORDER)
        val totalFrames = shortInput.remaining() / (channels * Short.SIZE_BYTES)
        outputBuffer = ensureOutputBuffer(totalFrames * channels * Short.SIZE_BYTES)

        if (passthrough) {
            // DSP idle, but the tap still needs the samples.
            repeat(totalFrames) {
                var mono = 0f
                for (ch in 0 until channels) {
                    val value = shortInput.short / 32768f
                    frameSamples[ch] = value
                    mono += value
                }
                monoScratch[0] = mono / channels
                pushTap(monoScratch, 0, 1)
                for (ch in 0 until channels) {
                    outputBuffer.putShort(floatToShort(frameSamples[ch]))
                }
            }
        } else {
            repeat(totalFrames) {
                val frame = processFrame { _ -> shortInput.short / 32768f }
                for (ch in 0 until channels) {
                    outputBuffer.putShort(floatToShort(frame[ch]))
                }
            }
        }
        outputBuffer.flip()
    }

    /**
     * Reads one frame from [read], applies preamp + limiter, feeds the tap and
     * returns the processed samples via [delayedSamples].
     */
    private inline fun processFrame(read: (channel: Int) -> Float): FloatArray {
        var peak = 0f
        for (ch in 0 until channels) {
            val sample = read(ch) * preampGain
            frameSamples[ch] = sample
            peak = max(peak, kotlin.math.abs(sample))
        }

        // Gain this frame will need once it leaves the lookahead delay.
        val neededGain = if (peak > limiterCeiling && peak > 0f) {
            min(1f, limiterCeiling / peak)
        } else {
            1f
        }
        pushGain(neededGain)

        // Fetch the frame from `lookaheadFrames` ago and apply the smoothed gain.
        val windowMin = currentWindowMin()
        if (windowMin < envelopeGain) {
            envelopeGain = windowMin // attack is instant; lookahead hides it
        } else {
            envelopeGain = windowMin + (envelopeGain - windowMin) * releaseCoef
        }

        var appliedGain = envelopeGain
        var delayedPeak = 0f
        for (ch in 0 until channels) {
            delayedSamples[ch] = delayLines[ch][delayWriteIndex]
            delayedPeak = max(delayedPeak, kotlin.math.abs(delayedSamples[ch]))
        }
        // Hard brickwall guarantee: never let a releasing envelope exceed what
        // the sample being emitted actually needs.
        if (delayedPeak > limiterCeiling && delayedPeak > 0f) {
            appliedGain = min(appliedGain, limiterCeiling / delayedPeak)
        }
        for (ch in 0 until channels) {
            delayedSamples[ch] *= appliedGain
            delayLines[ch][delayWriteIndex] = frameSamples[ch]
        }
        delayWriteIndex = (delayWriteIndex + 1) % lookaheadFrames

        pushTap(delayedSamples, 0, channels)
        return delayedSamples
    }

    // -------------------------------------------------------- Sliding min

    private fun pushGain(value: Float) {
        // Evict samples that left the window.
        while (windowMinCount > 0 &&
            (windowMinIndex[windowMinHead] + lookaheadFrames) <= gainHistoryIndex
        ) {
            windowMinHead = (windowMinHead + 1) % windowMinIndex.size
            windowMinCount--
        }
        // Maintain monotonic increasing values in the deque.
        while (windowMinCount > 0) {
            val tail = (windowMinTail - 1 + windowMinIndex.size) % windowMinIndex.size
            if (windowMinBuffer[tail] >= value) {
                windowMinTail = tail
                windowMinCount--
            } else break
        }
        windowMinBuffer[windowMinTail] = value
        windowMinIndex[windowMinTail] = gainHistoryIndex
        windowMinTail = (windowMinTail + 1) % windowMinIndex.size
        windowMinCount++
        gainHistoryIndex++
    }

    private fun currentWindowMin(): Float {
        return if (windowMinCount > 0) windowMinBuffer[windowMinHead] else 1f
    }

    // -------------------------------------------------------- Tap helpers

    private fun pushTap(samples: FloatArray, offset: Int, count: Int) {
        if (!dsp.tapActive) return
        if (count == 1) {
            tapBatch[tapBatchCount++] = samples[offset]
            if (tapBatchCount >= tapBatch.size) flushTap()
            return
        }
        var mono = 0f
        for (i in 0 until count) mono += samples[offset + i]
        tapBatch[tapBatchCount++] = mono / count
        if (tapBatchCount >= tapBatch.size) flushTap()
    }

    private fun flushTap() {
        if (tapBatchCount > 0) {
            dsp.writeTap(tapBatch, tapBatchCount)
            tapBatchCount = 0
        }
    }

    // -------------------------------------------------------- Utilities

    private fun dbToGain(db: Float): Float = 10.0.pow(db / 20.0).toFloat()

    private fun floatToShort(value: Float): Short =
        (value.coerceIn(-1f, 1f) * 32767f).toInt().toShort()

    private fun ensureOutputBuffer(requiredCapacity: Int): ByteBuffer {
        return if (outputBuffer.capacity() < requiredCapacity) {
            ByteBuffer.allocateDirect(requiredCapacity).order(NATIVE_ORDER).also {
                outputBuffer = it
            }
        } else {
            outputBuffer.clear()
            outputBuffer
        }
    }

    override fun getOutput(): ByteBuffer {
        val pendingOutput = outputBuffer
        outputBuffer = AudioProcessor.EMPTY_BUFFER
        return pendingOutput
    }

    override fun isEnded(): Boolean =
        inputEnded && outputBuffer === AudioProcessor.EMPTY_BUFFER

    override fun queueEndOfStream() {
        flushTap()
        inputEnded = true
    }

    @Deprecated("Media3 AudioProcessor now prefers flush(StreamMetadata); kept for interface compatibility")
    override fun flush() {
        outputBuffer = AudioProcessor.EMPTY_BUFFER
        inputEnded = false
        envelopeGain = 1f
        delayWriteIndex = 0
        tapBatchCount = 0
    }

    override fun reset() {
        outputBuffer = AudioProcessor.EMPTY_BUFFER
        inputEnded = false
        inputFormat = AudioFormat.NOT_SET
        envelopeGain = 1f
        delayWriteIndex = 0
        tapBatchCount = 0
    }
}
