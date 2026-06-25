package com.auraplay.player.data.model
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class Track(
    @PrimaryKey val id: Long = 0,
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val albumArtist: String = "",
    val duration: Long = 0,
    val filePath: String = "",
    val fileName: String = "",
    val fileSize: Long = 0,
    val mimeType: String = "",
    val bitrate: Int = 0,
    val sampleRate: Int = 0,
    val year: Int = 0,
    val trackNumber: Int = 0,
    val genre: String = "",
    val dateAdded: Long = 0,
    val albumArtUri: String? = null,
    val playCount: Int = 0,
    val lastPlayed: Long = 0,
    val isFavorite: Boolean = false,
    val folderName: String = ""
) {
    val formattedDuration get() = "%d:%02d".format(duration / 60000, (duration / 1000) % 60)
    val formattedBitrate get() = if (bitrate > 0) "${bitrate / 1000} kbps" else "?"
    val formattedSampleRate get() = if (sampleRate > 0) "${sampleRate} Hz" else "?"
    val formatName get() = mimeType.substringAfter("/").uppercase().ifEmpty { "?" }
}

enum class RepeatMode { OFF, ALL, ONE }

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val trackCount: Int = 0,
    val dateCreated: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlist_tracks", primaryKeys = ["playlistId", "trackId"])
data class PlaylistTrack(
    val playlistId: Long,
    val trackId: Long,
    val position: Int
)
