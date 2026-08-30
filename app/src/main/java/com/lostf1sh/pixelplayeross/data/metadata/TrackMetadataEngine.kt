package com.lostf1sh.pixelplayeross.data.metadata

import com.lostf1sh.pixelplayeross.data.model.Song
import com.lostf1sh.pixelplayeross.data.musicbrainz.MusicBrainzApiService
import com.lostf1sh.pixelplayeross.data.network.deezer.DeezerApiService
import com.lostf1sh.pixelplayeross.data.network.deezer.DeezerTrack
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Resolves accurate, per-track metadata by querying MusicBrainz (canonical
 * recording/release/artist IDs) and Deezer (durations, 1000×1000 artwork) in
 * parallel, merging the candidates and scoring them against the local track
 * with [MetadataMatcher].
 *
 * Duration is treated as the strongest per-track signal: a candidate that
 * differs by more than a few seconds is a different cut (single edit, live
 * version, remaster) and is demoted accordingly.
 */
@Singleton
class TrackMetadataEngine @Inject constructor(
    baseClient: OkHttpClient,
    private val deezerApiService: DeezerApiService,
    private val musicBrainzApiService: MusicBrainzApiService
) {

    private val client = baseClient.newBuilder().build()

    /** Downloaded artwork bytes with a sniffed MIME type. */
    data class Artwork(val bytes: ByteArray, val mimeType: String)

    /**
     * Resolves ranked matches for [song]. Sources are queried in parallel and
     * a failure in one source does not sink the other; an exception is thrown
     * only when both sources fail.
     */
    suspend fun resolve(song: Song): List<TrackMatch> = coroutineScope {
        val expected = ExpectedTrack(
            title = song.title,
            artist = song.displayArtist,
            album = song.album.takeIf { it.isNotBlank() && !it.equals("Unknown Album", ignoreCase = true) },
            durationMs = song.duration.takeIf { it > 0L }
        )

        val musicBrainzDeferred = async {
            runCatching {
                musicBrainzApiService.searchRecording(
                    title = expected.title,
                    artist = expected.artist,
                    album = expected.album,
                    durationMs = expected.durationMs
                )
            }.getOrElse { emptyList() }
        }
        val deezerDeferred = async { searchDeezer(expected) }

        val musicBrainzMatches = musicBrainzDeferred.await()
        val deezerTracks = deezerDeferred.await()
        if (musicBrainzMatches.isEmpty() && deezerTracks.isEmpty()) {
            throw IOException("No metadata source returned results")
        }

        val candidates = mutableListOf<Pair<ExpectedTrack, TrackMatch>>()

        musicBrainzMatches.forEach { match ->
            candidates += match.toExpected() to TrackMatch(
                source = SOURCE_MUSICBRAINZ,
                title = match.title,
                artist = match.artist,
                album = match.album,
                year = match.year,
                durationMs = match.durationMs,
                albumArtUrl = null,
                genre = null,
                mbRecordingId = match.recordingId,
                mbReleaseId = match.releaseId,
                mbArtistId = match.artistId,
                confidence = 0.0,
                durationVerified = false
            )
        }
        deezerTracks.forEach { track ->
            val artist = track.artist?.name ?: return@forEach
            val durationMs = track.durationSeconds * 1000L
            candidates += ExpectedTrack(
                title = track.title,
                artist = artist,
                album = track.album?.title.orEmpty(),
                durationMs = durationMs
            ) to TrackMatch(
                source = SOURCE_DEEZER,
                title = track.title,
                artist = artist,
                album = track.album?.title.orEmpty(),
                year = 0,
                durationMs = track.durationSeconds * 1000L,
                albumArtUrl = track.album?.bestCoverUrl,
                genre = null,
                mbRecordingId = null,
                mbReleaseId = null,
                mbArtistId = null,
                confidence = 0.0,
                durationVerified = false
            )
        }

        // Score, then merge duplicates across sources (same normalized title
        // + artist + duration bucket): the MusicBrainz entry contributes the
        // canonical IDs, the Deezer twin contributes high-res artwork.
        val scored = candidates.map { (candidate, raw) ->
            raw.copy(
                confidence = MetadataMatcher.score(expected, candidate),
                durationVerified = MetadataMatcher.durationVerified(
                    expected.durationMs,
                    candidate.durationMs ?: raw.durationMs
                )
            )
        }

        val merged = LinkedHashMap<String, TrackMatch>()
        scored.sortedByDescending { it.confidence }.forEach { match ->
            val key = MetadataMatcher.dedupeKey(match.title, match.artist, match.durationMs)
            val existing = merged[key]
            if (existing == null) {
                merged[key] = match
            } else {
                val mbIds = listOfNotNull(existing.mbRecordingId, match.mbRecordingId)
                val base = if (existing.source == SOURCE_MUSICBRAINZ) existing else match
                val other = if (base === existing) match else existing
                merged[key] = base.copy(
                    albumArtUrl = existing.albumArtUrl ?: other.albumArtUrl,
                    mbRecordingId = base.mbRecordingId ?: other.mbRecordingId,
                    mbReleaseId = base.mbReleaseId ?: other.mbReleaseId,
                    mbArtistId = base.mbArtistId ?: other.mbArtistId,
                    year = if (base.year > 0) base.year else other.year
                )
            }
        }

        merged.values
            .sortedByDescending { it.confidence }
            .take(MAX_RESULTS)
    }

    private suspend fun searchDeezer(expected: ExpectedTrack): List<DeezerTrack> {
        val advancedQuery = "artist:\"${sanitize(expected.artist)}\" track:\"${sanitize(expected.title)}\""
        val advanced = runCatching { deezerApiService.searchTrack(advancedQuery, DEEZER_LIMIT) }
            .getOrElse { return searchDeezerFallback(expected) }
        if (advanced.data.isNotEmpty()) return advanced.data
        return searchDeezerFallback(expected)
    }

    private suspend fun searchDeezerFallback(expected: ExpectedTrack): List<DeezerTrack> {
        val plainQuery = "${expected.artist} ${expected.title}".trim()
        if (plainQuery.isBlank()) return emptyList()
        return runCatching { deezerApiService.searchTrack(plainQuery, DEEZER_LIMIT) }
            .getOrNull()
            ?.data
            .orEmpty()
    }

    private fun sanitize(value: String): String = value.replace("\"", ' ').trim()

    private fun com.lostf1sh.pixelplayeross.data.musicbrainz.MusicBrainzMatch.toExpected() =
        ExpectedTrack(
            title = title,
            artist = artist,
            album = album.takeIf { it.isNotBlank() },
            durationMs = durationMs
        )

    /**
     * Downloads artwork from [url] (Deezer CDN) and sniffs the MIME type.
     * Returns null for oversized or non-image payloads.
     */
    suspend fun downloadArtwork(url: String): Artwork? = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@runCatching null
                val bytes = response.body?.bytes() ?: return@runCatching null
                if (bytes.size > MAX_ARTWORK_BYTES) return@runCatching null
                val mimeType = sniffImageMime(bytes) ?: return@runCatching null
                Artwork(bytes, mimeType)
            }
        }.getOrNull()
    }

    private fun sniffImageMime(bytes: ByteArray): String? = when {
        bytes.size >= 3 &&
            (bytes[0].toInt() and 0xFF) == 0xFF &&
            (bytes[1].toInt() and 0xFF) == 0xD8 &&
            (bytes[2].toInt() and 0xFF) == 0xFF -> "image/jpeg"
        bytes.size >= 8 &&
            (bytes[0].toInt() and 0xFF) == 0x89 &&
            bytes[1] == 0x50.toByte() && bytes[2] == 0x4E.toByte() &&
            bytes[3] == 0x47.toByte() -> "image/png"
        bytes.size >= 12 &&
            bytes[0] == 0x52.toByte() && bytes[1] == 0x49.toByte() &&
            bytes[2] == 0x46.toByte() && bytes[3] == 0x46.toByte() &&
            bytes[8] == 0x57.toByte() && bytes[9] == 0x45.toByte() &&
            bytes[10] == 0x42.toByte() && bytes[11] == 0x50.toByte() -> "image/webp"
        else -> null
    }

    companion object {
        private const val SOURCE_MUSICBRAINZ = "MusicBrainz"
        private const val SOURCE_DEEZER = "Deezer"
        private const val DEEZER_LIMIT = 10
        private const val MAX_RESULTS = 8
        private const val MAX_ARTWORK_BYTES = 12 * 1024 * 1024
    }
}
