package com.lostf1sh.pixelplayeross.presentation.screens

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Speaker
import androidx.compose.material.icons.rounded.Usb
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lostf1sh.pixelplayeross.R
import com.lostf1sh.pixelplayeross.data.model.AudioOutputMode
import com.lostf1sh.pixelplayeross.presentation.navigation.Screen
import com.lostf1sh.pixelplayeross.presentation.navigation.navigateSafely
import com.lostf1sh.pixelplayeross.presentation.viewmodel.PlayerViewModel
import com.lostf1sh.pixelplayeross.presentation.viewmodel.SettingsViewModel

private data class UsbDacInfo(val name: String, val sampleRates: List<Int>)

private fun detectUsbDac(audioManager: AudioManager): UsbDacInfo? =
    audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        .firstOrNull { device ->
            device.type == AudioDeviceInfo.TYPE_USB_DEVICE ||
                device.type == AudioDeviceInfo.TYPE_USB_HEADSET ||
                device.type == AudioDeviceInfo.TYPE_USB_ACCESSORY
        }
        ?.let { device ->
            UsbDacInfo(
                name = device.productName?.toString().orEmpty().ifBlank { "USB audio device" },
                sampleRates = device.sampleRates
                    .toList()
                    .filter { it >= 44_100 }
                    .distinct()
                    .sorted()
            )
        }

/**
 * AuraPlay Sound Engine — the audiophile control room.
 *
 * Shows the live output path (external USB DAC vs internal), the active signal chain,
 * and lets the listener switch output modes, DAC routing, and jump into the equalizer.
 */
@Composable
fun SoundEngineScreen(
    navController: androidx.navigation.NavController,
    @Suppress("UNUSED_PARAMETER") playerViewModel: PlayerViewModel,
) {
    val context = LocalContext.current
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val audioManager = remember {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    val dac = remember { detectUsbDac(audioManager) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = stringResource(R.string.sound_engine_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            text = stringResource(R.string.sound_engine_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // ---------------- Output device ----------------
        EngineCard(
            icon = if (dac != null) Icons.Rounded.Usb else Icons.Rounded.Speaker,
            title = stringResource(R.string.sound_engine_output_device)
        ) {
            if (dac != null) {
                Text(dac.name, style = MaterialTheme.typography.titleMedium)
                if (dac.sampleRates.isNotEmpty()) {
                    Text(
                        text = stringResource(
                            R.string.sound_engine_dac_rates,
                            dac.sampleRates.joinToString(" · ") { (it / 1000).toString() + " kHz" }
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(6.dp))
                StatusBadge(
                    text = if (uiState.preferUsbDacEnabled) {
                        stringResource(R.string.sound_engine_dac_active)
                    } else {
                        stringResource(R.string.sound_engine_dac_standby)
                    },
                    active = uiState.preferUsbDacEnabled
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.Headphones,
                        null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.sound_engine_no_dac),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    text = stringResource(R.string.sound_engine_no_dac_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // ---------------- Signal path ----------------
        EngineCard(
            icon = Icons.Rounded.Memory,
            title = stringResource(R.string.sound_engine_signal_path)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                PathChip(stringResource(R.string.sound_engine_stage_source))
                Arrow()
                PathChip(stringResource(R.string.sound_engine_stage_decoder))
                Arrow()
                PathChip(
                    text = if (uiState.audioOutputMode.usesFloatOutput) {
                        stringResource(R.string.sound_engine_stage_float)
                    } else {
                        stringResource(R.string.sound_engine_stage_float_off)
                    },
                    highlight = uiState.audioOutputMode.usesFloatOutput
                )
                Arrow()
                PathChip(
                    text = if (dac != null && uiState.preferUsbDacEnabled) {
                        stringResource(R.string.sound_engine_stage_dac)
                    } else {
                        stringResource(R.string.sound_engine_stage_speaker)
                    },
                    highlight = dac != null && uiState.preferUsbDacEnabled
                )
            }
        }

        // ---------------- Output mode ----------------
        EngineCard(
            icon = Icons.Rounded.GraphicEq,
            title = stringResource(R.string.sound_engine_mode_title)
        ) {
            val modes = listOf(
                Triple(
                    AudioOutputMode.SYSTEM_DEFAULT,
                    stringResource(R.string.setcat_audio_output_mode_system_default),
                    stringResource(R.string.setcat_audio_output_mode_system_default_description)
                ),
                Triple(
                    AudioOutputMode.DIRECT,
                    stringResource(R.string.setcat_audio_output_mode_direct),
                    stringResource(R.string.setcat_audio_output_mode_direct_description)
                ),
                Triple(
                    AudioOutputMode.PCM_FLOAT,
                    stringResource(R.string.setcat_audio_output_mode_pcm_float),
                    if (uiState.pcmFloatOutputSupported) {
                        stringResource(R.string.setcat_audio_output_mode_pcm_float_description)
                    } else {
                        stringResource(R.string.setcat_audio_output_mode_pcm_float_unsupported)
                    }
                )
            )
            modes.forEach { (mode, label, description) ->
                val enabled = mode != AudioOutputMode.PCM_FLOAT || uiState.pcmFloatOutputSupported
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .clickable(enabled = enabled) {
                            settingsViewModel.setAudioOutputMode(mode)
                        }
                ) {
                    RadioButton(
                        selected = uiState.audioOutputMode == mode,
                        onClick = if (enabled) {
                            { settingsViewModel.setAudioOutputMode(mode) }
                        } else null
                    )
                    Column {
                        Text(label, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ---------------- DAC routing switch ----------------
        EngineCard(
            icon = Icons.Rounded.Usb,
            title = stringResource(R.string.setcat_prefer_usb_dac_title)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.setcat_prefer_usb_dac_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = uiState.preferUsbDacEnabled,
                    onCheckedChange = { settingsViewModel.setPreferUsbDacEnabled(it) }
                )
            }
        }

        // ---------------- Equalizer shortcut ----------------
        FilledTonalButton(
            onClick = { navController.navigateSafely(Screen.Equalizer.createRoute()) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Rounded.GraphicEq, null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.sound_engine_open_eq))
        }

        Text(
            text = stringResource(R.string.drvsoft_footer),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun EngineCard(icon: ImageVector, title: String, content: @Composable () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun PathChip(text: String, highlight: Boolean = false) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = if (highlight) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        }
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = if (highlight) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun Arrow() {
    Icon(
        Icons.AutoMirrored.Filled.ArrowForward,
        null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .size(14.dp)
            .padding(horizontal = 1.dp)
    )
}

@Composable
private fun StatusBadge(text: String, active: Boolean) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = if (active) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        }
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = if (active) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}
