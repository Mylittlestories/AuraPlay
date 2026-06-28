package com.auraplay.player.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.auraplay.player.ui.navigation.urlEncode
import com.auraplay.player.ui.components.TrackListItem
import com.auraplay.player.ui.theme.*
import com.auraplay.player.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(navController: NavController, viewModel: MainViewModel = hiltViewModel()) {
    val libraryState by viewModel.libraryState.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Tracks", "Albums", "Artists", "Genres", "Folders", "Favorites")

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Library", style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
                Text("${libraryState.trackCount} tracks • ${libraryState.favorites.size} favorites", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            }
            IconButton(onClick = { navController.navigate("search") }) {
                Icon(Icons.Default.Search, "Search", tint = TextSecondary)
            }
            IconButton(onClick = { viewModel.scanForMusic() }) {
                Icon(Icons.Default.Refresh, "Rescan", tint = TextSecondary)
            }
        }

        if (libraryState.tracks.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.playQueue(libraryState.tracks, 0) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary)
                ) {
                    Icon(Icons.Default.PlayArrow, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Play all")
                }
                OutlinedButton(
                    onClick = { libraryState.tracks.randomOrNull()?.let { viewModel.playTrack(it) } },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                ) {
                    Icon(Icons.Default.Shuffle, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Shuffle")
                }
            }
        }

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Background,
            contentColor = Primary,
            edgePadding = 16.dp,
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, color = if (index == selectedTab) TextPrimary else TextSecondary) }
                )
            }
        }

        when (selectedTab) {
            0 -> TracksList(libraryState.tracks, playbackState.currentTrack, playbackState.isPlaying, { viewModel.playTrack(it) }, { viewModel.toggleFavorite(it) })
            1 -> AlbumsList(libraryState.albums) { navController.navigate("album_detail/${it.urlEncode()}") }
            2 -> ArtistsList(libraryState.artists) { navController.navigate("artist_detail/${it.urlEncode()}") }
            3 -> GenresList(libraryState.genres) { viewModel.playGenre(it) }
            4 -> FoldersList(libraryState.folders) { viewModel.playFolder(it) }
            5 -> TracksList(libraryState.favorites, playbackState.currentTrack, playbackState.isPlaying, { viewModel.playTrack(it) }, { viewModel.toggleFavorite(it) })
        }
    }
}

@Composable
fun TracksList(
    tracks: List<Track>,
    currentTrack: Track?,
    isPlaying: Boolean,
    onTrackClick: (Track) -> Unit,
    onFavoriteClick: (Track) -> Unit
) {
    if (tracks.isEmpty()) {
        EmptyState("No tracks found")
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 6.dp)) {
        items(tracks, key = { it.id }) { track ->
            TrackListItem(
                track,
                track.id == currentTrack?.id,
                isPlaying && track.id == currentTrack?.id,
                { onTrackClick(track) },
                { onFavoriteClick(track) }
            )
        }
    }
}

@Composable
fun AlbumsList(albums: List<String>, onAlbumClick: (String) -> Unit) {
    if (albums.isEmpty()) { EmptyState("No albums found"); return }
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 6.dp)) {
        items(albums) { album ->
            LibraryRow(
                title = album,
                subtitle = "Album",
                icon = Icons.Default.Album,
                iconTint = Primary,
                container = PrimaryContainer,
                shape = RoundedCornerShape(14.dp),
                onClick = { onAlbumClick(album) }
            )
        }
    }
}

@Composable
fun ArtistsList(artists: List<String>, onArtistClick: (String) -> Unit) {
    if (artists.isEmpty()) { EmptyState("No artists found"); return }
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 6.dp)) {
        items(artists) { artist ->
            LibraryRow(
                title = artist,
                subtitle = "Artist",
                icon = Icons.Default.Person,
                iconTint = Secondary,
                container = SecondaryContainer,
                shape = CircleShape,
                onClick = { onArtistClick(artist) }
            )
        }
    }
}

@Composable
fun GenresList(genres: List<String>, onGenreClick: (String) -> Unit) {
    if (genres.isEmpty()) { EmptyState("No genres found"); return }
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 6.dp)) {
        items(genres) { genre ->
            LibraryRow(
                title = genre,
                subtitle = "Genre",
                icon = Icons.Default.Category,
                iconTint = Secondary,
                container = SurfaceVariant,
                shape = RoundedCornerShape(14.dp),
                onClick = { onGenreClick(genre) }
            )
        }
    }
}

@Composable
fun FoldersList(folders: List<String>, onFolderClick: (String) -> Unit) {
    if (folders.isEmpty()) { EmptyState("No folders found"); return }
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 6.dp)) {
        items(folders) { folder ->
            LibraryRow(
                title = folder.substringAfterLast("/"),
                subtitle = folder,
                icon = Icons.Default.Folder,
                iconTint = Secondary,
                container = SurfaceVariant,
                shape = RoundedCornerShape(14.dp),
                onClick = { onFolderClick(folder) }
            )
        }
    }
}

@Composable
private fun LibraryRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    container: androidx.compose.ui.graphics.Color,
    shape: androidx.compose.ui.graphics.Shape,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(modifier = Modifier.size(52.dp), shape = shape, color = container) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = iconTint) }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis, color = TextPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, color = TextTertiary)
        }
        Surface(shape = CircleShape, color = Primary.copy(alpha = 0.14f)) {
            Icon(Icons.Default.ChevronRight, null, tint = Primary, modifier = Modifier.padding(8.dp).size(18.dp))
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.MusicOff, null, modifier = Modifier.size(48.dp), tint = TextTertiary)
            Spacer(modifier = Modifier.height(12.dp))
            Text(message, color = TextSecondary, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
