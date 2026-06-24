package com.auraplay.player.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auraplay.player.data.model.Track
import com.auraplay.player.ui.theme.*
import com.auraplay.player.ui.viewmodel.MainViewModel

enum class LibraryTab(val title: String, val icon: @Composable () -> Unit) {
    TRACKS({ Icon(Icons.Default.MusicNote, null) }),
    ALBUMS({ Icon(Icons.Default.Album, null) }),
    ARTISTS({ Icon(Icons.Default.Person, null) }),
    GENRES({ Icon(Icons.Default.Category, null) }),
    FOLDERS({ Icon(Icons.Default.Folder, null) }),
    FAVORITES({ Icon(Icons.Default.Favorite, null) })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: MainViewModel,
    onNavigateToAlbum: (String) -> Unit,
    onNavigateToArtist: (String) -> Unit,
    onNavigateToFolder: (String) -> Unit,
    onNavigateToNowPlaying: () -> Unit
) {
    val allTracks by viewModel.allTracks.collectAsStateWithLifecycle()
    val allAlbumNames by viewModel.allAlbumNames.collectAsStateWithLifecycle()
    val allArtistNames by viewModel.allArtistNames.collectAsStateWithLifecycle()
    val allGenreNames by viewModel.allGenreNames.collectAsStateWithLifecycle()
    val allFolderNames by viewModel.allFolderNames.collectAsStateWithLifecycle()
    val favoriteTracks by viewModel.favoriteTracks.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(LibraryTab.TRACKS) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        TopAppBar(
            title = {
                Text(
                    "Library",
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(onClick = { viewModel.scanForMusic() }) {
                    Icon(Icons.Default.Refresh, "Refresh")
                }
            }
        )

        // Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            LibraryTab.entries.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(tab.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    icon = tab.icon
                )
            }
        }

        // Content
        when (selectedTab) {
            LibraryTab.TRACKS -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.playAll(allTracks)
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
                                    viewModel.shuffleAll()
                                    onNavigateToNowPlaying()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Shuffle, null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Shuffle")
                            }
                        }
                    }
                    items(allTracks) { track ->
                        TrackListItem(
                            track = track,
                            onClick = {
                                viewModel.playTrack(track)
                                onNavigateToNowPlaying()
                            },
                            onMoreClick = { /* Show options */ }
                        )
                    }
                }
            }

            LibraryTab.ALBUMS -> {
                val albumGroups = allTracks.groupBy { it.album }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(albumGroups.entries.toList()) { (albumName, tracks) ->
                        AlbumGridItem(
                            albumName = albumName,
                            artist = tracks.firstOrNull()?.artist ?: "",
                            trackCount = tracks.size,
                            artUri = tracks.firstOrNull()?.albumArtUri,
                            onClick = { onNavigateToAlbum(albumName) }
                        )
                    }
                }
            }

            LibraryTab.ARTISTS -> {
                val artistGroups = allTracks.groupBy { it.artist }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(artistGroups.entries.toList()) { (artistName, tracks) ->
                        ArtistListItem(
                            name = artistName,
                            trackCount = tracks.size,
                            albumCount = tracks.map { it.album }.distinct().size,
                            artUri = tracks.firstOrNull()?.albumArtUri,
                            onClick = { onNavigateToArtist(artistName) }
                        )
                    }
                }
            }

            LibraryTab.GENRES -> {
                val genreGroups = allTracks.filter { it.genre.isNotEmpty() }.groupBy { it.genre }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(genreGroups.entries.toList()) { (genreName, tracks) ->
                        GenreListItem(
                            name = genreName,
                            trackCount = tracks.size,
                            onClick = {
                                viewModel.playAll(tracks)
                                onNavigateToNowPlaying()
                            }
                        )
                    }
                }
            }

            LibraryTab.FOLDERS -> {
                val folderGroups = allTracks.groupBy { it.folderName }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(folderGroups.entries.toList()) { (folderName, tracks) ->
                        FolderListItem(
                            name = folderName,
                            trackCount = tracks.size,
                            totalSize = tracks.sumOf { it.fileSize },
                            onClick = { onNavigateToFolder(folderName) }
                        )
                    }
                }
            }

            LibraryTab.FAVORITES -> {
                if (favoriteTracks.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.FavoriteBorder,
                                null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No favorites yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Tap the heart icon on any track to add it",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.playAll(favoriteTracks)
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
                                        viewModel.playAll(favoriteTracks, shuffle = true)
                                        onNavigateToNowPlaying()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Shuffle, null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Shuffle")
                                }
                            }
                        }
                        items(favoriteTracks) { track ->
                            TrackListItem(
                                track = track,
                                onClick = {
                                    viewModel.playTrack(track, favoriteTracks)
                                    onNavigateToNowPlaying()
                                },
                                onMoreClick = { /* Show options */ }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlbumGridItem(
    albumName: String,
    artist: String,
    trackCount: Int,
    artUri: String?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(AccentGradientStart, AccentGradientEnd)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (artUri != null) {
                    coil.compose.AsyncImage(
                        model = artUri,
                        contentDescription = "Album Art",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Album,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(64.dp)
                    )
                }
            }
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = albumName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$artist • $trackCount tracks",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ArtistListItem(
    name: String,
    trackCount: Int,
    albumCount: Int,
    artUri: String?,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(AccentBlue, AccentCyan)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (artUri != null) {
                    AsyncImage(
                        model = artUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$albumCount albums • $trackCount tracks",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun GenreListItem(
    name: String,
    trackCount: Int,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(AccentPurple, Color(0xFFE91E63))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Category,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$trackCount tracks",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun FolderListItem(
    name: String,
    trackCount: Int,
    totalSize: Long,
    onClick: () -> Unit
) {
    val sizeInMb = totalSize / (1024 * 1024)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$trackCount tracks • ${sizeInMb}MB",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}