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
fun ShuffleSettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val shuffleMode by viewModel.shuffleMode.collectAsStateWithLifecycle()

    val shuffleModes = listOf(
        ShuffleModeItem(
            mode = ShuffleManager.ShuffleMode.OFF,
            icon = Icons.Default.MusicNote,
            title = "Off",
            description = "Play tracks in order",
            color = Color.Gray
        ),
        ShuffleModeItem(
            mode = ShuffleManager.ShuffleMode.SMART,
            icon = Icons.Default.AutoAwesome,
            title = "Smart Shuffle",
            description = "No repetition, varied artists & albums. The best shuffle experience.",
            color = ShuffleSmart
        ),
        ShuffleModeItem(
            mode = ShuffleManager.ShuffleMode.TRUE_RANDOM,
            icon = Icons.Default.ShuffleOn,
            title = "True Random",
            description = "Pure random selection. May repeat tracks.",
            color = ShuffleRandom
        ),
        ShuffleModeItem(
            mode = ShuffleManager.ShuffleMode.ARTIST_MIX,
            icon = Icons.Default.Person,
            title = "Artist Mix",
            description = "Prioritizes different artists for variety.",
            color = ShuffleArtist
        ),
        ShuffleModeItem(
            mode = ShuffleManager.ShuffleMode.ALBUM_MIX,
            icon = Icons.Default.Album,
            title = "Album Mix",
            description = "Mixes tracks from different albums.",
            color = ShuffleAlbum
        ),
        ShuffleModeItem(
            mode = ShuffleManager.ShuffleMode.GENRE_MIX,
            icon = Icons.Default.Category,
            title = "Genre Mix",
            description = "Rotates through different genres.",
            color = ShuffleGenre
        ),
        ShuffleModeItem(
            mode = ShuffleManager.ShuffleMode.RATING_WEIGHTED,
            icon = Icons.Default.TrendingUp,
            title = "Most Played First",
            description = "Plays your favorite tracks more often.",
            color = Color(0xFFFFD700)
        ),
        ShuffleModeItem(
            mode = ShuffleManager.ShuffleMode.WEIGHTED,
            icon = Icons.Default.Explore,
            title = "Discovery Mode",
            description = "Prioritizes less-played tracks. Great for music discovery!",
            color = AccentCyan
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = { Text("Shuffle Mode", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
            }
        )

        // Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Choose how AuraPlay shuffles your music. Each mode offers a unique listening experience.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Shuffle Modes
        shuffleModes.forEach { item ->
            ShuffleModeCard(
                item = item,
                isSelected = shuffleMode == item.mode,
                onClick = { viewModel.setShuffleMode(item.mode) }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

data class ShuffleModeItem(
    val mode: ShuffleManager.ShuffleMode,
    val icon: ImageVector,
    val title: String,
    val description: String,
    val color: Color
)

@Composable
fun ShuffleModeCard(
    item: ShuffleModeItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                item.color.copy(alpha = 0.15f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = if (isSelected) BorderStroke(2.dp, item.color) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    item.icon,
                    contentDescription = null,
                    tint = if (isSelected) item.color else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) item.color else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = item.color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}