package com.auraplay.player.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.auraplay.player.ui.screens.*
import java.net.URLEncoder
import java.net.URLDecoder

@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController)
        }
        composable("library") {
            LibraryScreen(navController)
        }
        composable("now_playing") {
            NowPlayingScreen(navController)
        }
        composable("equalizer") {
            EqualizerScreen(navController)
        }
        composable("search") {
            SearchScreen(navController)
        }
        composable("queue") {
            QueueScreen(navController)
        }
        composable("settings") {
            SettingsScreen(navController)
        }
        composable("shuffle_settings") {
            ShuffleSettingsScreen(navController)
        }
        composable("playlists") {
            PlaylistsScreen(navController)
        }
        composable(
            route = "album_detail/{albumName}",
            arguments = listOf(navArgument("albumName") { type = NavType.StringType })
        ) { backStackEntry ->
            val albumName = URLDecoder.decode(
                backStackEntry.arguments?.getString("albumName") ?: "", "UTF-8"
            )
            AlbumDetailScreen(albumName, navController)
        }
        composable(
            route = "artist_detail/{artistName}",
            arguments = listOf(navArgument("artistName") { type = NavType.StringType })
        ) { backStackEntry ->
            val artistName = URLDecoder.decode(
                backStackEntry.arguments?.getString("artistName") ?: "", "UTF-8"
            )
            ArtistDetailScreen(artistName, navController)
        }
    }
}

fun String.urlEncode(): String = URLEncoder.encode(this, "UTF-8")
