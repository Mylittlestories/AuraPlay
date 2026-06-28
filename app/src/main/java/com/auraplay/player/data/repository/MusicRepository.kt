package com.auraplay.player.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import com.auraplay.player.data.local.TrackDao
import com.auraplay.player.data.local.PlaylistDao
import com.auraplay.player.data.model.Track
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepository @Inject constructor(
    private val trackDao: TrackDao,
    private val playlistDao: PlaylistDao,
    private val contentResolver: ContentResolver
) {
    fun getAllTracks(): Flow<List<Track>> = trackDao.getAllTracks()
    fun getFavoriteTracks(): Flow<List<Track>> = trackDao.getFavoriteTracks()
    fun getMostPlayed(): Flow<List<Track>> = trackDao.getMostPlayed()
    fun getRecentlyAdded(): Flow<List<Track>> = trackDao.getRecentlyAdded()
    fun getDistinctAlbums(): Flow<List<String>> = trackDao.getDistinctAlbums()
    fun getDistinctArtists(): Flow<List<String>> = trackDao.getDistinctArtists()
    fun getDistinctGenres(): Flow<List<String>> = trackDao.getDistinctGenres()
    fun getDistinctFolders(): Flow<List<String>> = trackDao.getDistinctFolders()
    fun searchTracks(query: String): Flow<List<Track>> = trackDao.searchTracks(query)
    fun getTracksByAlbum(album: String): Flow<List<Track>> = trackDao.getTracksByAlbum(album)
    fun getTracksByArtist(artist: String): Flow<List<Track>> = trackDao.getTracksByArtist(artist)
    fun getTracksByGenre(genre: String): Flow<List<Track>> = trackDao.getTracksByGenre(genre)
    fun getTracksByFolder(folder: String): Flow<List<Track>> = trackDao.getTracksByFolder(folder)
    fun getAllPlaylists() = playlistDao.getAllPlaylists()
    fun getPlaylistTrackIds(playlistId: Long) = playlistDao.getPlaylistTrackIds(playlistId)

    suspend fun setFavorite(trackId: Long, favorite: Boolean) = trackDao.setFavorite(trackId, favorite)
    suspend fun incrementPlayCount(trackId: Long) = trackDao.incrementPlayCount(trackId)
    suspend fun getTrackById(id: Long) = trackDao.getTrackById(id)

    suspend fun scanAndStoreMusic(): Int {
        val tracks = scanMediaStore()
        trackDao.deleteAll()
        trackDao.insertTracks(tracks)
        return tracks.size
    }

    private fun scanMediaStore(): List<Track> {
        val genreMap = buildGenreMap()
        val tracks = mutableListOf<Track>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DATE_ADDED
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE"

        contentResolver.query(collection, projection, selection, null, sortOrder)?.use { cursor ->
            val idCol = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
            val durationCol = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
            val dataCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val yearCol = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR)
            val trackCol = cursor.getColumnIndex(MediaStore.Audio.Media.TRACK)
            val dateAddedCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)

            if (idCol < 0 || titleCol < 0 || dataCol < 0 || durationCol < 0) return@use

            while (cursor.moveToNext()) {
                val data = cursor.getString(dataCol) ?: continue
                val duration = cursor.getLong(durationCol)
                if (duration < 5000) continue // skip <5s files

                val filePath = data
                val folder = filePath.substringBeforeLast("/")
                val trackId = cursor.getLong(idCol)
                val fileMetadata = readFileMetadata(trackId, filePath)

                val mediaStoreTitle = if (titleCol >= 0) cursor.getString(titleCol) else null
                val mediaStoreArtist = if (artistCol >= 0) cursor.getString(artistCol) else null
                val mediaStoreAlbum = if (albumCol >= 0) cursor.getString(albumCol) else null
                val mediaStoreYear = if (yearCol >= 0) cursor.getInt(yearCol) else 0
                val mediaStoreTrack = if (trackCol >= 0) cursor.getInt(trackCol) % 1000 else 0

                tracks.add(
                    Track(
                        id = trackId,
                        title = bestText(mediaStoreTitle, fileMetadata.title, filePath.substringAfterLast('/').substringBeforeLast('.'), "Unknown"),
                        artist = bestText(mediaStoreArtist, fileMetadata.artist, null, "Unknown Artist"),
                        album = bestText(mediaStoreAlbum, fileMetadata.album, null, "Unknown Album"),
                        albumId = if (albumIdCol >= 0) cursor.getLong(albumIdCol) else 0L,
                        genre = bestText(genreMap[trackId], fileMetadata.genre, null, ""),
                        duration = fileMetadata.duration.takeIf { it > 0 } ?: duration,
                        data = filePath,
                        folder = folder,
                        year = mediaStoreYear.takeIf { it > 0 } ?: fileMetadata.year,
                        trackNumber = mediaStoreTrack.takeIf { it > 0 } ?: fileMetadata.trackNumber,
                        dateAdded = if (dateAddedCol >= 0) cursor.getLong(dateAddedCol) * 1000 else 0L
                    )
                )
            }
        }

        return tracks
    }

    private data class FileMetadata(
        val title: String? = null,
        val artist: String? = null,
        val album: String? = null,
        val genre: String? = null,
        val year: Int = 0,
        val trackNumber: Int = 0,
        val duration: Long = 0L
    )

    private fun readFileMetadata(trackId: Long, filePath: String): FileMetadata {
        return try {
            val retriever = MediaMetadataRetriever()
            try {
                val audioUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, trackId)
                val descriptor = contentResolver.openFileDescriptor(audioUri, "r")
                if (descriptor != null) {
                    descriptor.use { retriever.setDataSource(it.fileDescriptor) }
                } else {
                    retriever.setDataSource(filePath)
                }
                FileMetadata(
                    title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                    artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                        ?: retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST),
                    album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM),
                    genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE),
                    year = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)?.toIntOrNull() ?: 0,
                    trackNumber = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)
                        ?.substringBefore('/')?.toIntOrNull() ?: 0,
                    duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                )
            } finally {
                retriever.release()
            }
        } catch (_: Exception) {
            FileMetadata()
        }
    }

    private fun bestText(primary: String?, fallback: String?, finalFallback: String?, unknown: String): String {
        fun usable(value: String?): Boolean {
            if (value.isNullOrBlank()) return false
            val normalized = value.trim().lowercase()
            return normalized != "unknown" && normalized != "<unknown>" && normalized != "unknown artist" && normalized != "unknown album"
        }
        return when {
            usable(primary) -> primary!!.trim()
            usable(fallback) -> fallback!!.trim()
            usable(finalFallback) -> finalFallback!!.trim()
            else -> unknown
        }
    }

    private fun buildGenreMap(): Map<Long, String> {
        val genreMap = mutableMapOf<Long, String>()
        try {
            val genreCollection = MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI
            contentResolver.query(
                genreCollection,
                arrayOf(MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME),
                null, null, null
            )?.use { genreCursor ->
                val idCol = genreCursor.getColumnIndex(MediaStore.Audio.Genres._ID)
                val nameCol = genreCursor.getColumnIndex(MediaStore.Audio.Genres.NAME)
                if (idCol < 0 || nameCol < 0) return@use

                while (genreCursor.moveToNext()) {
                    val genreId = genreCursor.getLong(idCol)
                    val genreName = genreCursor.getString(nameCol) ?: continue
                    if (genreName.isBlank()) continue

                    val membersUri = MediaStore.Audio.Genres.Members.getContentUri("external", genreId)
                    contentResolver.query(
                        membersUri,
                        arrayOf(MediaStore.Audio.Genres.Members.AUDIO_ID),
                        null, null, null
                    )?.use { memberCursor ->
                        val audioIdCol = memberCursor.getColumnIndex(
                            MediaStore.Audio.Genres.Members.AUDIO_ID
                        )
                        if (audioIdCol < 0) return@use
                        while (memberCursor.moveToNext()) {
                            genreMap[memberCursor.getLong(audioIdCol)] = genreName
                        }
                    }
                }
            }
        } catch (_: Exception) { }
        return genreMap
    }

    companion object {
        fun getAlbumArtUri(albumId: Long): Uri {
            return ContentUris.withAppendedId(
                Uri.parse("content://media/external/audio/albumart"), albumId
            )
        }
    }
}
