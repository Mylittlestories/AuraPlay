package com.auraplay.player.data.local
import androidx.room.*
import com.auraplay.player.data.model.*
import kotlinx.coroutines.flow.Flow

@Database(entities = [Track::class, Playlist::class, PlaylistTrack::class], version = 1, exportSchema = false)
abstract class AppDb : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun playlistDao(): PlaylistDao
}

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks ORDER BY title ASC") fun getAll(): Flow<List<Track>>
    @Query("SELECT * FROM tracks WHERE album=:a ORDER BY trackNumber") fun byAlbum(a: String): Flow<List<Track>>
    @Query("SELECT * FROM tracks WHERE artist=:a ORDER BY album,trackNumber") fun byArtist(a: String): Flow<List<Track>>
    @Query("SELECT * FROM tracks WHERE genre=:g") fun byGenre(g: String): Flow<List<Track>>
    @Query("SELECT * FROM tracks WHERE folderName=:f") fun byFolder(f: String): Flow<List<Track>>
    @Query("SELECT * FROM tracks WHERE isFavorite=1") fun favorites(): Flow<List<Track>>
    @Query("SELECT * FROM tracks ORDER BY playCount DESC LIMIT 50") fun mostPlayed(): Flow<List<Track>>
    @Query("SELECT * FROM tracks ORDER BY lastPlayed DESC LIMIT 50") fun recentlyPlayed(): Flow<List<Track>>
    @Query("SELECT * FROM tracks ORDER BY dateAdded DESC LIMIT 50") fun recentlyAdded(): Flow<List<Track>>
    @Query("SELECT * FROM tracks WHERE title LIKE '%'||:q||'%' OR artist LIKE '%'||:q||'%' OR album LIKE '%'||:q||'%'") fun search(q: String): Flow<List<Track>>
    @Query("SELECT DISTINCT album FROM tracks ORDER BY album") fun albumNames(): Flow<List<String>>
    @Query("SELECT DISTINCT artist FROM tracks WHERE artist!='' AND artist!='<unknown>' ORDER BY artist") fun artistNames(): Flow<List<String>>
    @Query("SELECT DISTINCT genre FROM tracks WHERE genre!='' ORDER BY genre") fun genreNames(): Flow<List<String>>
    @Query("SELECT DISTINCT folderName FROM tracks ORDER BY folderName") fun folderNames(): Flow<List<String>>
    @Query("SELECT * FROM tracks WHERE id=:id") suspend fun byId(id: Long): Track?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(tracks: List<Track>)
    @Update suspend fun update(track: Track)
    @Query("UPDATE tracks SET playCount=playCount+1, lastPlayed=:t WHERE id=:id") suspend fun incPlay(id: Long, t: Long = System.currentTimeMillis())
    @Query("UPDATE tracks SET isFavorite=:f WHERE id=:id") suspend fun setFav(id: Long, f: Boolean)
    @Query("DELETE FROM tracks") suspend fun clear()
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY name") fun getAll(): Flow<List<Playlist>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(p: Playlist): Long
    @Delete suspend fun delete(p: Playlist)
    @Query("SELECT trackId FROM playlist_tracks WHERE playlistId=:pid ORDER BY position") fun trackIds(pid: Long): Flow<List<Long>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertTrack(pt: PlaylistTrack)
    @Query("DELETE FROM playlist_tracks WHERE playlistId=:pid AND trackId=:tid") suspend fun removeTrack(pid: Long, tid: Long)
    @Query("DELETE FROM playlist_tracks WHERE playlistId=:pid") suspend fun clearTracks(pid: Long)
    @Query("SELECT MAX(position) FROM playlist_tracks WHERE playlistId=:pid") suspend fun maxPos(pid: Long): Int?
}
