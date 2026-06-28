package com.auraplay.player.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.auraplay.player.ui.components.AuraPlayTopBar
import com.auraplay.player.ui.theme.*
import com.auraplay.player.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController, viewModel: MainViewModel = hiltViewModel()) {
    val libraryState by viewModel.libraryState.collectAsState()
    val playbackState by viewModel.playbackState.collectAsState()
    var query by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        AuraPlayTopBar("Search", navController)

        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                viewModel.searchTracks(it)
            },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            placeholder = { Text("Song, artist, album...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = {
                        query = ""
                        viewModel.searchTracks("")
                    }) { Icon(Icons.Default.Close, "Clear") }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = Outline,
                focusedContainerColor = SurfaceVariant,
                unfocusedContainerColor = Surface,
                cursorColor = Primary
            )
        )

        when {
            query.isBlank() -> SearchStartState(
                tracks = libraryState.trackCount,
                albums = libraryState.albums.size,
                artists = libraryState.artists.size
            )
            libraryState.searchResults.isEmpty() -> SearchEmptyState(query)
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                item {
                    Text(
                        "${libraryState.searchResults.size} result${if (libraryState.searchResults.size == 1) "" else "s"}",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary
                    )
                }
                items(libraryState.searchResults, key = { it.id }) { track ->
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
}

@Composable
private fun SearchStartState(tracks: Int, albums: Int, artists: Int) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Search, null, modifier = Modifier.size(56.dp), tint = Primary)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Find music fast", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
        Text("Search across your local songs, artists and albums.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Spacer(modifier = Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = {}, label = { Text("$tracks songs") }, leadingIcon = { Icon(Icons.Default.LibraryMusic, null, Modifier.size(18.dp)) })
            AssistChip(onClick = {}, label = { Text("$albums albums") }, leadingIcon = { Icon(Icons.Default.Album, null, Modifier.size(18.dp)) })
            AssistChip(onClick = {}, label = { Text("$artists artists") }, leadingIcon = { Icon(Icons.Default.Mic, null, Modifier.size(18.dp)) })
        }
    }
}

@Composable
private fun SearchEmptyState(query: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Search, null, modifier = Modifier.size(48.dp), tint = TextTertiary)
            Spacer(modifier = Modifier.height(12.dp))
            Text("No results for \"$query\"", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            Text("Try a shorter name or another artist.", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
