package com.auraplay.player.service
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaSession

class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession
}
