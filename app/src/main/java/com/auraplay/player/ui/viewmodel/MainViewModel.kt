package com.auraplay.player.ui.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.auraplay.player.audio.*
import com.auraplay.player.data.model.*
import com.auraplay.player.data.repository.MusicRepo
import com.auraplay.player.playback.PlaybackManager
import com.auraplay.player.ui.theme.AppTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repo: MusicRepo, val pm: PlaybackManager,
    val audio: AudioEngine, private val sm: ShuffleManager, private val timer: SleepTimer
) : ViewModel() {
    val currentTheme = MutableStateFlow(AppTheme.AURAPLAY)
    fun setTheme(t: AppTheme) { currentTheme.value = t }

    val tracks = repo.allTracks().stateIn(vm, SharingStarted.WhileSubscribed(5000), emptyList())
    val albums = repo.albumNames().stateIn(vm, SharingStarted.WhileSubscribed(5000), emptyList())
    val artists = repo.artistNames().stateIn(vm, SharingStarted.WhileSubscribed(5000), emptyList())
    val genres = repo.genreNames().stateIn(vm, SharingStarted.WhileSubscribed(5000), emptyList())
    val folders = repo.folderNames().stateIn(vm, SharingStarted.WhileSubscribed(5000), emptyList())
    val favorites = repo.favorites().stateIn(vm, SharingStarted.WhileSubscribed(5000), emptyList())
    val recentPlayed = repo.recentlyPlayed().stateIn(vm, SharingStarted.WhileSubscribed(5000), emptyList())
    val topPlayed = repo.mostPlayed().stateIn(vm, SharingStarted.WhileSubscribed(5000), emptyList())
    val newAdded = repo.recentlyAdded().stateIn(vm, SharingStarted.WhileSubscribed(5000), emptyList())
    val playlists = repo.playlists().stateIn(vm, SharingStarted.WhileSubscribed(5000), emptyList())

    private val vm get() = viewModelScope
    val scanning = MutableStateFlow(false)
    val scanMsg = MutableStateFlow("")
    val query = MutableStateFlow("")
    val searchResults = query.debounce(300).filter { it.isNotBlank() }.flatMapLatest { repo.search(it) }.stateIn(vm, SharingStarted.WhileSubscribed(5000), emptyList())
    val albumT = MutableStateFlow<List<Track>>(emptyList())
    val artistT = MutableStateFlow<List<Track>>(emptyList())
    val folderT = MutableStateFlow<List<Track>>(emptyList())
    val plistT = MutableStateFlow<List<Track>>(emptyList())
    val ampMode = MutableStateFlow(AmpMode.OFF)

    val cur get() = pm.currentTrack; val playing get() = pm.isPlaying; val pos get() = pm.position; val dur get() = pm.duration
    val rep get() = pm.repeatMode; val shufOn get() = pm.shuffleOn; val shufMode get() = pm.shuffleMode
    val q get() = pm.queue; val idx get() = pm.curIndex; val spd get() = pm.speed
    val eqS get() = audio.eqState; val bbS get() = audio.bbState; val vzS get() = audio.vzState; val leS get() = audio.leState
    val timerOn get() = timer.running; val timerLeft get() = timer.remaining
    fun timerFmt() = timer.formatted()

    init { pm.init() }

    fun scan() { vm.launch { scanning.value = true; scanMsg.value = "Scanning..."; try { val n = repo.scan(); scanMsg.value = if (n > 0) "Found $n tracks" else "No music found" } catch (e: Exception) { scanMsg.value = "Error: ${e.message}" } finally { scanning.value = false } } }

    fun play(t: Track, list: List<Track> = tracks.value) { pm.setQueue(list, list.indexOf(t).coerceAtLeast(0)); vm.launch { repo.incPlay(t.id) } }
    fun playAlbum(a: String) { vm.launch { repo.byAlbum(a).first().let { if (it.isNotEmpty()) pm.playAll(it) } } }
    fun playArtist(a: String) { vm.launch { repo.byArtist(a).first().let { if (it.isNotEmpty()) pm.playAll(it) } } }
    fun playFolder(f: String) { vm.launch { repo.byFolder(f).first().let { if (it.isNotEmpty()) pm.playAll(it) } } }
    fun playPlist(id: Long) { vm.launch { repo.playlistTracks(id).first().let { if (it.isNotEmpty()) pm.playAll(it) } } }
    fun shuffleAll() { val t = tracks.value; if (t.isNotEmpty()) pm.playAll(t, true) }
    fun playAll(t: List<Track>, s: Boolean = false) = pm.playAll(t, s)
    fun toggle() = pm.toggle(); fun next() = pm.skipNext(); fun prev() = pm.skipPrev(); fun seek(ms: Long) = pm.seek(ms)
    fun toggleRep() = pm.toggleRepeat(); fun toggleShuf() = pm.toggleShuffle(); fun setShuf(m: ShuffleMode) = pm.setShuffleMode(m)
    fun addQ(t: Track) = pm.add(t); fun shuffleQ() = pm.shuffleQueue(); fun clearQ() = pm.clearQueue(); fun goIdx(i: Int) = pm.goTo(i)
    fun toggleFav(id: Long) { vm.launch { repo.toggleFav(id) } }
    fun createPlist(n: String) { vm.launch { repo.createPlaylist(n) } }
    fun delPlist(p: Playlist) { vm.launch { repo.deletePlaylist(p) } }
    fun loadAlbum(a: String) { vm.launch { repo.byAlbum(a).collect { albumT.value = it } } }
    fun loadArtist(a: String) { vm.launch { repo.byArtist(a).collect { artistT.value = it } } }
    fun loadFolder(f: String) { vm.launch { repo.byFolder(f).collect { folderT.value = it } } }
    fun loadPlist(id: Long) { vm.launch { repo.playlistTracks(id).collect { plistT.value = it } } }
    fun setQuery(q: String) { query.value = q }

    fun eqBand(b: Short, l: Short) = audio.setBand(b, l)
    fun eqPreset(p: Short) = audio.setPreset(p)
    fun bbStr(s: Short) = audio.setBbStrength(s)
    fun vzStr(s: Short) = audio.setVzStrength(s)
    fun loudG(g: Int) = audio.setLoudGain(g)
    fun eqOn(e: Boolean) = audio.eqEnabled(e)
    fun bbOn(e: Boolean) = audio.bbEnabled(e)
    fun vzOn(e: Boolean) = audio.vzEnabled(e)
    fun leOn(e: Boolean) = audio.leEnabled(e)
    fun setSpeed(s: Float) = pm.setSpeed(s)
    fun applyPreset(name: String) { val p = AudioPresets.eqPresets.find { it.first == name }; if (p != null) audio.applyCurve(p.second) }

    fun cycleAmp() {
        val modes = AmpMode.entries; ampMode.value = modes[(modes.indexOf(ampMode.value) + 1) % modes.size]
        val a = AudioPresets.get(ampMode.value); audio.applyCurve(a.curve, a.bb, a.vz, a.loud)
    }
    fun startTimer(ms: Long) { timer.onFinish = { pm.pause() }; timer.start(ms) }
    fun cancelTimer() = timer.cancel()
}
