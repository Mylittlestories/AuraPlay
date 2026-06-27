package com.auraplay.player.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.auraplay.player.data.model.RepeatMode
import com.auraplay.player.ui.viewmodel.MainViewModel

@Composable
fun NowPlayingScreen(viewModel: MainViewModel = hiltViewModel()) {
    val playbackState by viewModel.playbackState.collectAsState()
    val track = playbackState.currentTrack

    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (track == null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.MusicNote, null, modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))
                Text("No track playing", style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(0.5f))

            // Album art
            Surface(
                modifier = Modifier.size(280.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = "Album Art",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Track info
            Text(
                track.title,
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                track.artist,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Text(
                track.album,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Progress bar
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Slider(
                    value = if (playbackState.duration > 0)
                        (playbackState.progress.toFloat() / playbackState.duration.toFloat()).coerceIn(0f, 1f)
                    else 0f,
                    onValueChange = { viewModel.seekToFraction(it) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        formatDuration(playbackState.progress),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        formatDuration(playbackState.duration),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main controls
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle
                IconButton(onClick = { viewModel.toggleShuffle() }) {
                    Icon(
                        Icons.Default.Shuffle,
                        "Shuffle",
                        tint = if (playbackState.shuffleEnabled) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Previous
                IconButton(onClick = { viewModel.skipPrevious() },
                    modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Default.SkipPrevious, "Previous",
                        modifier = Modifier.size(32.dp))
                }

                // Play/Pause
                FABButton(isPlaying = playbackState.isPlaying) {
                    viewModel.togglePlayPause()
                }

                // Next
                IconButton(onClick = { viewModel.skipNext() },
                    modifier = Modifier.size(56.dp)) {
                    Icon(Icons.Default.SkipNext, "Next",
                        modifier = Modifier.size(32.dp))
                }

                // Repeat
                IconButton(onClick = { viewModel.cycleRepeatMode() }) {
                    Icon(
                        when (playbackState.repeatMode) {
                            RepeatMode.ONE -> Icons.Default.RepeatOne
                            else -> Icons.Default.Repeat
                        },
                        "Repeat",
                        tint = if (playbackState.repeatMode != RepeatMode.OFF)
                               MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))

            // Bottom actions
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = { viewModel.toggleFavorite(track) }) {
                    Icon(
                        if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        "Favorite",
                        tint = if (track.isFavorite) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { /* nav to queue */ }) {
                    Icon(Icons.Default.QueueMusic, "Queue")
                }
                IconButton(onClick = { /* nav to eq */ }) {
                    Icon(Icons.Default.Equalizer, "Equalizer")
                }
                IconButton(onClick = { /* more */ }) {
                    Icon(Icons.Default.MoreVert, "More")
                }
            }
        }
    }
}

@Composable
fun FABButton(isPlaying: Boolean, onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        shape = CircleShape,
        modifier = Modifier.size(64.dp)
    ) {
        Icon(
            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            if (isPlaying) "Pause" else "Play",
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}
