package com.lostf1sh.pixelplayeross.data.network.deezer

import com.google.gson.annotations.SerializedName

/**
 * Response from Deezer artist search API.
 */
data class DeezerSearchResponse(
    @SerializedName("data") val data: List<DeezerArtist> = emptyList(),
    @SerializedName("total") val total: Int = 0
)

/**
 * Artist data from Deezer API.
 * Contains multiple image sizes for different use cases.
 */
data class DeezerArtist(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("picture") val picture: String? = null,
    @SerializedName("picture_small") val pictureSmall: String? = null,
    @SerializedName("picture_medium") val pictureMedium: String? = null,
    @SerializedName("picture_big") val pictureBig: String? = null,
    @SerializedName("picture_xl") val pictureXl: String? = null,
    @SerializedName("nb_album") val albumCount: Int = 0,
    @SerializedName("nb_fan") val fanCount: Int = 0
)

/**
 * Response from Deezer track search API.
 */
data class DeezerTrackSearchResponse(
    @SerializedName("data") val data: List<DeezerTrack> = emptyList(),
    @SerializedName("total") val total: Int = 0
)

/**
 * Track data from the Deezer API.
 *
 * [duration] is in whole seconds; [album] carries high-resolution cover URLs
 * (`cover_xl` is 1000×1000) which makes Deezer the artwork source for the
 * accurate-metadata engine.
 */
data class DeezerTrack(
    @SerializedName("id") val id: Long = 0L,
    @SerializedName("title") val title: String = "",
    @SerializedName("duration") val durationSeconds: Int = 0,
    @SerializedName("explicit_lyrics") val explicitLyrics: Boolean = false,
    @SerializedName("artist") val artist: DeezerTrackArtist? = null,
    @SerializedName("album") val album: DeezerTrackAlbum? = null
)

data class DeezerTrackArtist(
    @SerializedName("id") val id: Long = 0L,
    @SerializedName("name") val name: String = ""
)

data class DeezerTrackAlbum(
    @SerializedName("id") val id: Long = 0L,
    @SerializedName("title") val title: String = "",
    @SerializedName("cover") val cover: String? = null,
    @SerializedName("cover_big") val coverBig: String? = null,
    @SerializedName("cover_xl") val coverXl: String? = null
) {
    /** Best available artwork URL, preferring the 1000×1000 variant. */
    val bestCoverUrl: String?
        get() = coverXl ?: coverBig ?: cover
}
