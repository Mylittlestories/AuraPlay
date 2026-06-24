package com.auraplay.player.audio

import com.auraplay.player.data.model.Track
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.random.Random

/**
 * Advanced Shuffle Manager with multiple shuffle algorithms
 * Prevents repetition and ensures variety
 */
@Singleton
class ShuffleManager @Inject constructor() {

    private var shuffleHistory = mutableListOf<Int>()
    private var shuffleIndex = -1
    private var currentQueue = listOf<Track>()
    private var shuffleMode = ShuffleMode.SMART
    private val recentlyPlayed = ArrayDeque<Long>(20) // Last 20 tracks

    enum class ShuffleMode {
        OFF,           // No shuffle
        SMART,         // Smart shuffle with no repetition and variety
        TRUE_RANDOM,   // Pure random (may repeat)
        ARTIST_MIX,    // Mix by different artists
        ALBUM_MIX,     // Mix from different albums
        GENRE_MIX,     // Mix by genre variety
        RATING_WEIGHTED, // Weighted by play count
        WEIGHTED       // Weighted towards less-played tracks
    }

    fun setQueue(tracks: List<Track>) {
        currentQueue = tracks
        shuffleHistory.clear()
        shuffleIndex = -1
    }

    fun setShuffleMode(mode: ShuffleMode) {
        shuffleMode = mode
        shuffleHistory.clear()
        shuffleIndex = -1
    }

    fun getShuffleMode(): ShuffleMode = shuffleMode

    fun isShuffleEnabled(): Boolean = shuffleMode != ShuffleMode.OFF

    /**
     * Get next shuffled index with smart algorithm
     */
    fun getNextShuffledIndex(currentIndex: Int): Int {
        if (currentQueue.isEmpty()) return 0

        return when (shuffleMode) {
            ShuffleMode.OFF -> (currentIndex + 1) % currentQueue.size
            ShuffleMode.SMART -> smartShuffle(currentIndex)
            ShuffleMode.TRUE_RANDOM -> Random.nextInt(currentQueue.size)
            ShuffleMode.ARTIST_MIX -> artistMixShuffle(currentIndex)
            ShuffleMode.ALBUM_MIX -> albumMixShuffle(currentIndex)
            ShuffleMode.GENRE_MIX -> genreMixShuffle(currentIndex)
            ShuffleMode.RATING_WEIGHTED -> ratingWeightedShuffle()
            ShuffleMode.WEIGHTED -> weightedShuffle()
        }
    }

    /**
     * Get previous shuffled index
     */
    fun getPreviousShuffledIndex(currentIndex: Int): Int {
        if (currentQueue.isEmpty()) return 0
        if (shuffleMode == ShuffleMode.OFF) {
            return if (currentIndex > 0) currentIndex - 1 else currentQueue.size - 1
        }

        return if (shuffleIndex > 0) {
            shuffleIndex--
            shuffleHistory[shuffleIndex]
        } else {
            currentIndex
        }
    }

    /**
     * Smart Shuffle: No consecutive same artists/albums, minimizes repetition
     */
    private fun smartShuffle(currentIndex: Int): Int {
        if (currentQueue.size <= 1) return 0

        val currentTrack = currentQueue[currentIndex]
        val candidates = currentQueue.indices.filter { i ->
            i != currentIndex &&
            currentQueue[i].id !in recentlyPlayed &&
            currentQueue[i].artist != currentTrack.artist
        }.ifEmpty {
            // If no candidates without same artist, allow same artist but not recent
            currentQueue.indices.filter { i ->
                i != currentIndex && currentQueue[i].id !in recentlyPlayed
            }.ifEmpty {
                // Last resort: any different track
                currentQueue.indices.filter { it != currentIndex }
            }
        }

        // Weight towards tracks that haven't been played much
        val weightedCandidates = candidates.map { i ->
            val track = currentQueue[i]
            val weight = 1.0 / (track.playCount + 1)
            Pair(i, weight)
        }

        val totalWeight = weightedCandidates.sumOf { it.second }
        var random = Random.nextDouble() * totalWeight
        for ((index, weight) in weightedCandidates) {
            random -= weight
            if (random <= 0) {
                recordShuffle(index)
                return index
            }
        }

        val chosen = candidates.random()
        recordShuffle(chosen)
        return chosen
    }

    /**
     * Artist Mix: Prioritizes different artists
     */
    private fun artistMixShuffle(currentIndex: Int): Int {
        val currentTrack = currentQueue[currentIndex]
        val differentArtist = currentQueue.indices.filter { i ->
            i != currentIndex && currentQueue[i].artist != currentTrack.artist
        }

        val candidates = differentArtist.ifEmpty {
            currentQueue.indices.filter { it != currentIndex }
        }

        val chosen = candidates.random()
        recordShuffle(chosen)
        return chosen
    }

    /**
     * Album Mix: Prioritizes different albums
     */
    private fun albumMixShuffle(currentIndex: Int): Int {
        val currentTrack = currentQueue[currentIndex]
        val differentAlbum = currentQueue.indices.filter { i ->
            i != currentIndex && currentQueue[i].album != currentTrack.album
        }

        val candidates = differentAlbum.ifEmpty {
            currentQueue.indices.filter { it != currentIndex }
        }

        val chosen = candidates.random()
        recordShuffle(chosen)
        return chosen
    }

    /**
     * Genre Mix: Prioritizes different genres
     */
    private fun genreMixShuffle(currentIndex: Int): Int {
        val currentTrack = currentQueue[currentIndex]
        val differentGenre = currentQueue.indices.filter { i ->
            i != currentIndex &&
            currentQueue[i].genre != currentTrack.genre &&
            currentQueue[i].genre.isNotEmpty()
        }

        val candidates = differentGenre.ifEmpty {
            currentQueue.indices.filter { it != currentIndex }
        }

        val chosen = candidates.random()
        recordShuffle(chosen)
        return chosen
    }

    /**
     * Rating Weighted: Plays tracks with higher play count more often
     */
    private fun ratingWeightedShuffle(): Int {
        val maxPlayCount = currentQueue.maxOfOrNull { it.playCount }?.coerceAtLeast(1) ?: 1

        val weightedTracks = currentQueue.indices.map { i ->
            val track = currentQueue[i]
            val weight = (track.playCount.toDouble() / maxPlayCount) + 0.1
            Pair(i, weight)
        }

        val totalWeight = weightedTracks.sumOf { it.second }
        var random = Random.nextDouble() * totalWeight
        for ((index, weight) in weightedTracks) {
            random -= weight
            if (random <= 0) {
                recordShuffle(index)
                return index
            }
        }

        val chosen = Random.nextInt(currentQueue.size)
        recordShuffle(chosen)
        return chosen
    }

    /**
     * Weighted: Plays less-played tracks more often (music discovery)
     */
    private fun weightedShuffle(): Int {
        val maxPlayCount = currentQueue.maxOfOrNull { it.playCount }?.coerceAtLeast(1) ?: 1

        val weightedTracks = currentQueue.indices.map { i ->
            val track = currentQueue[i]
            // Inverse weighting: less played = higher weight
            val weight = ((maxPlayCount - track.playCount).toDouble() / maxPlayCount) + 0.5
            Pair(i, weight)
        }

        val totalWeight = weightedTracks.sumOf { it.second }
        var random = Random.nextDouble() * totalWeight
        for ((index, weight) in weightedTracks) {
            random -= weight
            if (random <= 0) {
                recordShuffle(index)
                return index
            }
        }

        val chosen = Random.nextInt(currentQueue.size)
        recordShuffle(chosen)
        return chosen
    }

    private fun recordShuffle(index: Int) {
        // Remove future history if we went back
        if (shuffleIndex < shuffleHistory.size - 1) {
            shuffleHistory = shuffleHistory.subList(0, shuffleIndex + 1).toMutableList()
        }
        shuffleHistory.add(index)
        shuffleIndex = shuffleHistory.size - 1

        // Track recently played
        if (currentQueue.isNotEmpty()) {
            recentlyPlayed.addLast(currentQueue[index].id)
            if (recentlyPlayed.size > 20) {
                recentlyPlayed.removeFirst()
            }
        }
    }

    fun reset() {
        shuffleHistory.clear()
        shuffleIndex = -1
        recentlyPlayed.clear()
    }

    fun generateShuffledQueue(tracks: List<Track>): List<Track> {
        if (tracks.size <= 1) return tracks

        return when (shuffleMode) {
            ShuffleMode.OFF -> tracks
            ShuffleMode.SMART -> generateSmartQueue(tracks)
            ShuffleMode.TRUE_RANDOM -> tracks.shuffled()
            ShuffleMode.ARTIST_MIX -> generateArtistMixQueue(tracks)
            ShuffleMode.WEIGHTED -> tracks.shuffled() // Simple for initial queue
            else -> tracks.shuffled()
        }
    }

    private fun generateSmartQueue(tracks: List<Track>): List<Track> {
        val result = mutableListOf<Track>()
        val remaining = tracks.toMutableList()
        var lastArtist = ""
        var lastAlbum = ""

        while (remaining.isNotEmpty()) {
            // Prefer tracks with different artist
            val candidates = remaining.filter { it.artist != lastArtist }
                .ifEmpty { remaining }

            // Among those, prefer different album
            val preferred = candidates.filter { it.album != lastAlbum }
                .ifEmpty { candidates }

            val chosen = preferred.random()
            result.add(chosen)
            remaining.remove(chosen)
            lastArtist = chosen.artist
            lastAlbum = chosen.album
        }

        return result
    }

    private fun generateArtistMixQueue(tracks: List<Track>): List<Track> {
        val byArtist = tracks.groupBy { it.artist }.values.toMutableList()
        val result = mutableListOf<Track>()

        // Round-robin through artists
        while (byArtist.any { it.isNotEmpty() }) {
            for (artistTracks in byArtist.shuffled()) {
                if (artistTracks.isNotEmpty()) {
                    result.add(artistTracks.first())
                    (artistTracks as MutableList).removeAt(0)
                }
            }
        }

        return result
    }
}