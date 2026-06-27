package com.auraplay.player.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import com.auraplay.player.ui.navigation.urlEncode
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
import com.auraplay.player.ui.components.AuraPlayTopBar
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
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Library", style = MaterialTheme.typography.headlineMedium, color = Primary)
            Spacer(modifier = Modifier.weight(1f))
            Text("${libraryState.trackCount} tracks", style = MaterialTheme.typography.labelMedium, color = TextTertiary)
            IconButton(onClick = { viewModel.scanForMusic() }) {
                Icon(Icons.Default.Refresh, "Rescan", tint = TextSecondary)
            }
        }

        // Tabs — teal indicator
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Background,
            contentColor = Primary,
            edgePadding = 16.dp
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, color = if (index == selectedTab) Primary else TextSecondary) }
                )
            }
        }

        // Content
        when (selectedTab) {
            0 -> TracksList(libraryState.tracks, playbackState.currentTrack, playbackState.isPlaying,
                { viewModel.playTrack(it) }, { viewModel.toggleFavorite(it) })
            1 -> AlbumsList(libraryState.albums) { navController.navigate("album_detail/${it.urlEncode()}") }
            2 -> ArtistsList(libraryState.artists) { navController.navigate("artist_detail/${it.urlEncode()}") }
            3 -> GenresList(libraryState.genres) { viewModel.playGenre(it) }
            4 -> FoldersList(libraryState.folders) { viewModel.playFolder(it) }
            5 -> TracksList(libraryState.favorites, playbackState.currentTrack, playbackState.isPlaying,
                { viewModel.playTrack(it) }, { viewModel.toggleFavorite(it) })
        }
    }
}


@Composable
fun TracksList(
    tracks: List<com.auraplay.player.data.model.Track>,
    currentTrack: com.auraplay.player.data.model.Track?,
    isPlaying: Boolean,
    onTrackClick: (com.auraplay.player.data.model.Track) -> Unit,
    onFavoriteClick: (com.auraplay.player.data.model.Track) -> Unit
) {
    if (tracks.isEmpty()) {
        EmptyState("No tracks found")
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(tracks, key = { it.id }) { track ->
            TrackListItem(track, track.id == currentTrack?.id, isPlaying && track.id == currentTrack?.id,
                { onTrackClick(track) }, { onFavoriteClick(track) })
        }
    }
}

@Composable
fun AlbumsList(albums: List<String>, onAlbumClick: (String) -> Unit) {
    if (albums.isEmpty()) { EmptyState("No albums found"); return }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(albums) { album ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onAlbumClick(album) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(modifier = Modifier.size(50.dp), shape = RoundedCornerShape(10.dp), color = PrimaryContainer) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Album, null, tint = Primary) }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(album, style = MaterialTheme.typography.bodyLarge, maxLines = 1,
                    overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f), color = TextPrimary)
                Icon(Icons.Default.PlayArrow, null, tint = Primary, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun ArtistsList(artists: List<String>, onArtistClick: (String) -> Unit) {
    if (artists.isEmpty()) { EmptyState("No artists found"); return }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(artists) { artist ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onArtistClick(artist) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(modifier = Modifier.size(50.dp), shape = CircleShape, color = SecondaryContainer) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Person, null, tint = Secondary) }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(artist, style = MaterialTheme.typography.bodyLarge, maxLines = 1,
                    overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f), color = TextPrimary)
                Icon(Icons.Default.PlayArrow, null, tint = Primary, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun GenresList(genres: List<String>, onGenreClick: (String) -> Unit) {
    if (genres.isEmpty()) { EmptyState("No genres found"); return }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(genres) { genre ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onGenreClick(genre) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(modifier = Modifier.size(50.dp), shape = RoundedCornerShape(10.dp), color = SurfaceVariant) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Category, null, tint = Secondary) }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(genre, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f), color = TextPrimary)
                Icon(Icons.Default.PlayArrow, null, tint = Primary, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun FoldersList(folders: List<String>, onFolderClick: (String) -> Unit) {
    if (folders.isEmpty()) { EmptyState("No folders found"); return }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(folders) { folder ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onFolderClick(folder) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(modifier = Modifier.size(50.dp), shape = RoundedCornerShape(10.dp), color = SurfaceVariant) {
                    Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Folder, null, tint = Secondary) }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(folder.substringAfterLast("/"), style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1, overflow = TextOverflow.Ellipsis, color = TextPrimary)
                    Text(folder, style = MaterialTheme.typography.bodySmall,
                        maxLines = 1, overflow = TextOverflow.Ellipsis, color = TextTertiary)
                }
                Icon(Icons.Default.PlayArrow, null, tint = Primary, modifier = Modifier.size(24.dp))
            }
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
