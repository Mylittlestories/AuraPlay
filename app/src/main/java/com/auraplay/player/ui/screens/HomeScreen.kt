package com.auraplay.player.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.auraplay.player.data.model.Track
import com.auraplay.player.ui.components.MiniPlayer
import com.auraplay.player.ui.viewmodel.MainViewModel

@Composable
fun HomeScreen(navController: NavController, viewModel: MainViewModel = hiltViewModel()) {
    val libraryState by viewModel.libraryState.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 72.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "AuraPlay",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { navController.navigate("search") }) {
                    Icon(Icons.Default.Search, "Search")
                }
                IconButton(onClick = { navController.navigate("settings") }) {
                    Icon(Icons.Default.Settings, "Settings")
                }
            }

            // Permission / Scan
            if (!libraryState.hasPermission) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.LibraryMusic, null, modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Grant Access to Your Music", style = MaterialTheme.typography.titleMedium)
                        Text("AuraPlay needs permission to find and play your music.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else if (libraryState.isScanning) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Scanning for music...")
                    }
                }
            } else if (libraryState.trackCount == 0) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.MusicOff, null, modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No music found", style = MaterialTheme.typography.titleMedium)
                        Text("Add music files to your device and tap scan.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { viewModel.scanForMusic() }) {
                            Icon(Icons.Default.Refresh, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Scan Again")
                        }
                    }
                }
            }

            if (libraryState.trackCount > 0) {
                // Quick Actions
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionCard("Shuffle All", Icons.Default.Shuffle, Modifier.weight(1f)) {
                        viewModel.playTrack(libraryState.tracks.randomOrNull() ?: return@QuickActionCard)
                    }
                    QuickActionCard("Favorites", Icons.Default.Favorite, Modifier.weight(1f)) {
                        navController.navigate("library")
                    }
                    QuickActionCard("Equalizer", Icons.Default.Equalizer, Modifier.weight(1f)) {
                        navController.navigate("equalizer")
                    }
                }

                // Favorites section
                if (libraryState.favorites.isNotEmpty()) {
                    SectionHeader("Favorites") {
                        navController.navigate("library")
                    }
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(libraryState.favorites.take(10)) { track ->
                            SmallTrackCard(track) { viewModel.playTrack(track) }
                        }
                    }
                }

                // Recently added
                SectionHeader("Recently Added") {
                    navController.navigate("library")
                }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(libraryState.tracks.take(10)) { track ->
                        SmallTrackCard(track) { viewModel.playTrack(track) }
                    }
                }

                // Albums
                if (libraryState.albums.isNotEmpty()) {
                    SectionHeader("Albums") {
                        navController.navigate("library")
                    }
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(libraryState.albums.take(10)) { album ->
                            Chip(onClick = { navController.navigate("album_detail/$album") },
                                label = { Text(album, maxLines = 1, overflow = TextOverflow.Ellipsis) })
                        }
                    }
                }

                // Artists
                if (libraryState.artists.isNotEmpty()) {
                    SectionHeader("Artists") {
                        navController.navigate("library")
                    }
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(libraryState.artists.take(10)) { artist ->
                            Chip(onClick = { navController.navigate("artist_detail/$artist") },
                                label = { Text(artist, maxLines = 1, overflow = TextOverflow.Ellipsis) })
                        }
                    }
                }
            }
        }

        // MiniPlayer at bottom
        if (playbackState.currentTrack != null) {
            MiniPlayer(
                track = playbackState.currentTrack,
                isPlaying = playbackState.isPlaying,
                progress = playbackState.progress,
                duration = playbackState.duration,
                onTogglePlay = { viewModel.togglePlayPause() },
                onSkipNext = { viewModel.skipNext() },
                onSkipPrevious = { viewModel.skipPrevious() },
                onClick = { navController.navigate("now_playing") },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun QuickActionCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector,
                    modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun SectionHeader(title: String, onMore: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        TextButton(onClick = onMore) { Text("See all") }
    }
}

@Composable
fun SmallTrackCard(track: Track, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(140.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.MusicNote, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(track.title, style = MaterialTheme.typography.bodySmall,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(track.artist, style = MaterialTheme.typography.labelSmall,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
