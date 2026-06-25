package com.auraplay.player.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.auraplay.player.audio.AudioEngine
import com.auraplay.player.audio.AudioPreset
import com.auraplay.player.audio.MetadataReader
import com.auraplay.player.audio.ShuffleManager
import com.auraplay.player.audio.TrackMetadata
import com.auraplay.player.audio.TubeAmplifier
import com.auraplay.player.data.model.*
import com.auraplay.player.data.repository.MusicRepository
import com.auraplay.player.playback.PlaybackManager
import com.auraplay.player.ui.theme.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val repository: MusicRepository,
    val playbackManager: PlaybackManager,
    val audioEngine: AudioEngine,
    private val shuffleManager: ShuffleManager,
    private val metadataReader: MetadataReader,
    private val tubeAmplifier: TubeAmplifier
) : AndroidViewModel(application) {

    // ==================== Theme ====================
    private val _currentTheme = MutableStateFlow(AppTheme.AURAPLAY)
    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()

    fun setTheme(theme: AppTheme) {
        _currentTheme.value = theme
    }

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

    // ==================== Playback State ====================
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

    private val _ampMode = MutableStateFlow(TubeAmplifier.AmpMode.OFF)
    val ampMode: StateFlow<TubeAmplifier.AmpMode> = _ampMode.asStateFlow()

    init {
        playbackManager.initialize()
    }

    // ==================== Scanning ====================

    fun scanForMusic() {
        viewModelScope.launch {
            _isScanning.value = true
            _scanProgress.value = "Scanning device for music..."
            try {
                val count = repository.scanDeviceForMusic()
                _scanProgress.value = if (count > 0) "Found $count tracks" else "No music files found. Make sure you have music files in your storage."
            } catch (e: Exception) {
                _scanProgress.value = "Scan error: ${e.message}"
                e.printStackTrace()
            } finally {
                _isScanning.value = false
            }
        }
    }

    // ==================== Playback ====================

    fun playTrack(track: Track, queue: List<Track> = allTracks.value) {
        val index = queue.indexOf(track).coerceAtLeast(0)
        playbackManager.setQueueAndPlay(queue, index)
        viewModelScope.launch { repository.incrementPlayCount(track.id) }
    }

    fun playAlbum(album: String) {
        viewModelScope.launch {
            repository.getTracksByAlbum(album).first().let { tracks ->
                if (tracks.isNotEmpty()) playbackManager.playAll(tracks)
            }
        }
    }

    fun playArtist(artist: String) {
        viewModelScope.launch {
            repository.getTracksByArtist(artist).first().let { tracks ->
                if (tracks.isNotEmpty()) playbackManager.playAll(tracks)
            }
        }
    }

    fun playFolder(folder: String) {
        viewModelScope.launch {
            repository.getTracksByFolder(folder).first().let { tracks ->
                if (tracks.isNotEmpty()) playbackManager.playAll(tracks)
            }
        }
    }

    fun playPlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.getPlaylistTracks(playlistId).first().let { tracks ->
                if (tracks.isNotEmpty()) playbackManager.playAll(tracks)
            }
        }
    }

    fun shuffleAll() {
        val tracks = allTracks.value
        if (tracks.isNotEmpty()) playbackManager.playAll(tracks, shuffle = true)
    }

    fun playAll(tracks: List<Track>, shuffle: Boolean = false) = playbackManager.playAll(tracks, shuffle)
    fun togglePlayPause() = playbackManager.togglePlayPause()
    fun skipToNext() = playbackManager.skipToNext()
    fun skipToPrevious() = playbackManager.skipToPrevious()
    fun seekTo(positionMs: Long) = playbackManager.seekTo(positionMs)
    fun toggleRepeatMode() = playbackManager.toggleRepeatMode()
    fun toggleShuffle() = playbackManager.toggleShuffle()
    fun setShuffleMode(mode: ShuffleManager.ShuffleMode) = playbackManager.setShuffleMode(mode)
    fun addToQueue(track: Track) = playbackManager.addToQueue(track)
    fun shuffleQueue() = playbackManager.shuffleQueue()
    fun clearQueue() = playbackManager.clearQueue()
    fun skipToIndex(index: Int) = playbackManager.skipToIndex(index)

    // ==================== Favorites ====================
    fun toggleFavorite(trackId: Long) {
        viewModelScope.launch { repository.toggleFavorite(trackId) }
    }

    // ==================== Playlists ====================
    fun createPlaylist(name: String) { viewModelScope.launch { repository.createPlaylist(name) } }
    fun deletePlaylist(playlist: Playlist) { viewModelScope.launch { repository.deletePlaylist(playlist) } }
    fun addTrackToPlaylist(playlistId: Long, trackId: Long) { viewModelScope.launch { repository.addTrackToPlaylist(playlistId, trackId) } }
    fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) { viewModelScope.launch { repository.removeTrackFromPlaylist(playlistId, trackId) } }

    // ==================== Search ====================
    fun setSearchQuery(query: String) { _searchQuery.value = query }

    // ==================== Detail Loading ====================
    fun loadAlbumTracks(album: String) {
        viewModelScope.launch { repository.getTracksByAlbum(album).collect { _currentAlbumTracks.value = it } }
    }
    fun loadArtistTracks(artist: String) {
        viewModelScope.launch { repository.getTracksByArtist(artist).collect { _currentArtistTracks.value = it } }
    }
    fun loadFolderTracks(folder: String) {
        viewModelScope.launch { repository.getTracksByFolder(folder).collect { _currentFolderTracks.value = it } }
    }
    fun loadPlaylistTracks(playlistId: Long) {
        viewModelScope.launch { repository.getPlaylistTracks(playlistId).collect { _currentPlaylistTracks.value = it } }
    }

    // ==================== Audio Settings ====================
    fun setEqualizerBand(band: Short, level: Short) = audioEngine.setEqualizerBand(band, level)
    fun setEqualizerPreset(preset: Short) = audioEngine.setEqualizerPreset(preset)
    fun setBassBoostStrength(strength: Short) = audioEngine.setBassBoostStrength(strength)
    fun setVirtualizerStrength(strength: Short) = audioEngine.setVirtualizerStrength(strength)
    fun setLoudnessGain(gainMb: Int) = audioEngine.setLoudnessGain(gainMb)
    fun applyAudioPreset(preset: AudioPreset) = audioEngine.applyPreset(preset)
    fun setEqualizerEnabled(enabled: Boolean) = audioEngine.setEqualizerEnabled(enabled)
    fun setBassBoostEnabled(enabled: Boolean) = audioEngine.setBassBoostEnabled(enabled)
    fun setVirtualizerEnabled(enabled: Boolean) = audioEngine.setVirtualizerEnabled(enabled)
    fun setLoudnessEnabled(enabled: Boolean) = audioEngine.setLoudnessEnabled(enabled)
    fun setPlaybackSpeed(speed: Float) = playbackManager.setPlaybackSpeed(speed)

    // ==================== Tube Amplifier ====================
    fun cycleAmpMode() {
        val modes = TubeAmplifier.AmpMode.entries
        val currentIndex = modes.indexOf(_ampMode.value)
        val nextMode = modes[(currentIndex + 1) % modes.size]
        _ampMode.value = nextMode
        applyAmpMode(nextMode)
    }

    fun setAmpMode(mode: TubeAmplifier.AmpMode) {
        _ampMode.value = mode
        applyAmpMode(mode)
    }

    private fun applyAmpMode(mode: TubeAmplifier.AmpMode) {
        val settings = tubeAmplifier.getAmpCurve(mode)
        settings.bandLevels.forEachIndexed { i, level ->
            audioEngine.setEqualizerBand(i.toShort(), level.toShort())
        }
        audioEngine.setBassBoostStrength(settings.bassBoost.toShort())
        audioEngine.setVirtualizerStrength(settings.virtualizer.toShort())
        audioEngine.setLoudnessGain(settings.loudnessGain)
        if (mode != TubeAmplifier.AmpMode.OFF) {
            audioEngine.setEqualizerEnabled(true)
            audioEngine.setBassBoostEnabled(true)
            audioEngine.setVirtualizerEnabled(true)
            audioEngine.setLoudnessEnabled(true)
        }
    }

    // ==================== Metadata ====================
    fun getTrackMetadata(filePath: String): TrackMetadata? {
        return try { metadataReader.readMetadata(filePath) } catch (_: Exception) { null }
    }

    fun downloadAlbumArt(track: Track) { /* TODO: MusicBrainz API */ }
    fun downloadLyrics(track: Track) { /* TODO: Lyrics API */ }
}