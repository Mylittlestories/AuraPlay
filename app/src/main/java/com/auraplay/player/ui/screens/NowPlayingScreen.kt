package com.auraplay.player.ui.screens
package com.auraplay.player.ui.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.auraplay.player.data.model.RepeatMode
import com.auraplay.player.ui.theme.*
import com.auraplay.player.ui.components.formatDuration
import com.auraplay.player.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(navController: NavController, viewModel: MainViewModel = hiltViewModel()) {
    val playbackState by viewModel.playbackState.collectAsState()
    val track = playbackState.currentTrack

    Box(modifier = Modifier.fillMaxSize()) {
        if (track == null) {
            // Empty state
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = SurfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.MusicNote, null, modifier = Modifier.size(36.dp), tint = TextTertiary)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text("No track playing", style = MaterialTheme.typography.titleLarge, color = TextSecondary)
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Browse Library")
                }
            }
            return
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // ─── Top bar ───
            TopAppBar(
                title = { Text("Now Playing", style = MaterialTheme.typography.titleMedium, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.KeyboardArrowDown, "Back", tint = TextSecondary)
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("queue") }) {
                        Icon(Icons.Default.QueueMusic, "Queue", tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )

            // ─── Main content ───
            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Album art — large, rounded, with gradient background
                Surface(
                    modifier = Modifier.size(280.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = PrimaryContainer
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            Icons.Default.Album,
                            contentDescription = "Album Art",
                            modifier = Modifier.size(100.dp),
                            tint = Primary.copy(alpha = 0.4f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Track info — centered
                Text(
                    track.title,
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    track.artist,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                if (track.album.isNotBlank() && track.album != "Unknown Album") {
                    Text(
                        track.album,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // ─── Progress bar ───
                Column {
                    Slider(
                        value = if (playbackState.duration > 0)
                            (playbackState.progress.toFloat() / playbackState.duration.toFloat()).coerceIn(0f, 1f)
                        else 0f,
                        onValueChange = { viewModel.seekToFraction(it) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            thumbColor = Primary,
                            activeTrackColor = Primary,
                            inactiveTrackColor = SurfaceVariant
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            formatDuration(playbackState.progress),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                        Text(
                            formatDuration(playbackState.duration),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ─── Main controls ───
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shuffle
                    IconButton(onClick = { viewModel.toggleShuffle() }) {
                        Icon(
                            Icons.Default.Shuffle,
                            "Shuffle",
                            tint = if (playbackState.shuffleEnabled) Primary else TextTertiary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Previous
                    IconButton(onClick = { viewModel.skipPrevious() }, modifier = Modifier.size(56.dp)) {
                        Icon(Icons.Default.SkipPrevious, "Previous", modifier = Modifier.size(32.dp), tint = TextPrimary)
                    }

                    // Play/Pause — large teal circle
                    FloatingActionButton(
                        onClick = { viewModel.togglePlayPause() },
                        containerColor = Primary,
                        contentColor = OnPrimary,
                        shape = CircleShape,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            if (playbackState.isPlaying) "Pause" else "Play",
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Next
                    IconButton(onClick = { viewModel.skipNext() }, modifier = Modifier.size(56.dp)) {
                        Icon(Icons.Default.SkipNext, "Next", modifier = Modifier.size(32.dp), tint = TextPrimary)
                    }

                    // Repeat
                    IconButton(onClick = { viewModel.cycleRepeatMode() }) {
                        Icon(
                            when (playbackState.repeatMode) {
                                RepeatMode.ONE -> Icons.Default.RepeatOne
                                else -> Icons.Default.Repeat
                            },
                            "Repeat",
                            tint = if (playbackState.repeatMode != RepeatMode.OFF) Primary else TextTertiary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ─── Bottom action row ───
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Favorite — soft rose
                    IconButton(onClick = { viewModel.toggleFavorite(track) }) {
                        Icon(
                            if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            "Favorite",
                            tint = if (track.isFavorite) Tertiary else TextTertiary
                        )
                    }
                    // Equalizer
                    IconButton(onClick = { navController.navigate("equalizer") }) {
                        Icon(Icons.Default.Equalizer, "Equalizer", tint = TextTertiary)
                    }
                    // Shuffle modes
                    IconButton(onClick = { navController.navigate("shuffle_settings") }) {
                        Icon(Icons.Default.Shuffle, "Shuffle Mode", tint = TextTertiary)
                    }
                    // More
                    IconButton(onClick = { /* future: bottom sheet */ }) {
                        Icon(Icons.Default.MoreVert, "More", tint = TextTertiary)
                    }
                }
            }
        }
    }
}

