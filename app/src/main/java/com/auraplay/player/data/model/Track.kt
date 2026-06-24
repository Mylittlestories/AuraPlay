package com.auraplay.player.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class Track(
    @PrimaryKey
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumArtist: String = "",
    val duration: Long,
    val filePath: String,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String = "audio/mpeg",
    val bitrate: Int = 0,
    val sampleRate: Int = 0,
    val channels: Int = 0,
    val year: Int = 0,
    val trackNumber: Int = 0,
    val discNumber: Int = 0,
    val genre: String = "",
    val dateAdded: Long = System.currentTimeMillis(),
    val dateModified: Long = System.currentTimeMillis(),
    val albumArtUri: String? = null,
    val playCount: Int = 0,
    val lastPlayed: Long = 0,
    val isFavorite: Boolean = false,
    val folderName: String = ""
) {
    val formattedDuration: String
        get() {
            val totalSeconds = duration / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }

    val formattedBitrate: String
        get() = if (bitrate > 0) "${bitrate / 1000} kbps" else "Unknown"

    val formattedSampleRate: String
        get() = if (sampleRate > 0) "${sampleRate / 1000} kHz" else "Unknown"
}