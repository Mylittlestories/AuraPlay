package com.auraplay.player.audio

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class EqualizerState(
    val bands: List<EqualizerBand> = emptyList(),
    val bassBoost: Int = 0,
    val virtualizerStrength: Int = 0,
    val loudnessGain: Int = 0,
    val presetName: String = "Flat",
    val enabled: Boolean = true,
    val isReady: Boolean = false,
    val errorMessage: String? = null
)

data class EqualizerBand(
    val freq: Int,
    val level: Int = 0,
    val minLevel: Short = -1500,
    val maxLevel: Short = 1500
)

@Singleton
class AudioEngine @Inject constructor() {
    private var currentSessionId: Int = C.AUDIO_SESSION_ID_UNSET
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null

    private val _eqState = MutableStateFlow(
        EqualizerState(
            bands = defaultBands(),
            isReady = false,
            errorMessage = "Start playback to attach the equalizer."
        )
    )
    val eqState: StateFlow<EqualizerState> = _eqState.asStateFlow()

    val presets = listOf(
        "Flat", "Bass Boost", "Vocal", "Rock", "Pop",
        "Jazz", "Classical", "Electronic", "Hip-Hop", "Acoustic", "Bass & Treble"
    )

    private val presetLevels = mapOf(
        "Flat" to listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
        "Bass Boost" to listOf(900, 700, 450, 180, 0, -80, -80, 0, 120, 160),
        "Vocal" to listOf(-250, -100, 120, 420, 650, 650, 420, 160, -60, -180),
        "Rock" to listOf(520, 360, 120, -120, -220, -100, 160, 360, 520, 560),
        "Pop" to listOf(-120, 180, 420, 520, 420, 180, 0, -80, -160, -80),
        "Jazz" to listOf(330, 220, 40, -80, -180, -160, 40, 160, 260, 360),
        "Classical" to listOf(420, 280, 120, 80, 0, 0, 60, 140, 260, 420),
        "Electronic" to listOf(720, 560, 220, 0, -120, -100, 40, 240, 560, 720),
        "Hip-Hop" to listOf(680, 520, 180, -120, -220, -120, 0, 140, 380, 520),
        "Acoustic" to listOf(220, 120, 0, 180, 340, 340, 220, 100, 0, 0),
        "Bass & Treble" to listOf(700, 480, 80, -180, -260, -220, 40, 120, 480, 700)
    )

    fun setupPlayer(player: ExoPlayer) {
        setupSession(player.audioSessionId)
    }

    @Synchronized
    fun setupSession(audioSessionId: Int) {
        if (audioSessionId == C.AUDIO_SESSION_ID_UNSET || audioSessionId == 0) return
        if (audioSessionId == currentSessionId && equalizer != null) return

        val previous = _eqState.value
        releaseEffectsOnly()
        currentSessionId = audioSessionId

        try {
            equalizer = Equalizer(0, audioSessionId).apply { enabled = previous.enabled }
            bassBoost = runCatching {
                BassBoost(0, audioSessionId).apply {
                    enabled = previous.enabled
                    setStrength(previous.bassBoost.coerceIn(0, 1000).toShort())
                }
            }.getOrNull()
            virtualizer = runCatching {
                Virtualizer(0, audioSessionId).apply {
                    enabled = previous.enabled
                    setStrength(previous.virtualizerStrength.coerceIn(0, 1000).toShort())
                }
            }.getOrNull()
            loudnessEnhancer = runCatching {
                LoudnessEnhancer(audioSessionId).apply {
                    enabled = previous.enabled
                    setTargetGain(previous.loudnessGain.coerceIn(0, 5000))
                }
            }.getOrNull()

            applyLevels(previous.bands.map { it.level })
            updateBandsFromEqualizer()
            _eqState.value = _eqState.value.copy(
                bassBoost = previous.bassBoost,
                virtualizerStrength = previous.virtualizerStrength,
                loudnessGain = previous.loudnessGain,
                presetName = previous.presetName,
                enabled = previous.enabled,
                isReady = true,
                errorMessage = null
            )
        } catch (e: Exception) {
            releaseEffectsOnly()
            _eqState.value = previous.copy(
                isReady = false,
                errorMessage = "Equalizer is not available for this device/session."
            )
        }
    }

    fun setEnabled(enabled: Boolean) {
        equalizer?.enabled = enabled
        bassBoost?.enabled = enabled
        virtualizer?.enabled = enabled
        loudnessEnhancer?.enabled = enabled
        _eqState.value = _eqState.value.copy(enabled = enabled)
    }

    private fun updateBandsFromEqualizer() {
        val eq = equalizer ?: return
        val range = eq.bandLevelRange
        val bands = (0 until eq.numberOfBands.toInt()).map { i ->
            EqualizerBand(
                freq = eq.getCenterFreq(i.toShort()) / 1000,
                level = eq.getBandLevel(i.toShort()).toInt(),
                minLevel = range[0],
                maxLevel = range[1]
            )
        }
        _eqState.value = _eqState.value.copy(bands = bands, isReady = true, errorMessage = null)
    }

    fun setBandLevel(bandIndex: Int, level: Int) {
        val current = _eqState.value
        val updatedBands = current.bands.mapIndexed { index, band ->
            if (index == bandIndex) band.copy(level = level.coerceIn(band.minLevel.toInt(), band.maxLevel.toInt())) else band
        }
        _eqState.value = current.copy(bands = updatedBands, presetName = "Custom")

        val eq = equalizer ?: return
        if (bandIndex >= eq.numberOfBands) return
        runCatching {
            val band = updatedBands[bandIndex]
            eq.setBandLevel(bandIndex.toShort(), band.level.toShort())
            updateBandsFromEqualizer()
            _eqState.value = _eqState.value.copy(presetName = "Custom")
        }
    }

    fun setBassBoost(strength: Int) {
        val value = strength.coerceIn(0, 1000)
        bassBoost?.let { runCatching { it.setStrength(value.toShort()) } }
        _eqState.value = _eqState.value.copy(bassBoost = value)
    }

    fun setVirtualizer(strength: Int) {
        val value = strength.coerceIn(0, 1000)
        virtualizer?.let { runCatching { it.setStrength(value.toShort()) } }
        _eqState.value = _eqState.value.copy(virtualizerStrength = value)
    }

    fun setLoudness(gain: Int) {
        val value = gain.coerceIn(0, 5000)
        loudnessEnhancer?.let { runCatching { it.setTargetGain(value) } }
        _eqState.value = _eqState.value.copy(loudnessGain = value)
    }

    fun applyPreset(presetName: String) {
        val levels = presetLevels[presetName] ?: return
        val current = _eqState.value
        val updatedBands = current.bands.mapIndexed { index, band ->
            band.copy(level = (levels.getOrNull(index) ?: 0).coerceIn(band.minLevel.toInt(), band.maxLevel.toInt()))
        }
        _eqState.value = current.copy(bands = updatedBands, presetName = presetName)
        applyLevels(updatedBands.map { it.level })
        updateBandsFromEqualizer()
        _eqState.value = _eqState.value.copy(presetName = presetName)
    }

    private fun applyLevels(levels: List<Int>) {
        val eq = equalizer ?: return
        runCatching {
            levels.forEachIndexed { index, level ->
                if (index < eq.numberOfBands) {
                    val range = eq.bandLevelRange
                    eq.setBandLevel(index.toShort(), level.coerceIn(range[0].toInt(), range[1].toInt()).toShort())
                }
            }
        }
    }

    private fun releaseEffectsOnly() {
        runCatching { equalizer?.release() }
        runCatching { bassBoost?.release() }
        runCatching { virtualizer?.release() }
        runCatching { loudnessEnhancer?.release() }
        equalizer = null
        bassBoost = null
        virtualizer = null
        loudnessEnhancer = null
    }

    fun release() {
        releaseEffectsOnly()
        currentSessionId = C.AUDIO_SESSION_ID_UNSET
        _eqState.value = _eqState.value.copy(isReady = false)
    }

    companion object {
        private fun defaultBands(): List<EqualizerBand> {
            val freqs = listOf(60, 170, 310, 600, 1000, 3000, 6000, 12000, 14000, 16000)
            return freqs.map { EqualizerBand(freq = it) }
        }
    }
}
