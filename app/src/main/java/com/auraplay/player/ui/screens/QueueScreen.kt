package com.auraplay.player.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
fun QueueScreen(navController: NavController, viewModel: MainViewModel = hiltViewModel()) {
    val playbackState by viewModel.playbackState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        AuraPlayTopBar("Queue", navController)

        if (playbackState.queue.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Queue is empty", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(playbackState.queue, key = { _, track -> track.id }) { index, track ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TrackListItem(
                            track = track,
                            isCurrentTrack = index == playbackState.queueIndex,
                            isPlaying = playbackState.isPlaying && index == playbackState.queueIndex,
                            onClick = {
                                viewModel.playQueue(playbackState.queue, index)
                            },
                            onFavoriteClick = { viewModel.toggleFavorite(track) }
                        )
                        IconButton(onClick = { viewModel.removeFromQueue(index) }) {
                            Icon(Icons.Default.Close, "Remove", modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}
