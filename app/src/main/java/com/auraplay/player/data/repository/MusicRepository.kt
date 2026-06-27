package com.auraplay.player.data.repository

import android.content.ContentResolver
import android.content.ContentUris
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
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val yearCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val trackCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

            while (cursor.moveToNext()) {
                val data = cursor.getString(dataCol) ?: continue
                val duration = cursor.getLong(durationCol)
                if (duration < 5000) continue // skip <5s files

                val id = cursor.getLong(idCol)
                val filePath = data
                val folder = filePath.substringBeforeLast("/")
                val genre = genreMap[id] ?: ""

                tracks.add(
                    Track(
                        id = id,
                        title = cursor.getString(titleCol) ?: "Unknown",
                        artist = cursor.getString(artistCol) ?: "Unknown Artist",
                        album = cursor.getString(albumCol) ?: "Unknown Album",
                        albumId = cursor.getLong(albumIdCol),
                        genre = genre,
                        duration = duration,
                        data = filePath,
                        folder = folder,
                        year = try { cursor.getInt(yearCol) } catch (_: Exception) { 0 },
                        trackNumber = try { cursor.getInt(trackCol) % 1000 } catch (_: Exception) { 0 },
                        dateAdded = cursor.getLong(dateAddedCol) * 1000
                    )
                )
            }
        }

        return tracks
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
                val idCol = genreCursor.getColumnIndexOrThrow(MediaStore.Audio.Genres._ID)
                val nameCol = genreCursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME)

                while (genreCursor.moveToNext()) {
                    val genreId = genreCursor.getLong(idCol)
                    val genreName = genreCursor.getString(nameCol) ?: continue
                    if (genreName.isBlank()) continue

                    val membersUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Genres.Members.getContentUri("external"), genreId
                    )
                    contentResolver.query(
                        membersUri,
                        arrayOf(MediaStore.Audio.Genres.Members.AUDIO_ID),
                        null, null, null
                    )?.use { memberCursor ->
                        val audioIdCol = memberCursor.getColumnIndexOrThrow(
                            MediaStore.Audio.Genres.Members.AUDIO_ID
                        )
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
