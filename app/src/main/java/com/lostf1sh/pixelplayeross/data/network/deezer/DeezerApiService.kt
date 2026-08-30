package com.lostf1sh.pixelplayeross.data.network.deezer

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for Deezer API.
 * Used primarily for fetching artist artwork.
 */
interface DeezerApiService {

    /**
     * Search for an artist by name.
     * @param query Artist name to search for
     * @param limit Maximum number of results to return
     * @return Search response containing list of matching artists
     */
    @GET("search/artist")
    suspend fun searchArtist(
        @Query("q") query: String,
        @Query("limit") limit: Int = 1
    ): DeezerSearchResponse

    /**
     * Search for a track. Used by the accurate-metadata engine: Deezer track
     * entries carry exact durations and 1000×1000 album artwork.
     *
     * The query uses Deezer's advanced syntax (`artist:"..." track:"..."`);
     * callers should fall back to a plain free-text query when this returns
     * no results.
     */
    @GET("search/track")
    suspend fun searchTrack(
        @Query("q") query: String,
        @Query("limit") limit: Int = 10
    ): DeezerTrackSearchResponse
}
