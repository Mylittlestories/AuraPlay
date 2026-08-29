package com.lostf1sh.pixelplayeross.data.service.player

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shared, thread-safe state for AuraPlay's in-app audiophile DSP stage.
 *
 * The values here are read on the real-time audio thread by
 * [AudiophileAudioProcessor] and written from the UI/pref layers, so every
 * field is `@Volatile` and cheap to read (no allocation, no locks).
 *
 * The class also owns the **PCM tap** used by the Now-Playing spectrum
 * visualizer: the audio thread publishes a short rolling window of the
 * *post-DSP* mono mixdown into a small ring buffer, and the UI side copies it
 * out at ~30 Hz to run an FFT. Tapping the stream this way — instead of using
 * `android.media.audiofx.Visualizer` — needs no RECORD_AUDIO permission and
 * reflects exactly what the listener hears after preamp/limiter processing.
 */
@Singleton
class AudiophileDspState @Inject constructor() {

    companion object {
        /** Rolling window captured for the visualizer. 2048 samples → 1 Hz-ish bins at 48 kHz. */
        const val TAP_SIZE = 2048

        /** Roughly 21 ms of audio at 48 kHz — plenty for a responsive spectrum. */
        const val TAP_BATCH = 1024
    }

    /** Global preamp applied before the limiter, in dB (-15..+12). */
    @Volatile
    var preampDb: Float = 0f

    /** When true, the brickwall true-peak limiter guards against clipping. */
    @Volatile
    var limiterEnabled: Boolean = false

    /** When true, all in-app DSP is bypassed (Pure Direct). */
    @Volatile
    var pureDirect: Boolean = false

    /** Set by the UI while a visualizer is on screen so the tap keeps running. */
    @Volatile
    var tapActive: Boolean = false

    // ------------------------------------------------------------------ Tap

    private val tapLock = Any()
    private val tapBuffer = FloatArray(TAP_SIZE)
    private var tapWriteIndex = 0

    /** True when at least one full batch has been written since the last reset. */
    @Volatile
    private var tapHasData = false

    /**
     * Publishes a mono mixdown batch on the audio thread. Called with already
     * mixed samples; the copy is a handful of microseconds even under the lock.
     */
    fun writeTap(mono: FloatArray, count: Int) {
        if (!tapActive || count <= 0) return
        synchronized(tapLock) {
            var index = tapWriteIndex
            for (i in 0 until count) {
                tapBuffer[index] = mono[i]
                index = (index + 1) % TAP_SIZE
            }
            tapWriteIndex = index
            tapHasData = true
        }
    }

    /**
     * Copies the current rolling window (oldest sample first) into [out].
     * Returns true when fresh audio data was available.
     */
    fun copyTap(out: FloatArray): Boolean {
        if (out.size < TAP_SIZE) return false
        synchronized(tapLock) {
            if (!tapHasData) return false
            val head = tapWriteIndex // next write position == oldest sample
            for (i in 0 until TAP_SIZE) {
                out[i] = tapBuffer[(head + i) % TAP_SIZE]
            }
            return true
        }
    }

    /** Clears the tap so a stale waveform is not shown before playback starts. */
    fun resetTap() {
        synchronized(tapLock) {
            tapHasData = false
            tapBuffer.fill(0f)
            tapWriteIndex = 0
        }
    }
}
