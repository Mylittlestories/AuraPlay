package com.auraplay.player.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.auraplay.player.ui.screens.*
import com.auraplay.player.ui.viewmodel.MainViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Library : Screen("library")
    object Search : Screen("search")
    object Playlists : Screen("playlists")
    object Settings : Screen("settings")
    object NowPlaying : Screen("now_playing")
    object Queue : Screen("queue")
    object Equalizer : Screen("equalizer")
    object AlbumDetail : Screen("album/{albumName}") {
        fun createRoute(albumName: String) = "album/$albumName"
    }
    object ArtistDetail : Screen("artist/{artistName}") {
        fun createRoute(artistName: String) = "artist/$artistName"
    }
    object FolderDetail : Screen("folder/{folderName}") {
        fun createRoute(folderName: String) = "folder/$folderName"
    }
    object PlaylistDetail : Screen("playlist/{playlistId}") {
        fun createRoute(playlistId: Long) = "playlist/$playlistId"
    }
    object ShuffleSettings : Screen("shuffle_settings")
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Screen.Library, "Library", Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic),
    BottomNavItem(Screen.Search, "Search", Icons.Filled.Search, Icons.Outlined.Search),
    BottomNavItem(Screen.Playlists, "Playlists", Icons.Filled.QueueMusic, Icons.Outlined.QueueMusic),
    BottomNavItem(Screen.Settings, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuraPlayNavHost() {
    val navController = rememberNavController()
    val viewModel: MainViewModel = hiltViewModel()
    val currentTrack by viewModel.currentTrack.collectAsStateWithLifecycle()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavItems.map { it.screen.route }
    val showMiniPlayer = currentTrack != null && currentRoute != Screen.NowPlaying.route

    Scaffold(
        bottomBar = {
            Column {
                // Mini Player
                if (showMiniPlayer) {
                    MiniPlayer(
                        viewModel = viewModel,
                        onExpand = { navController.navigate(Screen.NowPlaying.route) }
                    )
                }

                // Bottom Navigation
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        bottomNavItems.forEach { item ->
                            val selected = currentRoute == item.screen.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(item.screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label) }
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues),
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToNowPlaying = { navController.navigate(Screen.NowPlaying.route) },
                    onNavigateToAlbum = { navController.navigate(Screen.AlbumDetail.createRoute(it)) },
                    onNavigateToArtist = { navController.navigate(Screen.ArtistDetail.createRoute(it)) }
                )
            }

            composable(Screen.Library.route) {
                LibraryScreen(
                    viewModel = viewModel,
                    onNavigateToAlbum = { navController.navigate(Screen.AlbumDetail.createRoute(it)) },
                    onNavigateToArtist = { navController.navigate(Screen.ArtistDetail.createRoute(it)) },
                    onNavigateToFolder = { navController.navigate(Screen.FolderDetail.createRoute(it)) },
                    onNavigateToNowPlaying = { navController.navigate(Screen.NowPlaying.route) }
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    viewModel = viewModel,
                    onNavigateToNowPlaying = { navController.navigate(Screen.NowPlaying.route) }
                )
            }

            composable(Screen.Playlists.route) {
                PlaylistsScreen(
                    viewModel = viewModel,
                    onNavigateToPlaylist = { navController.navigate(Screen.PlaylistDetail.createRoute(it)) }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateToEqualizer = { navController.navigate(Screen.Equalizer.route) },
                    onNavigateToShuffleSettings = { navController.navigate(Screen.ShuffleSettings.route) }
                )
            }

            composable(Screen.NowPlaying.route) {
                NowPlayingScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToQueue = { navController.navigate(Screen.Queue.route) },
                    onNavigateToEqualizer = { navController.navigate(Screen.Equalizer.route) }
                )
            }

            composable(Screen.Queue.route) {
                QueueScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Equalizer.route) {
                EqualizerScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.ShuffleSettings.route) {
                ShuffleSettingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.AlbumDetail.route) { backStackEntry ->
                val albumName = backStackEntry.arguments?.getString("albumName") ?: ""
                AlbumDetailScreen(
                    viewModel = viewModel,
                    albumName = albumName,
                    onBack = { navController.popBackStack() },
                    onNavigateToNowPlaying = { navController.navigate(Screen.NowPlaying.route) }
                )
            }

            composable(Screen.ArtistDetail.route) { backStackEntry ->
                val artistName = backStackEntry.arguments?.getString("artistName") ?: ""
                ArtistDetailScreen(
                    viewModel = viewModel,
                    artistName = artistName,
                    onBack = { navController.popBackStack() },
                    onNavigateToNowPlaying = { navController.navigate(Screen.NowPlaying.route) }
                )
            }

            composable(Screen.FolderDetail.route) { backStackEntry ->
                val folderName = backStackEntry.arguments?.getString("folderName") ?: ""
                FolderDetailScreen(
                    viewModel = viewModel,
                    folderName = folderName,
                    onBack = { navController.popBackStack() },
                    onNavigateToNowPlaying = { navController.navigate(Screen.NowPlaying.route) }
                )
            }

            composable(Screen.PlaylistDetail.route) { backStackEntry ->
                val playlistId = backStackEntry.arguments?.getString("playlistId")?.toLongOrNull() ?: 0L
                PlaylistDetailScreen(
                    viewModel = viewModel,
                    playlistId = playlistId,
                    onBack = { navController.popBackStack() },
                    onNavigateToNowPlaying = { navController.navigate(Screen.NowPlaying.route) }
                )
            }
        }
    }
}