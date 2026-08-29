package com.lostf1sh.pixelplayeross.data.service.player

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.AudioProcessingPipeline
import androidx.media3.common.audio.ToInt16PcmAudioProcessor
import com.google.common.collect.ImmutableList
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.random.Random

/**
 * Regression tests for the [AudiophileAudioProcessor] buffer lifecycle, driven
 * through the REAL [AudioProcessingPipeline] exactly the way
 * `DefaultAudioSink` drives it.
 *
 * v2.3.0 shipped a processor whose passthrough path did
 * `outputBuffer.put(inputBuffer)` with `outputBuffer == inputBuffer ==
 * AudioProcessor.EMPTY_BUFFER`. The pipeline feeds processors with
 * `EMPTY_BUFFER` from `getOutput()` → `processData(EMPTY_BUFFER)` once the
 * current input is exhausted, and `ByteBuffer.put` throws
 * `IllegalArgumentException("The source buffer is this buffer")` when source
 * and target are the same instance — killing playback on every track a few
 * dozen milliseconds in. These tests pin the contract so it cannot regress.
 */
class AudiophileAudioProcessorTest {

    private companion object {
        const val SR = 48_000
        const val CHANNELS = 2
        const val FRAME_BYTES = 4 // 16-bit stereo
    }

    private fun buildPipeline(dsp: AudiophileDspState): AudioProcessingPipeline {
        val pipeline = AudioProcessingPipeline(
            ImmutableList.of<AudioProcessor>(
                ToInt16PcmAudioProcessor(),
                AudiophileAudioProcessor(dsp)
            )
        )
        pipeline.configure(AudioProcessor.AudioFormat(SR, CHANNELS, C.ENCODING_PCM_16BIT))
        pipeline.flush()
        return pipeline
    }

    private fun bufferOf(frames: Int, rng: Random): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(frames * FRAME_BYTES).order(ByteOrder.nativeOrder())
        repeat(frames * CHANNELS) { buffer.putShort(rng.nextInt(-30000, 30000).toShort()) }
        buffer.flip()
        return buffer
    }

    /** Simulates DefaultAudioSink.processBuffers: drain output, then queue input. */
    private fun processBuffer(pipeline: AudioProcessingPipeline, input: ByteBuffer): Long {
        var drained = 0L
        while (!pipeline.isEnded()) {
            while (true) {
                val out = pipeline.getOutput()
                if (!out.hasRemaining()) break
                drained += out.remaining().toLong()
                out.position(out.limit())
            }
            if (!input.hasRemaining()) break
            pipeline.queueInput(input)
        }
        return drained
    }

    @Test
    fun emptyInputFeed_doesNotThrow() {
        // The exact v2.3.0 crash: after draining the first output, the sink's
        // getOutput() re-enters processData with EMPTY_BUFFER.
        val dsp = AudiophileDspState()
        val pipeline = buildPipeline(dsp)
        val input = bufferOf(2048, Random(42))
        pipeline.queueInput(input)
        // Drain once, then keep polling getOutput() like the sink does while
        // waiting for the next decoder buffer — this feeds EMPTY_BUFFER in.
        repeat(8) {
            while (true) {
                val out = pipeline.getOutput()
                if (!out.hasRemaining()) break
                out.position(out.limit())
            }
        }
        // Reaching this point without IllegalArgumentException is the assertion.
    }

    @Test
    fun passthrough_conservBytes_andConsumesInput() {
        val dsp = AudiophileDspState() // all DSP off
        val pipeline = buildPipeline(dsp)
        val rng = Random(7)
        var inBytes = 0L
        var outBytes = 0L
        repeat(100) {
            val input = bufferOf(2048, rng)
            inBytes += input.remaining()
            outBytes += processBuffer(pipeline, input)
            assertFalse("input must be fully consumed", input.hasRemaining())
        }
        assertEquals(inBytes, outBytes)
    }

    @Test
    fun limiterWithTap_conservBytes_endOfStreamTerminates() {
        val dsp = AudiophileDspState().apply {
            limiterEnabled = true
            preampDb = -4f
            tapActive = true
        }
        val pipeline = buildPipeline(dsp)
        val rng = Random(11)
        var inBytes = 0L
        var outBytes = 0L
        repeat(50) {
            val input = bufferOf(1024, rng)
            inBytes += input.remaining()
            outBytes += processBuffer(pipeline, input)
        }
        pipeline.queueEndOfStream()
        var guard = 0
        while (!pipeline.isEnded() && guard++ < 1_000_000) {
            while (true) {
                val out = pipeline.getOutput()
                if (!out.hasRemaining()) break
                outBytes += out.remaining().toLong()
                out.position(out.limit())
            }
        }
        assertTrue("pipeline must end after EOS", pipeline.isEnded())
        assertEquals(inBytes, outBytes)
        // The visualizer tap must have seen data.
        assertTrue(dsp.copyTap(FloatArray(AudiophileDspState.TAP_SIZE)))
    }

    @Test
    fun formatChange_afterFlush_keepsWorking() {
        val dsp = AudiophileDspState()
        val processor = AudiophileAudioProcessor(dsp)
        val pipeline = AudioProcessingPipeline(ImmutableList.of<AudioProcessor>(processor))
        pipeline.configure(AudioProcessor.AudioFormat(SR, CHANNELS, C.ENCODING_PCM_16BIT))
        pipeline.flush()
        pipeline.queueInput(bufferOf(512, Random(3)))
        while (true) {
            val out = pipeline.getOutput()
            if (!out.hasRemaining()) break
            out.position(out.limit())
        }
        pipeline.queueEndOfStream()
        // Reconfigure for a new track at 44.1 kHz like the sink does.
        pipeline.configure(AudioProcessor.AudioFormat(44_100, CHANNELS, C.ENCODING_PCM_16BIT))
        pipeline.flush()
        val input = bufferOf(512, Random(4))
        pipeline.queueInput(input)
        var got = 0L
        while (true) {
            val out = pipeline.getOutput()
            if (!out.hasRemaining()) break
            got += out.remaining().toLong()
            out.position(out.limit())
        }
        assertEquals((512 * FRAME_BYTES).toLong(), got)
        assertFalse(input.hasRemaining())
    }

    @Test
    fun floatInput_conservBytes() {
        val dsp = AudiophileDspState().apply { tapActive = true }
        val processor = AudiophileAudioProcessor(dsp)
        val pipeline = AudioProcessingPipeline(ImmutableList.of<AudioProcessor>(processor))
        pipeline.configure(AudioProcessor.AudioFormat(SR, CHANNELS, C.ENCODING_PCM_FLOAT))
        pipeline.flush()
        val rng = Random(5)
        var inBytes = 0L
        var outBytes = 0L
        repeat(20) {
            val input = ByteBuffer.allocateDirect(1024 * 8).order(ByteOrder.nativeOrder())
            repeat(1024 * CHANNELS) { input.putFloat(rng.nextFloat() * 1.5f - 0.75f) }
            input.flip()
            inBytes += input.remaining()
            outBytes += processBuffer(pipeline, input)
        }
        // Float (4 bytes/sample) → the processor re-emits float unchanged.
        assertEquals(inBytes, outBytes)
    }
}
