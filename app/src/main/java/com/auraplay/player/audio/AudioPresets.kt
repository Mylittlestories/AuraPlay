package com.auraplay.player.audio

enum class AmpMode { OFF, TUBE_WARM, TUBE_CLEAN, SOLID_STATE, LAMP_DAC, VINTAGE, STUDIO, HEADPHONE }

data class AmpPreset(val curve: List<Int>, val bb: Int, val vz: Int, val loud: Int, val desc: String)

object AudioPresets {
    fun get(mode: AmpMode): AmpPreset = when (mode) {
        AmpMode.OFF -> AmpPreset(List(10) { 0 }, 0, 0, 0, "Off")
        AmpMode.TUBE_WARM -> AmpPreset(listOf(400,350,250,150,50,-50,-100,0,100,200), 500, 600, 200, "Warm tube — rich mids, soft highs")
        AmpMode.TUBE_CLEAN -> AmpPreset(listOf(200,150,100,200,150,50,0,50,150,200), 300, 400, 100, "Clean tube — Fender clarity")
        AmpMode.SOLID_STATE -> AmpPreset(listOf(100,50,0,0,50,50,100,150,200,250), 200, 300, 300, "Solid state — precise, clean")
        AmpMode.LAMP_DAC -> AmpPreset(listOf(300,250,200,100,0,-50,-100,50,200,300), 400, 700, 400, "Lamp DAC — holographic stage")
        AmpMode.VINTAGE -> AmpPreset(listOf(500,400,200,100,0,-100,-50,100,200,150), 600, 500, 200, "Vintage — Marantz warmth")
        AmpMode.STUDIO -> AmpPreset(List(10) { 0 }, 0, 0, 500, "Studio — flat reference")
        AmpMode.HEADPHONE -> AmpPreset(listOf(200,100,0,-50,0,100,200,300,350,300), 300, 800, 500, "Headphone — wide stage")
    }
    val eqPresets = listOf("Flat" to List(10){0}, "Bass Boost" to listOf(600,500,300,100,0,0,0,0,0,0), "Rock" to listOf(500,300,-100,-300,-100,200,400,500,500,500), "Pop" to listOf(-100,100,300,400,300,0,-100,-100,0,0), "Jazz" to listOf(300,100,0,200,-200,-200,0,200,300,400), "Classical" to listOf(400,300,200,100,-100,-100,0,200,300,400), "Electronic" to listOf(500,400,100,0,-200,200,0,-100,400,500), "Hip-Hop" to listOf(500,500,300,100,-100,-200,100,0,200,300))
}
