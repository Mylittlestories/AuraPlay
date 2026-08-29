package com.lostf1sh.pixelplayeross.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * AuraPlay's "soft aurora" shape system.
 *
 * Deliberately rounder than stock Material defaults (8/16/24) so the whole
 * app reads as its own design language — pills, cards and sheets share one
 * generous, continuous silhouette that echoes the Sonic Aurora icon.
 */
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)
