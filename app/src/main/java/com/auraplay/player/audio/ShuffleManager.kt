package com.auraplay.player.audio

import com.auraplay.player.data.model.Track
import kotlin.random.Random

enum class ShuffleMode(val label: String) {
    OFF("Off"),
    SMART("Smart Shuffle"),
    TRUE_RANDOM("True Random"),
    ARTIST_MIX("Artist Mix"),
    ALBUM_MIX("Album Mix"),
    GENRE_MIX("Genre Mix"),
    MOST_PLAYED("Most Played First"),
    DISCOVERY("Discovery Mode")
}

class ShuffleManager {
    private val recentHistory = mutableSetOf<Long>()
    private var lastArtist: String? = null
    private var lastAlbum: String? = null
    private var lastGenre: String? = null

    fun shuffle(tracks: List<Track>, mode: ShuffleMode): List<Track> {
        if (tracks.isEmpty()) return emptyList()
        return when (mode) {
            ShuffleMode.OFF -> tracks
            ShuffleMode.SMART -> smartShuffle(tracks)
            ShuffleMode.TRUE_RANDOM -> tracks.shuffled(Random)
            ShuffleMode.ARTIST_MIX -> artistMix(tracks)
            ShuffleMode.ALBUM_MIX -> albumMix(tracks)
            ShuffleMode.GENRE_MIX -> genreMix(tracks)
            ShuffleMode.MOST_PLAYED -> mostPlayedFirst(tracks)
            ShuffleMode.DISCOVERY -> discoveryMode(tracks)
        }
    }

    private fun smartShuffle(tracks: List<Track>): List<Track> {
        val result = mutableListOf<Track>()
        val remaining = tracks.toMutableList()

        while (remaining.isNotEmpty()) {
            val candidates = remaining.filter { track ->
                track.id !in recentHistory &&
                track.artist != lastArtist &&
                track.album != lastAlbum
            }

            val pick = if (candidates.isNotEmpty()) {
                candidates.random(Random)
            } else {
                remaining.random(Random)
            }

            result.add(pick)
            remaining.remove(pick)
            lastArtist = pick.artist
            lastAlbum = pick.album
            recentHistory.add(pick.id)
            if (recentHistory.size > tracks.size / 2) {
                recentHistory.clear()
            }
        }
        return result
    }

    private fun artistMix(tracks: List<Track>): List<Track> {
        val byArtist = tracks.groupBy { it.artist }
        val result = mutableListOf<Track>()
        val queues = byArtist.map { it.value.toMutableList() }.toMutableList()

        while (queues.isNotEmpty()) {
            val idx = Random.nextInt(queues.size)
            val queue = queues[idx]
            result.add(queue.removeAt(Random.nextInt(queue.size)))
            if (queue.isEmpty()) queues.removeAt(idx)
        }
        return result
    }

    private fun albumMix(tracks: List<Track>): List<Track> {
        val byAlbum = tracks.groupBy { it.album }
        val result = mutableListOf<Track>()
        val queues = byAlbum.map { it.value.toMutableList() }.toMutableList()

        while (queues.isNotEmpty()) {
            val idx = Random.nextInt(queues.size)
            val queue = queues[idx]
            result.add(queue.removeAt(Random.nextInt(queue.size)))
            if (queue.isEmpty()) queues.removeAt(idx)
        }
        return result
    }

    private fun genreMix(tracks: List<Track>): List<Track> {
        val byGenre = tracks.groupBy { it.genre.ifEmpty { "Other" } }
        val result = mutableListOf<Track>()
        val queues = byGenre.map { it.value.toMutableList() }.toMutableList()

        while (queues.isNotEmpty()) {
            val idx = Random.nextInt(queues.size)
            val queue = queues[idx]
            result.add(queue.removeAt(Random.nextInt(queue.size)))
            if (queue.isEmpty()) queues.removeAt(idx)
        }
        return result
    }

    private fun mostPlayedFirst(tracks: List<Track>): List<Track> {
        return tracks.sortedByDescending { it.playCount }.shuffled(Random).let { shuffled ->
            val top = shuffled.take((shuffled.size * 0.4).toInt().coerceAtLeast(1))
                .sortedByDescending { it.playCount }
            val rest = shuffled.drop((shuffled.size * 0.4).toInt().coerceAtLeast(1))
                .shuffled(Random)
            top + rest
        }
    }

    private fun discoveryMode(tracks: List<Track>): List<Track> {
        return tracks.sortedBy { it.playCount }.shuffled(Random).let { shuffled ->
            val unplayed = shuffled.filter { it.playCount == 0 }.shuffled(Random)
            val played = shuffled.filter { it.playCount > 0 }.shuffled(Random)
            unplayed + played
        }
    }
}
