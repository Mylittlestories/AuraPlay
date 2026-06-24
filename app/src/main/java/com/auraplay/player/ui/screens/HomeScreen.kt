package com.auraplay.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.auraplay.player.data.model.Track
import com.auraplay.player.ui.theme.*
import com.auraplay.player.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToNowPlaying: () -> Unit,
    onNavigateToAlbum: (String) -> Unit,
    onNavigateToArtist: (String) -> Unit
) {
    val allTracks by viewModel.allTracks.collectAsStateWithLifecycle()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsStateWithLifecycle()
    val mostPlayed by viewModel.mostPlayed.collectAsStateWithLifecycle()
    val recentlyAdded by viewModel.recentlyAdded.collectAsStateWithLifecycle()
    val favoriteTracks by viewModel.favoriteTracks.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val scanProgress by viewModel.scanProgress.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (allTracks.isEmpty() && !isScanning) {
            viewModel.scanForMusic()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(AccentPurple.copy(alpha = 0.3f), Color.Transparent)
                        )
                    )
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = "AuraPlay",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (allTracks.isNotEmpty()) "${allTracks.size} tracks" else "Welcome",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (isScanning) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(scanProgress)
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Shuffle,
                    title = "Shuffle All",
                    subtitle = "${allTracks.size} tracks",
                    gradient = listOf(ShuffleSmart, AccentCyan),
                    onClick = { viewModel.shuffleAll() }
                )
                QuickActionCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Favorite,
                    title = "Favorites",
                    subtitle = "${favoriteTracks.size} tracks",
                    gradient = listOf(Color(0xFFFF6B6B), Color(0xFFFF8E8E)),
                    onClick = {
                        if (favoriteTracks.isNotEmpty()) {
                            viewModel.playTrack(favoriteTracks.first(), favoriteTracks)
                            onNavigateToNowPlaying()
                        }
                    }
                )
            }
        }

        if (recentlyPlayed.isNotEmpty()) {
            item {
                SectionHeader(title = "Recently Played", icon = Icons.Default.History, subtitle = "Pick up where you left off")
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recentlyPlayed.take(10)) { track ->
                        TrackCard(track = track, onClick = {
                            viewModel.playTrack(track, recentlyPlayed)
                            onNavigateToNowPlaying()
                        })
                    }
                }
            }
        }

        if (mostPlayed.isNotEmpty()) {
            item {
                SectionHeader(title = "Most Played", icon = Icons.Default.TrendingUp, subtitle = "Your top tracks")
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(mostPlayed.take(10)) { track ->
                        TrackCard(track = track, onClick = {
                            viewModel.playTrack(track, mostPlayed)
                            onNavigateToNowPlaying()
                        })
                    }
                }
            }
        }

        if (recentlyAdded.isNotEmpty()) {
            item {
                SectionHeader(title = "Recently Added", icon = Icons.Default.NewReleases, subtitle = "Fresh music")
            }
            items(recentlyAdded.take(20)) { track ->
                TrackListItem(
                    track = track,
                    onClick = {
                        viewModel.playTrack(track, recentlyAdded)
                        onNavigateToNowPlaying()
                    },
                    onMoreClick = { }
                )
            }
        }

        if (allTracks.isEmpty() && !isScanning) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.LibraryMusic, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No music found", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { viewModel.scanForMusic() }, modifier = Modifier.fillMaxWidth(0.7f)) {
                        Icon(Icons.Default.Refresh, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scan for Music")
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector, subtitle: String? = null) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (subtitle != null) {
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun QuickActionCard(modifier: Modifier = Modifier, icon: ImageVector, title: String, subtitle: String, gradient: List<Color>, onClick: () -> Unit) {
    Card(modifier = modifier.height(100.dp).clickable { onClick() }, shape = RoundedCornerShape(16.dp)) {
        Box(
            modifier = Modifier.fillMaxSize().background(Brush.linearGradient(gradient)).padding(16.dp)
        ) {
            Column {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.weight(1f))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
fun TrackCard(track: Track, onClick: () -> Unit) {
    Card(modifier = Modifier.width(140.dp).clickable { onClick() }, shape = RoundedCornerShape(12.dp)) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().height(140.dp).background(Brush.linearGradient(listOf(AccentGradientStart, AccentGradientEnd))),
                contentAlignment = Alignment.Center
            ) {
                if (track.albumArtUri != null) {
                    AsyncImage(model = track.albumArtUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.MusicNote, null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(48.dp))
                }
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(text = track.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = track.artist, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}