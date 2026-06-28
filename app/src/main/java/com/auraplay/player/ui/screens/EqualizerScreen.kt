package com.auraplay.player.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.auraplay.player.ui.components.AuraPlayTopBar
import com.auraplay.player.ui.theme.*
import com.auraplay.player.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(navController: NavController, viewModel: MainViewModel = hiltViewModel()) {
    val eqState by viewModel.eqState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        AuraPlayTopBar("Equalizer", navController)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(shape = CircleShape, color = PrimaryContainer, modifier = Modifier.size(48.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.GraphicEq, null, tint = Primary)
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sound shaping", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Text(
                            if (eqState.isReady) "Attached to active playback session" else (eqState.errorMessage ?: "Start playback first"),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (eqState.isReady) TextSecondary else Secondary
                        )
                    }
                    Switch(
                        checked = eqState.enabled,
                        onCheckedChange = { viewModel.setEqualizerEnabled(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Primary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text("Presets", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(viewModel.getPresets()) { preset ->
                    FilterChip(
                        selected = eqState.presetName == preset,
                        onClick = { viewModel.applyPreset(preset) },
                        label = { Text(preset) },
                        leadingIcon = if (eqState.presetName == preset) {{ Icon(Icons.Default.Equalizer, null, Modifier.size(16.dp)) }} else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryContainer,
                            selectedLabelColor = TextPrimary,
                            containerColor = Surface
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (eqState.bands.isNotEmpty()) {
                Text("Frequency Bands", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 16.dp)
                            .alpha(if (eqState.enabled) 1f else 0.45f),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        eqState.bands.forEachIndexed { index, band ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                val db = band.level / 100f
                                Text(
                                    if (db >= 0f) "+%.1f".format(db) else "%.1f".format(db),
                                    fontSize = 9.sp,
                                    color = if (band.level == 0) TextTertiary else Primary,
                                    textAlign = TextAlign.Center
                                )
                                Slider(
                                    value = band.level.toFloat(),
                                    onValueChange = { viewModel.setBandLevel(index, it.toInt()) },
                                    valueRange = band.minLevel.toFloat()..band.maxLevel.toFloat(),
                                    enabled = eqState.enabled,
                                    modifier = Modifier.height(138.dp).width(42.dp),
                                    colors = SliderDefaults.colors(
                                        thumbColor = Primary,
                                        activeTrackColor = Primary,
                                        inactiveTrackColor = SurfaceVariant
                                    )
                                )
                                Text(formatFrequency(band.freq), fontSize = 9.sp, color = TextSecondary, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            EffectSlider(
                title = "Bass Boost",
                description = "Adds weight to low frequencies",
                value = eqState.bassBoost,
                range = 0f..1000f,
                enabled = eqState.enabled,
                onChange = { viewModel.setBassBoost(it) }
            )
            EffectSlider(
                title = "Stereo Width",
                description = "Virtual surround effect",
                value = eqState.virtualizerStrength,
                range = 0f..1000f,
                enabled = eqState.enabled,
                onChange = { viewModel.setVirtualizer(it) }
            )
            EffectSlider(
                title = "Loudness",
                description = "Careful: high values may distort",
                value = eqState.loudnessGain,
                range = 0f..5000f,
                enabled = eqState.enabled,
                onChange = { viewModel.setLoudness(it) }
            )
        }
    }
}

@Composable
private fun EffectSlider(
    title: String,
    description: String,
    value: Int,
    range: ClosedFloatingPointRange<Float>,
    enabled: Boolean,
    onChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleSmall, color = TextPrimary)
                    Text(description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Text("${((value - range.start) / (range.endInclusive - range.start) * 100).toInt()}%", color = Primary, style = MaterialTheme.typography.labelLarge)
            }
            Slider(
                value = value.toFloat(),
                onValueChange = { onChange(it.toInt()) },
                valueRange = range,
                enabled = enabled,
                colors = SliderDefaults.colors(
                    thumbColor = Primary,
                    activeTrackColor = Primary,
                    inactiveTrackColor = SurfaceVariant
                )
            )
        }
    }
}

private fun formatFrequency(freq: Int): String {
    return if (freq >= 1000) "${freq / 1000}k" else freq.toString()
}
