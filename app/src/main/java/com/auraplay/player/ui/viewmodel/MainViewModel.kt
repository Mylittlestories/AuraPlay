package com.auraplay.player.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auraplay.player.audio.AudioEngine
import com.auraplay.player.audio.ShuffleManager
import com.auraplay.player.audio.ShuffleMode
import com.auraplay.player.data.model.RepeatMode
import com.auraplay.player.data.model.Track
import com.auraplay.player.data.repository.MusicRepository
import com.auraplay.player.playback.PlaybackManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaybackState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val progress: Long = 0,
    val duration: Long = 0,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val queue: List<Track> = emptyList(),
    val queueIndex: Int = -1
)

data class LibraryState(
    val tracks: List<Track> = emptyList(),
    val albums: List<String> = emptyList(),
    val artists: List<String> = emptyList(),
    val genres: List<String> = emptyList(),
    val folders: List<String> = emptyList(),
    val favorites: List<Track> = emptyList(),
    val searchResults: List<Track> = emptyList(),
    val searchQuery: String = "",
    val isScanning: Boolean = false,
    val scanComplete: Boolean = false,
    val trackCount: Int = 0,
    val hasPermission: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val playbackManager: PlaybackManager,
    private val audioEngine: AudioEngine
) : ViewModel() {

    private val shuffleManager = ShuffleManager()

    private val _libraryState = MutableStateFlow(LibraryState())
    val libraryState: StateFlow<LibraryState> = _libraryState.asStateFlow()

    val playbackState: StateFlow<PlaybackState> = combine(
        playbackManager.currentTrack,
        playbackManager.isPlaying
    ) { track, playing -> Pair(track, playing) }
        .combine(playbackManager.progress) { (track, playing), progress -> Triple(track, playing, progress) }
        .combine(playbackManager.duration) { prev, duration ->
            PlaybackState(currentTrack = prev.first, isPlaying = prev.second, progress = prev.third, duration = duration)
        }
        .combine(playbackManager.shuffleEnabled) { state, shuffle -> state.copy(shuffleEnabled = shuffle) }
        .combine(playbackManager.repeatMode) { state, repeat -> state.copy(repeatMode = repeat) }
        .combine(playbackManager.queue) { state, queue -> state.copy(queue = queue) }
        .combine(playbackManager.queueIndex) { state, idx -> state.copy(queueIndex = idx) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, PlaybackState())

    private val _shuffleMode = MutableStateFlow(ShuffleMode.SMART)
    val shuffleMode: StateFlow<ShuffleMode> = _shuffleMode.asStateFlow()

    val eqState = audioEngine.eqState

    init {
        viewModelScope.launch {
            repository.getAllTracks().collect { tracks ->
                _libraryState.value = _libraryState.value.copy(
                    tracks = tracks,
                    trackCount = tracks.size,
                    scanComplete = true
                )
            }
        }
        viewModelScope.launch {
            repository.getFavoriteTracks().collect { favorites ->
                _libraryState.value = _libraryState.value.copy(favorites = favorites)
            }
        }
        viewModelScope.launch {
            repository.getDistinctAlbums().collect { albums ->
                _libraryState.value = _libraryState.value.copy(albums = albums)
            }
        }
        viewModelScope.launch {
            repository.getDistinctArtists().collect { artists ->
                _libraryState.value = _libraryState.value.copy(artists = artists)
            }
        }
        viewModelScope.launch {
            repository.getDistinctGenres().collect { genres ->
                _libraryState.value = _libraryState.value.copy(genres = genres)
            }
        }
        viewModelScope.launch {
            repository.getDistinctFolders().collect { folders ->
                _libraryState.value = _libraryState.value.copy(folders = folders)
            }
        }
    }

    fun setPermissionGranted(granted: Boolean) {
        _libraryState.value = _libraryState.value.copy(hasPermission = granted)
        if (granted) scanForMusic()
    }

    fun scanForMusic() {
        viewModelScope.launch {
            _libraryState.value = _libraryState.value.copy(isScanning = true)
            val count = repository.scanAndStoreMusic()
            _libraryState.value = _libraryState.value.copy(
                isScanning = false,
                scanComplete = true,
                trackCount = count
            )
        }
    }

    fun playTrack(track: Track) {
        val tracks = _libraryState.value.tracks
        val index = tracks.indexOf(track)
        val queue = if (_shuffleMode.value != ShuffleMode.OFF) {
            shuffleManager.shuffle(tracks, _shuffleMode.value)
        } else tracks
        playbackManager.playQueue(queue, if (index >= 0) index else 0)
        viewModelScope.launch { repository.incrementPlayCount(track.id) }
    }

    fun playAlbum(album: String) {
        viewModelScope.launch {
            repository.getTracksByAlbum(album).first().let { tracks ->
                if (tracks.isNotEmpty()) playbackManager.playQueue(tracks, 0)
            }
        }
    }

    fun playArtist(artist: String) {
        viewModelScope.launch {
            repository.getTracksByArtist(artist).first().let { tracks ->
                if (tracks.isNotEmpty()) playbackManager.playQueue(tracks, 0)
            }
        }
    }

    fun playGenre(genre: String) {
        viewModelScope.launch {
            repository.getTracksByGenre(genre).first().let { tracks ->
                if (tracks.isNotEmpty()) playbackManager.playQueue(tracks, 0)
            }
        }
    }

    fun playFolder(folder: String) {
        viewModelScope.launch {
            repository.getTracksByFolder(folder).first().let { tracks ->
                if (tracks.isNotEmpty()) playbackManager.playQueue(tracks, 0)
            }
        }
    }

    fun playFavorites() {
        val favs = _libraryState.value.favorites
        if (favs.isNotEmpty()) playbackManager.playQueue(favs, 0)
    }

    fun togglePlayPause() = playbackManager.togglePlayPause()
    fun skipNext() = playbackManager.skipNext()
    fun skipPrevious() = playbackManager.skipPrevious()
    fun seekTo(position: Long) = playbackManager.seekTo(position)
    fun seekToFraction(fraction: Float) = playbackManager.seekToFraction(fraction)
    fun toggleShuffle() = playbackManager.toggleShuffle()
    fun cycleRepeatMode() = playbackManager.cycleRepeatMode()
    fun addToQueue(track: Track) = playbackManager.addToQueue(track)
    fun removeFromQueue(index: Int) = playbackManager.removeFromQueue(index)
    fun playQueue(tracks: List<Track>, startIndex: Int) = playbackManager.playQueue(tracks, startIndex)

    fun setShuffleMode(mode: ShuffleMode) {
        _shuffleMode.value = mode
    }

    fun toggleFavorite(track: Track) {
        viewModelScope.launch {
            repository.setFavorite(track.id, !track.isFavorite)
        }
    }

    fun searchTracks(query: String) {
        _libraryState.value = _libraryState.value.copy(searchQuery = query)
        if (query.isBlank()) {
            _libraryState.value = _libraryState.value.copy(searchResults = emptyList())
            return
        }
        viewModelScope.launch {
            repository.searchTracks(query).collect { results ->
                _libraryState.value = _libraryState.value.copy(searchResults = results)
            }
        }
    }

    fun getTracksByAlbum(album: String): Flow<List<Track>> = repository.getTracksByAlbum(album)
    fun getTracksByArtist(artist: String): Flow<List<Track>> = repository.getTracksByArtist(artist)
    fun getTracksByGenre(genre: String): Flow<List<Track>> = repository.getTracksByGenre(genre)
    fun getTracksByFolder(folder: String): Flow<List<Track>> = repository.getTracksByFolder(folder)

    // Equalizer controls
    fun setBandLevel(bandIndex: Int, level: Int) = audioEngine.setBandLevel(bandIndex, level)
    fun setBassBoost(strength: Int) = audioEngine.setBassBoost(strength)
    fun setVirtualizer(strength: Int) = audioEngine.setVirtualizer(strength)
    fun setLoudness(gain: Int) = audioEngine.setLoudness(gain)
    fun applyPreset(presetName: String) = audioEngine.applyPreset(presetName)
    fun getPresets(): List<String> = audioEngine.presets

    override fun onCleared() {
        super.onCleared()
        // PlaybackManager is @Singleton — don't release it, it persists across Activity lifecycles
        // AudioEngine is @Singleton too — same reason
    }
}
