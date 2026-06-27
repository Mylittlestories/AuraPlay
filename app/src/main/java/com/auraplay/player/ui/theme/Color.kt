package com.auraplay.player.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Warm Ocean Dark Theme ───
// Easy on the eyes: soft teal accent, warm amber secondary, gentle dark backgrounds

// Backgrounds — warm, not harsh blue-black
val Background = Color(0xFF101018)          // Deepest base
val Surface = Color(0xFF1B1B28)            // Cards, sheets
val SurfaceVariant = Color(0xFF252536)      // Elevated surfaces
val SurfaceHigh = Color(0xFF2F2F42)         // Chips, buttons background

// Primary accent — soft teal (restful, modern)
val Primary = Color(0xFF4ECDC4)             // Soft teal
val PrimaryDim = Color(0xFF3BA89F)          // Dimmer teal for less emphasis
val OnPrimary = Color(0xFF0A1A18)           // Dark text on teal
val PrimaryContainer = Color(0xFF1A3A38)    // Very dark teal container
val OnPrimaryContainer = Color(0xFF8EEEE6)  // Bright teal text

// Secondary accent — warm amber (friendly, inviting)
val Secondary = Color(0xFFFFB347)           // Warm amber
val SecondaryDim = Color(0xFFD4923A)        // Dimmer amber
val OnSecondary = Color(0xFF1A1200)         // Dark text on amber
val SecondaryContainer = Color(0xFF3A2A10)  // Dark amber container
val OnSecondaryContainer = Color(0xFFFFD699)

// Tertiary — soft rose (favorites, hearts)
val Tertiary = Color(0xFFE8838A)            // Soft rose
val OnTertiary = Color(0xFF1A0A0C)
val TertiaryContainer = Color(0xFF3A1A1E)
val OnTertiaryContainer = Color(0xFFFFBCC0)

// Text — warm off-whites, never pure white (reduces eye strain)
val TextPrimary = Color(0xFFF0ECE6)         // Warm off-white
val TextSecondary = Color(0xFFB0ACB8)       // Muted lavender gray
val TextTertiary = Color(0xFF706E78)        // Dim gray
val TextDisabled = Color(0xFF504E58)        // Very dim

// Outlines & dividers
val Outline = Color(0xFF3A3848)             // Subtle borders
val OutlineVariant = Color(0xFF2A2838)      // Even subtler

// Error
val Error = Color(0xFFCF6679)
val ErrorContainer = Color(0xFF3A1A20)

// Player-specific — album art gradient
val PlayerGradientStart = Color(0xFF1A2A38) // Deep blue-teal
val PlayerGradientEnd = Color(0xFF18182A)    // Deep dark
val PlayerOverlay = Color(0xCC000000)        // 80% black overlay

// Mini player
val MiniPlayerBg = Color(0xFF1E1E30)        // Slightly elevated
val MiniPlayerTrack = Color(0xFF2A2A40)      // Progress track
