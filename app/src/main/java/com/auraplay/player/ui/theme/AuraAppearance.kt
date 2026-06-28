package com.auraplay.player.ui.theme

enum class AuraThemePreset(val label: String) {
    OCEAN("Ocean Dark"),
    PURPLE("Purple Night"),
    AMOLED("AMOLED Black"),
    SUNSET("Sunset")
}

enum class AuraBackgroundPreset(val label: String) {
    DEEP("Deep"),
    BLACK("Black"),
    BLUE("Blue"),
    PURPLE("Purple")
}

data class AuraAppearance(
    val themePreset: AuraThemePreset = AuraThemePreset.OCEAN,
    val backgroundPreset: AuraBackgroundPreset = AuraBackgroundPreset.DEEP
)
