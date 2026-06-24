package com.auraplay.player.data.repository

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
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
    // ==================== Media Scanning ====================

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
            MediaStore.Audio.Media.SAMPLE_RATE,
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
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumArtistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ARTIST)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val bitrateColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.BITRATE)
            val sampleRateColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SAMPLE_RATE)
            val yearColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val trackColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val filePath = cursor.getString(dataColumn) ?: continue
                val file = File(filePath)

                if (!file.exists()) continue

                // Get additional metadata
                var genre = ""
                var discNumber = 0
                var channels = 0
                try {
                    retriever.setDataSource(filePath)
                    genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE) ?: ""
                    val disc = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER)
                    discNumber = disc?.split("/")?.firstOrNull()?.toIntOrNull() ?: 0
                    val channelCount = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS)
                    channels = channelCount?.toIntOrNull() ?: 0
                } catch (_: Exception) {}

                val albumArtUri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"),
                    id
                ).toString()

                val track = Track(
                    id = id,
                    title = cursor.getString(titleColumn) ?: file.nameWithoutExtension,
                    artist = cursor.getString(artistColumn) ?: "Unknown Artist",
                    album = cursor.getString(albumColumn) ?: "Unknown Album",
                    albumArtist = cursor.getString(albumArtistColumn) ?: "",
                    duration = cursor.getLong(durationColumn),
                    filePath = filePath,
                    fileName = cursor.getString(displayNameColumn) ?: file.name,
                    fileSize = cursor.getLong(sizeColumn),
                    mimeType = cursor.getString(mimeTypeColumn) ?: "audio/mpeg",
                    bitrate = cursor.getInt(bitrateColumn),
                    sampleRate = cursor.getInt(sampleRateColumn),
                    channels = channels,
                    year = cursor.getInt(yearColumn),
                    trackNumber = cursor.getInt(trackColumn) % 1000,
                    discNumber = discNumber,
                    genre = genre,
                    dateAdded = cursor.getLong(dateAddedColumn) * 1000,
                    dateModified = cursor.getLong(dateModifiedColumn) * 1000,
                    albumArtUri = albumArtUri,
                    folderName = file.parentFile?.name ?: "Unknown"
                )
                tracks.add(track)
            }
        }

        try {
            retriever.release()
        } catch (_: Exception) {}

        // Clear old data and insert new
        trackDao.deleteAllTracks()
        if (tracks.isNotEmpty()) {
            trackDao.insertTracks(tracks)
        }

        tracks.size
    }

    // ==================== Track Queries ====================

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

    // ==================== Album/Artist/Genre/Folder Queries ====================

    fun getAllAlbumNames(): Flow<List<String>> = trackDao.getAllAlbumNames()

    fun getAllArtistNames(): Flow<List<String>> = trackDao.getAllArtistNames()

    fun getAllGenreNames(): Flow<List<String>> = trackDao.getAllGenreNames()

    fun getAllFolderNames(): Flow<List<String>> = trackDao.getAllFolderNames()

    suspend fun getAlbums(): List<Album> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<Track>()
        trackDao.getAllTracks().collect { allTracks ->
            tracks.clear()
            tracks.addAll(allTracks)
        }
        tracks.groupBy { it.album }.map { (albumName, albumTracks) ->
            Album(
                id = albumTracks.firstOrNull()?.id ?: 0,
                title = albumName,
                artist = albumTracks.firstOrNull()?.albumArtist?.takeIf { it.isNotEmpty() }
                    ?: albumTracks.firstOrNull()?.artist ?: "Unknown",
                year = albumTracks.firstOrNull()?.year ?: 0,
                trackCount = albumTracks.size,
                artUri = albumTracks.firstOrNull()?.albumArtUri
            )
        }.sortedBy { it.title }
    }

    suspend fun getArtists(): List<Artist> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<Track>()
        trackDao.getAllTracks().collect { allTracks ->
            tracks.clear()
            tracks.addAll(allTracks)
        }
        tracks.groupBy { it.artist }.map { (artistName, artistTracks) ->
            Artist(
                name = artistName,
                albumCount = artistTracks.map { it.album }.distinct().size,
                trackCount = artistTracks.size,
                artUri = artistTracks.firstOrNull()?.albumArtUri
            )
        }.sortedBy { it.name }
    }

    suspend fun getFolders(): List<Folder> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<Track>()
        trackDao.getAllTracks().collect { allTracks ->
            tracks.clear()
            tracks.addAll(allTracks)
        }
        tracks.groupBy { it.folderName }.map { (folderName, folderTracks) ->
            Folder(
                path = folderTracks.firstOrNull()?.filePath?.substringBeforeLast("/") ?: "",
                name = folderName,
                trackCount = folderTracks.size,
                totalSize = folderTracks.sumOf { it.fileSize }
            )
        }.sortedBy { it.name }
    }

    suspend fun getGenres(): List<Genre> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<Track>()
        trackDao.getAllTracks().collect { allTracks ->
            tracks.clear()
            tracks.addAll(allTracks)
        }
        tracks.filter { it.genre.isNotEmpty() }.groupBy { it.genre }.map { (genreName, genreTracks) ->
            Genre(
                name = genreName,
                trackCount = genreTracks.size
            )
        }.sortedBy { it.name }
    }

    // ==================== Playlist Operations ====================

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

    suspend fun addToFavorites(trackId: Long) {
        trackDao.setFavorite(trackId, true)
    }

    suspend fun removeFromFavorites(trackId: Long) {
        trackDao.setFavorite(trackId, false)
    }
}

private fun android.net.Uri.Companion.parse(s: String): android.net.Uri {
    return android.net.Uri.parse(s)
}