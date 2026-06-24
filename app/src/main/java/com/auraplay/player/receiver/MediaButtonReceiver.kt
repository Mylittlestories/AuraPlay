package com.auraplay.player.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import com.auraplay.player.playback.PlaybackManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MediaButtonReceiver : BroadcastReceiver() {

    @Inject
    lateinit var playbackManager: PlaybackManager

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_MEDIA_BUTTON) {
            val event = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT) ?: return

            if (event.action == KeyEvent.ACTION_DOWN) {
                when (event.keyCode) {
                    KeyEvent.KEYCODE_MEDIA_PLAY -> playbackManager.play()
                    KeyEvent.KEYCODE_MEDIA_PAUSE -> playbackManager.pause()
                    KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> playbackManager.togglePlayPause()
                    KeyEvent.KEYCODE_MEDIA_NEXT -> playbackManager.skipToNext()
                    KeyEvent.KEYCODE_MEDIA_PREVIOUS -> playbackManager.skipToPrevious()
                    KeyEvent.KEYCODE_MEDIA_STOP -> playbackManager.pause()
                }
            }
        }
    }
}