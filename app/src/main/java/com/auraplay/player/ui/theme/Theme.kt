package com.auraplay.player.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private fun darkOcean(background: Color = Background) = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    background = background,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = Outline,
    outlineVariant = OutlineVariant,
    error = Error,
    errorContainer = ErrorContainer
)

private fun purpleNight(background: Color) = darkColorScheme(
    primary = Color(0xFFC7A4FF),
    onPrimary = Color(0xFF25133F),
    primaryContainer = Color(0xFF3A245E),
    onPrimaryContainer = Color(0xFFEBDDFF),
    secondary = Color(0xFFFFB4D1),
    onSecondary = Color(0xFF3B1024),
    secondaryContainer = Color(0xFF56223A),
    onSecondaryContainer = Color(0xFFFFD9E7),
    tertiary = Color(0xFF8DE7FF),
    onTertiary = Color(0xFF002A33),
    tertiaryContainer = Color(0xFF103E4A),
    onTertiaryContainer = Color(0xFFC4F4FF),
    background = background,
    onBackground = Color(0xFFF7EEFF),
    surface = Color(0xFF1E1830),
    onSurface = Color(0xFFF7EEFF),
    surfaceVariant = Color(0xFF2A2140),
    onSurfaceVariant = Color(0xFFD6C8E8),
    outline = Color(0xFF51445F),
    outlineVariant = Color(0xFF382D45)
)

private fun amoled(background: Color) = darkColorScheme(
    primary = Color(0xFF00E5C3),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF003D35),
    onPrimaryContainer = Color(0xFFB5FFF2),
    secondary = Color(0xFFFFC14D),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF332200),
    onSecondaryContainer = Color(0xFFFFE3A3),
    tertiary = Color(0xFFFF6F91),
    onTertiary = Color.Black,
    tertiaryContainer = Color(0xFF45101F),
    onTertiaryContainer = Color(0xFFFFD8E1),
    background = background,
    onBackground = Color(0xFFF4F4F4),
    surface = Color(0xFF080808),
    onSurface = Color(0xFFF4F4F4),
    surfaceVariant = Color(0xFF151515),
    onSurfaceVariant = Color(0xFFC8C8C8),
    outline = Color(0xFF333333),
    outlineVariant = Color(0xFF202020)
)

private fun sunset(background: Color) = darkColorScheme(
    primary = Color(0xFFFFB15E),
    onPrimary = Color(0xFF2E1700),
    primaryContainer = Color(0xFF53300C),
    onPrimaryContainer = Color(0xFFFFDDBB),
    secondary = Color(0xFFFF8FA3),
    onSecondary = Color(0xFF3F0010),
    secondaryContainer = Color(0xFF5D1727),
    onSecondaryContainer = Color(0xFFFFD9DF),
    tertiary = Color(0xFF8FD8FF),
    onTertiary = Color(0xFF002B3F),
    tertiaryContainer = Color(0xFF16405A),
    onTertiaryContainer = Color(0xFFC8E6FF),
    background = background,
    onBackground = Color(0xFFFFF1E8),
    surface = Color(0xFF241712),
    onSurface = Color(0xFFFFF1E8),
    surfaceVariant = Color(0xFF33211A),
    onSurfaceVariant = Color(0xFFE8CFC2),
    outline = Color(0xFF60483D),
    outlineVariant = Color(0xFF473229)
)

fun AuraAppearance.resolvedBackground(): Color = when (backgroundPreset) {
    AuraBackgroundPreset.DEEP -> when (themePreset) {
        AuraThemePreset.OCEAN -> Background
        AuraThemePreset.PURPLE -> Color(0xFF120D1F)
        AuraThemePreset.AMOLED -> Color.Black
        AuraThemePreset.SUNSET -> Color(0xFF170E0A)
    }
    AuraBackgroundPreset.BLACK -> Color.Black
    AuraBackgroundPreset.BLUE -> Color(0xFF081321)
    AuraBackgroundPreset.PURPLE -> Color(0xFF140B22)
}

@Composable
fun AuraPlayTheme(
    appearance: AuraAppearance = AuraAppearance(),
    content: @Composable () -> Unit
) {
    val background = appearance.resolvedBackground()
    val colorScheme = when (appearance.themePreset) {
        AuraThemePreset.OCEAN -> darkOcean(background)
        AuraThemePreset.PURPLE -> purpleNight(background)
        AuraThemePreset.AMOLED -> amoled(background)
        AuraThemePreset.SUNSET -> sunset(background)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
