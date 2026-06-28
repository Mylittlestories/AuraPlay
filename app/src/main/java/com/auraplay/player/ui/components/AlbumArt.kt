package com.auraplay.player.ui.components

import android.content.ContentUris
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.auraplay.player.data.model.Track
import com.auraplay.player.data.repository.MusicRepository
import com.auraplay.player.ui.theme.Primary
import com.auraplay.player.ui.theme.PrimaryContainer
import com.auraplay.player.ui.theme.SurfaceHigh
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AlbumArt(
    track: Track?,
    modifier: Modifier = Modifier,
    showMusicNote: Boolean = false,
    iconTint: Color = Primary.copy(alpha = 0.56f)
) {
    val context = LocalContext.current
    var image by remember(track?.id, track?.albumId, track?.data) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }

    LaunchedEffect(track?.id, track?.albumId, track?.data) {
        image = null
        if (track == null) return@LaunchedEffect
        image = withContext(Dispatchers.IO) {
            loadAlbumArt(context.contentResolver, track)?.asImageBitmap()
        }
    }

    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                listOf(PrimaryContainer, SurfaceHigh)
            )
        ),
        contentAlignment = Alignment.Center
    ) {
        val art = image
        if (art != null) {
            Image(
                bitmap = art,
                contentDescription = "Album art",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                if (showMusicNote) Icons.Default.MusicNote else Icons.Default.Album,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

private fun loadAlbumArt(contentResolver: android.content.ContentResolver, track: Track): android.graphics.Bitmap? {
    if (track.albumId > 0) {
        runCatching {
            contentResolver.openInputStream(MusicRepository.getAlbumArtUri(track.albumId))?.use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        }.getOrNull()?.let { return it }
    }

    return runCatching {
        val retriever = MediaMetadataRetriever()
        try {
            val audioUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, track.id)
            val descriptor = contentResolver.openFileDescriptor(audioUri, "r")
            if (descriptor != null) {
                descriptor.use { retriever.setDataSource(it.fileDescriptor) }
            } else {
                retriever.setDataSource(track.data)
            }
            val bytes = retriever.embeddedPicture ?: return@runCatching null
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } finally {
            retriever.release()
        }
    }.getOrNull()
}
