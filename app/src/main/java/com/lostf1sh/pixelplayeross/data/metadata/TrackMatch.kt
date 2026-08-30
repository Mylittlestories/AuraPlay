package com.lostf1sh.pixelplayeross.data.metadata

/**
 * One resolved candidate for a track, unified across sources
 * (MusicBrainz, Deezer) and scored against the local track.
 */
data class TrackMatch(
    /** Human-readable source name, e.g. "MusicBrainz" or "Deezer". */
    val source: String,
    val title: String,
    val artist: String,
    val album: String,
    val year: Int,
    val durationMs: Long?,
    /** Highest-resolution artwork URL found for this candidate's album. */
    val albumArtUrl: String?,
    val genre: String?,
    val mbRecordingId: String?,
    val mbReleaseId: String?,
    val mbArtistId: String?,
    /** Similarity confidence in 0..1 against the local track. */
    val confidence: Double,
    /** True when the candidate duration is within ±5 s of the local track. */
    val durationVerified: Boolean
) {
    val highConfidence: Boolean get() = confidence >= HIGH_CONFIDENCE_THRESHOLD

    companion object {
        const val HIGH_CONFIDENCE_THRESHOLD = 0.86
    }
}

/**
 * The local track's values a candidate is compared against.
 */
data class ExpectedTrack(
    val title: String,
    val artist: String,
    val album: String?,
    val durationMs: Long?
)

/**
 * String normalization + fuzzy similarity + weighted candidate scoring for
 * track metadata matching.
 *
 * Pure Kotlin (no Android dependencies) so it can be unit-tested on the JVM.
 *
 * The scoring is deliberately conservative: a candidate only reaches
 * [TrackMatch.HIGH_CONFIDENCE_THRESHOLD] when the title and artist are near
 * exact matches *and* the duration lines up. Duration is the strongest
 * per-track signal — two different recordings of the same song (single edit
 * vs album version, remaster, live cut) differ in length.
 */
object MetadataMatcher {

    // ---------------------------------------------------------------- normal

    /**
     * Folds a display string into a comparison key: lowercase, diacritics
     * stripped, bracketed noise (feat./remaster/version markers) removed and
     * everything that is not a letter or digit dropped.
     */
    fun normalize(value: String): String {
        val folded = stripDiacritics(value.lowercase())
        val withoutNoise = removeNoiseSegments(folded)
        return withoutNoise.filter { it.isLetterOrDigit() }
    }

    private fun stripDiacritics(value: String): String {
        val decomposed = java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
        return buildString(decomposed.length) {
            for (char in decomposed) {
                if (Character.getType(char) != Character.NON_SPACING_MARK.toInt()) {
                    append(char)
                }
            }
        }
    }

    private val noiseMarkers = listOf(
        "feat.", "feat", "ft.", "ft", "featuring",
        "remaster", "remastered", "re-mastered", "remasterizado",
        "radio edit", "radio-edit", "single version", "album version",
        "extended mix", "extended version", "bonus track",
        "live", "demo", "instrumental", "acoustic", "a cappella", "acapella",
        "mono", "stereo", "deluxe", "explicit", "clean", "version", "edit", "mix"
    )

    /**
     * Removes "(...)" / "[...]" segments that carry noise markers, e.g.
     * "Song (feat. X)" or "Song - 2011 Remaster". A bracketed segment that
     * has no noise keyword (e.g. a real subtitle) is kept.
     */
    internal fun removeNoiseSegments(value: String): String {
        var result = value
        // Bracketed segments.
        result = BRACKET_REGEX.replace(result) { match ->
            val inner = match.groupValues[1].trim()
            if (inner.split(WORD_SPLIT_REGEX).any { it in noiseMarkers }) "" else match.value
        }
        // Trailing " - Remaster" / " - 2011 Remaster" style suffixes.
        result = result.split(" - ").joinToString(" - ") { part ->
            val tokens = part.trim().split(WORD_SPLIT_REGEX).filter { it.isNotBlank() }
            val isNoise = tokens.isNotEmpty() && tokens.all { token ->
                token in noiseMarkers || token.all(Char::isDigit)
            }
            if (isNoise) "" else part
        }
        return result.replace(" - ", " ").replace(Regex("\\s+"), " ")
    }

    private val BRACKET_REGEX = Regex("""\(([^)]*)\)|\[([^]]*)]""")
    private val WORD_SPLIT_REGEX = Regex("""[\s,;:]""")
    private val TOKEN_SPLIT_REGEX = Regex("""\s+""")

    // ------------------------------------------------------------ similarity

    /**
     * Similarity of two display strings in 0..1, computed on normalized,
     * space-kept token forms.
     *
     * Combines three signals and keeps the best:
     *  1. Jaro-Winkler on the de-spaced strings (typo tolerant);
     *  2. Jaro-Winkler on the sorted token sets (word-order tolerant);
     *  3. a hard rule: when the two strings share **no** whole token at all,
     *     they are different titles by definition — the score is crushed
     *     (capped at [ZERO_OVERLAP_CAP]) so no amount of artist/album/duration
     *     agreement can make a wrong song look like a match.
     */
    fun similarityRaw(a: String, b: String): Double {
        val na = normalizeKeepSpaces(a)
        val nb = normalizeKeepSpaces(b)
        if (na.isEmpty() && nb.isEmpty()) return 1.0
        if (na.isEmpty() || nb.isEmpty()) return 0.0

        val tokensA = na.split(TOKEN_SPLIT_REGEX).filter { it.isNotBlank() }
        val tokensB = nb.split(TOKEN_SPLIT_REGEX).filter { it.isNotBlank() }

        if (tokensA.isNotEmpty() && tokensB.isNotEmpty()) {
            val overlap = (tokensA.toSet() intersect tokensB.toSet()).size.toDouble() /
                minOf(tokensA.size, tokensB.size)
            if (overlap == 0.0 && maxOf(tokensA.size, tokensB.size) >= 2) {
                // Multi-word titles that share no word at all: different
                // title. Character-level similarity alone (JW on letter
                // runs) is far too generous for short titles, so it only
                // modulates within the cap. Single-word pairs keep full
                // character similarity — a typo'd one-word title/artist has
                // no tokens left to match on.
                val jwFull = jaroWinkler(na.replace(" ", ""), nb.replace(" ", ""))
                return minOf(ZERO_OVERLAP_CAP, jwFull * ZERO_OVERLAP_CAP)
            }
        }

        val jwFull = jaroWinkler(na.replace(" ", ""), nb.replace(" ", ""))
        val jwSorted = if (tokensA.isNotEmpty() && tokensB.isNotEmpty()) {
            jaroWinkler(tokensA.sorted().joinToString(" "), tokensB.sorted().joinToString(" "))
        } else {
            0.0
        }
        return maxOf(jwFull, jwSorted)
    }

    private const val ZERO_OVERLAP_CAP = 0.45

    /** Normalize but keep single spaces so tokens survive. */
    internal fun normalizeKeepSpaces(value: String): String {
        val folded = stripDiacritics(value.lowercase())
        val withoutNoise = removeNoiseSegments(folded)
        return withoutNoise.filter { it.isLetterOrDigit() || it == ' ' }
            .replace(Regex("\\s+"), " ").trim()
    }

    /** Classic Jaro-Winkler similarity in 0..1. */
    internal fun jaroWinkler(a: String, b: String): Double {
        val jaro = jaro(a, b)
        if (jaro < 0.7) return jaro
        var prefix = 0
        val maxPrefix = minOf(4, a.length, b.length)
        while (prefix < maxPrefix && a[prefix] == b[prefix]) prefix++
        val winkler = jaro + prefix * WINKLER_SCALING * (1.0 - jaro)
        return minOf(1.0, winkler)
    }

    private const val WINKLER_SCALING = 0.1

    private fun jaro(a: String, b: String): Double {
        if (a == b) return 1.0
        if (a.isEmpty() || b.isEmpty()) return 0.0
        val matchWindow = maxOf(a.length, b.length) / 2 - 1
        if (matchWindow < 0) return if (a == b) 1.0 else 0.0
        val aMatched = BooleanArray(a.length)
        val bMatched = BooleanArray(b.length)
        var matches = 0
        for (i in a.indices) {
            val start = maxOf(0, i - matchWindow)
            val end = minOf(i + matchWindow + 1, b.length)
            for (j in start until end) {
                if (!bMatched[j] && a[i] == b[j]) {
                    aMatched[i] = true
                    bMatched[j] = true
                    matches++
                    break
                }
            }
        }
        if (matches == 0) return 0.0
        var transpositions = 0
        var k = 0
        for (i in a.indices) {
            if (!aMatched[i]) continue
            while (!bMatched[k]) k++
            if (a[i] != b[k]) transpositions++
            k++
        }
        transpositions /= 2
        val m = matches.toDouble()
        return (m / a.length + m / b.length + (m - transpositions) / m) / 3.0
    }

    // --------------------------------------------------------------- scoring

    /** Neutral score for a dimension we have no data for. */
    private const val NEUTRAL = 0.5

    private const val TITLE_WEIGHT = 0.42
    private const val ARTIST_WEIGHT = 0.30
    private const val ALBUM_WEIGHT = 0.14
    private const val DURATION_WEIGHT = 0.14

    /**
     * Scores a candidate against the local track. All inputs are raw display
     * strings; normalization happens internally.
     */
    fun score(expected: ExpectedTrack, candidate: ExpectedTrack): Double {
        val titleSim = similarityRaw(expected.title, candidate.title)
        val artistSim = similarityRaw(expected.artist, candidate.artist)
        val albumSim = if (expected.album.isNullOrBlank() || candidate.album.isNullOrBlank()) {
            NEUTRAL
        } else {
            similarityRaw(expected.album, candidate.album)
        }
        val durationScore = durationScore(expected.durationMs, candidate.durationMs)
        var total = TITLE_WEIGHT * titleSim +
            ARTIST_WEIGHT * artistSim +
            ALBUM_WEIGHT * albumSim +
            DURATION_WEIGHT * durationScore
        // Gate: a candidate whose title is clearly different can never be a
        // high-confidence match, no matter how well artist/album/duration fit.
        if (titleSim < TITLE_GATE) {
            total = minOf(total, TITLE_GATE_MAX)
        }
        return total
    }

    private const val TITLE_GATE = 0.55
    private const val TITLE_GATE_MAX = 0.72

    /** Whether the candidate's duration matches the local track within ±5 s. */
    fun durationVerified(expectedMs: Long?, candidateMs: Long?): Boolean {
        if (expectedMs == null || expectedMs <= 0L || candidateMs == null || candidateMs <= 0L) {
            return false
        }
        return kotlin.math.abs(expectedMs - candidateMs) <= 5_000L
    }

    private fun durationScore(expectedMs: Long?, candidateMs: Long?): Double {
        if (expectedMs == null || expectedMs <= 0L || candidateMs == null || candidateMs <= 0L) {
            return NEUTRAL
        }
        val delta = kotlin.math.abs(expectedMs - candidateMs)
        return when {
            delta <= 2_000L -> 1.0
            delta <= 5_000L -> 0.85
            delta <= 10_000L -> 0.55
            delta <= 20_000L -> 0.25
            else -> 0.0
        }
    }

    // ------------------------------------------------------------------ keys

    /**
     * Stable identity key used to dedupe candidates from different sources:
     * same normalized title+artist at the same length is the same recording.
     */
    fun dedupeKey(title: String, artist: String, durationMs: Long?): String {
        val bucket = when {
            durationMs == null || durationMs <= 0L -> "?"
            else -> (durationMs / 2_000L).toString()
        }
        return "${normalize(title)}|${normalize(artist)}|$bucket"
    }
}
