package com.lostf1sh.pixelplayeross.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lostf1sh.pixelplayeross.R
import com.lostf1sh.pixelplayeross.presentation.viewmodel.PlayerViewModel

/**
 * AuraPlay's signature home section: an animated "aura" orb (themed by the current color
 * scheme, so it re-tints with dynamic color / album-art palettes), a one-tap AuraShuffle
 * hero that runs the intelligent whole-library shuffle, and Mood Radio chips that build a
 * queue matching the listener's mood — fully offline via the NLP intent engine.
 */
@Composable
fun AuraMoodSection(
    playerViewModel: PlayerViewModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AuraOrb(Modifier.size(46.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.mood_radio_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.mood_radio_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // AuraShuffle hero — intelligent shuffle across the entire library.
        val heroGradient = Brush.linearGradient(
            listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.tertiary
            )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .background(heroGradient)
                .clickable { playerViewModel.shuffleAllSongs(queueName = "AuraShuffle") }
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Shuffle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.aura_shuffle_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = stringResource(R.string.aura_shuffle_subtitle),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(MoodChip.entries) { chip ->
                MoodChipPill(
                    label = stringResource(chip.labelRes),
                    emoji = chip.emoji,
                    onClick = { playerViewModel.playMoodRadio(chip.query, chip.queueName) }
                )
            }
        }
    }
}

@Composable
private fun MoodChipPill(label: String, emoji: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = "$emoji $label",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/** Rotating double aura ring; colors follow the active theme palette. */
@Composable
private fun AuraOrb(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "aura_orb")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 12_000, easing = LinearEasing)),
        label = "aura_angle"
    )
    val breathe by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 2_600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_breathe"
    )
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.primary
    )
    val coreColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
    Canvas(
        modifier = modifier.graphicsLayer {
            scaleX = breathe
            scaleY = breathe
        }
    ) {
        val radius = size.minDimension / 2f
        rotate(angle, pivot = Offset(size.width / 2f, size.height / 2f)) {
            drawCircle(
                brush = Brush.sweepGradient(colors, center = Offset(size.width / 2f, size.height / 2f)),
                radius = radius,
                style = Stroke(width = radius * 0.45f)
            )
        }
        rotate(-angle * 0.6f, pivot = Offset(size.width / 2f, size.height / 2f)) {
            drawCircle(
                brush = Brush.sweepGradient(
                    colors.reversed(),
                    center = Offset(size.width / 2f, size.height / 2f)
                ),
                radius = radius * 0.62f,
                style = Stroke(width = radius * 0.28f)
            )
        }
        drawCircle(
            color = coreColor,
            radius = radius * 0.3f
        )
    }
}

/**
 * Mood definitions. [query] uses verbatim trigger synonyms from the offline NLP lexicon
 * (MoodProfile), so parsing always resolves the intended mood profile.
 */
private enum class MoodChip(
    val labelRes: Int,
    val emoji: String,
    val query: String,
    val queueName: String,
) {
    CHILL(R.string.mood_chip_chill, "🌊", "chill", "Chill Radio"),
    ENERGY(R.string.mood_chip_energy, "⚡", "workout energetic", "Energy Radio"),
    HAPPY(R.string.mood_chip_happy, "☀️", "happy sunny", "Happy Radio"),
    MELANCHOLY(R.string.mood_chip_melancholy, "🌧️", "sad melancholy", "Melancholy Radio"),
    DISCOVERY(R.string.mood_chip_discovery, "✨", "new unplayed surprise", "Discovery Radio"),
    FAVORITES(R.string.mood_chip_favorites, "❤️", "favorite loved", "Favorites Radio")
}
