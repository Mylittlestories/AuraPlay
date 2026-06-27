package com.auraplay.player.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.auraplay.player.ui.components.*
import com.auraplay.player.ui.theme.*
import com.auraplay.player.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: MainViewModel = hiltViewModel()) {
    val libraryState by viewModel.libraryState.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
        ) {
            // ─── Top bar ───
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "AuraPlay",
                        style = MaterialTheme.typography.displaySmall,
                        color = Primary
                    )
                    Text(
                        "Your music, your way",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                IconButton(onClick = { navController.navigate("search") }) {
                    Icon(Icons.Default.Search, "Search", tint = TextSecondary)
                }
                IconButton(onClick = { navController.navigate("settings") }) {
                    Icon(Icons.Default.Settings, "Settings", tint = TextSecondary)
                }
            }

            // ─── Permission / Scan states ───
            if (!libraryState.hasPermission) {
                PermissionCard()
            } else if (libraryState.isScanning) {
                ScanningCard()
            } else if (libraryState.trackCount == 0) {
                EmptyLibraryCard { viewModel.scanForMusic() }
            }

            if (libraryState.trackCount > 0) {
                // ─── Quick Actions ───
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
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

                // ─── Favorites ───
                if (libraryState.favorites.isNotEmpty()) {
                    SectionHeader("♥ Favorites") { navController.navigate("library") }
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(libraryState.favorites.take(10)) { track ->
                            SmallTrackCard(track) { viewModel.playTrack(track) }
                        }
                    }
                }

                // ─── Recently Added ───
                SectionHeader("Recently Added") { navController.navigate("library") }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(libraryState.tracks.take(10)) { track ->
                        SmallTrackCard(track) { viewModel.playTrack(track) }
                    }
                }

                // ─── Albums ───
                if (libraryState.albums.isNotEmpty()) {
                    SectionHeader("Albums") { navController.navigate("library") }
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(libraryState.albums.take(10)) { album ->
                            BrowseChip(album, Icons.Default.Album) {
                                navController.navigate("album_detail/${album.urlEncode()}")
                            }
                        }
                    }
                }

                // ─── Artists ───
                if (libraryState.artists.isNotEmpty()) {
                    SectionHeader("Artists") { navController.navigate("library") }
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(libraryState.artists.take(10)) { artist ->
                            BrowseChip(artist, Icons.Default.Person) {
                                navController.navigate("artist_detail/${artist.urlEncode()}")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // ─── MiniPlayer ───
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

fun String.urlEncode(): String = java.net.URLEncoder.encode(this, "UTF-8")

// ─── Permission card ───
@Composable
fun PermissionCard() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = Primary.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.LibraryMusic, null, tint = Primary, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Access Your Music", style = MaterialTheme.typography.titleLarge, color = OnPrimaryContainer)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "AuraPlay needs permission to find and play your music. Your data never leaves your device.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

// ─── Scanning card ───
@Composable
fun ScanningCard() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp, color = Primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text("Scanning for music...", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
        }
    }
}

// ─── Empty library card ───
@Composable
fun EmptyLibraryCard(onScan: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.MusicOff, null, modifier = Modifier.size(56.dp), tint = TextTertiary)
            Spacer(modifier = Modifier.height(16.dp))
            Text("No music found", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Add music files to your device, then tap the button below.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onScan,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary)
            ) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scan Again")
            }
        }
    }
}

// ─── Browse chip for albums/artists ───
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseChip(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    SuggestionChip(
        onClick = onClick,
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, modifier = Modifier.size(16.dp), tint = Primary)
                Spacer(modifier = Modifier.width(6.dp))
                Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelMedium)
            }
        },
        border = ChipDefaults.outlinedChipBorder(enabled = true, borderColor = Outline),
        colors = ChipDefaults.suggestionChipColors(containerColor = SurfaceVariant)
    )
}
