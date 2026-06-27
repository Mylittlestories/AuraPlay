package com.auraplay.player.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class Track(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val genre: String,
    val duration: Long,
    val data: String,           // file path
    val folder: String,         // folder path
    val year: Int,
    val trackNumber: Int,
    val dateAdded: Long,
    val playCount: Int = 0,
    val isFavorite: Boolean = false
)
