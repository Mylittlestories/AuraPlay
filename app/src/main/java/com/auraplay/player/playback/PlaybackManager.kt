package com.auraplay.player.playback

import android.content.Context
import androidx.media3.common.*
import androidx.media3.exoplayer.ExoPlayer
import com.auraplay.player.audio.AudioEngine
import com.auraplay.player.data.model.RepeatMode
import com.auraplay.player.data.model.Track
import com.auraplay.player.data.repository.MusicRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioEngine: AudioEngine
) {
    private var player: ExoPlayer? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _progress = MutableStateFlow(0L)
    val progress: StateFlow<Long> = _progress.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _shuffleEnabled = MutableStateFlow(false)
    val shuffleEnabled: StateFlow<Boolean> = _shuffleEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _queueIndex = MutableStateFlow(-1)
    val queueIndex: StateFlow<Int> = _queueIndex.asStateFlow()

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                advanceQueue()
            }
            val p = player ?: return
            _duration.value = if (p.duration > 0) p.duration else 0L
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _shuffleEnabled.value = shuffleModeEnabled
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            _repeatMode.value = when (repeatMode) {
                Player.REPEAT_MODE_OFF -> RepeatMode.OFF
                Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                else -> RepeatMode.OFF
            }
        }

        override fun onAudioSessionIdChanged(audioSessionId: Int) {
            audioEngine.setupSession(audioSessionId)
        }
    }

    private fun ensurePlayer(): ExoPlayer {
        if (player == null) {
            player = ExoPlayer.Builder(context).build().also {
                it.addListener(listener)
                it.repeatMode = Player.REPEAT_MODE_OFF
                audioEngine.setupPlayer(it)
            }
            // Restart progress updates
            scope.launch {
                while (isActive) {
                    player?.let { p ->
                        if (p.isPlaying) {
                            _progress.value = p.currentPosition
                            if (p.duration > 0) _duration.value = p.duration
                        }
                    }
                    delay(500)
                }
            }
        }
        return player!!
    }

    fun playTrack(track: Track) {
        _currentTrack.value = track
        val p = ensurePlayer()
        val mediaItem = MediaItem.Builder()
            .setUri(track.data)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(track.title)
                    .setArtist(track.artist)
                    .setAlbumTitle(track.album)
                    .setArtworkUri(if (track.albumId > 0) MusicRepository.getAlbumArtUri(track.albumId) else null)
                    .build()
            )
            .build()
        p.setMediaItem(mediaItem)
        p.prepare()
        p.playWhenReady = true
        _duration.value = track.duration
    }

    fun playQueue(tracks: List<Track>, startIndex: Int = 0) {
        _queue.value = tracks
        _queueIndex.value = startIndex
        if (tracks.isNotEmpty() && startIndex in tracks.indices) {
            playTrack(tracks[startIndex])
        }
    }

    fun addToQueue(track: Track) {
        _queue.value = _queue.value + track
    }

    fun removeFromQueue(index: Int) {
        val currentQueue = _queue.value.toMutableList()
        if (index in currentQueue.indices) {
            currentQueue.removeAt(index)
            _queue.value = currentQueue
            if (index < _queueIndex.value) {
                _queueIndex.value -= 1
            }
        }
    }

    fun togglePlayPause() {
        val p = player ?: return
        if (p.isPlaying) p.pause() else p.play()
    }

    fun seekTo(position: Long) {
        player?.seekTo(position)
        _progress.value = position
    }

    fun seekToFraction(fraction: Float) {
        val p = player ?: return
        val dur = if (p.duration > 0) p.duration else 1L
        seekTo((fraction.coerceIn(0f, 1f) * dur).toLong())
    }

    fun skipNext() {
        advanceQueue()
    }

    fun skipPrevious() {
        val p = player ?: return
        if (p.currentPosition > 3000) {
            p.seekTo(0)
        } else {
            val idx = _queueIndex.value - 1
            if (idx >= 0 && idx < _queue.value.size) {
                _queueIndex.value = idx
                playTrack(_queue.value[idx])
            }
        }
    }

    private fun advanceQueue() {
        val q = _queue.value
        val idx = _queueIndex.value
        if (q.isEmpty()) return

        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                player?.seekTo(0)
                player?.play()
            }
            RepeatMode.ALL -> {
                val nextIdx = (idx + 1) % q.size
                _queueIndex.value = nextIdx
                playTrack(q[nextIdx])
            }
            RepeatMode.OFF -> {
                if (_shuffleEnabled.value) {
                    if (q.size <= 1) {
                        player?.seekTo(0)
                        player?.play()
                    } else {
                        val candidates = (0 until q.size).filter { it != idx }
                        val nextIdx = candidates.random()
                        _queueIndex.value = nextIdx
                        playTrack(q[nextIdx])
                    }
                } else if (idx + 1 < q.size) {
                    _queueIndex.value = idx + 1
                    playTrack(q[idx + 1])
                }
            }
        }
    }

    fun toggleShuffle() {
        _shuffleEnabled.value = !_shuffleEnabled.value
        player?.shuffleModeEnabled = _shuffleEnabled.value
    }

    fun cycleRepeatMode() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        player?.repeatMode = when (_repeatMode.value) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        player?.setPlaybackSpeed(speed)
    }

    fun getCurrentPosition(): Long = player?.currentPosition ?: 0L

    // Don't release from ViewModel onCleared — Singleton lives for app lifetime
    // The player is reused across Activity recreations
}
