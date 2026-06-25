package com.auraplay.player.ui.theme

import androidx.compose.ui.graphics.Color

enum class AppTheme(val displayName: String, val icon: String) {
    AURAPLAY("AuraPlay", "🎵"),
    WINAMP_CLASSIC("Winamp Classic", "🟨"),
    WINAMP_MODERN("Winamp Modern", "🔵"),
    DARK_LAVA("Dark Lava", "🌋"),
    NEON_CYBERPUNK("Neon Cyberpunk", "💜"),
    VINYL_RETRO("Vinyl Retro", "🎵"),
    OCEAN_BLUE("Ocean Blue", "🌊"),
    FOREST_GREEN("Forest Green", "🌲")
}

data class ThemeColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val primary: Color,
    val primaryVariant: Color,
    val secondary: Color,
    val accent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val playerBackground: Color,
    val playerSurface: Color,
    val progressActive: Color,
    val progressInactive: Color,
    val buttonGradient: List<Color>,
    val isDark: Boolean = true
)

object ThemePresets {

    fun getColors(theme: AppTheme): ThemeColors {
        return when (theme) {
            AppTheme.AURAPLAY -> ThemeColors(
                background = Color(0xFF0D0D1A),
                surface = Color(0xFF1A1A2E),
                surfaceVariant = Color(0xFF252540),
                primary = Color(0xFF9C27B0),
                primaryVariant = Color(0xFF7B1FA2),
                secondary = Color(0xFF2196F3),
                accent = Color(0xFF00BCD4),
                textPrimary = Color(0xFFE6E1E5),
                textSecondary = Color(0xFFCAC4D0),
                textTertiary = Color(0xFF938F99),
                playerBackground = Color(0xFF0A0A1A),
                playerSurface = Color(0xFF141428),
                progressActive = Color(0xFF7C4DFF),
                progressInactive = Color(0xFF2A2A4A),
                buttonGradient = listOf(Color(0xFF667eea), Color(0xFF764ba2))
            )

            AppTheme.WINAMP_CLASSIC -> ThemeColors(
                background = Color(0xFF1E1E1E),
                surface = Color(0xFF2B2B2B),
                surfaceVariant = Color(0xFF3C3C3C),
                primary = Color(0xFFFFD700),
                primaryVariant = Color(0xFFDAA520),
                secondary = Color(0xFF00FF00),
                accent = Color(0xFFFF6600),
                textPrimary = Color(0xFF00FF00),
                textSecondary = Color(0xFFFFD700),
                textTertiary = Color(0xFF888888),
                playerBackground = Color(0xFF0A0A0A),
                playerSurface = Color(0xFF1A1A1A),
                progressActive = Color(0xFF00FF00),
                progressInactive = Color(0xFF333333),
                buttonGradient = listOf(Color(0xFF4A4A00), Color(0xFF2A2A00))
            )

            AppTheme.WINAMP_MODERN -> ThemeColors(
                background = Color(0xFF0D1117),
                surface = Color(0xFF161B22),
                surfaceVariant = Color(0xFF21262D),
                primary = Color(0xFF58A6FF),
                primaryVariant = Color(0xFF388BFD),
                secondary = Color(0xFF3FB950),
                accent = Color(0xFFD29922),
                textPrimary = Color(0xFFC9D1D9),
                textSecondary = Color(0xFF8B949E),
                textTertiary = Color(0xFF6E7681),
                playerBackground = Color(0xFF010409),
                playerSurface = Color(0xFF0D1117),
                progressActive = Color(0xFF58A6FF),
                progressInactive = Color(0xFF21262D),
                buttonGradient = listOf(Color(0xFF1F6FEB), Color(0xFF388BFD))
            )

            AppTheme.DARK_LAVA -> ThemeColors(
                background = Color(0xFF1A0A0A),
                surface = Color(0xFF2D1515),
                surfaceVariant = Color(0xFF3D2020),
                primary = Color(0xFFFF4500),
                primaryVariant = Color(0xFFCC3700),
                secondary = Color(0xFFFF8C00),
                accent = Color(0xFFFFD700),
                textPrimary = Color(0xFFFFE0D0),
                textSecondary = Color(0xFFCC9980),
                textTertiary = Color(0xFF996650),
                playerBackground = Color(0xFF0D0505),
                playerSurface = Color(0xFF1A0A0A),
                progressActive = Color(0xFFFF4500),
                progressInactive = Color(0xFF331515),
                buttonGradient = listOf(Color(0xFFFF4500), Color(0xFFFF8C00))
            )

            AppTheme.NEON_CYBERPUNK -> ThemeColors(
                background = Color(0xFF0A000A),
                surface = Color(0xFF1A001A),
                surfaceVariant = Color(0xFF2A002A),
                primary = Color(0xFFFF00FF),
                primaryVariant = Color(0xFFCC00CC),
                secondary = Color(0xFF00FFFF),
                accent = Color(0xFFFFFF00),
                textPrimary = Color(0xFFE0E0FF),
                textSecondary = Color(0xFFAA00FF),
                textTertiary = Color(0xFF6600AA),
                playerBackground = Color(0xFF050005),
                playerSurface = Color(0xFF0F000F),
                progressActive = Color(0xFFFF00FF),
                progressInactive = Color(0xFF2A002A),
                buttonGradient = listOf(Color(0xFFFF00FF), Color(0xFF00FFFF))
            )

            AppTheme.VINYL_RETRO -> ThemeColors(
                background = Color(0xFF1C1410),
                surface = Color(0xFF2A1F18),
                surfaceVariant = Color(0xFF3A2D22),
                primary = Color(0xFFD4A574),
                primaryVariant = Color(0xFFB8865A),
                secondary = Color(0xFF8B6914),
                accent = Color(0xFFDAA520),
                textPrimary = Color(0xFFEDE0D4),
                textSecondary = Color(0xFFC4A882),
                textTertiary = Color(0xFF8A7050),
                playerBackground = Color(0xFF0E0A08),
                playerSurface = Color(0xFF1C1410),
                progressActive = Color(0xFFD4A574),
                progressInactive = Color(0xFF2A1F18),
                buttonGradient = listOf(Color(0xFFD4A574), Color(0xFF8B6914))
            )

            AppTheme.OCEAN_BLUE -> ThemeColors(
                background = Color(0xFF0A0D1A),
                surface = Color(0xFF0F1528),
                surfaceVariant = Color(0xFF162040),
                primary = Color(0xFF0099FF),
                primaryVariant = Color(0xFF0077CC),
                secondary = Color(0xFF00CCFF),
                accent = Color(0xFF00FFCC),
                textPrimary = Color(0xFFE0F0FF),
                textSecondary = Color(0xFF80BBEE),
                textTertiary = Color(0xFF4080AA),
                playerBackground = Color(0xFF050810),
                playerSurface = Color(0xFF0A0D1A),
                progressActive = Color(0xFF0099FF),
                progressInactive = Color(0xFF162040),
                buttonGradient = listOf(Color(0xFF0099FF), Color(0xFF00CCFF))
            )

            AppTheme.FOREST_GREEN -> ThemeColors(
                background = Color(0xFF0A1A0A),
                surface = Color(0xFF0F280F),
                surfaceVariant = Color(0xFF163016),
                primary = Color(0xFF4CAF50),
                primaryVariant = Color(0xFF388E3C),
                secondary = Color(0xFF8BC34A),
                accent = Color(0xFFCDDC39),
                textPrimary = Color(0xFFE0FFE0),
                textSecondary = Color(0xFF80CC80),
                textTertiary = Color(0xFF408040),
                playerBackground = Color(0xFF051005),
                playerSurface = Color(0xFF0A1A0A),
                progressActive = Color(0xFF4CAF50),
                progressInactive = Color(0xFF163016),
                buttonGradient = listOf(Color(0xFF4CAF50), Color(0xFF8BC34A))
            )
        }
    }
}