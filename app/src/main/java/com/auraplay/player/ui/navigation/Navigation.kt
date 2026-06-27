package com.auraplay.player.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.auraplay.player.ui.screens.*
import java.net.URLDecoder

@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController)
        }
        composable("library") {
            LibraryScreen()
        }
        composable("now_playing") {
            NowPlayingScreen()
        }
        composable("equalizer") {
            EqualizerScreen()
        }
        composable("search") {
            SearchScreen()
        }
        composable("queue") {
            QueueScreen()
        }
        composable("settings") {
            SettingsScreen()
        }
        composable("shuffle_settings") {
            ShuffleSettingsScreen()
        }
        composable("playlists") {
            PlaylistsScreen()
        }
        composable(
            route = "album_detail/{albumName}",
            arguments = listOf(navArgument("albumName") { type = NavType.StringType })
        ) { backStackEntry ->
            val albumName = URLDecoder.decode(
                backStackEntry.arguments?.getString("albumName") ?: "", "UTF-8"
            )
            AlbumDetailScreen(albumName)
        }
        composable(
            route = "artist_detail/{artistName}",
            arguments = listOf(navArgument("artistName") { type = NavType.StringType })
        ) { backStackEntry ->
            val artistName = URLDecoder.decode(
                backStackEntry.arguments?.getString("artistName") ?: "", "UTF-8"
            )
            ArtistDetailScreen(artistName)
        }
    }
}
