package app.gptsound.android.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.gptsound.android.domain.ThemeProfile
import app.gptsound.android.domain.Track
import app.gptsound.android.ui.theme.colorFromHex
import coil.compose.AsyncImage

@Composable
fun GlassCard(
    profile: ThemeProfile,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(profile.cornerRadius.dp)
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Box(
        modifier = modifier
            .clip(shape)
            .background(Color.White.copy(alpha = profile.glassAlpha))
            .border(1.dp, Color.White.copy(alpha = 0.18f), shape)
            .then(clickModifier)
            .padding(18.dp),
    ) {
        content()
    }
}

@Composable
fun GlowButton(
    text: String,
    profile: ThemeProfile,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorFromHex(profile.accent),
            contentColor = Color(0xFF07131D),
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 20.dp,
            vertical = 13.dp,
        ),
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String? = null) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        if (!subtitle.isNullOrBlank()) {
            Spacer(Modifier.height(3.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun StatusChip(text: String, connected: Boolean) {
    val color = if (connected) Color(0xFF75E6A4) else Color(0xFFFFC46B)
    Surface(
        shape = RoundedCornerShape(100.dp),
        color = color.copy(alpha = 0.13f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.45f)),
    ) {
        Row(
            Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Box(Modifier.size(7.dp).background(color, CircleShape))
            Text(text, style = MaterialTheme.typography.labelMedium, color = color)
        }
    }
}

@Composable
fun TrackArtwork(
    track: Track,
    profile: ThemeProfile,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(22.dp)
    if (track.artworkUrl.isNullOrBlank()) {
        Box(
            modifier = modifier
                .clip(shape)
                .background(
                    Brush.linearGradient(
                        listOf(colorFromHex(profile.secondary), colorFromHex(profile.accent)),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text("≈", style = MaterialTheme.typography.displayMedium, color = Color.White)
        }
    } else {
        AsyncImage(
            model = track.artworkUrl,
            contentDescription = "Обложка ${track.title}",
            modifier = modifier.clip(shape),
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
fun TrackRow(
    track: Track,
    profile: ThemeProfile,
    isFavorite: Boolean,
    onPlay: () -> Unit,
    onFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassCard(profile, modifier = modifier.fillMaxWidth(), onClick = onPlay) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            TrackArtwork(track, profile, Modifier.size(62.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    track.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    track.artist,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Text(
                if (isFavorite) "♥" else "♡",
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onFavorite)
                    .padding(9.dp),
                color = if (isFavorite) colorFromHex(profile.accent) else Color.White,
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}

@Composable
fun BottomNavItemContent(
    symbol: String,
    label: String,
    selected: Boolean,
    profile: ThemeProfile,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            symbol,
            color = if (selected) colorFromHex(profile.accent) else Color.White.copy(alpha = 0.62f),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            label,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.55f),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}
