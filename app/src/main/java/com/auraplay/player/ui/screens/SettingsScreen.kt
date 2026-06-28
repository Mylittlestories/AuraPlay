package com.auraplay.player.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.auraplay.player.ui.components.AuraPlayTopBar
import com.auraplay.player.ui.theme.*
import com.auraplay.player.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: MainViewModel = hiltViewModel()) {
    val libraryState by viewModel.libraryState.collectAsState()
    val appearance by viewModel.appearance.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        AuraPlayTopBar("Settings", navController)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsCard(title = "Appearance", icon = Icons.Default.Palette) {
                Text("Theme", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AuraThemePreset.entries.forEach { preset ->
                        FilterChip(
                            selected = appearance.themePreset == preset,
                            onClick = { viewModel.setThemePreset(preset) },
                            label = { Text(preset.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(Modifier.height(18.dp))

                Text("Background color", style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                Text("Choose the main app backdrop color.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AuraBackgroundPreset.entries.forEach { preset ->
                        FilterChip(
                            selected = appearance.backgroundPreset == preset,
                            onClick = { viewModel.setBackgroundPreset(preset) },
                            label = { Text(preset.label) },
                            leadingIcon = {
                                Surface(
                                    modifier = Modifier.size(16.dp),
                                    shape = CircleShape,
                                    color = previewColor(preset)
                                ) {}
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            SettingsCard(title = "Library", icon = Icons.Default.Storage) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tracks in library", color = TextPrimary)
                    Text("${libraryState.trackCount}", color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.scanForMusic() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (libraryState.isScanning) "Scanning..." else "Rescan Music Library")
                }
            }

            SettingsCard(title = "About", icon = Icons.Default.Palette) {
                Text("AuraPlay v1.1.0", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("A premium offline music player", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "This app does not collect, store, or transmit personal data. All music files are accessed locally on your device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(40.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            }
            Spacer(Modifier.height(14.dp))
            content()
        }
    }
}

private fun previewColor(preset: AuraBackgroundPreset): Color = when (preset) {
    AuraBackgroundPreset.DEEP -> Background
    AuraBackgroundPreset.BLACK -> Color.Black
    AuraBackgroundPreset.BLUE -> Color(0xFF081321)
    AuraBackgroundPreset.PURPLE -> Color(0xFF140B22)
}
