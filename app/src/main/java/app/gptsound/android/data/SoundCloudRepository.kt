package app.gptsound.android.data

import app.gptsound.android.domain.CleanFeedFilter
import app.gptsound.android.domain.DemoCatalog
import app.gptsound.android.domain.Track
import app.gptsound.android.domain.WaveEngine

class SoundCloudRepository(
    private val api: SoundCloudApi,
    private val vault: SecretVault,
) {
    fun hasToken(): Boolean = vault.accessToken() != null

    suspend fun search(query: String, cleanFeed: Boolean): List<Track> {
        val token = vault.accessToken()
        if (token == null) {
            return DemoCatalog.tracks.filter {
                query.isBlank() || it.title.contains(query, ignoreCase = true) ||
                    it.artist.contains(query, ignoreCase = true) ||
                    it.genre.contains(query, ignoreCase = true)
            }
        }
        return CleanFeedFilter.apply(api.searchTracks(token, query).tracks, cleanFeed)
    }

    suspend fun wave(
        seed: Track,
        favorites: List<Track>,
        cleanFeed: Boolean,
    ): List<Track> {
        val token = vault.accessToken()
        if (token == null || seed.isDemo || !seed.urn.startsWith("soundcloud:tracks:")) {
            val demoRelated = DemoCatalog.tracks.filterNot { it.stableId == seed.stableId }
            return WaveEngine.rank(seed, demoRelated, favorites)
        }
        val related = api.relatedTracks(token, seed.urn).tracks
        return WaveEngine.rank(seed, CleanFeedFilter.apply(related, cleanFeed), favorites)
    }
}
