package com.auraplay.player.data.repository
import com.auraplay.player.data.local.TrackDao
import javax.inject.Inject

class MusicRepository @Inject constructor(
    private val trackDao: TrackDao
) {
    fun getAllTracks() = trackDao.getAllTracks()
}
