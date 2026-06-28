package com.auraplay.player.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.auraplay.player.ui.components.MiniPlayer
import com.auraplay.player.ui.screens.*
import com.auraplay.player.ui.theme.Background
import com.auraplay.player.ui.theme.PrimaryContainer
import com.auraplay.player.ui.theme.Surface
import com.auraplay.player.ui.theme.TextPrimary
import com.auraplay.player.ui.theme.TextSecondary
import com.auraplay.player.ui.viewmodel.MainViewModel
import java.net.URLDecoder
import java.net.URLEncoder

private data class BottomDestination(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private val bottomDestinations = listOf(
    BottomDestination("home", "Home", Icons.Default.Home),
    BottomDestination("library", "Library", Icons.Default.LibraryMusic),
    BottomDestination("search", "Search", Icons.Default.Search),
    BottomDestination("equalizer", "EQ", Icons.Default.Equalizer),
    BottomDestination("settings", "Settings", Icons.Default.Settings)
)

@Composable
fun Navigation(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val playbackState by viewModel.playbackState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (currentRoute != "now_playing") {
                Column {
                    if (playbackState.currentTrack != null) {
                        MiniPlayer(
                            track = playbackState.currentTrack,
                            isPlaying = playbackState.isPlaying,
                            progress = playbackState.progress,
                            duration = playbackState.duration,
                            onTogglePlay = { viewModel.togglePlayPause() },
                            onSkipNext = { viewModel.skipNext() },
                            onSkipPrevious = { viewModel.skipPrevious() },
                            onClick = { navController.navigate("now_playing") }
                        )
                    }
                    AuraBottomNavigation(navController)
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen(navController, viewModel) }
            composable("library") { LibraryScreen(navController, viewModel) }
            composable("now_playing") { NowPlayingScreen(navController, viewModel) }
            composable("equalizer") { EqualizerScreen(navController, viewModel) }
            composable("search") { SearchScreen(navController, viewModel) }
            composable("queue") { QueueScreen(navController, viewModel) }
            composable("settings") { SettingsScreen(navController, viewModel) }
            composable("shuffle_settings") { ShuffleSettingsScreen(navController, viewModel) }
            composable("playlists") { PlaylistsScreen(navController, viewModel) }
            composable(
                route = "album_detail/{albumName}",
                arguments = listOf(navArgument("albumName") { type = NavType.StringType })
            ) { backStackEntry ->
                val albumName = URLDecoder.decode(backStackEntry.arguments?.getString("albumName") ?: "", "UTF-8")
                AlbumDetailScreen(albumName, navController, viewModel)
            }
            composable(
                route = "artist_detail/{artistName}",
                arguments = listOf(navArgument("artistName") { type = NavType.StringType })
            ) { backStackEntry ->
                val artistName = URLDecoder.decode(backStackEntry.arguments?.getString("artistName") ?: "", "UTF-8")
                ArtistDetailScreen(artistName, navController, viewModel)
            }
        }
    }
}

@Composable
private fun AuraBottomNavigation(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        containerColor = Surface,
        tonalElevation = 10.dp
    ) {
        bottomDestinations.forEach { destination ->
            val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(destination.icon, contentDescription = destination.label) },
                label = { Text(destination.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = TextPrimary,
                    selectedTextColor = TextPrimary,
                    indicatorColor = PrimaryContainer,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                )
            )
        }
    }
}

fun String.urlEncode(): String = URLEncoder.encode(this, "UTF-8")
