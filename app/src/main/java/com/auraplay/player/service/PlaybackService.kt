package com.auraplay.player.service
import android.app.*
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.auraplay.player.MainActivity
import com.auraplay.player.playback.PlaybackManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {
    @Inject lateinit var pm: PlaybackManager
    private var session: MediaSession? = null
    override fun onCreate() {
        super.onCreate()
        val ch = NotificationChannel("play", "Playback", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        val p = pm.getPlayer() ?: return
        val pi = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        session = MediaSession.Builder(this, p).setSessionActivity(pi).build()
        startForeground(1, NotificationCompat.Builder(this, "play").setContentTitle("AuraPlay").setSmallIcon(android.R.drawable.ic_media_play).setContentIntent(pi).setOngoing(true).build())
    }
    override fun onGetSession(ci: androidx.media3.session.MediaSession.ControllerInfo) = session
    override fun onTaskRemoved(root: Intent?) { val p = session?.player; if (p == null || !p.playWhenReady || p.mediaItemCount == 0) stopSelf() }
    override fun onDestroy() { session?.let { it.player.release(); it.release() }; super.onDestroy() }
}
