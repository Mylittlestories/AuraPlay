package com.auraplay.player.ui.navigation
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.auraplay.player.ui.screens.HomeScreen

@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("library") { com.auraplay.player.ui.screens.LibraryScreen() }
        composable("now_playing") { com.auraplay.player.ui.screens.NowPlayingScreen() }
        composable("equalizer") { com.auraplay.player.ui.screens.EqualizerScreen() }
        composable("search") { com.auraplay.player.ui.screens.SearchScreen() }
        composable("queue") { com.auraplay.player.ui.screens.QueueScreen() }
        composable("settings") { com.auraplay.player.ui.screens.SettingsScreen() }
        composable("shuffle_settings") { com.auraplay.player.ui.screens.ShuffleSettingsScreen() }
        composable("playlists") { com.auraplay.player.ui.screens.PlaylistsScreen() }
    }
}
