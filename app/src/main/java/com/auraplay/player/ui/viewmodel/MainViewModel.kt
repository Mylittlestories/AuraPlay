package com.auraplay.player.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auraplay.player.audio.AudioEngine
import com.auraplay.player.audio.AudioPreset
import com.auraplay.player.audio.ShuffleManager
import com.auraplay.player.data.model.*
import com.auraplay.player.data.repository.MusicRepository
import com.auraplay.player.playback.PlaybackManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: MusicRepository,
    val playbackManager: PlaybackManager,
    val audioEngine: AudioEngine,
    private val shuffleManager: ShuffleManager
) : ViewModel() {

    // ==================== Library Data ====================
    val allTracks = repository.getAllTracks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAlbumNames = repository.getAllAlbumNames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allArtistNames = repository.getAllArtistNames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGenreNames = repository.getAllGenreNames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFolderNames = repository.getAllFolderNames()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteTracks = repository.getFavoriteTracks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyPlayed = repository.getRecentlyPlayed()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mostPlayed = repository.getMostPlayed()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentlyAdded = repository.getRecentlyAdded()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists = repository.getAllPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ==================== UI State ====================
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanProgress = MutableStateFlow("")
    val scanProgress: StateFlow<String> = _scanProgress.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults = _searchQuery
        .debounce(300)
        .filter { it.isNotBlank() }
        .flatMapLatest { query -> repository.searchTracks(query) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentAlbumTracks = MutableStateFlow<List<Track>>(emptyList())
    val currentAlbumTracks: StateFlow<List<Track>> = _currentAlbumTracks.asStateFlow()

    private val _currentArtistTracks = MutableStateFlow<List<Track>>(emptyList())
    val currentArtistTracks: StateFlow<List<Track>> = _currentArtistTracks.asStateFlow()

    private val _currentFolderTracks = MutableStateFlow<List<Track>>(emptyList())
    val currentFolderTracks: StateFlow<List<Track>> = _currentFolderTracks.asStateFlow()

    private val _currentPlaylistTracks = MutableStateFlow<List<Track>>(emptyList())
    val currentPlaylistTracks: StateFlow<List<Track>> = _currentPlaylistTracks.asStateFlow()

    // ==================== Playback State (exposed) ====================
    val currentTrack = playbackManager.currentTrack
    val isPlaying = playbackManager.isPlaying
    val currentPosition = playbackManager.currentPosition
    val duration = playbackManager.duration
    val repeatMode = playbackManager.repeatMode
    val shuffleEnabled = playbackManager.shuffleEnabled
    val shuffleMode = playbackManager.shuffleMode
    val queue = playbackManager.queue
    val currentIndex = playbackManager.currentIndex
    val playbackSpeed = playbackManager.playbackSpeed
    val equalizerSettings = audioEngine.equalizerSettings
    val bassBoostSettings = audioEngine.bassBoostSettings
    val virtualizerSettings = audioEngine.virtualizerSettings
    val loudnessSettings = audioEngine.loudnessSettings
    val reverbSettings = audioEngine.equalizerSettings // alias for compat

    init {
        playbackManager.initialize()
    }

    // ==================== Scanning ====================

    fun scanForMusic() {
        viewModelScope.launch {
            _isScanning.value = true
            _scanProgress.value = "Scanning for music..."
            try {
                val count = repository.scanDeviceForMusic()
                _scanProgress.value = "Found $count tracks"
            } catch (e: Exception) {
                _scanProgress.value = "Scan failed: ${e.message}"
            } finally {
                _isScanning.value = false
            }
        }
    }

    // ==================== Playback ====================

    fun playTrack(track: Track, queue: List<Track> = allTracks.value) {
        val index = queue.indexOf(track).coerceAtLeast(0)
        playbackManager.setQueueAndPlay(queue, index)
        viewModelScope.launch {
            repository.incrementPlayCount(track.id)
        }
    }

    fun playAlbum(album: String) {
        viewModelScope.launch {
            repository.getTracksByAlbum(album).first().let { tracks ->
                if (tracks.isNotEmpty()) {
                    playbackManager.playAll(tracks)
                }
            }
        }
    }

    fun playArtist(artist: String) {
        viewModelScope.launch {
            repository.getTracksByArtist(artist).first().let { tracks ->
                if (tracks.isNotEmpty()) {
                    playbackManager.playAll(tracks)
                }
            }
        }
    }

    fun playFolder(folder: String) {
        viewModelScope.launch {
            repository.getTracksByFolder(folder).first().let { tracks ->
                if (tracks.isNotEmpty()) {
                    playbackManager.playAll(tracks)
                }
            }
        }
    }

    fun playPlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.getPlaylistTracks(playlistId).first().let { tracks ->
                if (tracks.isNotEmpty()) {
                    playbackManager.playAll(tracks)
                }
            }
        }
    }

    fun shuffleAll() {
        val tracks = allTracks.value
        if (tracks.isNotEmpty()) {
            playbackManager.playAll(tracks, shuffle = true)
        }
    }

    fun togglePlayPause() = playbackManager.togglePlayPause()
    fun skipToNext() = playbackManager.skipToNext()
    fun skipToPrevious() = playbackManager.skipToPrevious()
    fun seekTo(positionMs: Long) = playbackManager.seekTo(positionMs)
    fun toggleRepeatMode() = playbackManager.toggleRepeatMode()
    fun toggleShuffle() = playbackManager.toggleShuffle()

    fun setShuffleMode(mode: ShuffleManager.ShuffleMode) {
        playbackManager.setShuffleMode(mode)
    }

    fun addToQueue(track: Track) = playbackManager.addToQueue(track)
    fun shuffleQueue() = playbackManager.shuffleQueue()
    fun clearQueue() = playbackManager.clearQueue()
    fun skipToIndex(index: Int) = playbackManager.skipToIndex(index)
    fun playAll(tracks: List<Track>, shuffle: Boolean = false) = playbackManager.playAll(tracks, shuffle)

    // ==================== Favorites ====================

    fun toggleFavorite(trackId: Long) {
        viewModelScope.launch {
            repository.toggleFavorite(trackId)
        }
    }

    // ==================== Playlists ====================

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            repository.createPlaylist(name)
        }
    }

    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            repository.deletePlaylist(playlist)
        }
    }

    fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            repository.addTrackToPlaylist(playlistId, trackId)
        }
    }

    fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            repository.removeTrackFromPlaylist(playlistId, trackId)
        }
    }

    // ==================== Search ====================

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // ==================== Album/Artist/Folder Details ====================

    fun loadAlbumTracks(album: String) {
        viewModelScope.launch {
            repository.getTracksByAlbum(album).collect {
                _currentAlbumTracks.value = it
            }
        }
    }

    fun loadArtistTracks(artist: String) {
        viewModelScope.launch {
            repository.getTracksByArtist(artist).collect {
                _currentArtistTracks.value = it
            }
        }
    }

    fun loadFolderTracks(folder: String) {
        viewModelScope.launch {
            repository.getTracksByFolder(folder).collect {
                _currentFolderTracks.value = it
            }
        }
    }

    fun loadPlaylistTracks(playlistId: Long) {
        viewModelScope.launch {
            repository.getPlaylistTracks(playlistId).collect {
                _currentPlaylistTracks.value = it
            }
        }
    }

    // ==================== Audio Settings ====================

    fun setEqualizerBand(band: Short, level: Short) {
        audioEngine.setEqualizerBand(band, level)
    }

    fun setEqualizerPreset(preset: Short) {
        audioEngine.setEqualizerPreset(preset)
    }

    fun setBassBoostStrength(strength: Short) {
        audioEngine.setBassBoostStrength(strength)
    }

    fun setVirtualizerStrength(strength: Short) {
        audioEngine.setVirtualizerStrength(strength)
    }

    fun setLoudnessGain(gainMb: Int) {
        audioEngine.setLoudnessGain(gainMb)
    }

    fun applyAudioPreset(preset: AudioPreset) {
        audioEngine.applyPreset(preset)
    }

    fun setEqualizerEnabled(enabled: Boolean) {
        audioEngine.setEqualizerEnabled(enabled)
    }

    fun setBassBoostEnabled(enabled: Boolean) {
        audioEngine.setBassBoostEnabled(enabled)
    }

    fun setVirtualizerEnabled(enabled: Boolean) {
        audioEngine.setVirtualizerEnabled(enabled)
    }

    fun setLoudnessEnabled(enabled: Boolean) {
        audioEngine.setLoudnessEnabled(enabled)
    }

    fun setPlaybackSpeed(speed: Float) {
        playbackManager.setPlaybackSpeed(speed)
    }

    override fun onCleared() {
        super.onCleared()
        // Don't release here - let the service handle it
    }
}