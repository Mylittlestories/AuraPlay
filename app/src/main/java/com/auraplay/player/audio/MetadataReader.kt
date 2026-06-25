package com.auraplay.player.audio

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetadataReader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Extract full metadata from a track file
     */
    fun readMetadata(filePath: String): TrackMetadata {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(filePath)

            return TrackMetadata(
                title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
                album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM),
                albumArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST),
                genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE),
                year = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR),
                trackNumber = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER),
                discNumber = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER),
                composer = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER),
                writer = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_WRITER),
                duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull(),
                bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull(),
                sampleRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)?.toIntOrNull(),
                mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE),
                hasEmbeddedArt = retriever.embeddedPicture != null,
                albumArt = retriever.embeddedPicture?.let { BitmapFactory.decodeByteArray(it, 0, it.size) },
                lyrics = extractLyrics(retriever),
                fileSize = File(filePath).length(),
                fileName = File(filePath).name
            )
        } catch (e: Exception) {
            return TrackMetadata(fileName = File(filePath).name, fileSize = File(filePath).length())
        } finally {
            try { retriever.release() } catch (_: Exception) {}
        }
    }

    /**
     * Extract album art bitmap
     */
    fun extractAlbumArt(filePath: String): Bitmap? {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(filePath)
            val art = retriever.embeddedPicture ?: return null
            return BitmapFactory.decodeByteArray(art, 0, art.size)
        } catch (_: Exception) {
            return null
        } finally {
            try { retriever.release() } catch (_: Exception) {}
        }
    }

    /**
     * Extract album art as bytes (for saving)
     */
    fun extractAlbumArtBytes(filePath: String): ByteArray? {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(filePath)
            return retriever.embeddedPicture
        } catch (_: Exception) {
            return null
        } finally {
            try { retriever.release() } catch (_: Exception) {}
        }
    }

    private fun extractLyrics(retriever: MediaMetadataRetriever): String? {
        // Try to get lyrics from metadata (some formats support this)
        return try {
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_WRITER)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Check if an LRC file exists for a track
     */
    fun findLrcFile(trackPath: String): File? {
        val trackFile = File(trackPath)
        val lrcFile = File(trackFile.parent, trackFile.nameWithoutExtension + ".lrc")
        return if (lrcFile.exists()) lrcFile else null
    }

    /**
     * Parse LRC file for synced lyrics
     */
    fun parseLrcFile(lrcFile: File): List<LyricLine> {
        val lines = mutableListOf<LyricLine>()
        try {
            lrcFile.readLines().forEach { line ->
                val match = Regex("\\[(\\d{2}):(\\d{2})\\.?(\\d{0,3})\\](.*)").find(line)
                if (match != null) {
                    val min = match.groupValues[1].toIntOrNull() ?: 0
                    val sec = match.groupValues[2].toIntOrNull() ?: 0
                    val ms = (match.groupValues[3].toIntOrNull() ?: 0) * (if (match.groupValues[3].length == 2) 10 else 1)
                    val text = match.groupValues[4].trim()
                    if (text.isNotEmpty()) {
                        lines.add(LyricLine(timeMs = (min * 60 + sec) * 1000L + ms, text = text))
                    }
                }
            }
        } catch (_: Exception) {}
        return lines.sortedBy { it.timeMs }
    }
}

data class TrackMetadata(
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val albumArtist: String? = null,
    val genre: String? = null,
    val year: String? = null,
    val trackNumber: String? = null,
    val discNumber: String? = null,
    val composer: String? = null,
    val writer: String? = null,
    val duration: Long? = null,
    val bitrate: Int? = null,
    val sampleRate: Int? = null,
    val mimeType: String? = null,
    val hasEmbeddedArt: Boolean = false,
    val albumArt: Bitmap? = null,
    val lyrics: String? = null,
    val fileSize: Long = 0,
    val fileName: String = ""
) {
    val formattedBitrate: String
        get() = if (bitrate != null) "${bitrate / 1000} kbps" else "Unknown"

    val formattedSampleRate: String
        get() = if (sampleRate != null) "${sampleRate / 1000} kHz" else "Unknown"

    val formattedDuration: String
        get() {
            val d = duration ?: return "Unknown"
            val totalSeconds = d / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }

    val formattedFileSize: String
        get() {
            val kb = fileSize / 1024.0
            val mb = kb / 1024.0
            return if (mb >= 1) "%.1f MB".format(mb) else "%.0f KB".format(kb)
        }

    val formatName: String
        get() = when {
            mimeType?.contains("flac", true) == true -> "FLAC"
            mimeType?.contains("mp3", true) == true -> "MP3"
            mimeType?.contains("aac", true) == true -> "AAC"
            mimeType?.contains("ogg", true) == true -> "OGG"
            mimeType?.contains("wav", true) == true -> "WAV"
            mimeType?.contains("opus", true) == true -> "OPUS"
            mimeType?.contains("m4a", true) == true -> "M4A"
            else -> mimeType?.substringAfterLast("/")?.uppercase() ?: "Unknown"
        }
}

data class LyricLine(
    val timeMs: Long,
    val text: String
)