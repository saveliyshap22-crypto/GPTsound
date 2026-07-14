package app.gptsound.android.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.gptsound.android.domain.ThemeProfile
import app.gptsound.android.domain.Track
import app.gptsound.android.ui.components.GlassCard
import app.gptsound.android.ui.components.SectionHeader
import app.gptsound.android.ui.components.TrackRow

@Composable
fun LibraryScreen(
    theme: ThemeProfile,
    favorites: List<Track>,
    history: List<Track>,
    onPlay: (Track) -> Unit,
    onFavorite: (Track) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 24.dp, bottom = 118.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        item {
            Text("Моя музыка", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
        }
        item { SectionHeader("Любимые", "Хранятся только на этом устройстве") }
        if (favorites.isEmpty()) {
            item {
                GlassCard(theme) {
                    Text(
                        "Нажмите ♡ у трека — он появится здесь.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            items(favorites, key = { "favorite:${it.stableId}" }) { track ->
                TrackRow(
                    track = track,
                    profile = theme,
                    isFavorite = true,
                    onPlay = { onPlay(track) },
                    onFavorite = { onFavorite(track) },
                )
            }
        }
        item { SectionHeader("Недавно слушали") }
        if (history.isEmpty()) {
            item {
                Text(
                    "История пока пуста",
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(history, key = { "history:${it.stableId}" }) { track ->
                TrackRow(
                    track = track,
                    profile = theme,
                    isFavorite = favorites.any { it.stableId == track.stableId },
                    onPlay = { onPlay(track) },
                    onFavorite = { onFavorite(track) },
                )
            }
        }
    }
}
