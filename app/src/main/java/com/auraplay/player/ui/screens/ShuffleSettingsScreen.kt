package com.auraplay.player.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auraplay.player.audio.ShuffleManager
import com.auraplay.player.ui.theme.*
import com.auraplay.player.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShuffleSettingsScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val shuffleMode by viewModel.shuffleMode.collectAsStateWithLifecycle()

    val modes = listOf(
        Triple(ShuffleManager.ShuffleMode.OFF, Icons.Default.MusicNote, "Off") to "Play tracks in order",
        Triple(ShuffleManager.ShuffleMode.SMART, Icons.Default.Star, "Smart Shuffle") to "No repetition, varied artists & albums",
        Triple(ShuffleManager.ShuffleMode.TRUE_RANDOM, Icons.Default.Shuffle, "True Random") to "Pure random selection. May repeat tracks",
        Triple(ShuffleManager.ShuffleMode.ARTIST_MIX, Icons.Default.Person, "Artist Mix") to "Prioritizes different artists",
        Triple(ShuffleManager.ShuffleMode.ALBUM_MIX, Icons.Default.Album, "Album Mix") to "Mixes tracks from different albums",
        Triple(ShuffleManager.ShuffleMode.GENRE_MIX, Icons.Default.Category, "Genre Mix") to "Rotates through different genres",
        Triple(ShuffleManager.ShuffleMode.RATING_WEIGHTED, Icons.Default.TrendingUp, "Most Played First") to "Plays your favorites more often",
        Triple(ShuffleManager.ShuffleMode.WEIGHTED, Icons.Default.Explore, "Discovery Mode") to "Prioritizes less-played tracks"
    )

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        TopAppBar(title = { Text("Shuffle Mode", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } })

        Card(modifier = Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Choose how AuraPlay shuffles your music.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        modes.forEach { (triple, desc) ->
            val (mode, icon, title) = triple
            val isSelected = shuffleMode == mode
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable { viewModel.setShuffleMode(mode) },
                colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, null, tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                        Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (isSelected) { Icon(Icons.Default.CheckCircle, "Selected", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp)) }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}