package com.auraplay.player.di
import android.content.Context
import androidx.room.Room
import com.auraplay.player.data.local.AppDb
import com.auraplay.player.data.local.PlaylistDao
import com.auraplay.player.data.local.TrackDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module @InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun db(@ApplicationContext c: Context) = Room.databaseBuilder(c, AppDb::class.java, "auraplay").fallbackToDestructiveMigration().build()
    @Provides @Singleton fun trackDao(db: AppDb): TrackDao = db.trackDao()
    @Provides @Singleton fun playlistDao(db: AppDb): PlaylistDao = db.playlistDao()
}
