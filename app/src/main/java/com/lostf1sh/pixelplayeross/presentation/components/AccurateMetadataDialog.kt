package com.lostf1sh.pixelplayeross.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.lostf1sh.pixelplayeross.R
import com.lostf1sh.pixelplayeross.data.metadata.TrackMatch
import com.lostf1sh.pixelplayeross.data.model.Song
import com.lostf1sh.pixelplayeross.presentation.viewmodel.SongInfoBottomSheetViewModel.AccurateMetadataUiState
import com.lostf1sh.pixelplayeross.ui.theme.Aurora
import com.lostf1sh.pixelplayeross.utils.formatDuration

/**
 * The "accurate metadata" flow: resolves the exact recording for a track
 * (MusicBrainz + Deezer, duration-verified) and shows a confidence-ranked
 * correction the user can apply to the library — or straight into the file
 * tags, artwork included.
 */
@Composable
fun AccurateMetadataDialog(
    song: Song,
    state: AccurateMetadataUiState,
    onApply: (match: TrackMatch, writeToTags: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    when (state) {
        AccurateMetadataUiState.Idle -> Unit
        is AccurateMetadataUiState.Applied -> Unit

        AccurateMetadataUiState.Loading -> Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(34.dp))
                    Spacer(Modifier.height(18.dp))
                    Text(
                        text = stringResource(R.string.accurate_metadata_matching),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        is AccurateMetadataUiState.Error -> Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Rounded.TravelExplore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = stringResource(R.string.accurate_metadata_failed),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(18.dp))
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            }
        }

        is AccurateMetadataUiState.Results -> {
            var selected by remember(song.id) {
                mutableStateOf(state.matches.firstOrNull())
            }
            var showAlternatives by remember { mutableStateOf(false) }
            val match = selected ?: return

            Dialog(onDismissRequest = onDismiss) {
                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.accurate_metadata_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(12.dp))

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 420.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                BestMatchCard(match = match)
                            }

                            item {
                                DiffSection(song = song, match = match)
                            }

                            if (state.matches.size > 1) {
                                item {
                                    TextButton(
                                        onClick = { showAlternatives = !showAlternatives }
                                    ) {
                                        Text(
                                            stringResource(
                                                R.string.accurate_metadata_other_matches,
                                                state.matches.size - 1
                                            )
                                        )
                                    }
                                }
                            }

                            if (showAlternatives) {
                                items(
                                    state.matches.drop(1),
                                    key = { it.source + it.title + it.artist + (it.durationMs ?: 0L) }
                                ) { other ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(MaterialTheme.shapes.medium)
                                            .clickable {
                                                selected = other
                                                showAlternatives = false
                                            },
                                        color = MaterialTheme.colorScheme.surfaceContainer
                                    ) {
                                        Column(Modifier.padding(12.dp)) {
                                            Text(
                                                other.title,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                listOfNotNull(
                                                    other.artist.takeIf { it.isNotBlank() },
                                                    other.album.takeIf { it.isNotBlank() },
                                                    other.year.takeIf { it > 0 }?.toString()
                                                ).joinToString(" · "),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                stringResource(
                                                    R.string.accurate_metadata_confidence,
                                                    (other.confidence * 100).toInt()
                                                ),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (state.canWriteTags) {
                                Button(
                                    onClick = { onApply(match, true) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(stringResource(R.string.accurate_metadata_apply_tags))
                                }
                            }
                            FilledTonalButton(
                                onClick = { onApply(match, false) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.accurate_metadata_apply_library))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BestMatchCard(match: TrackMatch) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmartImage(
                model = match.albumArtUrl,
                contentDescription = null,
                shape = RoundedCornerShape(14.dp),
                targetSize = coil.size.Size(300f, 300f),
                modifier = Modifier.size(76.dp)
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = match.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = match.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = match.album.takeIf { it.isNotBlank() }
                        ?: stringResource(R.string.accurate_metadata_unknown_album),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ConfidenceChip(match)
                    if (match.durationVerified) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Rounded.Schedule,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = match.durationMs?.let { formatDuration(it) } ?: "",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfidenceChip(match: TrackMatch) {
    val highConfidence = match.confidence >= TrackMatch.HIGH_CONFIDENCE_THRESHOLD
    val contentColor = MaterialTheme.colorScheme.primary
    // High-confidence matches carry the signature aurora tint.
    val chipShape = RoundedCornerShape(50)
    val chipModifier = if (highConfidence) {
        Modifier.background(
            brush = Aurora.tint(
                primary = MaterialTheme.colorScheme.primary,
                tertiary = MaterialTheme.colorScheme.tertiary
            ),
            shape = chipShape
        )
    } else {
        Modifier.background(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
            shape = chipShape
        )
    }
    Row(
        modifier = chipModifier.clip(chipShape).padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(13.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = stringResource(
                R.string.accurate_metadata_confidence,
                (match.confidence * 100).toInt()
            ),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}

/** Shows what applying the match would change — only fields that actually differ. */
@Composable
private fun DiffSection(song: Song, match: TrackMatch) {
    val titleLabel = stringResource(R.string.accurate_metadata_field_title)
    val artistLabel = stringResource(R.string.accurate_metadata_field_artist)
    val albumLabel = stringResource(R.string.accurate_metadata_field_album)
    val yearLabel = stringResource(R.string.accurate_metadata_field_year)
    val emptyLabel = stringResource(R.string.accurate_metadata_empty)

    val rows = buildList {
        if (!song.title.equals(match.title, ignoreCase = true)) {
            add(titleLabel to (song.title to match.title))
        }
        if (!song.displayArtist.equals(match.artist, ignoreCase = true)) {
            add(artistLabel to (song.displayArtist to match.artist))
        }
        if (match.album.isNotBlank() && !song.album.equals(match.album, ignoreCase = true)) {
            add(albumLabel to (song.album to match.album))
        }
        if (match.year > 0 && song.year > 0 && song.year != match.year) {
            add(yearLabel to (song.year.toString() to match.year.toString()))
        }
    }

    if (rows.isEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.accurate_metadata_already_accurate),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(R.string.accurate_metadata_changes),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
        rows.forEach { (label, values) ->
            val (current, new) = values
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = current.ifBlank { emptyLabel },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Text(
                            text = "  →  ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = new,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }
            }
        }
    }
}
