package com.auraplay.player.playback

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.*
import androidx.media3.exoplayer.ExoPlayer
import com.auraplay.player.audio.AudioEngine
import com.auraplay.player.audio.ShuffleManager
import com.auraplay.player.data.model.RepeatMode
import com.auraplay.player.data.model.Track
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackManager @Inject constructor(
    @ApplicationContext private val context: Context,
    val audioEngine: AudioEngine,
    val shuffleManager: ShuffleManager
) {
    companion object {
        private const val TAG = "PlaybackManager"
    }

    private var exoPlayer: ExoPlayer? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _shuffleEnabled = MutableStateFlow(false)
    val shuffleEnabled: StateFlow<Boolean> = _shuffleEnabled.asStateFlow()

    private val _shuffleMode = MutableStateFlow(ShuffleManager.ShuffleMode.SMART)
    val shuffleMode: StateFlow<ShuffleManager.ShuffleMode> = _shuffleMode.asStateFlow()

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow(-1)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private var positionUpdateJob: Job? = null

    fun initialize() {
        if (exoPlayer != null) return

        exoPlayer = ExoPlayer.Builder(context)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
            .apply {
                addListener(playerListener)
                audioEngine.initialize(audioSessionId)
            }

        startPositionUpdates()
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val newIndex = exoPlayer?.currentMediaItemIndex ?: return
            if (newIndex < _queue.value.size && newIndex != _currentIndex.value) {
                _currentIndex.value = newIndex
                _currentTrack.value = _queue.value[newIndex]
                _duration.value = exoPlayer?.duration?.takeIf { it > 0 } ?: 0L
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                onTrackEnded()
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.e(TAG, "Playback error", error)
            skipToNext()
        }
    }

    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = scope.launch {
            while (isActive) {
                delay(200)
                exoPlayer?.let { player ->
                    _currentPosition.value = player.currentPosition
                    if (player.duration > 0) {
                        _duration.value = player.duration
                    }
                }
            }
        }
    }

    fun play() { exoPlayer?.play() }
    fun pause() { exoPlayer?.pause() }
    fun togglePlayPause() { if (_isPlaying.value) pause() else play() }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
        _currentPosition.value = positionMs
    }

    fun skipToNext() {
        val queueSize = _queue.value.size
        if (queueSize == 0) return
        val newIndex = if (_shuffleEnabled.value) {
            shuffleManager.getNextShuffledIndex(_currentIndex.value)
        } else {
            (_currentIndex.value + 1) % queueSize
        }
        skipToIndex(newIndex)
    }

    fun skipToPrevious() {
        val queueSize = _queue.value.size
        if (queueSize == 0) return
        if (_currentPosition.value > 3000) { seekTo(0); return }
        val newIndex = if (_shuffleEnabled.value) {
            shuffleManager.getPreviousShuffledIndex(_currentIndex.value)
        } else {
            if (_currentIndex.value > 0) _currentIndex.value - 1 else queueSize - 1
        }
        skipToIndex(newIndex)
    }

    fun skipToIndex(index: Int) {
        val q = _queue.value
        if (index < 0 || index >= q.size) return
        _currentIndex.value = index
        _currentTrack.value = q[index]
        _duration.value = q[index].duration
        exoPlayer?.let { player ->
            player.seekToDefaultPosition(index)
            player.play()
        }
    }

    fun setQueueAndPlay(tracks: List<Track>, startIndex: Int = 0) {
        if (tracks.isEmpty()) return
        _queue.value = tracks
        _currentIndex.value = startIndex
        if (_shuffleEnabled.value) shuffleManager.setQueue(tracks)
        buildAndSetMediaItems(tracks, startIndex)
    }

    fun playAll(tracks: List<Track>, shuffle: Boolean = false) {
        if (tracks.isEmpty()) return
        if (shuffle) {
            _shuffleEnabled.value = true
            shuffleManager.setQueue(tracks)
            val shuffled = shuffleManager.generateShuffledQueue(tracks)
            _queue.value = shuffled
            _currentIndex.value = 0
            buildAndSetMediaItems(shuffled, 0)
        } else {
            _shuffleEnabled.value = false
            _queue.value = tracks
            _currentIndex.value = 0
            buildAndSetMediaItems(tracks, 0)
        }
    }

    fun addToQueue(track: Track) {
        val q = _queue.value.toMutableList()
        q.add(track)
        _queue.value = q
    }

    fun clearQueue() {
        _queue.value = emptyList()
        _currentIndex.value = -1
        _currentTrack.value = null
        exoPlayer?.clearMediaItems()
    }

    fun shuffleQueue() {
        val q = _queue.value.toMutableList()
        if (q.size <= 1) return
        val current = _currentTrack.value
        q.shuffle()
        if (current != null) {
            val idx = q.indexOfFirst { it.id == current.id }
            if (idx > 0) { q.removeAt(idx); q.add(0, current) }
            _currentIndex.value = 0
        }
        _queue.value = q
        rebuildMediaItems()
    }

    private fun buildAndSetMediaItems(tracks: List<Track>, startIndex: Int) {
        exoPlayer?.let { player ->
            val items = tracks.map { track ->
                MediaItem.Builder()
                    .setUri(Uri.parse("content://media/external/audio/media/${track.id}"))
                    .setMediaId(track.id.toString())
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(track.title)
                            .setArtist(track.artist)
                            .setAlbumTitle(track.album)
                            .build()
                    )
                    .build()
            }
            player.setMediaItems(items, startIndex, 0L)
            player.prepare()
            player.play()
            _currentTrack.value = tracks[startIndex]
        }
    }

    private fun rebuildMediaItems() {
        val player = exoPlayer ?: return
        val q = _queue.value
        val idx = _currentIndex.value
        val pos = player.currentPosition
        val items = q.map { track ->
            MediaItem.Builder()
                .setUri(Uri.parse("content://media/external/audio/media/${track.id}"))
                .setMediaId(track.id.toString())
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(track.title)
                        .setArtist(track.artist)
                        .setAlbumTitle(track.album)
                        .build()
                )
                .build()
        }
        player.setMediaItems(items, idx.coerceAtLeast(0), pos)
        player.prepare()
    }

    fun toggleRepeatMode() {
        val newMode = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        _repeatMode.value = newMode
        exoPlayer?.repeatMode = when (newMode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
        }
    }

    fun toggleShuffle() {
        val newEnabled = !_shuffleEnabled.value
        _shuffleEnabled.value = newEnabled
        if (newEnabled) {
            shuffleManager.setQueue(_queue.value)
            shuffleManager.setShuffleMode(_shuffleMode.value)
        } else {
            shuffleManager.reset()
        }
    }

    fun setShuffleMode(mode: ShuffleManager.ShuffleMode) {
        _shuffleMode.value = mode
        shuffleManager.setShuffleMode(mode)
        _shuffleEnabled.value = mode != ShuffleManager.ShuffleMode.OFF
        if (mode != ShuffleManager.ShuffleMode.OFF) shuffleManager.setQueue(_queue.value)
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        exoPlayer?.setPlaybackSpeed(speed)
    }

    private fun onTrackEnded() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> { seekTo(0); play() }
            RepeatMode.ALL -> skipToNext()
            RepeatMode.OFF -> {
                if (_currentIndex.value < _queue.value.size - 1 || _shuffleEnabled.value) {
                    skipToNext()
                } else {
                    _isPlaying.value = false
                }
            }
        }
    }

    fun getExoPlayer(): ExoPlayer? = exoPlayer
    fun getAudioSessionId(): Int = exoPlayer?.audioSessionId ?: 0

    fun release() {
        positionUpdateJob?.cancel()
        audioEngine.release()
        exoPlayer?.release()
        exoPlayer = null
    }
}