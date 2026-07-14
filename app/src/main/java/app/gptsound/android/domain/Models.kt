package app.gptsound.android.domain

import kotlinx.serialization.Serializable

@Serializable
data class Track(
    val urn: String,
    val title: String,
    val artist: String,
    val artworkUrl: String? = null,
    val permalinkUrl: String = "",
    val durationMs: Long = 0,
    val genre: String = "",
    val description: String = "",
    val access: String = "playable",
    val isDemo: Boolean = false,
) {
    val stableId: String get() = urn.ifBlank { "$artist::$title" }
}

@Serializable
data class ThemeProfile(
    val name: String = "Aurora",
    val accent: String = "#8BE9FF",
    val secondary: String = "#A78BFA",
    val backgroundTop: String = "#0B1020",
    val backgroundBottom: String = "#20103A",
    val glassAlpha: Float = 0.13f,
    val cornerRadius: Int = 28,
    val glowStrength: Float = 0.55f,
) {
    companion object {
        val Aurora = ThemeProfile()

        val Ember = ThemeProfile(
            name = "Ember",
            accent = "#FFB86C",
            secondary = "#FF5C8A",
            backgroundTop = "#190D17",
            backgroundBottom = "#35151C",
            glassAlpha = 0.14f,
            glowStrength = 0.62f,
        )

        val Arctic = ThemeProfile(
            name = "Arctic",
            accent = "#E0FBFC",
            secondary = "#63C7FF",
            backgroundTop = "#07131D",
            backgroundBottom = "#0B3552",
            glassAlpha = 0.11f,
            cornerRadius = 22,
            glowStrength = 0.45f,
        )

        val presets = listOf(Aurora, Ember, Arctic)
    }
}

data class AuthSettings(
    val clientId: String = "",
    val redirectUri: String = "gptsound://oauth/callback",
)

data class ApiPage(
    val tracks: List<Track>,
    val nextHref: String? = null,
)
