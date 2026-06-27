package com.auraplay.player.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.auraplay.player.ui.components.AuraPlayTopBar
import com.auraplay.player.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(navController: NavController, viewModel: MainViewModel = hiltViewModel()) {
    val eqState by viewModel.eqState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        AuraPlayTopBar("Equalizer", navController)

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            // Presets
            Text("Presets", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(viewModel.getPresets()) { preset ->
                    FilterChip(
                        selected = eqState.presetName == preset,
                        onClick = { viewModel.applyPreset(preset) },
                        label = { Text(preset) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Band sliders
            if (eqState.bands.isNotEmpty()) {
                Text("Frequency Bands", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    eqState.bands.forEachIndexed { index, band ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            val freqText = when {
                                band.freq >= 1000 -> "${band.freq / 1000}k"
                                else -> "${band.freq}"
                            }
                            Text(freqText, fontSize = 9.sp, textAlign = TextAlign.Center)

                            Slider(
                                value = band.level.toFloat(),
                                onValueChange = { viewModel.setBandLevel(index, it.toInt()) },
                                valueRange = band.minLevel.toFloat()..band.maxLevel.toFloat(),
                                modifier = Modifier.height(120.dp).width(40.dp),
                                colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary)
                            )

                            Text("${band.level / 100}dB", fontSize = 8.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bass Boost
            Text("Bass Boost", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = eqState.bassBoost.toFloat(),
                onValueChange = { viewModel.setBassBoost(it.toInt()) },
                valueRange = 0f..1000f,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Virtualizer
            Text("Surround Sound", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = eqState.virtualizerStrength.toFloat(),
                onValueChange = { viewModel.setVirtualizer(it.toInt()) },
                valueRange = 0f..1000f,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Loudness
            Text("Loudness Enhancer", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = eqState.loudnessGain.toFloat(),
                onValueChange = { viewModel.setLoudness(it.toInt()) },
                valueRange = 0f..5000f,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
