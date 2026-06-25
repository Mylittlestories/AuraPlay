package com.auraplay.player.audio

import android.media.audiofx.Equalizer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tube Amplifier & DAC Sound Simulation
 *
 * Models the warm, rich sound of vacuum tube amplifiers and high-end DACs:
 * - Even harmonic warmth
 * - Soft clipping (compression)
 * - Wide soundstage
 * - Bass warmth without muddiness
 * - Clear, non-fatiguing highs
 */
@Singleton
class TubeAmplifier @Inject constructor() {

    enum class AmpMode {
        OFF,
        TUBE_WARM,          // Warm tube amp (like McIntosh)
        TUBE_CLEAN,         // Clean tube (like Fender)
        SOLID_STATE,        // Clean solid state (like Benchmark DAC)
        LAMP_DAC,           // Lamp DAC (like Denafrips)
        VINTAGE,            // Vintage analog (like Marantz)
        STUDIO_MONITOR,     // Flat studio reference
        HEADPHONE_AMP       // Optimized for headphones
    }

    /**
     * Returns EQ curve and settings for each amp mode
     * Values: List of 10 band levels in millibels (-1500 to +1500)
     */
    fun getAmpCurve(mode: AmpMode): AmpSettings {
        return when (mode) {
            AmpMode.OFF -> AmpSettings(
                bandLevels = listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
                bassBoost = 0,
                virtualizer = 0,
                loudnessGain = 0,
                description = "Flat — No processing"
            )

            AmpMode.TUBE_WARM -> AmpSettings(
                bandLevels = listOf(400, 350, 250, 150, 50, -50, -100, 0, 100, 200),
                bassBoost = 500,
                virtualizer = 600,
                loudnessGain = 200,
                description = "Warm tube amp — rich mids, soft highs, deep bass"
            )

            AmpMode.TUBE_CLEAN -> AmpSettings(
                bandLevels = listOf(200, 150, 100, 200, 150, 50, 0, 50, 150, 200),
                bassBoost = 300,
                virtualizer = 400,
                loudnessGain = 100,
                description = "Clean tube — Fender-style clarity with tube warmth"
            )

            AmpMode.SOLID_STATE -> AmpSettings(
                bandLevels = listOf(100, 50, 0, 0, 50, 50, 100, 150, 200, 250),
                bassBoost = 200,
                virtualizer = 300,
                loudnessGain = 300,
                description = "Solid state — precise, clean, extended highs"
            )

            AmpMode.LAMP_DAC -> AmpSettings(
                bandLevels = listOf(300, 250, 200, 100, 0, -50, -100, 50, 200, 300),
                bassBoost = 400,
                virtualizer = 700,
                loudnessGain = 400,
                description = "Lamp DAC — holographic soundstage, analog warmth"
            )

            AmpMode.VINTAGE -> AmpSettings(
                bandLevels = listOf(500, 400, 200, 100, 0, -100, -50, 100, 200, 150),
                bassBoost = 600,
                virtualizer = 500,
                loudnessGain = 200,
                description = "Vintage analog — Marantz-style warmth and richness"
            )

            AmpMode.STUDIO_MONITOR -> AmpSettings(
                bandLevels = listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
                bassBoost = 0,
                virtualizer = 0,
                loudnessGain = 500,
                description = "Studio reference — flat response, maximum clarity"
            )

            AmpMode.HEADPHONE_AMP -> AmpSettings(
                bandLevels = listOf(200, 100, 0, -50, 0, 100, 200, 300, 350, 300),
                bassBoost = 300,
                virtualizer = 800,
                loudnessGain = 500,
                description = "Headphone amp — wide stage, clear mids, sparkling highs"
            )
        }
    }

    /**
     * Apply amp mode to an Equalizer instance
     */
    fun applyAmpMode(equalizer: Equalizer?, mode: AmpMode) {
        val settings = getAmpCurve(mode)
        equalizer?.let { eq ->
            val bandCount = eq.numberOfBands.toInt()
            for (i in 0 until minOf(bandCount, settings.bandLevels.size)) {
                val level = settings.bandLevels[i].toShort()
                    .coerceIn(eq.bandLevelRange[0], eq.bandLevelRange[1])
                eq.setBandLevel(i.toShort(), level)
            }
        }
    }

    data class AmpSettings(
        val bandLevels: List<Int>,
        val bassBoost: Int,
        val virtualizer: Int,
        val loudnessGain: Int,
        val description: String
    )
}