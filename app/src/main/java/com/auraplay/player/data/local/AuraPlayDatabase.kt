package com.auraplay.player.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.auraplay.player.data.model.Playlist
import com.auraplay.player.data.model.PlaylistTrack
import com.auraplay.player.data.model.Track

@Database(
    entities = [Track::class, Playlist::class, PlaylistTrack::class],
    version = 1,
    exportSchema = false
)
abstract class AuraPlayDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun playlistDao(): PlaylistDao
}