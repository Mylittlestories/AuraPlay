package com.auraplay.player.ui.theme
import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class AppTheme(val label: String) {
    AURAPLAY("AuraPlay"), WINAMP("Winamp Classic"), NEON("Neon Cyberpunk"),
    LAVA("Dark Lava"), VINYL("Vinyl Retro"), OCEAN("Ocean Blue"),
    FOREST("Forest Green"), MIDNIGHT("Midnight")
}

data class TColors(
    val bg: Color, val surface: Color, val surfaceVar: Color,
    val primary: Color, val secondary: Color, val accent: Color,
    val text: Color, val text2: Color, val text3: Color,
    val progress: Color, val progressBg: Color, val grad: List<Color>
)

object Themes {
    fun get(t: AppTheme): TColors = when (t) {
        AppTheme.AURAPLAY -> TColors(Color(0xFF0D0D1A), Color(0xFF1A1A2E), Color(0xFF252540), Color(0xFF9C27B0), Color(0xFF2196F3), Color(0xFF00BCD4), Color(0xFFE6E1E5), Color(0xFFCAC4D0), Color(0xFF938F99), Color(0xFF7C4DFF), Color(0xFF2A2A4A), listOf(Color(0xFF667eea), Color(0xFF764ba2)))
        AppTheme.WINAMP -> TColors(Color(0xFF1E1E1E), Color(0xFF2B2B2B), Color(0xFF3C3C3C), Color(0xFF00FF00), Color(0xFFFFD700), Color(0xFFFF6600), Color(0xFF00FF00), Color(0xFFFFD700), Color(0xFF888888), Color(0xFF00FF00), Color(0xFF333333), listOf(Color(0xFF4A4A00), Color(0xFF2A2A00)))
        AppTheme.NEON -> TColors(Color(0xFF0A000A), Color(0xFF1A001A), Color(0xFF2A002A), Color(0xFFFF00FF), Color(0xFF00FFFF), Color(0xFFFFFF00), Color(0xFFE0E0FF), Color(0xFFAA00FF), Color(0xFF6600AA), Color(0xFFFF00FF), Color(0xFF2A002A), listOf(Color(0xFFFF00FF), Color(0xFF00FFFF)))
        AppTheme.LAVA -> TColors(Color(0xFF1A0A0A), Color(0xFF2D1515), Color(0xFF3D2020), Color(0xFFFF4500), Color(0xFFFF8C00), Color(0xFFFFD700), Color(0xFFFFE0D0), Color(0xFFCC9980), Color(0xFF996650), Color(0xFFFF4500), Color(0xFF331515), listOf(Color(0xFFFF4500), Color(0xFFFF8C00)))
        AppTheme.VINYL -> TColors(Color(0xFF1C1410), Color(0xFF2A1F18), Color(0xFF3A2D22), Color(0xFFD4A574), Color(0xFF8B6914), Color(0xFFDAA520), Color(0xFFEDE0D4), Color(0xFFC4A882), Color(0xFF8A7050), Color(0xFFD4A574), Color(0xFF2A1F18), listOf(Color(0xFFD4A574), Color(0xFF8B6914)))
        AppTheme.OCEAN -> TColors(Color(0xFF0A0D1A), Color(0xFF0F1528), Color(0xFF162040), Color(0xFF0099FF), Color(0xFF00CCFF), Color(0xFF00FFCC), Color(0xFFE0F0FF), Color(0xFF80BBEE), Color(0xFF4080AA), Color(0xFF0099FF), Color(0xFF162040), listOf(Color(0xFF0099FF), Color(0xFF00CCFF)))
        AppTheme.FOREST -> TColors(Color(0xFF0A1A0A), Color(0xFF0F280F), Color(0xFF163016), Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFFCDDC39), Color(0xFFE0FFE0), Color(0xFF80CC80), Color(0xFF408040), Color(0xFF4CAF50), Color(0xFF163016), listOf(Color(0xFF4CAF50), Color(0xFF8BC34A)))
        AppTheme.MIDNIGHT -> TColors(Color(0xFF0D0D1A), Color(0xFF141428), Color(0xFF1E1E3A), Color(0xFF6C63FF), Color(0xFF3F3D9E), Color(0xFFB8B5FF), Color(0xFFEEEEFF), Color(0xFFAAAACC), Color(0xFF666688), Color(0xFF6C63FF), Color(0xFF1E1E3A), listOf(Color(0xFF6C63FF), Color(0xFF3F3D9E)))
    }
}

val LocalColors = staticCompositionLocalOf { Themes.get(AppTheme.AURAPLAY) }

@Composable
fun AuraPlayTheme(appTheme: AppTheme = AppTheme.AURAPLAY, content: @Composable () -> Unit) {
    val c = Themes.get(appTheme)
    val cs = darkColorScheme(primary = c.primary, secondary = c.secondary, tertiary = c.accent, background = c.bg, surface = c.surface, surfaceVariant = c.surfaceVar, onBackground = c.text, onSurface = c.text, onSurfaceVariant = c.text2, primaryContainer = c.primary, error = Color(0xFFCF6679))
    val view = LocalView.current
    if (!view.isInEditMode) SideEffect { val w = (view.context as Activity).window; w.statusBarColor = android.graphics.Color.TRANSPARENT; w.navigationBarColor = android.graphics.Color.TRANSPARENT; WindowCompat.getInsetsController(w, view).isAppearanceLightStatusBars = false }
    CompositionLocalProvider(LocalColors provides c) { MaterialTheme(colorScheme = cs, typography = Typography, content = content) }
}
