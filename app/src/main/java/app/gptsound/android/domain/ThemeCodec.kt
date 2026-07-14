package app.gptsound.android.domain

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object ThemeCodec {
    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    private val colorPattern = Regex("^#[0-9a-fA-F]{6}([0-9a-fA-F]{2})?$")

    fun encode(theme: ThemeProfile): String = json.encodeToString(validate(theme))

    fun decode(value: String): ThemeProfile = validate(json.decodeFromString(value))

    fun validate(theme: ThemeProfile): ThemeProfile {
        require(theme.name.trim().isNotEmpty()) { "Название темы не может быть пустым" }
        listOf(
            "accent" to theme.accent,
            "secondary" to theme.secondary,
            "backgroundTop" to theme.backgroundTop,
            "backgroundBottom" to theme.backgroundBottom,
        ).forEach { (field, value) ->
            require(colorPattern.matches(value)) { "$field должен быть цветом #RRGGBB" }
        }

        return theme.copy(
            name = theme.name.trim().take(40),
            accent = theme.accent.uppercase(),
            secondary = theme.secondary.uppercase(),
            backgroundTop = theme.backgroundTop.uppercase(),
            backgroundBottom = theme.backgroundBottom.uppercase(),
            glassAlpha = theme.glassAlpha.coerceIn(0.05f, 0.35f),
            cornerRadius = theme.cornerRadius.coerceIn(12, 40),
            glowStrength = theme.glowStrength.coerceIn(0f, 1f),
        )
    }
}
