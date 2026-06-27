package com.auraplay.player.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.KeyEvent

class MediaButtonReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_MEDIA_BUTTON) {
            val keyEvent = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT) as? KeyEvent ?: return
            if (keyEvent.action != KeyEvent.ACTION_DOWN) return

            when (keyEvent.keyCode) {
                KeyEvent.KEYCODE_MEDIA_PLAY,
                KeyEvent.KEYCODE_MEDIA_PAUSE,
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                    // Handled by Media3 session
                }
                KeyEvent.KEYCODE_MEDIA_NEXT -> {
                    // Handled by Media3 session
                }
                KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                    // Handled by Media3 session
                }
            }
        }
    }
}
