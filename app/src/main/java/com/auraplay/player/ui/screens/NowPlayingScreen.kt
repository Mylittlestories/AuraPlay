package com.auraplay.player.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import androidx.compose.animation.core.RepeatMode as AnimRepeatMode
import com.auraplay.player.data.model.RepeatMode
import com.auraplay.player.ui.theme.*
import com.auraplay.player.ui.viewmodel.MainViewModel
import kotlin.math.abs

@Composable
fun NowPlayingScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onNavigateToQueue: () -> Unit,
    onNavigateToEqualizer: () -> Unit
) {
    val currentTrack by viewModel.currentTrack.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentPosition by viewModel.currentPosition.collectAsStateWithLifecycle()
    val duration by viewModel.duration.collectAsStateWithLifecycle()
    val repeatMode by viewModel.repeatMode.collectAsStateWithLifecycle()
    val shuffleEnabled by viewModel.shuffleEnabled.collectAsStateWithLifecycle()
    val playbackSpeed by viewModel.playbackSpeed.collectAsStateWithLifecycle()

    val track = currentTrack ?: return

    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = AnimRepeatMode.Restart
        ),
        label = "rotation"
    )

    var isSeeking by remember { mutableStateOf(false) }
    var seekPosition by remember { mutableStateOf(0f) }

    val progress = if (duration > 0) {
        if (isSeeking) seekPosition
        else (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(PlayerBackground, Color(0xFF0D0D2B), PlayerBackground))
        )
    ) {
        if (track.albumArtUri != null) {
            AsyncImage(
                model = track.albumArtUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().blur(50.dp),
                contentScale = ContentScale.Crop,
                alpha = 0.2f
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (abs(dragAmount) > 100) {
                        if (dragAmount > 0) viewModel.skipToPrevious()
                        else viewModel.skipToNext()
                    }
                }
            }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.KeyboardArrowDown, "Back", tint = Color.White, modifier = Modifier.size(32.dp))
                }
                Text("NOW PLAYING", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f), letterSpacing = 3.sp)
                IconButton(onClick = onNavigateToQueue) {
                    Icon(Icons.Default.QueueMusic, "Queue", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))

            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier.fillMaxSize(0.95f).rotate(if (isPlaying) rotation else 0f).clip(CircleShape)
                        .background(Brush.radialGradient(listOf(Color(0xFF1A1A1A), Color(0xFF0D0D0D), Color(0xFF1A1A1A)))),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(0.45f).clip(CircleShape)
                            .background(Brush.linearGradient(listOf(AccentGradientStart, AccentGradientEnd))),
                        contentAlignment = Alignment.Center
                    ) {
                        if (track.albumArtUri != null) {
                            AsyncImage(model = track.albumArtUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Icon(Icons.Default.MusicNote, null, tint = Color.White, modifier = Modifier.size(48.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = track.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "${track.artist} • ${track.album}", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                IconButton(onClick = { viewModel.toggleFavorite(track.id) }) {
                    Icon(
                        if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        "Favorite",
                        tint = if (track.isFavorite) Color.Red else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column {
                Slider(
                    value = progress,
                    onValueChange = { isSeeking = true; seekPosition = it },
                    onValueChangeFinished = { isSeeking = false; viewModel.seekTo((seekPosition * duration).toLong()) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(thumbColor = PlayerAccent, activeTrackColor = PlayerAccent, inactiveTrackColor = PlayerProgressBackground)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(formatDuration(if (isSeeking) (seekPosition * duration).toLong() else currentPosition), style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
                    Text(formatDuration(duration), style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.toggleShuffle() }) {
                    Icon(Icons.Default.Shuffle, "Shuffle", tint = if (shuffleEnabled) ShuffleSmart else Color.White.copy(alpha = 0.5f), modifier = Modifier.size(24.dp))
                }
                IconButton(onClick = { viewModel.skipToPrevious() }) {
                    Icon(Icons.Default.SkipPrevious, "Previous", tint = Color.White, modifier = Modifier.size(40.dp))
                }
                Box(
                    modifier = Modifier.size(72.dp).clip(CircleShape).background(Brush.linearGradient(listOf(AccentGradientStart, AccentGradientEnd))).padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = { viewModel.togglePlayPause() }, modifier = Modifier.size(64.dp)) {
                        Icon(if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, if (isPlaying) "Pause" else "Play", tint = Color.White, modifier = Modifier.size(40.dp))
                    }
                }
                IconButton(onClick = { viewModel.skipToNext() }) {
                    Icon(Icons.Default.SkipNext, "Next", tint = Color.White, modifier = Modifier.size(40.dp))
                }
                IconButton(onClick = { viewModel.toggleRepeatMode() }) {
                    Icon(
                        when (repeatMode) { RepeatMode.ONE -> Icons.Default.RepeatOne; else -> Icons.Default.Repeat },
                        "Repeat",
                        tint = when (repeatMode) { RepeatMode.OFF -> Color.White.copy(alpha = 0.5f); else -> AccentPurple },
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = {
                    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
                    val idx = speeds.indexOf(playbackSpeed).coerceAtLeast(0)
                    viewModel.setPlaybackSpeed(speeds[(idx + 1) % speeds.size])
                }) {
                    Text("${playbackSpeed}x", color = if (playbackSpeed != 1.0f) AccentCyan else Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Medium)
                }
                Text("${track.formattedBitrate} • ${track.formattedSampleRate}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.4f))
                IconButton(onClick = onNavigateToEqualizer) {
                    Icon(Icons.Default.Equalizer, "Equalizer", tint = Color.White.copy(alpha = 0.7f))
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

fun formatDuration(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds)
    else "%d:%02d".format(minutes, seconds)
}