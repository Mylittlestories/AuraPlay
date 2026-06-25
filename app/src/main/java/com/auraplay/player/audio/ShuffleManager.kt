package com.auraplay.player.audio
import com.auraplay.player.data.model.Track
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

enum class ShuffleMode { OFF, SMART, RANDOM, ARTIST_MIX, ALBUM_MIX, DISCOVERY }

@Singleton
class ShuffleManager @Inject constructor() {
    private var queue = listOf<Track>()
    private val recent = ArrayDeque<Long>(20)
    fun setQueue(q: List<Track>) { queue = q; recent.clear() }
    fun next(cur: Int, mode: ShuffleMode): Int {
        if (queue.isEmpty()) return 0
        return when (mode) {
            ShuffleMode.OFF -> (cur + 1) % queue.size
            ShuffleMode.SMART -> smart(cur)
            ShuffleMode.RANDOM -> Random.nextInt(queue.size)
            ShuffleMode.ARTIST_MIX -> artistMix(cur)
            ShuffleMode.ALBUM_MIX -> albumMix(cur)
            ShuffleMode.DISCOVERY -> discovery()
        }
    }
    fun prev(cur: Int, mode: ShuffleMode): Int {
        if (mode == ShuffleMode.OFF) return if (cur > 0) cur - 1 else queue.size - 1
        return cur
    }
    private fun smart(cur: Int): Int {
        val curTrack = queue[cur]
        val cands = queue.indices.filter { it != cur && queue[it].id !in recent && queue[it].artist != curTrack.artist }
            .ifEmpty { queue.indices.filter { it != cur && queue[it].id !in recent } }
            .ifEmpty { queue.indices.filter { it != cur } }
        val chosen = cands.random()
        record(chosen); return chosen
    }
    private fun artistMix(cur: Int): Int {
        val cands = queue.indices.filter { it != cur && queue[it].artist != queue[cur].artist }.ifEmpty { queue.indices.filter { it != cur } }
        val chosen = cands.random(); record(chosen); return chosen
    }
    private fun albumMix(cur: Int): Int {
        val cands = queue.indices.filter { it != cur && queue[it].album != queue[cur].album }.ifEmpty { queue.indices.filter { it != cur } }
        val chosen = cands.random(); record(chosen); return chosen
    }
    private fun discovery(): Int {
        val max = queue.maxOfOrNull { it.playCount }?.coerceAtLeast(1) ?: 1
        val w = queue.indices.map { it to ((max - queue[it].playCount).toDouble() / max + 0.5) }
        val total = w.sumOf { it.second }; var r = Random.nextDouble() * total
        for ((i, wt) in w) { r -= wt; if (r <= 0) { record(i); return i } }
        val c = Random.nextInt(queue.size); record(c); return c
    }
    private fun record(i: Int) { recent.addLast(queue[i].id); if (recent.size > 20) recent.removeFirst() }
    fun shuffle(tracks: List<Track>): List<Track> {
        if (tracks.size <= 1) return tracks
        val r = tracks.toMutableList(); r.shuffle()
        val byArtist = r.groupBy { it.artist }.values.toMutableList()
        val result = mutableListOf<Track>()
        while (byArtist.any { it.isNotEmpty() }) { for (a in byArtist.shuffled()) { if (a.isNotEmpty()) { result.add(a.first()); (a as MutableList).removeAt(0) } } }
        return result
    }
}
