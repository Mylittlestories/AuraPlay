package com.auraplay.player.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.auraplay.player.data.model.Track
import com.auraplay.player.data.model.Playlist
import com.auraplay.player.data.model.PlaylistTrack

@Database(
    entities = [Track::class, Playlist::class, PlaylistTrack::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AuraPlayDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun playlistDao(): PlaylistDao
}
