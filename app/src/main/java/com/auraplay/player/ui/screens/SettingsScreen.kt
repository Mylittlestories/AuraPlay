package com.auraplay.player.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auraplay.player.audio.ShuffleManager
import com.auraplay.player.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel, onNavigateToEqualizer: () -> Unit, onNavigateToShuffleSettings: () -> Unit) {
    val playbackSpeed by viewModel.playbackSpeed.collectAsStateWithLifecycle()
    val shuffleMode by viewModel.shuffleMode.collectAsStateWithLifecycle()
    val allTracks by viewModel.allTracks.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        TopAppBar(title = { Text("Settings", fontWeight = FontWeight.Bold) })

        Text("Library", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
            SettingsItem(icon = Icons.Default.Refresh, title = "Scan for Music", subtitle = "${allTracks.size} tracks found", onClick = { viewModel.scanForMusic() })
        }

        Text("Audio", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
            SettingsItem(icon = Icons.Default.Equalizer, title = "Equalizer & Sound", subtitle = "Bass boost, virtualizer, EQ bands", onClick = onNavigateToEqualizer)
            SettingsItem(icon = Icons.Default.Speed, title = "Playback Speed", subtitle = "Current: ${playbackSpeed}x", onClick = {
                val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
                val idx = speeds.indexOf(playbackSpeed).coerceAtLeast(0)
                viewModel.setPlaybackSpeed(speeds[(idx + 1) % speeds.size])
            })
        }

        Text("Shuffle", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
            SettingsItem(icon = Icons.Default.Shuffle, title = "Shuffle Mode", subtitle = getShuffleModeName(shuffleMode), onClick = onNavigateToShuffleSettings)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

fun getShuffleModeName(mode: ShuffleManager.ShuffleMode): String {
    return when (mode) {
        ShuffleManager.ShuffleMode.OFF -> "Off"
        ShuffleManager.ShuffleMode.SMART -> "Smart (No Repetition)"
        ShuffleManager.ShuffleMode.TRUE_RANDOM -> "True Random"
        ShuffleManager.ShuffleMode.ARTIST_MIX -> "Artist Mix"
        ShuffleManager.ShuffleMode.ALBUM_MIX -> "Album Mix"
        ShuffleManager.ShuffleMode.GENRE_MIX -> "Genre Mix"
        ShuffleManager.ShuffleMode.RATING_WEIGHTED -> "Most Played First"
        ShuffleManager.ShuffleMode.WEIGHTED -> "Discovery (Less Played)"
    }
}