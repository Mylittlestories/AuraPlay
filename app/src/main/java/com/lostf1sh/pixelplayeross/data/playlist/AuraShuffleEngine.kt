package com.lostf1sh.pixelplayeross.data.playlist

import com.lostf1sh.pixelplayeross.data.DailyMixManager
import com.lostf1sh.pixelplayeross.data.model.Song
import com.lostf1sh.pixelplayeross.data.repository.MusicRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

/**
 * AuraPlay's intelligent whole-library shuffle.
 *
 * Unlike a plain `ORDER BY RANDOM()`, AuraShuffle weighs every song before rolling the dice:
 *
 *  - **Discovery boost** — never-played songs surface more often, so forgotten corners of the
 *    library get airtime instead of the same 40 hits.
 *  - **Recency penalty** — songs played in the last hours/days are heavily damped; no more
 *    hearing the same track twice in one commute.
 *  - **Favorite boost** — liked songs keep a modest edge.
 *  - **Freshness boost** — recently added files get extra spins.
 *  - **Artist/album spacing** — a repair pass swaps songs so the same artist doesn't play
 *    twice within a short window (no more "artist blocks" that pure randomness produces).
 *
 * Ordering uses weighted sampling without replacement (Efraimidis–Spirakis: each song gets
 * key = u^(1/w), largest key plays first), so *every* song in the library is eventually
 * played — the weights shape the order, they never drop songs. A fresh [SecureRandom] seed
 * per invocation guarantees the sequence is different every single time.
 */
@Singleton
class AuraShuffleEngine @Inject constructor(
    private val musicRepository: MusicRepository,
    private val dailyMixManager: DailyMixManager,
) {

    suspend fun generateQueue(maxSongs: Int = DEFAULT_MAX_QUEUE): List<Song> =
        withContext(Dispatchers.Default) {
            val songs = musicRepository.getAllSongsOnce()
            if (songs.isEmpty()) return@withContext emptyList()

            val stats = try {
                dailyMixManager.getAllEngagementStats()
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Could not load engagement stats; shuffling unweighted")
                emptyMap<String, DailyMixManager.SongEngagementStats>()
            }

            val now = System.currentTimeMillis()
            val random = SecureRandom()

            val ordered = songs
                .map { song ->
                    val weight = weightOf(song, stats[song.id], now)
                    // Efraimidis–Spirakis weighted sampling key (higher = earlier).
                    val key = random.nextDouble().pow(1.0 / weight)
                    song to key
                }
                .sortedByDescending { it.second }
                .map { it.first }

            val spaced = enforceArtistSpacing(ordered)

            Timber.tag(TAG).i(
                "AuraShuffle queue: %d songs (from library of %d), spacing adjustments applied",
                spaced.size, songs.size
            )
            spaced.take(maxSongs)
        }

    private fun weightOf(
        song: Song,
        stats: DailyMixManager.SongEngagementStats?,
        now: Long,
    ): Double {
        var weight = BASE_WEIGHT
        val playCount = stats?.playCount ?: 0

        // Discovery: unplayed songs are the whole point of an intelligent shuffle.
        weight += when {
            playCount == 0 -> DISCOVERY_BOOST
            playCount <= LOW_PLAY_THRESHOLD -> LOW_PLAY_BOOST
            else -> 0.0
        }

        if (song.isFavorite) weight += FAVORITE_BOOST

        // Freshness: recently added files get extra spins.
        if (song.dateAdded > 0 && now - song.dateAdded < FRESH_WINDOW_MS) {
            weight *= FRESH_MULTIPLIER
        }

        // Recency: recently played songs are damped hard so sessions don't repeat themselves.
        val lastPlayed = stats?.lastPlayedTimestamp ?: 0L
        if (lastPlayed > 0) {
            val age = now - lastPlayed
            weight *= when {
                age < RECENT_6H_MS -> 0.05
                age < RECENT_48H_MS -> 0.35
                age < RECENT_14D_MS -> 0.75
                else -> 1.0
            }
        }

        return weight.coerceAtLeast(MIN_WEIGHT)
    }

    /**
     * Single repair pass: if a song's artist appeared within [SPACING_WINDOW] previous picks,
     * swap it with the nearest upcoming song whose artist is not in the recent window.
     * Bounded lookahead keeps this O(n · window) — instant even for 50k-song libraries.
     */
    private fun enforceArtistSpacing(songs: List<Song>): List<Song> {
        if (songs.size < 3) return songs
        val result = songs.toMutableList()
        val recentArtists = ArrayDeque<Long>(SPACING_WINDOW)

        for (i in result.indices) {
            val artist = result[i].artistId
            if (artist in recentArtists) {
                val swapIndex = (i + 1 until minOf(result.size, i + LOOKAHEAD))
                    .firstOrNull { j ->
                        result[j].artistId != artist &&
                            result[j].artistId !in recentArtists &&
                            result[i].artistId != result[j].artistId
                    }
                if (swapIndex != null) {
                    val tmp = result[i]
                    result[i] = result[swapIndex]
                    result[swapIndex] = tmp
                }
            }
            recentArtists.addLast(result[i].artistId)
            if (recentArtists.size > SPACING_WINDOW) recentArtists.removeFirst()
        }
        return result
    }

    companion object {
        private const val TAG = "AuraShuffleEngine"
        private const val DEFAULT_MAX_QUEUE = 10_000

        private const val BASE_WEIGHT = 1.0
        private const val DISCOVERY_BOOST = 1.6
        private const val LOW_PLAY_BOOST = 0.5
        private const val FAVORITE_BOOST = 0.6
        private const val LOW_PLAY_THRESHOLD = 2
        private const val MIN_WEIGHT = 0.05

        private const val FRESH_MULTIPLIER = 1.4
        private const val FRESH_WINDOW_MS = 14L * 24 * 60 * 60 * 1000

        private const val RECENT_6H_MS = 6L * 60 * 60 * 1000
        private const val RECENT_48H_MS = 48L * 60 * 60 * 1000
        private const val RECENT_14D_MS = 14L * 24 * 60 * 60 * 1000

        private const val SPACING_WINDOW = 4
        private const val LOOKAHEAD = 40
    }
}
