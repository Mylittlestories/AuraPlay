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
class MusicRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val trackDao: TrackDao,
    private val playlistDao: PlaylistDao
) {
    suspend fun scanDeviceForMusic(): Int = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<Track>()
        val retriever = MediaMetadataRetriever()

        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.BITRATE,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.IS_MUSIC
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        context.contentResolver.query(
            collection, projection, selection, null, sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumArtistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ARTIST)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val displayCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val bitrateCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.BITRATE)
            val yearCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val trackCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val addedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val modifiedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val filePath = cursor.getString(dataCol) ?: continue
                val file = File(filePath)
                if (!file.exists()) continue

                var genre = ""
                var sampleRate = 0
                try {
                    retriever.setDataSource(filePath)
                    genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE) ?: ""
                    sampleRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)?.toIntOrNull() ?: 0
                } catch (_: Exception) {}

                val albumArtUri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"), id
                ).toString()

                tracks.add(
                    Track(
                        id = id,
                        title = cursor.getString(titleCol) ?: file.nameWithoutExtension,
                        artist = cursor.getString(artistCol) ?: "Unknown Artist",
                        album = cursor.getString(albumCol) ?: "Unknown Album",
                        albumArtist = cursor.getString(albumArtistCol) ?: "",
                        duration = cursor.getLong(durationCol),
                        filePath = filePath,
                        fileName = cursor.getString(displayCol) ?: file.name,
                        fileSize = cursor.getLong(sizeCol),
                        mimeType = cursor.getString(mimeCol) ?: "audio/mpeg",
                        bitrate = cursor.getInt(bitrateCol),
                        sampleRate = sampleRate,
                        year = cursor.getInt(yearCol),
                        trackNumber = cursor.getInt(trackCol) % 1000,
                        genre = genre,
                        dateAdded = cursor.getLong(addedCol) * 1000,
                        dateModified = cursor.getLong(modifiedCol) * 1000,
                        albumArtUri = albumArtUri,
                        folderName = file.parentFile?.name ?: "Unknown"
                    )
                )
            }
        }

        try { retriever.release() } catch (_: Exception) {}

        trackDao.deleteAllTracks()
        if (tracks.isNotEmpty()) {
            trackDao.insertTracks(tracks)
        }
        tracks.size
    }

    fun getAllTracks(): Flow<List<Track>> = trackDao.getAllTracks()
    fun getTracksByAlbum(album: String): Flow<List<Track>> = trackDao.getTracksByAlbum(album)
    fun getTracksByArtist(artist: String): Flow<List<Track>> = trackDao.getTracksByArtist(artist)
    fun getTracksByGenre(genre: String): Flow<List<Track>> = trackDao.getTracksByGenre(genre)
    fun getTracksByFolder(folder: String): Flow<List<Track>> = trackDao.getTracksByFolder(folder)
    fun getFavoriteTracks(): Flow<List<Track>> = trackDao.getFavoriteTracks()
    fun getMostPlayed(limit: Int = 50): Flow<List<Track>> = trackDao.getMostPlayed(limit)
    fun getRecentlyPlayed(limit: Int = 50): Flow<List<Track>> = trackDao.getRecentlyPlayed(limit)
    fun getRecentlyAdded(limit: Int = 50): Flow<List<Track>> = trackDao.getRecentlyAdded(limit)
    fun searchTracks(query: String): Flow<List<Track>> = trackDao.searchTracks(query)
    suspend fun getTrackById(id: Long): Track? = trackDao.getTrackById(id)

    suspend fun toggleFavorite(trackId: Long) {
        val track = trackDao.getTrackById(trackId) ?: return
        trackDao.setFavorite(trackId, !track.isFavorite)
    }

    suspend fun incrementPlayCount(trackId: Long) {
        trackDao.incrementPlayCount(trackId)
    }

    fun getAllAlbumNames(): Flow<List<String>> = trackDao.getAllAlbumNames()
    fun getAllArtistNames(): Flow<List<String>> = trackDao.getAllArtistNames()
    fun getAllGenreNames(): Flow<List<String>> = trackDao.getAllGenreNames()
    fun getAllFolderNames(): Flow<List<String>> = trackDao.getAllFolderNames()

    fun getAllPlaylists(): Flow<List<Playlist>> = playlistDao.getAllPlaylists()

    suspend fun createPlaylist(name: String): Long {
        return playlistDao.insertPlaylist(Playlist(name = name))
    }

    suspend fun deletePlaylist(playlist: Playlist) {
        playlistDao.clearPlaylist(playlist.id)
        playlistDao.deletePlaylist(playlist)
    }

    suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        val maxPos = playlistDao.getMaxPosition(playlistId) ?: -1
        playlistDao.insertPlaylistTrack(
            PlaylistTrack(playlistId = playlistId, trackId = trackId, position = maxPos + 1)
        )
    }

    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        playlistDao.removeTrackFromPlaylist(playlistId, trackId)
    }

    fun getPlaylistTrackIds(playlistId: Long): Flow<List<Long>> =
        playlistDao.getPlaylistTrackIds(playlistId)

    fun getPlaylistTracks(playlistId: Long): Flow<List<Track>> {
        return combine(
            playlistDao.getPlaylistTrackIds(playlistId),
            trackDao.getAllTracks()
        ) { ids, allTracks ->
            ids.mapNotNull { id -> allTracks.find { it.id == id } }
        }.flowOn(Dispatchers.IO)
    }
}