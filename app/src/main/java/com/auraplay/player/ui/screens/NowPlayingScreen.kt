package com.auraplay.player.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.core.RepeatMode as AnimRepeatMode
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.auraplay.player.audio.MetadataReader
import com.auraplay.player.audio.TubeAmplifier
import com.auraplay.player.audio.TrackMetadata
import com.auraplay.player.data.model.RepeatMode
import com.auraplay.player.ui.components.WinampSpectrumVisualizer
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
    val ampMode by viewModel.ampMode.collectAsStateWithLifecycle()

    val track = currentTrack ?: return

    var showMetadata by remember { mutableStateOf(false) }
    var trackMetadata by remember { mutableStateOf<TrackMetadata?>(null) }

    // Load metadata when track changes
    LaunchedEffect(track.id) {
        trackMetadata = viewModel.getTrackMetadata(track.filePath)
    }

    // Album art rotation
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(20000, easing = LinearEasing), repeatMode = AnimRepeatMode.Restart),
        label = "rotation"
    )

    var isSeeking by remember { mutableStateOf(false) }
    var seekPosition by remember { mutableStateOf(0f) }
    val progress = if (duration > 0) {
        if (isSeeking) seekPosition else (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Box(
        modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(PlayerBackground, Color(0xFF0D0D2B), PlayerBackground)))
    ) {
        // Blurred album art background
        if (track.albumArtUri != null) {
            AsyncImage(model = track.albumArtUri, contentDescription = null, modifier = Modifier.fillMaxSize().blur(60.dp), contentScale = ContentScale.Crop, alpha = 0.15f)
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp).pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (abs(dragAmount) > 100) {
                        if (dragAmount > 0) viewModel.skipToPrevious() else viewModel.skipToNext()
                    }
                }
            }
        ) {
            // Top Bar
            Row(modifier = Modifier.fillMaxWidth().padding(top = 48.dp, bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.KeyboardArrowDown, "Back", tint = Color.White, modifier = Modifier.size(32.dp)) }
                Text("NOW PLAYING", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f), letterSpacing = 3.sp)
                IconButton(onClick = { showMetadata = true }) { Icon(Icons.Default.Info, "Info", tint = Color.White) }
            }

            Spacer(modifier = Modifier.weight(0.3f))

            // === ALBUM ART — Large rotating vinyl ===
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f), contentAlignment = Alignment.Center) {
                // Vinyl disc
                Box(
                    modifier = Modifier.fillMaxSize(0.95f)
                        .rotate(if (isPlaying) rotation else 0f)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(Color(0xFF1A1A1A), Color(0xFF0D0D0D), Color(0xFF1A1A1A)))),
                    contentAlignment = Alignment.Center
                ) {
                    // Vinyl grooves
                    for (i in 1..8) {
                        Box(modifier = Modifier.fillMaxSize(1f - i * 0.1f).clip(CircleShape).background(Color.Transparent))
                    }

                    // Center album art (large, prominent)
                    Box(
                        modifier = Modifier.fillMaxSize(0.50f).clip(CircleShape).background(Brush.linearGradient(listOf(AccentGradientStart, AccentGradientEnd))),
                        contentAlignment = Alignment.Center
                    ) {
                        if (track.albumArtUri != null) {
                            AsyncImage(model = track.albumArtUri, contentDescription = "Album Art", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.MusicNote, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(48.dp))
                                Text("No Art", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.3f))

            // === TRACK INFO ===
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = track.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "${track.artist} • ${track.album}", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    // Audio quality badge
                    val qualityText = trackMetadata?.let { "${it.formatName} • ${it.formattedBitrate} • ${it.formattedSampleRate}" } ?: ""
                    if (qualityText.isNotEmpty()) {
                        Text(text = qualityText, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.4f), maxLines = 1)
                    }
                }
                IconButton(onClick = { viewModel.toggleFavorite(track.id) }) {
                    Icon(if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, "Favorite", tint = if (track.isFavorite) Color.Red else Color.White.copy(alpha = 0.7f), modifier = Modifier.size(28.dp))
                }
            }

            // === WINAMP VISUALIZER ===
            WinampSpectrumVisualizer(
                isPlaying = isPlaying,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // === PROGRESS ===
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

            Spacer(modifier = Modifier.height(16.dp))

            // === MAIN CONTROLS ===
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.toggleShuffle() }) {
                    Icon(Icons.Default.Shuffle, "Shuffle", tint = if (shuffleEnabled) ShuffleSmart else Color.White.copy(alpha = 0.5f), modifier = Modifier.size(24.dp))
                }
                IconButton(onClick = { viewModel.skipToPrevious() }) {
                    Icon(Icons.Default.SkipPrevious, "Previous", tint = Color.White, modifier = Modifier.size(40.dp))
                }
                // Big play button
                Box(modifier = Modifier.size(72.dp).clip(CircleShape).background(Brush.linearGradient(listOf(AccentGradientStart, AccentGradientEnd))).padding(4.dp), contentAlignment = Alignment.Center) {
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

            Spacer(modifier = Modifier.height(12.dp))

            // === BOTTOM BAR — Speed, Amp, EQ ===
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                // Playback speed
                TextButton(onClick = {
                    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
                    val idx = speeds.indexOf(playbackSpeed).coerceAtLeast(0)
                    viewModel.setPlaybackSpeed(speeds[(idx + 1) % speeds.size])
                }) { Text("${playbackSpeed}x", color = if (playbackSpeed != 1.0f) AccentCyan else Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Medium) }

                // Amp mode
                TextButton(onClick = { viewModel.cycleAmpMode() }) {
                    Text(ampMode.name.replace("_", " "), color = if (ampMode != TubeAmplifier.AmpMode.OFF) AccentCyan else Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                }

                // Equalizer
                IconButton(onClick = onNavigateToEqualizer) {
                    Icon(Icons.Default.Equalizer, "Equalizer", tint = Color.White.copy(alpha = 0.7f))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Metadata Dialog
    if (showMetadata) {
        MetadataDialog(
            metadata = trackMetadata,
            albumArtUri = track.albumArtUri,
            onDismiss = { showMetadata = false },
            onDownloadArt = { viewModel.downloadAlbumArt(track) },
            onDownloadLyrics = { viewModel.downloadLyrics(track) }
        )
    }
}

fun formatDuration(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds) else "%d:%02d".format(minutes, seconds)
}