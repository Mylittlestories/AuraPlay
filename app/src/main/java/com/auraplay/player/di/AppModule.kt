package com.auraplay.player.di

import android.content.Context
import androidx.room.Room
import com.auraplay.player.data.local.AuraPlayDatabase
import com.auraplay.player.data.local.PlaylistDao
import com.auraplay.player.data.local.TrackDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AuraPlayDatabase {
        return Room.databaseBuilder(
            context,
            AuraPlayDatabase::class.java,
            "auraplay_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideTrackDao(database: AuraPlayDatabase): TrackDao {
        return database.trackDao()
    }

    @Provides
    @Singleton
    fun providePlaylistDao(database: AuraPlayDatabase): PlaylistDao {
        return database.playlistDao()
    }
}