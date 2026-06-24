package com.auraplay.player.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auraplay.player.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: MainViewModel,
    onNavigateToNowPlaying: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val allTracks by viewModel.allTracks.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search Bar
        TopAppBar(
            title = {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search songs, artists, albums...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Search, "Search")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, "Clear")
                            }
                        }
                    },
                    shape = MaterialTheme.shapes.large
                )
            }
        )

        if (searchQuery.isBlank()) {
            // Show recent searches or suggestions
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    Text(
                        "Quick Access",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                item {
                    QuickSearchItem(
                        icon = Icons.Default.Shuffle,
                        title = "Shuffle All",
                        subtitle = "${allTracks.size} tracks",
                        onClick = { viewModel.shuffleAll(); onNavigateToNowPlaying() }
                    )
                }

                item {
                    QuickSearchItem(
                        icon = Icons.Default.Favorite,
                        title = "Favorites",
                        subtitle = "Your liked songs",
                        onClick = {
                            val favs = viewModel.favoriteTracks.value
                            if (favs.isNotEmpty()) {
                                viewModel.playTrack(favs.first(), favs)
                                onNavigateToNowPlaying()
                            }
                        }
                    )
                }

                item {
                    QuickSearchItem(
                        icon = Icons.Default.TrendingUp,
                        title = "Most Played",
                        subtitle = "Your top tracks",
                        onClick = {
                            val most = viewModel.mostPlayed.value
                            if (most.isNotEmpty()) {
                                viewModel.playTrack(most.first(), most)
                                onNavigateToNowPlaying()
                            }
                        }
                    )
                }

                item {
                    QuickSearchItem(
                        icon = Icons.Default.History,
                        title = "Recently Played",
                        subtitle = "Pick up where you left off",
                        onClick = {
                            val recent = viewModel.recentlyPlayed.value
                            if (recent.isNotEmpty()) {
                                viewModel.playTrack(recent.first(), recent)
                                onNavigateToNowPlaying()
                            }
                        }
                    )
                }
            }
        } else {
            // Search Results
            if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No results found",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Try a different search term",
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
                        Text(
                            "${searchResults.size} results",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    items(searchResults) { track ->
                        TrackListItem(
                            track = track,
                            onClick = {
                                viewModel.playTrack(track, searchResults)
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

@Composable
fun QuickSearchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
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
        }
    }
}