package com.auraplay.player.data.repository
import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.auraplay.player.data.local.PlaylistDao
import com.auraplay.player.data.local.TrackDao
import com.auraplay.player.data.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepo @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val td: TrackDao,
    private val pd: PlaylistDao
) {
    suspend fun scan(): Int = withContext(Dispatchers.IO) {
        val list = mutableListOf<Track>()
        val r = MediaMetadataRetriever()
        val uri = if (Build.VERSION.SDK_INT >= 29) MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                  else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cols = arrayOf(
            MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ALBUM_ARTIST, MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.BITRATE, MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.TRACK, MediaStore.Audio.Media.DATE_ADDED, MediaStore.Audio.Media.IS_MUSIC
        )
        ctx.contentResolver.query(uri, cols, "${MediaStore.Audio.Media.IS_MUSIC}!=0", null, "${MediaStore.Audio.Media.TITLE} ASC")?.use { c ->
            val ci = cols.associateWith { c.getColumnIndexOrThrow(it) }
            while (c.moveToNext()) {
                val path = c.getString(ci[MediaStore.Audio.Media.DATA]!!) ?: continue
                val f = File(path); if (!f.exists()) continue
                val id = c.getLong(ci[MediaStore.Audio.Media._ID]!!)
                var genre = ""; var sr = 0
                try { r.setDataSource(path); genre = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE) ?: ""; sr = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)?.toIntOrNull() ?: 0 } catch (_: Exception) {}
                val art = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), id).toString()
                list.add(Track(id = id, title = c.getString(ci[MediaStore.Audio.Media.TITLE]!!) ?: f.nameWithoutExtension, artist = c.getString(ci[MediaStore.Audio.Media.ARTIST]!!) ?: "Unknown", album = c.getString(ci[MediaStore.Audio.Media.ALBUM]!!) ?: "Unknown", albumArtist = c.getString(ci[MediaStore.Audio.Media.ALBUM_ARTIST]!!) ?: "", duration = c.getLong(ci[MediaStore.Audio.Media.DURATION]!!), filePath = path, fileName = c.getString(ci[MediaStore.Audio.Media.DISPLAY_NAME]!!) ?: f.name, fileSize = c.getLong(ci[MediaStore.Audio.Media.SIZE]!!), mimeType = c.getString(ci[MediaStore.Audio.Media.MIME_TYPE]!!) ?: "", bitrate = c.getInt(ci[MediaStore.Audio.Media.BITRATE]!!), sampleRate = sr, year = c.getInt(ci[MediaStore.Audio.Media.YEAR]!!), trackNumber = c.getInt(ci[MediaStore.Audio.Media.TRACK]!!) % 1000, genre = genre, dateAdded = c.getLong(ci[MediaStore.Audio.Media.DATE_ADDED]!!) * 1000, albumArtUri = art, folderName = f.parentFile?.name ?: "Unknown"))
            }
        }
        try { r.release() } catch (_: Exception) {}
        td.clear(); if (list.isNotEmpty()) td.insertAll(list)
        list.size
    }

    fun allTracks() = td.getAll()
    fun byAlbum(a: String) = td.byAlbum(a)
    fun byArtist(a: String) = td.byArtist(a)
    fun byGenre(g: String) = td.byGenre(g)
    fun byFolder(f: String) = td.byFolder(f)
    fun favorites() = td.favorites()
    fun mostPlayed() = td.mostPlayed()
    fun recentlyPlayed() = td.recentlyPlayed()
    fun recentlyAdded() = td.recentlyAdded()
    fun search(q: String) = td.search(q)
    fun albumNames() = td.albumNames()
    fun artistNames() = td.artistNames()
    fun genreNames() = td.genreNames()
    fun folderNames() = td.folderNames()
    suspend fun toggleFav(id: Long) { val t = td.byId(id) ?: return; td.setFav(id, !t.isFavorite) }
    suspend fun incPlay(id: Long) = td.incPlay(id)
    fun playlists() = pd.getAll()
    suspend fun createPlaylist(n: String) = pd.insert(Playlist(name = n))
    suspend fun deletePlaylist(p: Playlist) { pd.clearTracks(p.id); pd.delete(p) }
    suspend fun addToPlaylist(pid: Long, tid: Long) { val mp = pd.maxPos(pid) ?: -1; pd.insertTrack(PlaylistTrack(pid, tid, mp + 1)) }
    fun playlistTracks(pid: Long) = combine(pd.trackIds(pid), td.getAll()) { ids, all -> ids.mapNotNull { id -> all.find { it.id == id } } }.flowOn(Dispatchers.IO)
}
