package com.lostf1sh.pixelplayeross.data.metadata

import com.lostf1sh.pixelplayeross.BuildConfig
import com.lostf1sh.pixelplayeross.data.model.Song
import com.lostf1sh.pixelplayeross.data.musicbrainz.MusicBrainzApiService
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

/**
 * Resolves accurate, per-track metadata by querying three independent,
 * key-less sources in parallel:
 *
 *  * **MusicBrainz** — canonical recording/release/artist IDs
 *    (`MusicBrainzApiService`, org.json parsing)
 *  * **Deezer** — exact durations and 1000×1000 album artwork (raw org.json)
 *  * **iTunes Search** — durations, artwork, genre and year (raw org.json)
 *
 * Candidates are merged (MusicBrainz IDs + streaming-service artwork) and
 * scored against the local track with [MetadataMatcher]. Duration is the
 * strongest per-track signal: a candidate more than a few seconds off is a
 * different cut (single edit, live version, remaster) and is demoted.
 *
 * Responses are parsed with org.json instead of Gson/Retrofit so the release
 * build's R8 pass can never interfere with the models, and each source fails
 * independently: [ResolveOutcome.failedSources] reports which services could
 * not be reached so the UI can say "no match" vs "network problem" — two very
 * different situations the previous version conflated.
 */
@Singleton
class TrackMetadataEngine @Inject constructor(
    baseClient: OkHttpClient,
    private val musicBrainzApiService: MusicBrainzApiService
) {

    private val client = baseClient.newBuilder()
        .addInterceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .header(
                        "User-Agent",
                        "AuraPlay/${BuildConfig.VERSION_NAME} " +
                            "(https://github.com/Mylittlestories/AuraPlay)"
                    )
                    .header("Accept", "application/json")
                    .build()
            )
        }
        .build()

    /** Downloaded artwork bytes with a sniffed MIME type. */
    data class Artwork(val bytes: ByteArray, val mimeType: String)

    /** Ranked matches plus the sources that could not be reached. */
    data class ResolveOutcome(
        val matches: List<TrackMatch>,
        val failedSources: List<String>
    ) {
        val allSourcesFailed: Boolean get() = failedSources.size == SOURCE_COUNT
    }

    /** A candidate before scoring, normalized across sources. */
    private data class RawCandidate(
        val source: String,
        val title: String,
        val artist: String,
        val album: String,
        val year: Int,
        val durationMs: Long?,
        val albumArtUrl: String?,
        val genre: String?,
        val mbRecordingId: String?,
        val mbReleaseId: String?,
        val mbArtistId: String?
    )

    /**
     * Resolves ranked matches for [song]. A failure in one source does not
     * sink the others; only when *all* sources fail does the outcome carry no
     * matches at all.
     */
    suspend fun resolve(song: Song): ResolveOutcome = coroutineScope {
        val expected = ExpectedTrack(
            title = song.title,
            artist = song.displayArtist,
            album = song.album.takeIf { it.isNotBlank() && !it.equals("Unknown Album", ignoreCase = true) },
            durationMs = song.duration.takeIf { it > 0L }
        )

        val musicBrainz = async {
            runCatching { searchMusicBrainzWithRetry(expected) }
                .getOrElse { null to SOURCE_MUSICBRAINZ }
        }
        val deezer = async {
            runCatching { searchDeezer(expected) }
                .getOrElse { null to SOURCE_DEEZER }
        }
        val itunes = async {
            runCatching { searchItunes(expected) }
                .getOrElse { null to SOURCE_ITUNES }
        }

        val (mbMatches, mbFailed) = musicBrainz.await()
        val (deezerCandidates, deezerFailed) = deezer.await()
        val (itunesCandidates, itunesFailed) = itunes.await()

        val failedSources = listOfNotNull(mbFailed, deezerFailed, itunesFailed)

        val candidates = buildList {
            mbMatches?.forEach { match ->
                add(
                    RawCandidate(
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
                        mbArtistId = match.artistId
                    )
                )
            }
            addAll(deezerCandidates.orEmpty())
            addAll(itunesCandidates.orEmpty())
        }

        // Score every candidate against the local track.
        val scored = candidates.map { raw ->
            TrackMatch(
                source = raw.source,
                title = raw.title,
                artist = raw.artist,
                album = raw.album,
                year = raw.year,
                durationMs = raw.durationMs,
                albumArtUrl = raw.albumArtUrl,
                genre = raw.genre,
                mbRecordingId = raw.mbRecordingId,
                mbReleaseId = raw.mbReleaseId,
                mbArtistId = raw.mbArtistId,
                confidence = MetadataMatcher.score(
                    expected,
                    ExpectedTrack(
                        title = raw.title,
                        artist = raw.artist,
                        album = raw.album.takeIf { it.isNotBlank() },
                        durationMs = raw.durationMs
                    )
                ),
                durationVerified = MetadataMatcher.durationVerified(expected.durationMs, raw.durationMs)
            )
        }

        // Merge duplicates across sources (same normalized title + artist +
        // duration bucket): the MusicBrainz entry contributes canonical IDs,
        // the streaming twins contribute high-res artwork and genre.
        val merged = LinkedHashMap<String, TrackMatch>()
        scored.sortedByDescending { it.confidence }.forEach { match ->
            val key = MetadataMatcher.dedupeKey(match.title, match.artist, match.durationMs)
            val existing = merged[key]
            if (existing == null) {
                merged[key] = match
            } else {
                val base = if (existing.source == SOURCE_MUSICBRAINZ) existing else match
                val other = if (base === existing) match else existing
                merged[key] = base.copy(
                    albumArtUrl = existing.albumArtUrl ?: other.albumArtUrl,
                    genre = base.genre ?: other.genre,
                    mbRecordingId = base.mbRecordingId ?: other.mbRecordingId,
                    mbReleaseId = base.mbReleaseId ?: other.mbReleaseId,
                    mbArtistId = base.mbArtistId ?: other.mbArtistId,
                    year = if (base.year > 0) base.year else other.year,
                    durationMs = base.durationMs ?: other.durationMs
                )
            }
        }

        ResolveOutcome(
            matches = merged.values.sortedByDescending { it.confidence }.take(MAX_RESULTS),
            failedSources = failedSources
        )
    }

    // ------------------------------------------------------------- sources

    /**
     * MusicBrainz rate-limits anonymous clients (1 req/s). On a 503/timeout a
     * single retry after a pause recovers most bursts.
     */
    private suspend fun searchMusicBrainzWithRetry(
        expected: ExpectedTrack
    ): Pair<List<com.lostf1sh.pixelplayeross.data.musicbrainz.MusicBrainzMatch>, String?> {
        return try {
            musicBrainzApiService.searchRecording(
                title = expected.title,
                artist = expected.artist,
                album = expected.album,
                durationMs = expected.durationMs
            ) to null
        } catch (first: Exception) {
            if (first is kotlinx.coroutines.CancellationException) throw first
            delay(MUSICBRAINZ_RETRY_DELAY_MS)
            musicBrainzApiService.searchRecording(
                title = expected.title,
                artist = expected.artist,
                album = expected.album,
                durationMs = expected.durationMs
            ) to null
        }
    }

    /** Deezer track search (plain free-text query; the advanced `track:""` syntax returns nothing). */
    private suspend fun searchDeezer(expected: ExpectedTrack): Pair<List<RawCandidate>, String?> {
        val query = "${expected.artist} ${expected.title}".trim()
        if (query.isBlank()) return emptyList<RawCandidate>() to null

        val url = DEEZER_SEARCH_URL.toHttpUrl().newBuilder()
            .addQueryParameter("q", query)
            .addQueryParameter("limit", PER_SOURCE_LIMIT.toString())
            .build()
        val body = executeForJsonBody(url)

        val candidates = mutableListOf<RawCandidate>()
        val data = body.optJSONArray("data") ?: JSONArray()
        for (index in 0 until data.length()) {
            val track = data.optJSONObject(index) ?: continue
            val title = track.optString("title").takeIf { it.isNotBlank() } ?: continue
            val artist = track.optJSONObject("artist")?.optString("name")
                ?.takeIf { it.isNotBlank() } ?: continue
            val album = track.optJSONObject("album")
            val durationSeconds = track.optInt("duration", 0)
            candidates += RawCandidate(
                source = SOURCE_DEEZER,
                title = title,
                artist = artist,
                album = album?.optString("title").orEmpty(),
                year = 0,
                durationMs = durationSeconds.takeIf { it > 0 }?.let { it * 1000L },
                albumArtUrl = album?.let { obj ->
                    obj.optString("cover_xl").ifBlank {
                        obj.optString("cover_big").ifBlank { obj.optString("cover") }
                    }
                }?.takeIf { it.isNotBlank() },
                genre = null,
                mbRecordingId = null,
                mbReleaseId = null,
                mbArtistId = null
            )
        }
        return candidates to null
    }

    /**
     * iTunes Search API — extremely generous, key-less, and a completely
     * independent CDN from the other two sources. Artwork is upgraded from
     * 100×100 to 1000×1000 by swapping the URL suffix.
     */
    private suspend fun searchItunes(expected: ExpectedTrack): Pair<List<RawCandidate>, String?> {
        val term = "${expected.artist} ${expected.title}".trim()
        if (term.isBlank()) return emptyList<RawCandidate>() to null

        val url = ITUNES_SEARCH_URL.toHttpUrl().newBuilder()
            .addQueryParameter("term", term)
            .addQueryParameter("entity", "song")
            .addQueryParameter("limit", PER_SOURCE_LIMIT.toString())
            .build()
        val body = executeForJsonBody(url)

        val candidates = mutableListOf<RawCandidate>()
        val results = body.optJSONArray("results") ?: JSONArray()
        for (index in 0 until results.length()) {
            val track = results.optJSONObject(index) ?: continue
            val title = track.optString("trackName").takeIf { it.isNotBlank() } ?: continue
            val artist = track.optString("artistName").takeIf { it.isNotBlank() } ?: continue
            val durationMs = track.optLong("trackTimeMillis", 0L).takeIf { it > 0L }
            val releaseYear = track.optString("releaseDate").take(4).toIntOrNull() ?: 0
            candidates += RawCandidate(
                source = SOURCE_ITUNES,
                title = title,
                artist = artist,
                album = track.optString("collectionName").orEmpty(),
                year = releaseYear,
                durationMs = durationMs,
                albumArtUrl = track.optString("artworkUrl100")
                    .replace("/100x100bb.jpg", "/1000x1000bb.jpg")
                    .takeIf { it.isNotBlank() },
                genre = track.optString("primaryGenreName").takeIf { it.isNotBlank() },
                mbRecordingId = null,
                mbReleaseId = null,
                mbArtistId = null
            )
        }
        return candidates to null
    }

    private suspend fun executeForJsonBody(url: okhttp3.HttpUrl): JSONObject =
        withContext(Dispatchers.IO) {
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("HTTP ${response.code} from ${url.host}")
                }
                JSONObject(response.body!!.string())
            }
        }

    // ------------------------------------------------------------- artwork

    /**
     * Downloads artwork from [url] (Deezer/Apple CDN) and sniffs the MIME
     * type. Returns null for oversized or non-image payloads.
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
        private const val SOURCE_ITUNES = "iTunes"
        private const val SOURCE_COUNT = 3
        private const val DEEZER_SEARCH_URL = "https://api.deezer.com/search/track"
        private const val ITUNES_SEARCH_URL = "https://itunes.apple.com/search"
        private const val PER_SOURCE_LIMIT = 10
        private const val MAX_RESULTS = 8
        private const val MUSICBRAINZ_RETRY_DELAY_MS = 1_400L
        private const val MAX_ARTWORK_BYTES = 12 * 1024 * 1024
    }
}
