package com.lostf1sh.pixelplayeross.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * AuraPlay's signature "Sonic Aurora" gradient — the same three stops as the
 * launcher icon (electric violet → hot magenta → warm amber). Used for the
 * home wordmark and select accents so the brand carries through the UI, not
 * just the app icon.
 */
object Aurora {

    val Violet: Color = Color(0xFF7C3AED)
    val Magenta: Color = Color(0xFFEC4899)
    val Amber: Color = Color(0xFFFB923C)

    val colors: List<Color> = listOf(Violet, Magenta, Amber)

    /** Diagonal brand gradient (top-left → bottom-right), like the icon. */
    fun brush(): Brush = Brush.linearGradient(colors)

    /**
     * Soft brand tint for surfaces: the two outer stops at low alpha over
     * whatever the container is. Theme-adaptive in both light and dark.
     */
    fun tint(primary: Color, tertiary: Color): Brush = Brush.linearGradient(
        listOf(primary.copy(alpha = 0.34f), tertiary.copy(alpha = 0.30f))
    )
}
