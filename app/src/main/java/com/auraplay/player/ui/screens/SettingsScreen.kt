package com.auraplay.player.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.auraplay.player.ui.components.AuraPlayTopBar
import com.auraplay.player.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: MainViewModel = hiltViewModel()) {
    val libraryState by viewModel.libraryState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        AuraPlayTopBar("Settings", navController)

        Column(modifier = Modifier.padding(16.dp)) {
            // Library section
            Text("Library", style = MaterialTheme.typography.titleMedium)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Tracks in library")
                Text("${libraryState.trackCount}", color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { viewModel.scanForMusic() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Rescan Music Library")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // About section
            Text("About", style = MaterialTheme.typography.titleMedium)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text("AuraPlay v1.0.4", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text("A premium offline music player", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text("This app does not collect, store, or transmit any personal data. " +
                 "All music files are accessed locally on your device.", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
