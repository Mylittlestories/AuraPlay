package com.auraplay.player.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val year: Int = 0,
    val trackCount: Int = 0,
    val artUri: String? = null
)

data class Artist(
    val name: String,
    val albumCount: Int = 0,
    val trackCount: Int = 0,
    val artUri: String? = null
)

data class Folder(
    val path: String,
    val name: String,
    val trackCount: Int = 0,
    val totalSize: Long = 0
)

data class Genre(
    val name: String,
    val trackCount: Int = 0
)

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val trackCount: Int = 0,
    val dateCreated: Long = System.currentTimeMillis(),
    val dateModified: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "playlist_tracks",
    primaryKeys = ["playlistId", "trackId"]
)
data class PlaylistTrack(
    val playlistId: Long,
    val trackId: Long,
    val position: Int,
    val dateAdded: Long = System.currentTimeMillis()
)