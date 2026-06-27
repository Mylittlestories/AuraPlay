package com.auraplay.player.audio

import android.content.Context
import android.media.audiofx.Equalizer
import android.media.audiofx.BassBoost
import android.media.audiofx.Virtualizer
import android.media.audiofx.LoudnessEnhancer
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class EqualizerState(
    val bands: List<EqualizerBand> = emptyList(),
    val bassBoost: Int = 0,
    val virtualizerStrength: Int = 0,
    val loudnessGain: Int = 0,
    val presetName: String = "Flat",
    val enabled: Boolean = true
)

data class EqualizerBand(
    val freq: Int,
    val level: Int = 0,
    val minLevel: Short = -1500,
    val maxLevel: Short = 1500
)

@javax.inject.Singleton
class AudioEngine @javax.inject.Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context
) {
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null

    private val _eqState = MutableStateFlow(EqualizerState())
    val eqState: StateFlow<EqualizerState> = _eqState.asStateFlow()

    val presets = listOf(
        "Flat", "Bass Boost", "Vocal", "Rock", "Pop",
        "Jazz", "Classical", "Electronic", "Hip-Hop", "Acoustic", "Bass & Treble"
    )

    private val presetLevels = mapOf(
        "Flat" to listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "Bass Boost" to listOf(800, 600, 400, 200, 0, 0, 0, 0, 0, 0),
        "Vocal" to listOf(-200, 0, 200, 400, 600, 600, 400, 200, 0, -200),
        "Rock" to listOf(400, 300, 100, -100, -200, -100, 100, 300, 400, 400),
        "Pop" to listOf(-100, 200, 400, 500, 400, 200, 0, -100, -200, -100),
        "Jazz" to listOf(300, 200, 0, -100, -200, -200, 0, 100, 200, 300),
        "Classical" to listOf(400, 300, 100, 100, 0, 0, 0, 100, 200, 400),
        "Electronic" to listOf(600, 500, 200, 0, -100, -100, 0, 200, 500, 600),
        "Hip-Hop" to listOf(500, 400, 100, -100, -200, -100, 0, 100, 300, 400),
        "Acoustic" to listOf(200, 100, 0, 200, 300, 300, 200, 100, 0, 0),
        "Bass & Treble" to listOf(600, 400, 0, -200, -200, -200, 0, 0, 400, 600)
    )

    fun setupPlayer(player: ExoPlayer) {
        try {
            val audioSessionId = player.audioSessionId

            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = true
            }

            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = true
                setStrength(0.toShort())
            }

            virtualizer = Virtualizer(0, audioSessionId).apply {
                enabled = true
                setStrength(0.toShort())
            }

            try {
                loudnessEnhancer = LoudnessEnhancer(audioSessionId).apply {
                    enabled = true
                    setTargetGain(0)
                }
            } catch (_: Exception) { }

            updateBandsFromEqualizer()
        } catch (_: Exception) { }
    }

    private fun updateBandsFromEqualizer() {
        val eq = equalizer ?: return
        val numBands = eq.numberOfBands.toInt()
        val bands = (0 until numBands).map { i ->
            val freq = eq.getCenterFreq(i.toShort()) / 1000
            val level = eq.getBandLevel(i.toShort()).toInt()
            EqualizerBand(
                freq = freq,
                level = level,
                minLevel = eq.bandLevelRange[0],
                maxLevel = eq.bandLevelRange[1]
            )
        }
        _eqState.value = _eqState.value.copy(bands = bands)
    }

    fun setBandLevel(bandIndex: Int, level: Int) {
        val eq = equalizer ?: return
        if (bandIndex >= eq.numberOfBands) return
        try {
            eq.setBandLevel(bandIndex.toShort(), level.toShort())
            updateBandsFromEqualizer()
            _eqState.value = _eqState.value.copy(presetName = "Custom")
        } catch (_: Exception) { }
    }

    fun setBassBoost(strength: Int) {
        try {
            bassBoost?.setStrength(strength.coerceIn(0, 1000).toShort())
            _eqState.value = _eqState.value.copy(bassBoost = strength)
        } catch (_: Exception) { }
    }

    fun setVirtualizer(strength: Int) {
        try {
            virtualizer?.setStrength(strength.coerceIn(0, 1000).toShort())
            _eqState.value = _eqState.value.copy(virtualizerStrength = strength)
        } catch (_: Exception) { }
    }

    fun setLoudness(gain: Int) {
        try {
            loudnessEnhancer?.setTargetGain(gain.coerceIn(0, 5000))
            _eqState.value = _eqState.value.copy(loudnessGain = gain)
        } catch (_: Exception) { }
    }

    fun applyPreset(presetName: String) {
        val levels = presetLevels[presetName] ?: return
        val eq = equalizer ?: return
        try {
            levels.forEachIndexed { i, level ->
                if (i < eq.numberOfBands) {
                    eq.setBandLevel(i.toShort(), level.toShort())
                }
            }
            updateBandsFromEqualizer()
            _eqState.value = _eqState.value.copy(presetName = presetName)
        } catch (_: Exception) { }
    }

    fun release() {
        equalizer?.release()
        bassBoost?.release()
        virtualizer?.release()
        loudnessEnhancer?.release()
    }
}
