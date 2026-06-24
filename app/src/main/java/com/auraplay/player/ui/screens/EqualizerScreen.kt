package com.auraplay.player.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.auraplay.player.audio.AudioPreset
import com.auraplay.player.ui.theme.*
import com.auraplay.player.ui.viewmodel.MainViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val eqSettings by viewModel.equalizerSettings.collectAsStateWithLifecycle()
    val bassSettings by viewModel.bassBoostSettings.collectAsStateWithLifecycle()
    val virtualizerSettings by viewModel.virtualizerSettings.collectAsStateWithLifecycle()
    val loudnessSettings by viewModel.loudnessSettings.collectAsStateWithLifecycle()

    val presets = listOf(
        "Flat" to AudioPreset.FLAT,
        "Bass Boost" to AudioPreset.BASS_BOOST,
        "Vocal" to AudioPreset.VOCAL,
        "Rock" to AudioPreset.ROCK,
        "Pop" to AudioPreset.POP,
        "Jazz" to AudioPreset.JAZZ,
        "Classical" to AudioPreset.CLASSICAL,
        "Electronic" to AudioPreset.ELECTRONIC,
        "Hip-Hop" to AudioPreset.HIPHOP,
        "Acoustic" to AudioPreset.ACOUSTIC,
        "Bass & Treble" to AudioPreset.BASS_AND_TREBLE
    )

    val bandFrequencies = listOf("60", "170", "310", "600", "1K", "3K", "6K", "12K", "14K", "16K")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PlayerBackground, DarkBackground)
                )
            )
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar
        TopAppBar(
            title = { Text("Equalizer & Sound", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )

        // Presets
        Text(
            "Presets",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(presets) { (name, preset) ->
                PresetChip(
                    name = name,
                    isSelected = false,
                    onClick = { viewModel.applyAudioPreset(preset) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Equalizer Bands
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = PlayerSurface
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Equalizer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Switch(
                        checked = eqSettings.isEnabled,
                        onCheckedChange = { viewModel.setEqualizerEnabled(it) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // EQ Visualization
                AnimatedVisibility(visible = eqSettings.isEnabled) {
                    Column {
                        // Band Sliders
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            val bandLevels = eqSettings.bandLevels
                            val bandCount = bandLevels.size.coerceAtMost(bandFrequencies.size)

                            for (i in 0 until bandCount) {
                                val normalizedLevel = if (bandLevels.isNotEmpty() && i < bandLevels.size) {
                                    ((bandLevels[i].toFloat() + 1500) / 3000).coerceIn(0f, 1f)
                                } else 0.5f

                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .width(24.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(PlayerProgressBackground),
                                        contentAlignment = Alignment.Bottom
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .fillMaxHeight(normalizedLevel)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    Brush.verticalGradient(
                                                        colors = listOf(
                                                            EqBandColors.getOrElse(i) { AccentPurple },
                                                            EqBandColors.getOrElse(i) { AccentPurple }.copy(alpha = 0.5f)
                                                        )
                                                    )
                                                )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        bandFrequencies.getOrElse(i) { "" },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 8.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Individual Band Sliders
                        val bandLevels = eqSettings.bandLevels
                        val bandCount = bandLevels.size.coerceAtMost(bandFrequencies.size)

                        for (i in 0 until bandCount) {
                            val level = if (i < bandLevels.size) bandLevels[i].toFloat() else 0f
                            val normalizedLevel = ((level + 1500) / 3000).coerceIn(0f, 1f)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    bandFrequencies[i],
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.width(32.dp)
                                )
                                Slider(
                                    value = normalizedLevel,
                                    onValueChange = { newValue ->
                                        val bandLevel = ((newValue * 3000) - 1500).roundToInt().toShort()
                                        viewModel.setEqualizerBand(i.toShort(), bandLevel)
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = SliderDefaults.colors(
                                        thumbColor = EqBandColors.getOrElse(i) { AccentPurple },
                                        activeTrackColor = EqBandColors.getOrElse(i) { AccentPurple },
                                        inactiveTrackColor = PlayerProgressBackground
                                    )
                                )
                                Text(
                                    "${level.roundToInt()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.width(40.dp),
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bass Boost
        AudioEffectCard(
            title = "Bass Boost",
            icon = Icons.Default.MusicNote,
            isEnabled = bassSettings.isEnabled,
            onToggle = { viewModel.setBassBoostEnabled(it) },
            value = bassSettings.strength.toFloat(),
            maxValue = 1000f,
            onValueChange = { viewModel.setBassBoostStrength(it.roundToInt().toShort()) },
            valueLabel = "${(bassSettings.strength / 10).toInt()}%"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Virtualizer
        AudioEffectCard(
            title = "Virtualizer",
            icon = Icons.Default.Headphones,
            isEnabled = virtualizerSettings.isEnabled,
            onToggle = { viewModel.setVirtualizerEnabled(it) },
            value = virtualizerSettings.strength.toFloat(),
            maxValue = 1000f,
            onValueChange = { viewModel.setVirtualizerStrength(it.roundToInt().toShort()) },
            valueLabel = "${(virtualizerSettings.strength / 10).toInt()}%"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Loudness Enhancer
        AudioEffectCard(
            title = "Loudness Enhancer",
            icon = Icons.Default.VolumeUp,
            isEnabled = loudnessSettings.isEnabled,
            onToggle = { viewModel.setLoudnessEnabled(it) },
            value = loudnessSettings.gainMb.toFloat(),
            maxValue = 3000f,
            onValueChange = { viewModel.setLoudnessGain(it.roundToInt()) },
            valueLabel = "${(loudnessSettings.gainMb / 100).toInt()} dB"
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun PresetChip(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        color = if (isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) Color.White
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun AudioEffectCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    value: Float,
    maxValue: Float,
    onValueChange: (Float) -> Unit,
    valueLabel: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = PlayerSurface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle
                )
            }

            AnimatedVisibility(visible = isEnabled) {
                Column {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Strength",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Text(
                            valueLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = value,
                        onValueChange = onValueChange,
                        valueRange = 0f..maxValue,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = PlayerProgressBackground
                        )
                    )
                }
            }
        }
    }
}