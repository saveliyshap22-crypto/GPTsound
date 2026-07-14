package app.gptsound.android.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.gptsound.android.auth.SoundCloudAuth
import app.gptsound.android.data.AppPreferences
import app.gptsound.android.data.SecretVault
import app.gptsound.android.data.SoundCloudApi
import app.gptsound.android.data.SoundCloudException
import app.gptsound.android.data.SoundCloudRepository
import app.gptsound.android.domain.AuthSettings
import app.gptsound.android.domain.DemoCatalog
import app.gptsound.android.domain.ThemeCodec
import app.gptsound.android.domain.ThemeProfile
import app.gptsound.android.domain.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppUiState(
    val theme: ThemeProfile,
    val authSettings: AuthSettings,
    val isConnected: Boolean,
    val cleanFeed: Boolean,
    val searchQuery: String = "",
    val searchResults: List<Track> = DemoCatalog.tracks,
    val waveTracks: List<Track> = DemoCatalog.tracks,
    val waveSeed: Track = DemoCatalog.tracks.first(),
    val favorites: List<Track> = emptyList(),
    val history: List<Track> = emptyList(),
    val nowPlaying: Track? = null,
    val isLoading: Boolean = false,
    val message: String? = null,
)

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = AppPreferences(application)
    private val vault = SecretVault(application)
    private val auth = SoundCloudAuth(application, vault)
    private val repository = SoundCloudRepository(SoundCloudApi(), vault)

    private val _state = MutableStateFlow(
        AppUiState(
            theme = preferences.theme(),
            authSettings = preferences.authSettings(),
            isConnected = repository.hasToken(),
            cleanFeed = preferences.cleanFeed(),
            favorites = preferences.favorites(),
            history = preferences.history(),
        ),
    )
    val state: StateFlow<AppUiState> = _state.asStateFlow()

    fun updateSearchQuery(value: String) {
        _state.update { it.copy(searchQuery = value) }
    }

    fun search() {
        val query = _state.value.searchQuery.trim()
        if (query.isBlank()) {
            showMessage("Введите исполнителя, трек или жанр")
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, message = null) }
            runCatching { repository.search(query, _state.value.cleanFeed) }
                .onSuccess { tracks ->
                    _state.update {
                        it.copy(
                            searchResults = tracks,
                            isLoading = false,
                            message = if (tracks.isEmpty()) "Ничего не найдено" else null,
                        )
                    }
                }
                .onFailure(::handleError)
        }
    }

    fun startWave(seed: Track = _state.value.waveSeed) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, waveSeed = seed, message = null) }
            runCatching {
                repository.wave(seed, _state.value.favorites, _state.value.cleanFeed)
            }.onSuccess { tracks ->
                _state.update {
                    it.copy(
                        waveTracks = listOf(seed) + tracks,
                        waveSeed = seed,
                        isLoading = false,
                    )
                }
            }.onFailure(::handleError)
        }
    }

    fun play(track: Track) {
        val history = (listOf(track) + _state.value.history)
            .distinctBy(Track::stableId)
            .take(100)
        preferences.saveHistory(history)
        _state.update { it.copy(nowPlaying = track, history = history) }
    }

    fun dismissPlayer() {
        _state.update { it.copy(nowPlaying = null) }
    }

    fun toggleFavorite(track: Track) {
        val old = _state.value.favorites
        val next = if (old.any { it.stableId == track.stableId }) {
            old.filterNot { it.stableId == track.stableId }
        } else {
            listOf(track) + old
        }
        preferences.saveFavorites(next)
        _state.update { it.copy(favorites = next) }
    }

    fun applyTheme(theme: ThemeProfile) {
        runCatching { ThemeCodec.validate(theme) }
            .onSuccess {
                preferences.saveTheme(it)
                _state.update { state -> state.copy(theme = it) }
            }
            .onFailure { showMessage(it.message ?: "Некорректная тема") }
    }

    fun importTheme(value: String) {
        runCatching { ThemeCodec.decode(value) }
            .onSuccess {
                applyTheme(it)
                showMessage("Тема «${it.name}» применена")
            }
            .onFailure { showMessage("Не удалось импортировать тему: ${it.message}") }
    }

    fun exportTheme(): String = ThemeCodec.encode(_state.value.theme)

    fun saveAuthSettings(settings: AuthSettings) {
        val normalized = settings.copy(
            clientId = settings.clientId.trim(),
            redirectUri = settings.redirectUri.trim().ifBlank { AppPreferences.DEFAULT_REDIRECT },
        )
        preferences.saveAuthSettings(normalized)
        _state.update { it.copy(authSettings = normalized) }
        showMessage("Настройки API сохранены")
    }

    fun authorizationUri(): Uri? = runCatching {
        auth.authorizationUri(_state.value.authSettings)
    }.onFailure { showMessage(it.message ?: "Не удалось начать вход") }.getOrNull()

    fun handleOAuthCallback(uri: Uri?) {
        if (uri?.scheme != "gptsound" || uri.host != "oauth") return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            runCatching { auth.completeCallback(uri, _state.value.authSettings) }
                .onSuccess {
                    _state.update {
                        it.copy(isConnected = true, isLoading = false, message = "SoundCloud подключён")
                    }
                }
                .onFailure(::handleError)
        }
    }

    fun logout() {
        auth.logout()
        _state.update { it.copy(isConnected = false, message = "SoundCloud отключён") }
    }

    fun setCleanFeed(enabled: Boolean) {
        preferences.saveCleanFeed(enabled)
        _state.update { it.copy(cleanFeed = enabled) }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }

    private fun showMessage(value: String) {
        _state.update { it.copy(message = value) }
    }

    private fun handleError(error: Throwable) {
        val message = when (error) {
            SoundCloudException.AuthenticationRequired -> "Сессия SoundCloud закончилась. Подключитесь заново"
            is SoundCloudException.RateLimited -> error.message
            else -> error.message ?: "Неизвестная ошибка"
        }
        _state.update { it.copy(isLoading = false, message = message) }
    }
}
