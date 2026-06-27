package com.auraplay.player.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.auraplay.player.data.model.Track

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks ORDER BY title COLLATE NOCASE")
    fun getAllTracks(): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE id = :id")
    suspend fun getTrackById(id: Long): Track?

    @Query("SELECT * FROM tracks WHERE isFavorite = 1 ORDER BY title COLLATE NOCASE")
    fun getFavoriteTracks(): Flow<List<Track>>

    @Query("SELECT * FROM tracks ORDER BY playCount DESC")
    fun getMostPlayed(): Flow<List<Track>>

    @Query("SELECT * FROM tracks ORDER BY dateAdded DESC")
    fun getRecentlyAdded(): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE album = :album ORDER BY trackNumber")
    fun getTracksByAlbum(album: String): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE artist = :artist ORDER BY album, trackNumber")
    fun getTracksByArtist(artist: String): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE genre = :genre ORDER BY title COLLATE NOCASE")
    fun getTracksByGenre(genre: String): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE folder = :folder ORDER BY title COLLATE NOCASE")
    fun getTracksByFolder(folder: String): Flow<List<Track>>

    @Query("SELECT DISTINCT album FROM tracks WHERE album != '' ORDER BY album COLLATE NOCASE")
    fun getDistinctAlbums(): Flow<List<String>>

    @Query("SELECT DISTINCT artist FROM tracks WHERE artist != '' ORDER BY artist COLLATE NOCASE")
    fun getDistinctArtists(): Flow<List<String>>

    @Query("SELECT DISTINCT genre FROM tracks WHERE genre != '' ORDER BY genre COLLATE NOCASE")
    fun getDistinctGenres(): Flow<List<String>>

    @Query("SELECT DISTINCT folder FROM tracks ORDER BY folder")
    fun getDistinctFolders(): Flow<List<String>>

    @Query("SELECT * FROM tracks WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' OR album LIKE '%' || :query || '%'")
    fun searchTracks(query: String): Flow<List<Track>>

    @Query("UPDATE tracks SET isFavorite = :favorite WHERE id = :trackId")
    suspend fun setFavorite(trackId: Long, favorite: Boolean)

    @Query("UPDATE tracks SET playCount = playCount + 1 WHERE id = :trackId")
    suspend fun incrementPlayCount(trackId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<Track>)

    @Query("DELETE FROM tracks")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM tracks")
    suspend fun getCount(): Int
}
