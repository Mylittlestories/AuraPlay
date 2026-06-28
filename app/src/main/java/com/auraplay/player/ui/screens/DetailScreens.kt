package com.auraplay.player.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.auraplay.player.ui.components.AlbumArt
import com.auraplay.player.ui.components.AuraPlayTopBar
import com.auraplay.player.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    albumName: String,
    navController: NavController,
    viewModel: MainViewModel = hiltViewModel()
) {
    val playbackState by viewModel.playbackState.collectAsState()
    var tracks by remember { mutableStateOf(emptyList<com.auraplay.player.data.model.Track>()) }

    LaunchedEffect(albumName) {
        viewModel.getTracksByAlbum(albumName).collect { tracks = it }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AuraPlayTopBar(albumName, navController)

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                AlbumArt(track = tracks.firstOrNull(), modifier = Modifier.fillMaxSize())
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(albumName, style = MaterialTheme.typography.headlineSmall)
                Text("${tracks.size} tracks", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (tracks.isNotEmpty()) {
            OutlinedButton(
                onClick = { viewModel.playAlbum(albumName) },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text("Play All")
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(tracks, key = { it.id }) { track ->
                TrackListItem(
                    track = track,
                    isCurrentTrack = track.id == playbackState.currentTrack?.id,
                    isPlaying = playbackState.isPlaying && track.id == playbackState.currentTrack?.id,
                    onClick = { viewModel.playTrack(track) },
                    onFavoriteClick = { viewModel.toggleFavorite(track) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    artistName: String,
    navController: NavController,
    viewModel: MainViewModel = hiltViewModel()
) {
    val playbackState by viewModel.playbackState.collectAsState()
    var tracks by remember { mutableStateOf(emptyList<com.auraplay.player.data.model.Track>()) }

    LaunchedEffect(artistName) {
        viewModel.getTracksByArtist(artistName).collect { tracks = it }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AuraPlayTopBar(artistName, navController)

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(artistName, style = MaterialTheme.typography.headlineSmall)
                Text("${tracks.size} tracks", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (tracks.isNotEmpty()) {
            OutlinedButton(
                onClick = { viewModel.playArtist(artistName) },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text("Play All")
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(tracks, key = { it.id }) { track ->
                TrackListItem(
                    track = track,
                    isCurrentTrack = track.id == playbackState.currentTrack?.id,
                    isPlaying = playbackState.isPlaying && track.id == playbackState.currentTrack?.id,
                    onClick = { viewModel.playTrack(track) },
                    onFavoriteClick = { viewModel.toggleFavorite(track) }
                )
            }
        }
    }
}
