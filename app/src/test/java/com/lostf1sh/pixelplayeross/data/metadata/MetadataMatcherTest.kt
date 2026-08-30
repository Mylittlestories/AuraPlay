package com.lostf1sh.pixelplayeross.data.metadata

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pins the accurate-metadata matching contract. These cases encode what
 * "accurate to the track" means: duration and token overlap decide between
 * otherwise plausible candidates.
 */
class MetadataMatcherTest {

    private val expected = ExpectedTrack(
        title = "Get Lucky",
        artist = "Daft Punk",
        album = "Random Access Memories",
        durationMs = 369_000L
    )

    @Test
    fun normalization_stripsNoise() {
        assertEquals("getlucky", MetadataMatcher.normalize("Get Lucky (feat. Pharrell Williams)"))
        assertEquals("backinblack", MetadataMatcher.normalize("Back in Black - 2011 Remaster"))
        assertEquals("beyonce", MetadataMatcher.normalize("Béyoncé"))
        // Real subtitles survive.
        assertEquals(
            "moneythatswhatiwant",
            MetadataMatcher.normalize("Money (That's What I Want)")
        )
    }

    @Test
    fun exactMatch_scoresPerfect() {
        val score = MetadataMatcher.score(
            expected,
            ExpectedTrack("Get Lucky", "Daft Punk", "Random Access Memories", 368_000L)
        )
        assertTrue("expected >= 0.98, was $score", score >= 0.98)
    }

    @Test
    fun featuringVariant_isStillTheSameTrack() {
        val score = MetadataMatcher.score(
            expected,
            ExpectedTrack("Get Lucky (feat. Pharrell Williams)", "Daft Punk", "Random Access Memories", 369_000L)
        )
        assertTrue("expected >= 0.95, was $score", score >= 0.95)
    }

    @Test
    fun differentSongBySameArtist_isNotAConfidentMatch() {
        val score = MetadataMatcher.score(
            expected,
            ExpectedTrack("Lose Yourself to Dance", "Daft Punk", "Random Access Memories", 371_000L)
        )
        assertTrue("expected < 0.70, was $score", score < 0.70)
        assertTrue(score < TrackMatch.HIGH_CONFIDENCE_THRESHOLD)
    }

    @Test
    fun singleEdit_ranksBelowAlbumVersion() {
        val albumVersion = MetadataMatcher.score(
            expected,
            ExpectedTrack("Get Lucky", "Daft Punk", "Random Access Memories", 369_000L)
        )
        val singleEdit = MetadataMatcher.score(
            expected,
            ExpectedTrack("Get Lucky", "Daft Punk", "Random Access Memories", 252_000L)
        )
        assertTrue(singleEdit < albumVersion - 0.06)
        assertFalse(MetadataMatcher.durationVerified(369_000L, 252_000L))
        assertTrue(MetadataMatcher.durationVerified(369_000L, 368_000L))
    }

    @Test
    fun typos_areTolerated() {
        val score = MetadataMatcher.score(
            ExpectedTrack("Bohemian Rhapsody", "Queen", null, 354_000L),
            ExpectedTrack("Bohemian Rhapsode", "Qeen", null, 355_000L)
        )
        assertTrue("expected >= 0.85, was $score", score >= 0.85)
    }

    @Test
    fun wordOrder_doesNotPunish() {
        val similarity = MetadataMatcher.similarityRaw("The Beatles", "Beatles, The")
        assertTrue("expected >= 0.93, was $similarity", similarity >= 0.93)
    }

    @Test
    fun sameTitleDifferentArtist_isNotAMatch() {
        val score = MetadataMatcher.score(
            ExpectedTrack("Someone Like You", "Adele", "21", 285_000L),
            ExpectedTrack("Someone Like You", "Van Morrison", null, 193_000L)
        )
        assertTrue("expected < 0.65, was $score", score < 0.65)
    }

    @Test
    fun missingDuration_isNeutral() {
        val score = MetadataMatcher.score(
            ExpectedTrack("X", "Y", "Z", null),
            ExpectedTrack("X", "Y", "Z", null)
        )
        assertTrue("expected >= 0.92, was $score", score >= 0.92)
    }

    @Test
    fun dedupeKey_mergesVariants_splitsVersions() {
        assertEquals(
            MetadataMatcher.dedupeKey("Get Lucky", "Daft Punk", 369_000L),
            MetadataMatcher.dedupeKey("Get Lucky (feat. Pharrell Williams)", "Daft Punk", 368_000L)
        )
        org.junit.Assert.assertNotEquals(
            MetadataMatcher.dedupeKey("Get Lucky", "Daft Punk", 369_000L),
            MetadataMatcher.dedupeKey("Get Lucky", "Daft Punk", 252_000L)
        )
    }
}
