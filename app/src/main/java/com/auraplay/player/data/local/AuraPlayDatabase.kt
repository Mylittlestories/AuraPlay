package com.auraplay.player.data.local
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.auraplay.player.data.model.Track

@Database(entities = [Track::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AuraPlayDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun playlistDao(): PlaylistDao
}
