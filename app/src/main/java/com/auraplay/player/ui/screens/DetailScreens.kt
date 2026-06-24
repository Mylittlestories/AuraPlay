package com.auraplay.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.auraplay.player.data.model.Playlist
import com.auraplay.player.ui.theme.*
import com.auraplay.player.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    viewModel: MainViewModel,
    albumName: String,
    onBack: () -> Unit,
    onNavigateToNowPlaying: () -> Unit
) {
    val albumTracks by viewModel.currentAlbumTracks.collectAsStateWithLifecycle()

    LaunchedEffect(albumName) {
        viewModel.loadAlbumTracks(albumName)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(albumName, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
            }
        )

        if (albumTracks.isNotEmpty()) {
            // Album Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(AccentGradientStart.copy(alpha = 0.3f), Color.Transparent)
                        )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(AccentGradientStart, AccentGradientEnd)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val artUri = albumTracks.firstOrNull()?.albumArtUri
                        if (artUri != null) {
                            AsyncImage(
                                model = artUri,
                                contentDescription = "Album Art",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.Album,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            albumName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            albumTracks.firstOrNull()?.artist ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "${albumTracks.size} tracks",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (albumTracks.firstOrNull()?.year != 0) {
                            Text(
                                "${albumTracks.first()?.year}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Play Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.playAlbum(albumName)
                        onNavigateToNowPlaying()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PlayArrow, null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Play All")
                }
                OutlinedButton(
                    onClick = {
                        viewModel.playAll(albumTracks, shuffle = true)
                        onNavigateToNowPlaying()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Shuffle, null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Shuffle")
                }
            }

            // Track List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(albumTracks.size) { index ->
                    val track = albumTracks[index]
                    TrackListItem(
                        track = track,
                        onClick = {
                            viewModel.playTrack(track, albumTracks)
                            onNavigateToNowPlaying()
                        },
                        onMoreClick = { /* Show options */ },
                        showIndex = true,
                        index = index
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    viewModel: MainViewModel,
    artistName: String,
    onBack: () -> Unit,
    onNavigateToNowPlaying: () -> Unit
) {
    val artistTracks by viewModel.currentArtistTracks.collectAsStateWithLifecycle()

    LaunchedEffect(artistName) {
        viewModel.loadArtistTracks(artistName)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(artistName, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
            }
        )

        if (artistTracks.isNotEmpty()) {
            // Artist Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(AccentBlue.copy(alpha = 0.3f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(40.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(AccentBlue, AccentCyan)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "${artistTracks.size} tracks • ${artistTracks.map { it.album }.distinct().size} albums",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Play Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.playArtist(artistName)
                        onNavigateToNowPlaying()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PlayArrow, null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Play All")
                }
                OutlinedButton(
                    onClick = {
                        viewModel.playAll(artistTracks, shuffle = true)
                        onNavigateToNowPlaying()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Shuffle, null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Shuffle")
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(artistTracks) { track ->
                    TrackListItem(
                        track = track,
                        onClick = {
                            viewModel.playTrack(track, artistTracks)
                            onNavigateToNowPlaying()
                        },
                        onMoreClick = { /* Show options */ }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderDetailScreen(
    viewModel: MainViewModel,
    folderName: String,
    onBack: () -> Unit,
    onNavigateToNowPlaying: () -> Unit
) {
    val folderTracks by viewModel.currentFolderTracks.collectAsStateWithLifecycle()

    LaunchedEffect(folderName) {
        viewModel.loadFolderTracks(folderName)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(folderName, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
            }
        )

        if (folderTracks.isNotEmpty()) {
            // Folder Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        folderName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${folderTracks.size} tracks",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Play Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.playFolder(folderName)
                        onNavigateToNowPlaying()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PlayArrow, null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Play All")
                }
                OutlinedButton(
                    onClick = {
                        viewModel.playAll(folderTracks, shuffle = true)
                        onNavigateToNowPlaying()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Shuffle, null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Shuffle")
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(folderTracks) { track ->
                    TrackListItem(
                        track = track,
                        onClick = {
                            viewModel.playTrack(track, folderTracks)
                            onNavigateToNowPlaying()
                        },
                        onMoreClick = { /* Show options */ }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    viewModel: MainViewModel,
    playlistId: Long,
    onBack: () -> Unit,
    onNavigateToNowPlaying: () -> Unit
) {
    val playlistTracks by viewModel.currentPlaylistTracks.collectAsStateWithLifecycle()

    LaunchedEffect(playlistId) {
        viewModel.loadPlaylistTracks(playlistId)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Playlist", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
            }
        )

        // Play Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    viewModel.playPlaylist(playlistId)
                    onNavigateToNowPlaying()
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.PlayArrow, null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Play All")
            }
            OutlinedButton(
                onClick = {
                    viewModel.playAll(playlistTracks, shuffle = true)
                    onNavigateToNowPlaying()
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Shuffle, null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Shuffle")
            }
        }

        if (playlistTracks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.QueueMusic,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Playlist is empty",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(playlistTracks.size) { index ->
                    val track = playlistTracks[index]
                    TrackListItem(
                        track = track,
                        onClick = {
                            viewModel.playTrack(track, playlistTracks)
                            onNavigateToNowPlaying()
                        },
                        onMoreClick = { /* Show options */ },
                        showIndex = true,
                        index = index
                    )
                }
            }
        }
    }
}