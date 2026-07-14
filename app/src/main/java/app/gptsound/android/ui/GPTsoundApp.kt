package app.gptsound.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import app.gptsound.android.ui.components.BottomNavItemContent
import app.gptsound.android.ui.player.SoundCloudPlayerSheet
import app.gptsound.android.ui.screens.DesignScreen
import app.gptsound.android.ui.screens.HomeScreen
import app.gptsound.android.ui.screens.LibraryScreen
import app.gptsound.android.ui.screens.WaveScreen
import app.gptsound.android.ui.theme.AnimatedGlassBackground

private enum class Destination(val label: String, val symbol: String) {
    Home("Главная", "⌂"),
    Wave("Волна", "≈"),
    Library("Моё", "♡"),
    Design("Дизайн", "✦"),
}

@Composable
fun GPTsoundApp(state: AppUiState, viewModel: AppViewModel) {
    var destination by remember { mutableStateOf(Destination.Home) }
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    AnimatedGlassBackground(state.theme) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbar) },
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.86f)) {
                    Destination.entries.forEach { item ->
                        NavigationBarItem(
                            selected = destination == item,
                            onClick = { destination = item },
                            icon = {
                                BottomNavItemContent(
                                    symbol = item.symbol,
                                    label = item.label,
                                    selected = destination == item,
                                    profile = state.theme,
                                )
                            },
                            alwaysShowLabel = false,
                        )
                    }
                }
            },
        ) { innerPadding ->
            Box(Modifier.fillMaxSize().padding(innerPadding)) {
                when (destination) {
                    Destination.Home -> HomeScreen(
                        theme = state.theme,
                        connected = state.isConnected,
                        query = state.searchQuery,
                        tracks = state.searchResults,
                        favorites = state.favorites,
                        isLoading = state.isLoading,
                        onQueryChange = viewModel::updateSearchQuery,
                        onSearch = viewModel::search,
                        onOpenWave = {
                            viewModel.startWave(it)
                            destination = Destination.Wave
                        },
                        onPlay = viewModel::play,
                        onFavorite = viewModel::toggleFavorite,
                        modifier = Modifier.background(Color.Transparent),
                    )

                    Destination.Wave -> WaveScreen(
                        theme = state.theme,
                        tracks = state.waveTracks,
                        favorites = state.favorites,
                        isLoading = state.isLoading,
                        connected = state.isConnected,
                        onPlay = viewModel::play,
                        onFavorite = viewModel::toggleFavorite,
                        onRefresh = viewModel::startWave,
                    )

                    Destination.Library -> LibraryScreen(
                        theme = state.theme,
                        favorites = state.favorites,
                        history = state.history,
                        onPlay = viewModel::play,
                        onFavorite = viewModel::toggleFavorite,
                    )

                    Destination.Design -> DesignScreen(
                        theme = state.theme,
                        authSettings = state.authSettings,
                        connected = state.isConnected,
                        cleanFeed = state.cleanFeed,
                        exportTheme = viewModel::exportTheme,
                        onApplyTheme = viewModel::applyTheme,
                        onImportTheme = viewModel::importTheme,
                        onSaveAuth = viewModel::saveAuthSettings,
                        onConnect = viewModel::authorizationUri,
                        onLogout = viewModel::logout,
                        onCleanFeedChange = viewModel::setCleanFeed,
                    )
                }
            }
        }

        state.nowPlaying?.let { track ->
            SoundCloudPlayerSheet(
                track = track,
                profile = state.theme,
                onDismiss = viewModel::dismissPlayer,
            )
        }
    }
}
