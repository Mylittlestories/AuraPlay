package com.auraplay.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.auraplay.player.data.model.Track
import com.auraplay.player.ui.theme.*

// ─── Track List Item ───
@Composable
fun TrackListItem(
    track: Track,
    isCurrentTrack: Boolean = false,
    isPlaying: Boolean = false,
    onClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isCurrentTrack) PrimaryContainer.copy(alpha = 0.56f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(52.dp),
            shape = RoundedCornerShape(12.dp),
            color = if (isCurrentTrack) PrimaryContainer else SurfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center) {
                AlbumArt(track = track, modifier = Modifier.fillMaxSize(), showMusicNote = true)
                if (isCurrentTrack && isPlaying) {
                    Surface(shape = CircleShape, color = PlayerOverlay.copy(alpha = 0.42f)) {
                        Icon(
                            Icons.Default.Equalizer,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.padding(7.dp).size(20.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Track info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isCurrentTrack) Primary else TextPrimary
            )
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = TextSecondary
            )
        }

        // Duration — subtle
        Text(
            text = formatDuration(track.duration),
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary
        )

        Spacer(modifier = Modifier.width(4.dp))

        // Favorite — soft rose when active
        IconButton(onClick = onFavoriteClick, modifier = Modifier.size(36.dp)) {
            Icon(
                if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (track.isFavorite) Tertiary else TextTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

// ─── Section Header ───
@Composable
fun SectionHeader(title: String, onMore: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.weight(1f))
        TextButton(onClick = onMore) {
            Text("See all", color = Primary)
        }
    }
}

// ─── Quick Action Card ───
@Composable
fun QuickActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = PrimaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = Primary, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelMedium, color = TextPrimary)
        }
    }
}

// ─── Mini Track Card (for horizontal scrolls) ───
@Composable
fun SmallTrackCard(track: Track, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(140.dp).clip(RoundedCornerShape(14.dp)).clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = RoundedCornerShape(12.dp),
                color = SurfaceHigh
            ) {
                AlbumArt(track = track, modifier = Modifier.fillMaxSize())
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                track.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = TextPrimary
            )
            Text(
                track.artist,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = TextSecondary
            )
        }
    }
}
