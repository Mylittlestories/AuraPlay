package com.auraplay.player.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.auraplay.player.ui.components.AuraPlayTopBar
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
            placeholder = { Text("Search songs, artists, albums...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            singleLine = true,
            shape = MaterialTheme.shapes.large
        )

        if (query.isBlank()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Type to search your music", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else if (libraryState.searchResults.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No results for \"$query\"", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
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
