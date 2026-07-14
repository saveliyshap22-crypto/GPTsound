package app.gptsound.android.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DiscoveryTest {
    @Test
    fun cleanFeedOnlyFiltersExplicitPromotionMarkers() {
        val regular = Track("1", "Night drive", "A")
        val sponsored = Track("2", "Night drive #ad", "B")
        val result = CleanFeedFilter.apply(listOf(regular, sponsored), enabled = true)
        assertEquals(listOf(regular), result)
        assertEquals(2, CleanFeedFilter.apply(listOf(regular, sponsored), enabled = false).size)
    }

    @Test
    fun waveAvoidsBackToBackArtistsWhenPossible() {
        val seed = Track("seed", "Seed", "Seed Artist", durationMs = 200_000, genre = "indie")
        val related = listOf(
            Track("1", "One", "Same", durationMs = 200_000, genre = "indie"),
            Track("2", "Two", "Same", durationMs = 201_000, genre = "indie"),
            Track("3", "Three", "Different", durationMs = 205_000, genre = "indie"),
        )
        val ranked = WaveEngine.rank(seed, related, emptyList())
        assertEquals(3, ranked.size)
        assertFalse(ranked[0].artist.equals(ranked[1].artist, ignoreCase = true))
        assertTrue(ranked.map(Track::stableId).containsAll(related.map(Track::stableId)))
    }
}
