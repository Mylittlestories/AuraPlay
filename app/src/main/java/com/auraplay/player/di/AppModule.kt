package com.auraplay.player.di

import android.content.ContentResolver
import android.content.Context
import androidx.room.Room
import com.auraplay.player.data.local.AuraPlayDatabase
import com.auraplay.player.data.local.TrackDao
import com.auraplay.player.data.local.PlaylistDao
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
            "auraplay_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideTrackDao(db: AuraPlayDatabase): TrackDao = db.trackDao()

    @Provides
    fun providePlaylistDao(db: AuraPlayDatabase): PlaylistDao = db.playlistDao()

    @Provides
    fun provideContentResolver(@ApplicationContext context: Context): ContentResolver = context.contentResolver
}
