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

    // Playback state
    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackState = MutableStateFlow(PlaybackState.IDLE)
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

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

    private val _crossfadeEnabled = MutableStateFlow(false)
    val crossfadeEnabled: StateFlow<Boolean> = _crossfadeEnabled.asStateFlow()

    private val _crossfadeDuration = MutableStateFlow(3000) // 3 seconds
    val crossfadeDuration: StateFlow<Int> = _crossfadeDuration.asStateFlow()

    private val _gaplessEnabled = MutableStateFlow(true)
    val gaplessEnabled: StateFlow<Boolean> = _gaplessEnabled.asStateFlow()

    private var positionUpdateJob: Job? = null

    fun initialize() {
        if (exoPlayer != null) return

        exoPlayer = ExoPlayer.Builder(context)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true // Handle audio focus
            )
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build()
            .apply {
                addListener(playerListener)
                audioEngine.initialize(audioSessionId)
            }

        startPositionUpdates()
        Log.d(TAG, "PlaybackManager initialized")
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_IDLE -> _playbackState.value = PlaybackState.IDLE
                Player.STATE_BUFFERING -> _playbackState.value = PlaybackState.BUFFERING
                Player.STATE_READY -> _playbackState.value = PlaybackState.READY
                Player.STATE_ENDED -> {
                    _playbackState.value = PlaybackState.ENDED
                    onTrackEnded()
                }
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO_ADVANCE ||
                reason == Player.MEDIA_ITEM_TRANSITION_REASON_SEEK) {
                val newIndex = exoPlayer?.currentMediaItemIndex ?: return
                if (newIndex < _queue.value.size) {
                    _currentIndex.value = newIndex
                    _currentTrack.value = _queue.value[newIndex]
                    _duration.value = exoPlayer?.duration?.takeIf { it > 0 } ?: 0L
                }
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.e(TAG, "Playback error", error)
            _playbackState.value = PlaybackState.ERROR
            // Try to play next track
            skipToNext()
        }
    }

    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = scope.launch {
            while (isActive) {
                delay(100)
                exoPlayer?.let { player ->
                    _currentPosition.value = player.currentPosition
                    if (player.duration > 0) {
                        _duration.value = player.duration
                    }
                }
            }
        }
    }

    // ==================== Playback Controls ====================

    fun play() {
        exoPlayer?.play()
    }

    fun pause() {
        exoPlayer?.pause()
    }

    fun togglePlayPause() {
        if (_isPlaying.value) pause() else play()
    }

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

        // If more than 3 seconds in, restart current track
        if ((_currentPosition.value) > 3000) {
            seekTo(0)
            return
        }

        val newIndex = if (_shuffleEnabled.value) {
            shuffleManager.getPreviousShuffledIndex(_currentIndex.value)
        } else {
            if (_currentIndex.value > 0) _currentIndex.value - 1 else queueSize - 1
        }

        skipToIndex(newIndex)
    }

    fun skipToIndex(index: Int) {
        val queue = _queue.value
        if (index < 0 || index >= queue.size) return

        _currentIndex.value = index
        _currentTrack.value = queue[index]
        _duration.value = queue[index].duration

        exoPlayer?.let { player ->
            player.seekToDefaultPosition(index)
            player.play()
        }
    }

    // ==================== Queue Management ====================

    fun setQueueAndPlay(tracks: List<Track>, startIndex: Int = 0) {
        if (tracks.isEmpty()) return

        _queue.value = tracks
        _currentIndex.value = startIndex

        // Set shuffle queue
        if (_shuffleEnabled.value) {
            shuffleManager.setQueue(tracks)
        }

        buildAndSetMediaItems(tracks, startIndex)
    }

    fun playAll(tracks: List<Track>, shuffle: Boolean = false) {
        if (tracks.isEmpty()) return

        if (shuffle) {
            _shuffleEnabled.value = true
            shuffleManager.setQueue(tracks)
            val shuffledTracks = shuffleManager.generateShuffledQueue(tracks)
            _queue.value = shuffledTracks
            _currentIndex.value = 0
            buildAndSetMediaItems(shuffledTracks, 0)
        } else {
            _shuffleEnabled.value = false
            _queue.value = tracks
            _currentIndex.value = 0
            buildAndSetMediaItems(tracks, 0)
        }
    }

    fun addToQueue(track: Track) {
        val currentQueue = _queue.value.toMutableList()
        currentQueue.add(track)
        _queue.value = currentQueue
        rebuildMediaItems()
    }

    fun removeFromQueue(index: Int) {
        val currentQueue = _queue.value.toMutableList()
        if (index < 0 || index >= currentQueue.size) return

        currentQueue.removeAt(index)
        _queue.value = currentQueue

        if (index < _currentIndex.value) {
            _currentIndex.value = _currentIndex.value - 1
        } else if (index == _currentIndex.value) {
            // Current track removed, play next
            if (currentQueue.isNotEmpty()) {
                val newIndex = _currentIndex.value.coerceAtMost(currentQueue.size - 1)
                _currentIndex.value = newIndex
                _currentTrack.value = currentQueue[newIndex]
                rebuildMediaItems()
                exoPlayer?.seekToDefaultPosition(newIndex)
            }
        }

        rebuildMediaItems()
    }

    fun moveInQueue(from: Int, to: Int) {
        val currentQueue = _queue.value.toMutableList()
        if (from < 0 || from >= currentQueue.size || to < 0 || to >= currentQueue.size) return

        val track = currentQueue.removeAt(from)
        currentQueue.add(to, track)
        _queue.value = currentQueue

        // Update current index
        _currentIndex.value = when (_currentIndex.value) {
            from -> to
            in minOf(from, to)..maxOf(from, to) -> {
                if (from < to) _currentIndex.value - 1 else _currentIndex.value + 1
            }
            else -> _currentIndex.value
        }

        rebuildMediaItems()
    }

    fun clearQueue() {
        _queue.value = emptyList()
        _currentIndex.value = -1
        _currentTrack.value = null
        exoPlayer?.clearMediaItems()
    }

    fun shuffleQueue() {
        val currentQueue = _queue.value.toMutableList()
        if (currentQueue.size <= 1) return

        val currentTrack = _currentTrack.value
        currentQueue.shuffle()

        // Move current track to front
        if (currentTrack != null) {
            val currentIndex = currentQueue.indexOfFirst { it.id == currentTrack.id }
            if (currentIndex > 0) {
                currentQueue.removeAt(currentIndex)
                currentQueue.add(0, currentTrack)
            }
            _currentIndex.value = 0
        }

        _queue.value = currentQueue
        rebuildMediaItems()
    }

    private fun buildAndSetMediaItems(tracks: List<Track>, startIndex: Int) {
        exoPlayer?.let { player ->
            val mediaItems = tracks.map { track ->
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

            player.setMediaItems(mediaItems, startIndex, 0L)
            player.prepare()
            player.play()
            _currentTrack.value = tracks[startIndex]
        }
    }

    private fun rebuildMediaItems() {
        val player = exoPlayer ?: return
        val queue = _queue.value
        val currentIndex = _currentIndex.value
        val currentPosition = player.currentPosition

        val mediaItems = queue.map { track ->
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

        player.setMediaItems(mediaItems, currentIndex.coerceAtLeast(0), currentPosition)
        player.prepare()
    }

    // ==================== Repeat & Shuffle ====================

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

        if (mode != ShuffleManager.ShuffleMode.OFF) {
            shuffleManager.setQueue(_queue.value)
        }
    }

    // ==================== Advanced Settings ====================

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        exoPlayer?.setPlaybackSpeed(speed)
    }

    fun setCrossfade(enabled: Boolean, durationMs: Int = 3000) {
        _crossfadeEnabled.value = enabled
        _crossfadeDuration.value = durationMs
    }

    fun setGapless(enabled: Boolean) {
        _gaplessEnabled.value = enabled
    }

    private fun onTrackEnded() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                seekTo(0)
                play()
            }
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

    fun release() {
        positionUpdateJob?.cancel()
        audioEngine.release()
        exoPlayer?.release()
        exoPlayer = null
        scope.cancel()
    }

    fun getAudioSessionId(): Int = exoPlayer?.audioSessionId ?: 0
}

enum class PlaybackState {
    IDLE, BUFFERING, READY, ENDED, ERROR
}