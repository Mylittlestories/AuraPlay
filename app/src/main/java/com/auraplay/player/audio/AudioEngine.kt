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

    private val _equalizerSettings = MutableStateFlow(EqualizerSettings())
    val equalizerSettings: StateFlow<EqualizerSettings> = _equalizerSettings.asStateFlow()

    private val _bassBoostSettings = MutableStateFlow(BassBoostSettings())
    val bassBoostSettings: StateFlow<BassBoostSettings> = _bassBoostSettings.asStateFlow()

    private val _virtualizerSettings = MutableStateFlow(VirtualizerSettings())
    val virtualizerSettings: StateFlow<VirtualizerSettings> = _virtualizerSettings.asStateFlow()

    private val _loudnessSettings = MutableStateFlow(LoudnessSettings())
    val loudnessSettings: StateFlow<LoudnessSettings> = _loudnessSettings.asStateFlow()

    fun initialize(sessionId: Int) {
        release()
        Log.d(TAG, "Initializing audio effects for session: $sessionId")

        try {
            equalizer = Equalizer(0, sessionId).apply { enabled = true }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to init equalizer", e)
        }

        try {
            bassBoost = BassBoost(0, sessionId).apply { enabled = true }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to init bass boost", e)
        }

        try {
            virtualizer = Virtualizer(0, sessionId).apply { enabled = true }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to init virtualizer", e)
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                loudnessEnhancer = LoudnessEnhancer(sessionId).apply { enabled = true }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to init loudness enhancer", e)
        }

        updateStates()
    }

    fun getEqualizerBandCount(): Int = equalizer?.numberOfBands?.toInt() ?: 0

    fun getEqualizerBandFrequencies(): List<Int> {
        return equalizer?.let { eq ->
            (0 until eq.numberOfBands.toInt()).map { i ->
                eq.getCenterFreq(i.toShort()) / 1000
            }
        } ?: emptyList()
    }

    fun getEqualizerPresets(): List<String> {
        return equalizer?.let { eq ->
            (0 until eq.numberOfPresets.toInt()).map { i ->
                eq.getPresetName(i.toShort())
            }
        } ?: emptyList()
    }

    fun setEqualizerBand(band: Short, level: Short) {
        equalizer?.let { eq ->
            val clamped = level.coerceIn(eq.bandLevelRange[0], eq.bandLevelRange[1])
            eq.setBandLevel(band, clamped)
            updateEqualizerState()
        }
    }

    fun setEqualizerPreset(preset: Short) {
        equalizer?.usePreset(preset)
        updateEqualizerState()
    }

    fun setBassBoostStrength(strength: Short) {
        bassBoost?.setStrength(strength)
        updateBassBoostState()
    }

    fun setVirtualizerStrength(strength: Short) {
        virtualizer?.setStrength(strength)
        updateVirtualizerState()
    }

    fun setLoudnessGain(gainMb: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            loudnessEnhancer?.setTargetGain(gainMb)
            updateLoudnessState()
        }
    }

    fun applyPreset(preset: AudioPreset) {
        when (preset) {
            AudioPreset.FLAT -> {
                equalizer?.let { eq ->
                    for (i in 0 until eq.numberOfBands) {
                        eq.setBandLevel(i.toShort(), 0)
                    }
                }
                bassBoost?.setStrength(0)
                virtualizer?.setStrength(0)
            }
            AudioPreset.BASS_BOOST -> {
                applyCustomCurve(listOf(600, 500, 300, 100, 0, 0, 0, 0, 0, 0))
                bassBoost?.setStrength(800)
                virtualizer?.setStrength(400)
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
            else -> {}
        }
        updateStates()
    }

    private fun applyCustomCurve(levels: List<Int>) {
        equalizer?.let { eq ->
            val bandCount = eq.numberOfBands.toInt()
            for (i in 0 until minOf(bandCount, levels.size)) {
                val clamped = levels[i].toShort().coerceIn(eq.bandLevelRange[0], eq.bandLevelRange[1])
                eq.setBandLevel(i.toShort(), clamped)
            }
        }
    }

    private fun updateEqualizerState() {
        equalizer?.let { eq ->
            val bands = (0 until eq.numberOfBands.toInt()).map { i ->
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

    private fun updateStates() {
        updateEqualizerState()
        updateBassBoostState()
        updateVirtualizerState()
        updateLoudnessState()
    }

    fun release() {
        try { equalizer?.release() } catch (_: Exception) {}
        try { bassBoost?.release() } catch (_: Exception) {}
        try { virtualizer?.release() } catch (_: Exception) {}
        try { loudnessEnhancer?.release() } catch (_: Exception) {}
        equalizer = null
        bassBoost = null
        virtualizer = null
        loudnessEnhancer = null
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
}

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

enum class AudioPreset {
    FLAT, BASS_BOOST, ROCK, POP, JAZZ, CLASSICAL,
    ELECTRONIC, HIPHOP, ACOUSTIC, BASS_AND_TREBLE
}