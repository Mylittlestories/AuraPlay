package com.auraplay.player.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.auraplay.player.data.model.Track
import com.auraplay.player.ui.theme.*

@Composable
fun MiniPlayer(
    track: Track?,
    isPlaying: Boolean,
    progress: Long,
    duration: Long,
    onTogglePlay: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (track == null) return

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MiniPlayerBg,
        tonalElevation = 8.dp,
        shadowElevation = 12.dp
    ) {
        Column {
            // Progress bar — thin teal line
            LinearProgressIndicator(
                progress = { if (duration > 0) (progress.toFloat() / duration.toFloat()) else 0f },
                modifier = Modifier.fillMaxWidth().height(2.dp),
                color = Primary,
                trackColor = MiniPlayerTrack
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Small album icon
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = PrimaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Track info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = TextPrimary
                    )
                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = TextSecondary
                    )
                }

                // Controls — teal accent
                IconButton(onClick = onSkipPrevious, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.SkipPrevious, "Previous", tint = TextSecondary, modifier = Modifier.size(22.dp))
                }

                IconButton(onClick = onTogglePlay, modifier = Modifier.size(42.dp)) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        if (isPlaying) "Pause" else "Play",
                        tint = Primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                IconButton(onClick = onSkipNext, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.SkipNext, "Next", tint = TextSecondary, modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}
