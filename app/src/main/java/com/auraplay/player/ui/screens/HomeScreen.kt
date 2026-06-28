package com.auraplay.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.auraplay.player.ui.components.*
import com.auraplay.player.ui.navigation.urlEncode
import com.auraplay.player.ui.theme.*
import com.auraplay.player.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: MainViewModel = hiltViewModel()) {
    val libraryState by viewModel.libraryState.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        HomeHero(
            trackCount = libraryState.trackCount,
            favoriteCount = libraryState.favorites.size,
            currentTrackTitle = playbackState.currentTrack?.title,
            isPlaying = playbackState.isPlaying,
            onSearch = { navController.navigate("search") },
            onSettings = { navController.navigate("settings") },
            onNowPlaying = { navController.navigate("now_playing") }
        )

        when {
            !libraryState.hasPermission -> PermissionCard()
            libraryState.isScanning -> ScanningCard()
            libraryState.trackCount == 0 -> EmptyLibraryCard { viewModel.scanForMusic() }
        }

        if (libraryState.trackCount > 0) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionCard("Shuffle", Icons.Default.Shuffle, Modifier.weight(1f)) {
                    libraryState.tracks.randomOrNull()?.let { viewModel.playTrack(it) }
                }
                QuickActionCard("Library", Icons.Default.LibraryMusic, Modifier.weight(1f)) {
                    navController.navigate("library")
                }
                QuickActionCard("Search", Icons.Default.Search, Modifier.weight(1f)) {
                    navController.navigate("search")
                }
            }

            if (libraryState.favorites.isNotEmpty()) {
                SectionHeader("Favorites") { navController.navigate("library") }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(libraryState.favorites.take(10), key = { it.id }) { track ->
                        SmallTrackCard(track) { viewModel.playTrack(track) }
                    }
                }
            }

            SectionHeader("Recently Added") { navController.navigate("library") }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(libraryState.tracks.take(10), key = { it.id }) { track ->
                    SmallTrackCard(track) { viewModel.playTrack(track) }
                }
            }

            if (libraryState.albums.isNotEmpty()) {
                SectionHeader("Albums") { navController.navigate("library") }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(libraryState.albums.take(12)) { album ->
                        BrowseChip(album, Icons.Default.Album) {
                            navController.navigate("album_detail/${album.urlEncode()}")
                        }
                    }
                }
            }

            if (libraryState.artists.isNotEmpty()) {
                SectionHeader("Artists") { navController.navigate("library") }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(libraryState.artists.take(12)) { artist ->
                        BrowseChip(artist, Icons.Default.Person) {
                            navController.navigate("artist_detail/${artist.urlEncode()}")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HomeHero(
    trackCount: Int,
    favoriteCount: Int,
    currentTrackTitle: String?,
    isPlaying: Boolean,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    onNowPlaying: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(PrimaryContainer, SurfaceVariant, Background)
                )
            )
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    color = Primary.copy(alpha = 0.18f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.GraphicEq, null, tint = Primary, modifier = Modifier.size(28.dp))
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("AuraPlay", style = MaterialTheme.typography.displaySmall, color = TextPrimary)
                    Text("Fast, simple music control", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
                IconButton(onClick = onSearch) { Icon(Icons.Default.Search, "Search", tint = TextPrimary) }
                IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, "Settings", tint = TextPrimary) }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HeroStat("Songs", trackCount.toString(), Modifier.weight(1f))
                HeroStat("Favorites", favoriteCount.toString(), Modifier.weight(1f))
            }

            if (currentTrackTitle != null) {
                Spacer(modifier = Modifier.height(14.dp))
                FilledTonalButton(
                    onClick = onNowPlaying,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Primary.copy(alpha = 0.18f),
                        contentColor = TextPrimary
                    )
                ) {
                    Icon(if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (isPlaying) "Playing: $currentTrackTitle" else "Resume: $currentTrackTitle",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroStat(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Surface.copy(alpha = 0.62f)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(value, style = MaterialTheme.typography.headlineMedium, color = Primary)
            Text(label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
        }
    }
}

@Composable
fun PermissionCard() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryContainer)
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(modifier = Modifier.size(60.dp), shape = CircleShape, color = Primary.copy(alpha = 0.2f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.LibraryMusic, null, tint = Primary, modifier = Modifier.size(30.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Allow music access", style = MaterialTheme.typography.titleLarge, color = OnPrimaryContainer)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "AuraPlay needs access to scan songs stored on this device. Nothing is uploaded.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnPrimaryContainer.copy(alpha = 0.84f)
            )
        }
    }
}

@Composable
fun ScanningCard() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp, color = Primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text("Scanning your music library...", style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
        }
    }
}

@Composable
fun EmptyLibraryCard(onScan: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.MusicOff, null, modifier = Modifier.size(56.dp), tint = TextTertiary)
            Spacer(modifier = Modifier.height(16.dp))
            Text("No music found", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Add audio files to your device, then rescan your library.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onScan,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary)
            ) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scan Again")
            }
        }
    }
}

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
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = SurfaceVariant,
            labelColor = TextPrimary
        )
    )
}
