package com.auraplay.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.auraplay.player.audio.TrackMetadata

@Composable
fun MetadataDialog(
    metadata: TrackMetadata?,
    albumArtUri: String?,
    onDismiss: () -> Unit,
    onDownloadArt: () -> Unit,
    onDownloadLyrics: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Track Info",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }

                Divider()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Album Art
                    if (albumArtUri != null || metadata?.albumArt != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            if (metadata?.albumArt != null) {
                                androidx.compose.foundation.Image(
                                    bitmap = androidx.compose.ui.graphics.asImageBitmap(metadata.albumArt),
                                    contentDescription = "Album Art",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            } else if (albumArtUri != null) {
                                AsyncImage(
                                    model = albumArtUri,
                                    contentDescription = "Album Art",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Download buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDownloadArt,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Image, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Get Artwork")
                        }
                        OutlinedButton(
                            onClick = onDownloadLyrics,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.MusicNote, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Get Lyrics")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Metadata
                    if (metadata != null) {
                        Text(
                            "Basic Info",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        MetadataCard {
                            MetadataRow("Title", metadata.title ?: "Unknown")
                            MetadataRow("Artist", metadata.artist ?: "Unknown")
                            MetadataRow("Album", metadata.album ?: "Unknown")
                            MetadataRow("Album Artist", metadata.albumArtist ?: "Unknown")
                            if (!metadata.genre.isNullOrBlank()) MetadataRow("Genre", metadata.genre)
                            if (!metadata.year.isNullOrBlank()) MetadataRow("Year", metadata.year)
                            if (!metadata.trackNumber.isNullOrBlank()) MetadataRow("Track", metadata.trackNumber)
                            if (!metadata.discNumber.isNullOrBlank()) MetadataRow("Disc", metadata.discNumber)
                            if (!metadata.composer.isNullOrBlank()) MetadataRow("Composer", metadata.composer)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            "Audio Quality",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        MetadataCard {
                            MetadataRow("Format", metadata.formatName)
                            MetadataRow("Bitrate", metadata.formattedBitrate)
                            MetadataRow("Sample Rate", metadata.formattedSampleRate)
                            MetadataRow("Duration", metadata.formattedDuration)
                            MetadataRow("File Size", metadata.formattedFileSize)
                            MetadataRow("File Name", metadata.fileName)
                            MetadataRow("MIME Type", metadata.mimeType ?: "Unknown")
                            MetadataRow("Embedded Art", if (metadata.hasEmbeddedArt) "Yes ✓" else "No")
                        }

                        // Quality badge
                        Spacer(modifier = Modifier.height(16.dp))
                        val quality = when {
                            metadata.formatName == "FLAC" -> "🟢 Lossless"
                            metadata.bitrate != null && metadata.bitrate >= 320000 -> "🟡 High Quality"
                            metadata.bitrate != null && metadata.bitrate >= 192000 -> "🟠 Good Quality"
                            else -> "🔴 Standard"
                        }
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                quality,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Loading metadata...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetadataCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), content = content)
    }
}

@Composable
private fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.6f)
        )
    }
}