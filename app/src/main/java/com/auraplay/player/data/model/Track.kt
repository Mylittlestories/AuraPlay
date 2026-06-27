package com.auraplay.player.data.model
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class Track(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val albumId: Long,
    val duration: Long,
    val data: String,
    val playCount: Int = 0
)
