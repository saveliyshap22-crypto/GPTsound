package app.gptsound.android.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ThemeCodecTest {
    @Test
    fun roundTripPreservesTheme() {
        val source = ThemeProfile.Ember.copy(name = "My Ember", cornerRadius = 31)
        assertEquals(source, ThemeCodec.decode(ThemeCodec.encode(source)))
    }

    @Test
    fun valuesAreClampedToSupportedRange() {
        val theme = ThemeCodec.validate(
            ThemeProfile(glassAlpha = 2f, cornerRadius = 80, glowStrength = -1f),
        )
        assertEquals(0.35f, theme.glassAlpha)
        assertEquals(40, theme.cornerRadius)
        assertEquals(0f, theme.glowStrength)
    }

    @Test
    fun malformedColorIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            ThemeCodec.validate(ThemeProfile(accent = "blue"))
        }
    }
}
