package app.gptsound.android.ui.theme

import android.graphics.Color.parseColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberInfiniteTransition
import androidx.compose.runtime.remember
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import app.gptsound.android.domain.ThemeProfile

fun colorFromHex(value: String, fallback: Color = Color.White): Color = runCatching {
    Color(parseColor(value))
}.getOrDefault(fallback)

@Composable
fun GPTsoundTheme(profile: ThemeProfile, content: @Composable () -> Unit) {
    val accent = colorFromHex(profile.accent, Color(0xFF8BE9FF))
    val secondary = colorFromHex(profile.secondary, Color(0xFFA78BFA))
    val top = colorFromHex(profile.backgroundTop, Color(0xFF0B1020))

    val colors = remember(profile) {
        darkColorScheme(
            primary = accent,
            secondary = secondary,
            tertiary = Color(0xFFFF8FB3),
            background = top,
            surface = top,
            surfaceVariant = Color.White.copy(alpha = 0.10f),
            onPrimary = Color(0xFF07131D),
            onSecondary = Color.White,
            onBackground = Color(0xFFF7F7FF),
            onSurface = Color(0xFFF7F7FF),
            onSurfaceVariant = Color(0xFFC9CBDA),
        )
    }

    MaterialTheme(colorScheme = colors, typography = AppTypography, content = content)
}

@Composable
fun AnimatedGlassBackground(
    profile: ThemeProfile,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val accent = colorFromHex(profile.accent)
    val secondary = colorFromHex(profile.secondary)
    val top = colorFromHex(profile.backgroundTop, Color(0xFF0B1020))
    val bottom = colorFromHex(profile.backgroundBottom, Color(0xFF20103A))
    val transition = rememberInfiniteTransition(label = "background")
    val drift by transition.animateFloat(
        initialValue = -28f,
        targetValue = 34f,
        animationSpec = infiniteRepeatable(tween(8_000), RepeatMode.Reverse),
        label = "drift",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(top, bottom))),
    ) {
        Box(
            Modifier
                .size(260.dp)
                .offset(x = (-90).dp, y = 70.dp)
                .graphicsLayer { translationX = drift }
                .blur(72.dp)
                .background(accent.copy(alpha = 0.28f * profile.glowStrength), CircleShape),
        )
        Box(
            Modifier
                .size(310.dp)
                .offset(x = 190.dp, y = 430.dp)
                .graphicsLayer { translationY = -drift }
                .blur(90.dp)
                .background(secondary.copy(alpha = 0.25f * profile.glowStrength), CircleShape),
        )
        content()
    }
}

private val AppTypography = Typography()
