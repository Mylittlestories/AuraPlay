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
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateToEqualizer: () -> Unit,
    onNavigateToShuffleSettings: () -> Unit
) {
    val playbackSpeed by viewModel.playbackSpeed.collectAsStateWithLifecycle()
    val shuffleMode by viewModel.shuffleMode.collectAsStateWithLifecycle()
    val allTracks by viewModel.allTracks.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = { Text("Settings", fontWeight = FontWeight.Bold) }
        )

        // Library Section
        SettingsSection(title = "Library") {
            SettingsItem(
                icon = Icons.Default.Refresh,
                title = "Scan for Music",
                subtitle = "${allTracks.size} tracks found",
                onClick = { viewModel.scanForMusic() }
            )
        }

        // Audio Section
        SettingsSection(title = "Audio") {
            SettingsItem(
                icon = Icons.Default.Equalizer,
                title = "Equalizer & Sound",
                subtitle = "Bass boost, virtualizer, EQ bands",
                onClick = onNavigateToEqualizer
            )

            SettingsItem(
                icon = Icons.Default.Speed,
                title = "Playback Speed",
                subtitle = "Current: ${playbackSpeed}x",
                onClick = {
                    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f)
                    val currentIndex = speeds.indexOf(playbackSpeed).coerceAtLeast(0)
                    val nextIndex = (currentIndex + 1) % speeds.size
                    viewModel.setPlaybackSpeed(speeds[nextIndex])
                }
            )
        }

        // Shuffle Section
        SettingsSection(title = "Shuffle") {
            SettingsItem(
                icon = Icons.Default.Shuffle,
                title = "Shuffle Mode",
                subtitle = getShuffleModeName(shuffleMode),
                onClick = onNavigateToShuffleSettings
            )
        }

        // About Section
        SettingsSection(title = "About") {
            SettingsItem(
                icon = Icons.Default.Info,
                title = "AuraPlay",
                subtitle = "Version 1.0.0 • DAC Quality Audio Player",
                onClick = { }
            )

            SettingsItem(
                icon = Icons.Default.Code,
                title = "Built with",
                subtitle = "ExoPlayer • Jetpack Compose • Material 3",
                onClick = { }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
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
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    subtitle,
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