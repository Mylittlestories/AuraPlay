package com.auraplay.player.playback
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.*
import androidx.media3.exoplayer.ExoPlayer
import com.auraplay.player.audio.AudioEngine
import com.auraplay.player.audio.ShuffleManager
import com.auraplay.player.audio.ShuffleMode
import com.auraplay.player.data.model.RepeatMode
import com.auraplay.player.data.model.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackManager @Inject constructor(
    @ApplicationContext private val ctx: Context,
    val audio: AudioEngine,
    val shuffle: ShuffleManager
) {
    private var player: ExoPlayer? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    val currentTrack = MutableStateFlow<Track?>(null)
    val isPlaying = MutableStateFlow(false)
    val position = MutableStateFlow(0L)
    val duration = MutableStateFlow(0L)
    val repeatMode = MutableStateFlow(RepeatMode.OFF)
    val shuffleOn = MutableStateFlow(false)
    val shuffleMode = MutableStateFlow(ShuffleMode.SMART)
    val queue = MutableStateFlow<List<Track>>(emptyList())
    val curIndex = MutableStateFlow(-1)
    val speed = MutableStateFlow(1f)

    fun init() {
        if (player != null) return
        player = ExoPlayer.Builder(ctx).setAudioAttributes(
            AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).build(), true
        ).setHandleAudioBecomingNoisy(true).build().apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(p: Boolean) { isPlaying.value = p }
                override fun onMediaItemTransition(item: MediaItem?, reason: Int) {
                    val i = player?.currentMediaItemIndex ?: return
                    if (i < queue.value.size && i != curIndex.value) { curIndex.value = i; currentTrack.value = queue.value[i]; duration.value = player?.duration?.takeIf { it > 0 } ?: 0 }
                }
                override fun onPlaybackStateChanged(s: Int) { if (s == Player.STATE_ENDED) onEnd() }
                override fun onPlayerError(e: PlaybackException) { Log.e("Playback", "Error", e); skipNext() }
            })
            audio.init(audioSessionId)
        }
        scope.launch { while (isActive) { delay(200); player?.let { position.value = it.currentPosition; if (it.duration > 0) duration.value = it.duration } } }
    }

    fun play() { player?.play() }
    fun pause() { player?.pause() }
    fun toggle() { if (isPlaying.value) pause() else play() }
    fun seek(ms: Long) { player?.seekTo(ms); position.value = ms }

    fun skipNext() {
        val sz = queue.value.size; if (sz == 0) return
        val ni = if (shuffleOn.value) shuffle.next(curIndex.value, shuffleMode.value) else (curIndex.value + 1) % sz
        goTo(ni)
    }
    fun skipPrev() {
        val sz = queue.value.size; if (sz == 0) return
        if (position.value > 3000) { seek(0); return }
        val ni = if (shuffleOn.value) shuffle.prev(curIndex.value, shuffleMode.value) else if (curIndex.value > 0) curIndex.value - 1 else sz - 1
        goTo(ni)
    }
    fun goTo(i: Int) {
        val q = queue.value; if (i < 0 || i >= q.size) return
        curIndex.value = i; currentTrack.value = q[i]; duration.value = q[i].duration
        player?.let { it.seekToDefaultPosition(i); it.play() }
    }

    fun playAll(tracks: List<Track>, doShuffle: Boolean = false) {
        if (tracks.isEmpty()) return
        val t = if (doShuffle) { shuffle.setQueue(tracks); shuffle.shuffle(tracks) } else tracks
        queue.value = t; curIndex.value = 0; shuffleOn.value = doShuffle
        buildItems(t, 0)
    }
    fun setQueue(tracks: List<Track>, start: Int = 0) {
        queue.value = tracks; curIndex.value = start
        if (shuffleOn.value) shuffle.setQueue(tracks)
        buildItems(tracks, start)
    }
    fun add(t: Track) { queue.value = queue.value + t }
    fun clearQueue() { queue.value = emptyList(); curIndex.value = -1; currentTrack.value = null; player?.clearMediaItems() }
    fun shuffleQueue() {
        val q = queue.value.toMutableList(); if (q.size <= 1) return
        val cur = currentTrack.value; q.shuffle()
        if (cur != null) { val idx = q.indexOfFirst { it.id == cur.id }; if (idx > 0) { q.removeAt(idx); q.add(0, cur) }; curIndex.value = 0 }
        queue.value = q; rebuild()
    }
    fun removeAt(i: Int) { val q = queue.value.toMutableList(); if (i in q.indices) { q.removeAt(i); queue.value = q; rebuild() } }

    fun toggleRepeat() { repeatMode.value = when (repeatMode.value) { RepeatMode.OFF -> RepeatMode.ALL; RepeatMode.ALL -> RepeatMode.ONE; RepeatMode.OFF -> RepeatMode.OFF }
        player?.repeatMode = when (repeatMode.value) { RepeatMode.OFF -> Player.REPEAT_MODE_OFF; RepeatMode.ALL -> Player.REPEAT_MODE_ALL; RepeatMode.ONE -> Player.REPEAT_MODE_ONE } }
    fun toggleShuffle() { shuffleOn.value = !shuffleOn.value; if (shuffleOn.value) shuffle.setQueue(queue.value) }
    fun setShuffleMode(m: ShuffleMode) { shuffleMode.value = m; shuffleOn.value = m != ShuffleMode.OFF; if (shuffleOn.value) shuffle.setQueue(queue.value) }
    fun setSpeed(s: Float) { speed.value = s; player?.setPlaybackSpeed(s) }

    private fun buildItems(tracks: List<Track>, start: Int) {
        player?.let { p ->
            val items = tracks.map { t -> MediaItem.Builder().setUri(Uri.parse("content://media/external/audio/media/${t.id}")).setMediaId(t.id.toString()).setMediaMetadata(MediaMetadata.Builder().setTitle(t.title).setArtist(t.artist).setAlbumTitle(t.album).build()).build() }
            p.setMediaItems(items, start, 0); p.prepare(); p.play(); currentTrack.value = tracks[start]
        }
    }
    private fun rebuild() { val p = player ?: return; val q = queue.value; val items = q.map { t -> MediaItem.Builder().setUri(Uri.parse("content://media/external/audio/media/${t.id}")).setMediaId(t.id.toString()).setMediaMetadata(MediaMetadata.Builder().setTitle(t.title).setArtist(t.artist).build()).build() }; p.setMediaItems(items, curIndex.value.coerceAtLeast(0), p.currentPosition); p.prepare() }
    private fun onEnd() { when (repeatMode.value) { RepeatMode.ONE -> { seek(0); play() }; RepeatMode.ALL -> skipNext(); RepeatMode.OFF -> if (curIndex.value < queue.value.size - 1 || shuffleOn.value) skipNext() else isPlaying.value = false } }
    fun getPlayer() = player
    fun sessionId() = player?.audioSessionId ?: 0
    fun release() { audio.release(); player?.release(); player = null; scope.cancel() }
}
