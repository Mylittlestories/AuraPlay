package com.auraplay.player.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.auraplay.player.audio.ShuffleMode
import com.auraplay.player.ui.components.AuraPlayTopBar
import com.auraplay.player.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShuffleSettingsScreen(navController: NavController, viewModel: MainViewModel = hiltViewModel()) {
    val currentMode by viewModel.shuffleMode.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        AuraPlayTopBar("Shuffle Mode", navController)

        LazyColumn(modifier = Modifier.padding(16.dp)) {
            items(ShuffleMode.entries) { mode ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (mode == currentMode) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface
                    ),
                    onClick = { viewModel.setShuffleMode(mode) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(mode.label, style = MaterialTheme.typography.bodyLarge)
                            Text(getShuffleDescription(mode),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        RadioButton(selected = mode == currentMode, onClick = { viewModel.setShuffleMode(mode) })
                    }
                }
            }
        }
    }
}

fun getShuffleDescription(mode: ShuffleMode): String = when (mode) {
    ShuffleMode.OFF -> "Play tracks in order"
    ShuffleMode.SMART -> "No repetition, varied artists & albums"
    ShuffleMode.TRUE_RANDOM -> "Pure random selection"
    ShuffleMode.ARTIST_MIX -> "Prioritizes different artists"
    ShuffleMode.ALBUM_MIX -> "Mixes tracks from different albums"
    ShuffleMode.GENRE_MIX -> "Rotates through different genres"
    ShuffleMode.MOST_PLAYED -> "Plays your favorites more often"
    ShuffleMode.DISCOVERY -> "Prioritizes less-played tracks"
}
