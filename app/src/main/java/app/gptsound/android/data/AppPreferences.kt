package app.gptsound.android.data

import android.content.Context
import app.gptsound.android.domain.AuthSettings
import app.gptsound.android.domain.ThemeCodec
import app.gptsound.android.domain.ThemeProfile
import app.gptsound.android.domain.Track
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AppPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("gptsound_preferences", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    fun theme(): ThemeProfile = runCatching {
        ThemeCodec.decode(prefs.getString(KEY_THEME, null).orEmpty())
    }.getOrDefault(ThemeProfile.Aurora)

    fun saveTheme(theme: ThemeProfile) {
        prefs.edit().putString(KEY_THEME, ThemeCodec.encode(theme)).apply()
    }

    fun authSettings(): AuthSettings = AuthSettings(
        clientId = prefs.getString(KEY_CLIENT_ID, "").orEmpty(),
        redirectUri = prefs.getString(KEY_REDIRECT_URI, DEFAULT_REDIRECT).orEmpty()
            .ifBlank { DEFAULT_REDIRECT },
    )

    fun saveAuthSettings(settings: AuthSettings) {
        prefs.edit()
            .putString(KEY_CLIENT_ID, settings.clientId.trim())
            .putString(KEY_REDIRECT_URI, settings.redirectUri.trim())
            .apply()
    }

    fun cleanFeed(): Boolean = prefs.getBoolean(KEY_CLEAN_FEED, true)

    fun saveCleanFeed(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CLEAN_FEED, enabled).apply()
    }

    fun favorites(): List<Track> = decodeTracks(prefs.getString(KEY_FAVORITES, null))

    fun saveFavorites(tracks: List<Track>) {
        prefs.edit().putString(KEY_FAVORITES, json.encodeToString(tracks.take(200))).apply()
    }

    fun history(): List<Track> = decodeTracks(prefs.getString(KEY_HISTORY, null))

    fun saveHistory(tracks: List<Track>) {
        prefs.edit().putString(KEY_HISTORY, json.encodeToString(tracks.take(100))).apply()
    }

    private fun decodeTracks(value: String?): List<Track> = runCatching {
        if (value.isNullOrBlank()) emptyList() else json.decodeFromString(value)
    }.getOrDefault(emptyList())

    companion object {
        const val DEFAULT_REDIRECT = "gptsound://oauth/callback"
        private const val KEY_THEME = "theme"
        private const val KEY_CLIENT_ID = "soundcloud_client_id"
        private const val KEY_REDIRECT_URI = "soundcloud_redirect_uri"
        private const val KEY_CLEAN_FEED = "clean_feed"
        private const val KEY_FAVORITES = "favorites"
        private const val KEY_HISTORY = "history"
    }
}
