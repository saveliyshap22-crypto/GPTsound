package app.gptsound.android.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.gptsound.android.domain.ThemeProfile
import app.gptsound.android.domain.Track
import app.gptsound.android.ui.components.GlassCard
import app.gptsound.android.ui.components.GlowButton
import app.gptsound.android.ui.components.StatusChip
import app.gptsound.android.ui.components.TrackArtwork
import app.gptsound.android.ui.theme.colorFromHex
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WaveScreen(
    theme: ThemeProfile,
    tracks: List<Track>,
    favorites: List<Track>,
    isLoading: Boolean,
    connected: Boolean,
    onPlay: (Track) -> Unit,
    onFavorite: (Track) -> Unit,
    onRefresh: (Track) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (tracks.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Сначала выберите трек для Волны")
        }
        return
    }

    val pager = rememberPagerState(pageCount = { tracks.size })
    val scope = rememberCoroutineScope()
    val currentTrack = tracks[pager.currentPage.coerceIn(tracks.indices)]

    Column(
        modifier = modifier.fillMaxSize().padding(start = 18.dp, end = 18.dp, top = 22.dp, bottom = 108.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("Волна", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
                Text(
                    "Свайпайте — подборка продолжится",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            StatusChip(if (connected) "SoundCloud" else "Демо", connected)
        }

        if (isLoading) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colorFromHex(theme.accent))
            }
        } else {
            HorizontalPager(
                state = pager,
                modifier = Modifier.weight(1f),
                pageSpacing = 12.dp,
                beyondViewportPageCount = 1,
            ) { page ->
                val track = tracks[page]
                GlassCard(theme, modifier = Modifier.fillMaxSize()) {
                    Column(
                        Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        TrackArtwork(track, theme, Modifier.fillMaxWidth().weight(1f).aspectRatio(1f))
                        Text(
                            track.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            track.artist,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (track.genre.isNotBlank()) {
                            Text(
                                track.genre,
                                color = colorFromHex(theme.accent),
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            GlowButton(
                                text = if (favorites.any { it.stableId == track.stableId }) "♥ Сохранено" else "♡ Нравится",
                                profile = theme,
                                modifier = Modifier.weight(1f),
                                onClick = { onFavorite(track) },
                            )
                            GlowButton(
                                text = "▶ Слушать",
                                profile = theme,
                                modifier = Modifier.weight(1f),
                                onClick = { onPlay(track) },
                            )
                        }
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GlowButton(
                    text = "Обновить от этого трека",
                    profile = theme,
                    modifier = Modifier.weight(1f),
                    onClick = { onRefresh(currentTrack) },
                )
                GlowButton(
                    text = "Дальше →",
                    profile = theme,
                    enabled = pager.currentPage < tracks.lastIndex,
                    onClick = {
                        scope.launch { pager.animateScrollToPage((pager.currentPage + 1).coerceAtMost(tracks.lastIndex)) }
                    },
                )
            }
        }
    }
}
