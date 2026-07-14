package app.gptsound.android.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.gptsound.android.domain.ThemeProfile
import app.gptsound.android.domain.Track
import app.gptsound.android.ui.components.GlassCard
import app.gptsound.android.ui.components.GlowButton
import app.gptsound.android.ui.components.SectionHeader
import app.gptsound.android.ui.components.StatusChip
import app.gptsound.android.ui.components.TrackArtwork
import app.gptsound.android.ui.components.TrackRow
import app.gptsound.android.ui.theme.colorFromHex

@Composable
fun HomeScreen(
    theme: ThemeProfile,
    connected: Boolean,
    query: String,
    tracks: List<Track>,
    favorites: List<Track>,
    isLoading: Boolean,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onOpenWave: (Track) -> Unit,
    onPlay: (Track) -> Unit,
    onFavorite: (Track) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 24.dp, bottom = 118.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        "GPTsound",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        "Новый звук, твой ритм",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                StatusChip(if (connected) "API онлайн" else "Демо", connected)
            }
        }

        item {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    placeholder = { Text("Трек, автор или жанр") },
                    leadingIcon = { Text("⌕", style = MaterialTheme.typography.titleLarge) },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = theme.glassAlpha),
                        unfocusedContainerColor = Color.White.copy(alpha = theme.glassAlpha * 0.75f),
                        focusedBorderColor = colorFromHex(theme.accent).copy(alpha = 0.8f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.16f),
                    ),
                )
                GlowButton("Найти", theme, enabled = !isLoading, onClick = onSearch)
            }
        }

        item {
            GlassCard(theme, modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Волна",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = colorFromHex(theme.accent),
                            )
                            Text(
                                "Бесконечная подборка похожих треков с разнообразием исполнителей.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text("〰", style = MaterialTheme.typography.displaySmall)
                    }
                    GlowButton(
                        text = "Запустить волну",
                        profile = theme,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { tracks.firstOrNull()?.let(onOpenWave) },
                    )
                }
            }
        }

        if (isLoading) {
            item {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colorFromHex(theme.accent))
                }
            }
        }

        if (tracks.isNotEmpty()) {
            item { SectionHeader("Сейчас в эфире", "Результаты поиска и свежая подборка") }
            item {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    tracks.take(8).forEach { track ->
                        GlassCard(
                            profile = theme,
                            modifier = Modifier.width(164.dp),
                            onClick = { onPlay(track) },
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                TrackArtwork(track, theme, Modifier.fillMaxWidth().height(128.dp))
                                Text(
                                    track.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    track.artist,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                                TextButton(
                                    onClick = { onOpenWave(track) },
                                    contentPadding = PaddingValues(0.dp),
                                ) {
                                    Text("В волну →", color = colorFromHex(theme.accent))
                                }
                            }
                        }
                    }
                }
            }
            item { SectionHeader("Все треки") }
            items(tracks, key = Track::stableId) { track ->
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
