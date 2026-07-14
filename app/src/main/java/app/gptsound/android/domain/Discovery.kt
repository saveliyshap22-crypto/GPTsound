package app.gptsound.android.domain

import kotlin.math.abs

object CleanFeedFilter {
    private val promotionMarkers = listOf(
        "#ad",
        "#sponsored",
        "sponsored",
        "paid promo",
        "реклама",
        "рекламный",
    )

    fun apply(tracks: List<Track>, enabled: Boolean): List<Track> {
        if (!enabled) return tracks
        return tracks.filterNot { track ->
            val searchable = "${track.title} ${track.description}".lowercase()
            promotionMarkers.any(searchable::contains)
        }
    }
}

object WaveEngine {
    /**
     * Re-ranks SoundCloud's related results. The API remains the recommendation
     * source; this lightweight pass adds artist variety and genre continuity.
     */
    fun rank(seed: Track, related: List<Track>, favorites: List<Track>): List<Track> {
        val favoriteGenres = favorites
            .map { it.genre.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .groupingBy { it }
            .eachCount()

        val scored = related.distinctBy(Track::stableId).mapIndexed { index, track ->
            val genre = track.genre.trim().lowercase()
            val seedGenre = seed.genre.trim().lowercase()
            var score = 1000.0 - index
            if (genre.isNotEmpty() && genre == seedGenre) score += 80
            score += (favoriteGenres[genre] ?: 0) * 20
            if (track.artist.equals(seed.artist, ignoreCase = true)) score -= 35
            score -= abs(track.durationMs - seed.durationMs).coerceAtMost(900_000) / 90_000.0
            track to score
        }.sortedByDescending { it.second }

        val result = mutableListOf<Track>()
        val pending = scored.map { it.first }.toMutableList()
        while (pending.isNotEmpty()) {
            val previousArtist = result.lastOrNull()?.artist
            val nextIndex = pending.indexOfFirst {
                previousArtist == null || !it.artist.equals(previousArtist, ignoreCase = true)
            }.takeIf { it >= 0 } ?: 0
            result += pending.removeAt(nextIndex)
        }
        return result
    }
}

object DemoCatalog {
    val tracks = listOf(
        Track(
            urn = "demo:midnight-signal",
            title = "Midnight Signal",
            artist = "Nova Avenue",
            durationMs = 218_000,
            genre = "Electronic",
            isDemo = true,
        ),
        Track(
            urn = "demo:violet-rooms",
            title = "Violet Rooms",
            artist = "Kite Theory",
            durationMs = 194_000,
            genre = "Electronic",
            isDemo = true,
        ),
        Track(
            urn = "demo:afterglow",
            title = "Afterglow Drive",
            artist = "Blue Static",
            durationMs = 235_000,
            genre = "Synthwave",
            isDemo = true,
        ),
        Track(
            urn = "demo:soft-focus",
            title = "Soft Focus",
            artist = "Lumen Club",
            durationMs = 201_000,
            genre = "Indie",
            isDemo = true,
        ),
        Track(
            urn = "demo:orbit",
            title = "Low Orbit",
            artist = "Echo Arcade",
            durationMs = 226_000,
            genre = "Ambient",
            isDemo = true,
        ),
        Track(
            urn = "demo:sunline",
            title = "Sunline",
            artist = "Paper Satellites",
            durationMs = 189_000,
            genre = "Indie",
            isDemo = true,
        ),
    )
}
