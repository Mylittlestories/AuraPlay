package com.auraplay.player.audio

import android.content.Context
import android.media.audiofx.*
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Advanced Audio Engine with DAC-quality processing
 * Supports: Equalizer, Bass Boost, Virtualizer, Reverb, Loudness Enhancer
 */
@Singleton
class AudioEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "AudioEngine"
    }

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var presetReverb: PresetReverb? = null

    private val _equalizerSettings = MutableStateFlow(EqualizerSettings())
    val equalizerSettings: StateFlow<EqualizerSettings> = _equalizerSettings.asStateFlow()

    private val _bassBoostSettings = MutableStateFlow(BassBoostSettings())
    val bassBoostSettings: StateFlow<BassBoostSettings> = _bassBoostSettings.asStateFlow()

    private val _virtualizerSettings = MutableStateFlow(VirtualizerSettings())
    val virtualizerSettings: StateFlow<VirtualizerSettings> = _virtualizerSettings.asStateFlow()

    private val _loudnessSettings = MutableStateFlow(LoudnessSettings())
    val loudnessSettings: StateFlow<LoudnessSettings> = _loudnessSettings.asStateFlow()

    private val _reverbSettings = MutableStateFlow(ReverbSettings())
    val reverbSettings: StateFlow<ReverbSettings> = _reverbSettings.asStateFlow()

    private val _volumeSettings = MutableStateFlow(VolumeSettings())
    val volumeSettings: StateFlow<VolumeSettings> = _volumeSettings.asStateFlow()

    private var audioSessionId: Int = 0

    fun initialize(sessionId: Int) {
        release()
        audioSessionId = sessionId
        Log.d(TAG, "Initializing audio effects for session: $sessionId")

        try {
            equalizer = Equalizer(0, sessionId).apply {
                enabled = true
            }
            Log.d(TAG, "Equalizer initialized: ${equalizer?.numberOfBands()} bands")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to init equalizer", e)
        }

        try {
            bassBoost = BassBoost(0, sessionId).apply {
                enabled = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to init bass boost", e)
        }

        try {
            virtualizer = Virtualizer(0, sessionId).apply {
                enabled = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to init virtualizer", e)
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                loudnessEnhancer = LoudnessEnhancer(sessionId).apply {
                    enabled = true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to init loudness enhancer", e)
        }

        try {
            presetReverb = PresetReverb(0, sessionId).apply {
                enabled = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to init reverb", e)
        }

        updateStates()
    }

    // ==================== Equalizer ====================

    fun getEqualizerBandCount(): Int = equalizer?.numberOfBands()?.toInt() ?: 0

    fun getEqualizerBandFrequencies(): List<Int> {
        return equalizer?.let { eq ->
            (0 until eq.numberOfBands().toInt()).map { i ->
                eq.getCenterFreq(i.toShort()) / 1000 // Convert mHz to Hz
            }
        } ?: emptyList()
    }

    fun getEqualizerBandNames(): List<String> {
        return equalizer?.let { eq ->
            (0 until eq.numberOfBands().toInt()).map { i ->
                eq.getBand(i.toShort()).toString()
            }
        } ?: emptyList()
    }

    fun getEqualizerPresets(): List<String> {
        return equalizer?.let { eq ->
            (0 until eq.numberOfPresets().toInt()).map { i ->
                eq.getPresetName(i.toShort())
            }
        } ?: emptyList()
    }

    fun setEqualizerBand(band: Short, level: Short) {
        equalizer?.let { eq ->
            val clampedLevel = level.coerceIn(eq.bandLevelRange[0], eq.bandLevelRange[1])
            eq.setBandLevel(band, clampedLevel)
            updateEqualizerState()
        }
    }

    fun setEqualizerPreset(preset: Short) {
        equalizer?.usePreset(preset)
        updateEqualizerState()
    }

    fun getEqualizerBandLevel(band: Short): Short {
        return equalizer?.getBandLevel(band) ?: 0
    }

    fun getEqualizerBandLevelRange(): Pair<Short, Short> {
        return equalizer?.let {
            Pair(it.bandLevelRange[0], it.bandLevelRange[1])
        } ?: Pair(-1500, 1500)
    }

    // ==================== Bass Boost ====================

    fun setBassBoostStrength(strength: Short) {
        bassBoost?.setStrength(strength)
        updateBassBoostState()
    }

    fun getBassBoostStrength(): Short = bassBoost?.roundedStrength ?: 0

    // ==================== Virtualizer ====================

    fun setVirtualizerStrength(strength: Short) {
        virtualizer?.setStrength(strength)
        updateVirtualizerState()
    }

    fun getVirtualizerStrength(): Short = virtualizer?.roundedStrength ?: 0

    // ==================== Loudness Enhancer ====================

    fun setLoudnessGain(gainMb: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            loudnessEnhancer?.setTargetGain(gainMb)
            updateLoudnessState()
        }
    }

    // ==================== Reverb ====================

    fun setReverbPreset(preset: Short) {
        presetReverb?.preset = preset
        updateReverbState()
    }

    // ==================== Volume ====================

    fun setVolume(left: Float, right: Float) {
        _volumeSettings.value = VolumeSettings(left = left, right = right)
    }

    // ==================== Presets ====================

    fun applyPreset(preset: AudioPreset) {
        when (preset) {
            AudioPreset.FLAT -> {
                equalizer?.let { eq ->
                    for (i in 0 until eq.numberOfBands()) {
                        eq.setBandLevel(i.toShort(), 0)
                    }
                }
                bassBoost?.setStrength(0)
                virtualizer?.setStrength(0)
                loudnessEnhancer?.setTargetGain(0)
            }
            AudioPreset.BASS_BOOST -> {
                applyCustomCurve(listOf(600, 500, 300, 100, 0, 0, 0, 0, 0, 0))
                bassBoost?.setStrength(800)
                virtualizer?.setStrength(400)
            }
            AudioPreset.VOCAL -> {
                applyCustomCurve(listOf(-200, -100, 0, 200, 400, 400, 200, 0, -100, -200))
                bassBoost?.setStrength(0)
                virtualizer?.setStrength(200)
            }
            AudioPreset.ROCK -> {
                applyCustomCurve(listOf(500, 300, -100, -300, -100, 200, 400, 500, 500, 500))
                bassBoost?.setStrength(400)
                virtualizer?.setStrength(600)
            }
            AudioPreset.POP -> {
                applyCustomCurve(listOf(-100, 100, 300, 400, 300, 0, -100, -100, 0, 0))
                bassBoost?.setStrength(300)
                virtualizer?.setStrength(300)
            }
            AudioPreset.JAZZ -> {
                applyCustomCurve(listOf(300, 100, 0, 200, -200, -200, 0, 200, 300, 400))
                bassBoost?.setStrength(200)
                virtualizer?.setStrength(400)
            }
            AudioPreset.CLASSICAL -> {
                applyCustomCurve(listOf(400, 300, 200, 100, -100, -100, 0, 200, 300, 400))
                bassBoost?.setStrength(100)
                virtualizer?.setStrength(500)
            }
            AudioPreset.ELECTRONIC -> {
                applyCustomCurve(listOf(500, 400, 100, 0, -200, 200, 0, -100, 400, 500))
                bassBoost?.setStrength(700)
                virtualizer?.setStrength(700)
            }
            AudioPreset.HIPHOP -> {
                applyCustomCurve(listOf(500, 500, 300, 100, -100, -200, 100, 0, 200, 300))
                bassBoost?.setStrength(800)
                virtualizer?.setStrength(400)
            }
            AudioPreset.ACOUSTIC -> {
                applyCustomCurve(listOf(300, 200, 0, 100, 200, 200, 200, 300, 300, 200))
                bassBoost?.setStrength(300)
                virtualizer?.setStrength(300)
            }
            AudioPreset.BASS_AND_TREBLE -> {
                applyCustomCurve(listOf(500, 300, 0, -200, -300, -300, -200, 0, 300, 500))
                bassBoost?.setStrength(600)
                virtualizer?.setStrength(400)
            }
        }
        updateStates()
    }

    private fun applyCustomCurve(levels: List<Int>) {
        equalizer?.let { eq ->
            val bandCount = eq.numberOfBands().toInt()
            for (i in 0 until minOf(bandCount, levels.size)) {
                val clamped = levels[i].toShort().coerceIn(eq.bandLevelRange[0], eq.bandLevelRange[1])
                eq.setBandLevel(i.toShort(), clamped)
            }
        }
    }

    private fun updateEqualizerState() {
        equalizer?.let { eq ->
            val bands = (0 until eq.numberOfBands().toInt()).map { i ->
                eq.getBandLevel(i.toShort())
            }
            _equalizerSettings.value = EqualizerSettings(
                isEnabled = eq.enabled,
                bandLevels = bands,
                currentPreset = eq.curPreset.toInt()
            )
        }
    }

    private fun updateBassBoostState() {
        bassBoost?.let { bb ->
            _bassBoostSettings.value = BassBoostSettings(
                isEnabled = bb.enabled,
                strength = bb.roundedStrength
            )
        }
    }

    private fun updateVirtualizerState() {
        virtualizer?.let { v ->
            _virtualizerSettings.value = VirtualizerSettings(
                isEnabled = v.enabled,
                strength = v.roundedStrength
            )
        }
    }

    private fun updateLoudnessState() {
        loudnessEnhancer?.let { le ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                _loudnessSettings.value = LoudnessSettings(
                    isEnabled = le.enabled,
                    gainMb = le.targetGain.toInt()
                )
            }
        }
    }

    private fun updateReverbState() {
        presetReverb?.let { r ->
            _reverbSettings.value = ReverbSettings(
                isEnabled = r.enabled,
                preset = r.preset
            )
        }
    }

    private fun updateStates() {
        updateEqualizerState()
        updateBassBoostState()
        updateVirtualizerState()
        updateLoudnessState()
        updateReverbState()
    }

    fun release() {
        try { equalizer?.release() } catch (_: Exception) {}
        try { bassBoost?.release() } catch (_: Exception) {}
        try { virtualizer?.release() } catch (_: Exception) {}
        try { loudnessEnhancer?.release() } catch (_: Exception) {}
        try { presetReverb?.release() } catch (_: Exception) {}
        equalizer = null
        bassBoost = null
        virtualizer = null
        loudnessEnhancer = null
        presetReverb = null
    }

    fun setEqualizerEnabled(enabled: Boolean) {
        equalizer?.enabled = enabled
        updateEqualizerState()
    }

    fun setBassBoostEnabled(enabled: Boolean) {
        bassBoost?.enabled = enabled
        updateBassBoostState()
    }

    fun setVirtualizerEnabled(enabled: Boolean) {
        virtualizer?.enabled = enabled
        updateVirtualizerState()
    }

    fun setLoudnessEnabled(enabled: Boolean) {
        loudnessEnhancer?.enabled = enabled
        updateLoudnessState()
    }

    fun setReverbEnabled(enabled: Boolean) {
        presetReverb?.enabled = enabled
        updateReverbState()
    }
}

// Data classes for audio settings
data class EqualizerSettings(
    val isEnabled: Boolean = false,
    val bandLevels: List<Short> = emptyList(),
    val currentPreset: Int = -1
)

data class BassBoostSettings(
    val isEnabled: Boolean = false,
    val strength: Short = 0
)

data class VirtualizerSettings(
    val isEnabled: Boolean = false,
    val strength: Short = 0
)

data class LoudnessSettings(
    val isEnabled: Boolean = false,
    val gainMb: Int = 0
)

data class ReverbSettings(
    val isEnabled: Boolean = false,
    val preset: Short = 0
)

data class VolumeSettings(
    val left: Float = 1.0f,
    val right: Float = 1.0f
)

enum class AudioPreset {
    FLAT, BASS_BOOST, VOCAL, ROCK, POP, JAZZ, CLASSICAL,
    ELECTRONIC, HIPHOP, ACOUSTIC, BASS_AND_TREBLE
}