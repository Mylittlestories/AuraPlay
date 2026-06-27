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
import com.auraplay.player.ui.components.AuraPlayTopBar
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
            Text("Library", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.weight(1f))
            Text("${libraryState.trackCount} tracks", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            IconButton(onClick = { viewModel.scanForMusic() }) {
                Icon(Icons.Default.Refresh, "Rescan")
            }
        }

        // Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        // Tab content
        when (selectedTab) {
            0 -> TracksList(
                tracks = libraryState.tracks,
                currentTrack = playbackState.currentTrack,
                isPlaying = playbackState.isPlaying,
                onTrackClick = { viewModel.playTrack(it) },
                onFavoriteClick = { viewModel.toggleFavorite(it) }
            )
            1 -> AlbumsList(
                albums = libraryState.albums,
                onAlbumClick = { album ->
                    navController.navigate("album_detail/${album.urlEncode()}")
                }
            )
            2 -> ArtistsList(
                artists = libraryState.artists,
                onArtistClick = { artist ->
                    navController.navigate("artist_detail/${artist.urlEncode()}")
                }
            )
            3 -> GenresList(
                genres = libraryState.genres,
                onGenreClick = { viewModel.playGenre(it) }
            )
            4 -> FoldersList(
                folders = libraryState.folders,
                onFolderClick = { viewModel.playFolder(it) }
            )
            5 -> TracksList(
                tracks = libraryState.favorites,
                currentTrack = playbackState.currentTrack,
                isPlaying = playbackState.isPlaying,
                onTrackClick = { viewModel.playTrack(it) },
                onFavoriteClick = { viewModel.toggleFavorite(it) }
            )
        }
    }
}

fun String.urlEncode(): String = java.net.URLEncoder.encode(this, "UTF-8")

@Composable
fun TracksList(
    tracks: List<com.auraplay.player.data.model.Track>,
    currentTrack: com.auraplay.player.data.model.Track?,
    isPlaying: Boolean,
    onTrackClick: (com.auraplay.player.data.model.Track) -> Unit,
    onFavoriteClick: (com.auraplay.player.data.model.Track) -> Unit
) {
    if (tracks.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No tracks found", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(tracks, key = { it.id }) { track ->
            TrackListItem(
                track = track,
                isCurrentTrack = track.id == currentTrack?.id,
                isPlaying = isPlaying && track.id == currentTrack?.id,
                onClick = { onTrackClick(track) },
                onFavoriteClick = { onFavoriteClick(track) }
            )
        }
    }
}

@Composable
fun AlbumsList(albums: List<String>, onAlbumClick: (String) -> Unit) {
    if (albums.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No albums found", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(albums) { album ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onAlbumClick(album) }
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Album, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(album, style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Icon(Icons.Default.PlayArrow, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun ArtistsList(artists: List<String>, onArtistClick: (String) -> Unit) {
    if (artists.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No artists found", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(artists) { artist ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onArtistClick(artist) }
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(artist, style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Icon(Icons.Default.PlayArrow, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun GenresList(genres: List<String>, onGenreClick: (String) -> Unit) {
    if (genres.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No genres found", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(genres) { genre ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onGenreClick(genre) }
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Category, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Text(genre, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Icon(Icons.Default.PlayArrow, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun FoldersList(folders: List<String>, onFolderClick: (String) -> Unit) {
    if (folders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No folders found", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(folders) { folder ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onFolderClick(folder) }
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Folder, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Text(folder.substringAfterLast("/"), style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Text(folder, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
