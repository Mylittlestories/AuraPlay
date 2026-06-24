package com.auraplay.player.data.model

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

data class Playlist(
    val id: Long = 0,
    val name: String,
    val trackCount: Int = 0,
    val dateCreated: Long = System.currentTimeMillis(),
    val dateModified: Long = System.currentTimeMillis()
)

data class PlaylistTrack(
    val playlistId: Long,
    val trackId: Long,
    val position: Int,
    val dateAdded: Long = System.currentTimeMillis()
)