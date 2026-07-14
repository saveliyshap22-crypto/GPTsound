package app.gptsound.android.ui.screens

import android.content.Intent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.gptsound.android.domain.AuthSettings
import app.gptsound.android.domain.ThemeProfile
import app.gptsound.android.ui.components.GlassCard
import app.gptsound.android.ui.components.GlowButton
import app.gptsound.android.ui.components.SectionHeader
import app.gptsound.android.ui.components.StatusChip
import app.gptsound.android.ui.theme.colorFromHex

@Composable
fun DesignScreen(
    theme: ThemeProfile,
    authSettings: AuthSettings,
    connected: Boolean,
    cleanFeed: Boolean,
    exportTheme: () -> String,
    onApplyTheme: (ThemeProfile) -> Unit,
    onImportTheme: (String) -> Unit,
    onSaveAuth: (AuthSettings) -> Unit,
    onConnect: () -> android.net.Uri?,
    onLogout: () -> Unit,
    onCleanFeedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    var themeName by remember(theme.name) { mutableStateOf(theme.name) }
    var accent by remember(theme.accent) { mutableStateOf(theme.accent) }
    var secondary by remember(theme.secondary) { mutableStateOf(theme.secondary) }
    var backgroundTop by remember(theme.backgroundTop) { mutableStateOf(theme.backgroundTop) }
    var backgroundBottom by remember(theme.backgroundBottom) { mutableStateOf(theme.backgroundBottom) }
    var glassAlpha by remember(theme.glassAlpha) { mutableFloatStateOf(theme.glassAlpha) }
    var cornerRadius by remember(theme.cornerRadius) { mutableIntStateOf(theme.cornerRadius) }
    var glowStrength by remember(theme.glowStrength) { mutableFloatStateOf(theme.glowStrength) }
    var clientId by remember(authSettings.clientId) { mutableStateOf(authSettings.clientId) }
    var redirectUri by remember(authSettings.redirectUri) { mutableStateOf(authSettings.redirectUri) }

    fun draftTheme() = ThemeProfile(
        name = themeName,
        accent = accent,
        secondary = secondary,
        backgroundTop = backgroundTop,
        backgroundBottom = backgroundBottom,
        glassAlpha = glassAlpha,
        cornerRadius = cornerRadius,
        glowStrength = glowStrength,
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 24.dp, bottom = 118.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Дизайн", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
                    Text("Соберите собственный GPTsound", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusChip(if (connected) "API онлайн" else "Демо", connected)
            }
        }

        item { SectionHeader("Готовые стили", "Их можно менять после выбора") }
        item {
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ThemeProfile.presets.forEach { preset ->
                    GlassCard(
                        profile = preset,
                        modifier = Modifier.width(150.dp),
                        onClick = { onApplyTheme(preset) },
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("●", color = colorFromHex(preset.accent), style = MaterialTheme.typography.headlineMedium)
                            Text(preset.name, fontWeight = FontWeight.Bold)
                            Text("Применить →", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        item {
            GlassCard(theme, Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader("Редактор темы", "Цвета задаются в формате #RRGGBB")
                    ThemeTextField("Название", themeName) { themeName = it }
                    ThemeTextField("Акцент", accent) { accent = it }
                    ThemeTextField("Второй цвет", secondary) { secondary = it }
                    ThemeTextField("Фон сверху", backgroundTop) { backgroundTop = it }
                    ThemeTextField("Фон снизу", backgroundBottom) { backgroundBottom = it }

                    Text("Прозрачность стекла ${(glassAlpha * 100).toInt()}%")
                    Slider(value = glassAlpha, onValueChange = { glassAlpha = it }, valueRange = 0.05f..0.35f)
                    Text("Скругление $cornerRadius dp")
                    Slider(
                        value = cornerRadius.toFloat(),
                        onValueChange = { cornerRadius = it.toInt() },
                        valueRange = 12f..40f,
                    )
                    Text("Сила свечения ${(glowStrength * 100).toInt()}%")
                    Slider(value = glowStrength, onValueChange = { glowStrength = it }, valueRange = 0f..1f)

                    GlowButton(
                        text = "Применить дизайн",
                        profile = theme,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onApplyTheme(draftTheme()) },
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = { clipboard.setText(AnnotatedString(exportTheme())) }) {
                            Text("Копировать JSON")
                        }
                        TextButton(
                            onClick = {
                                clipboard.getText()?.text?.takeIf(String::isNotBlank)?.let(onImportTheme)
                            },
                        ) {
                            Text("Импорт из буфера")
                        }
                    }
                }
            }
        }

        item {
            GlassCard(theme, Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                    SectionHeader("SoundCloud API", "OAuth 2.1 + PKCE, секреты не вшиваются в APK")
                    ThemeTextField("Client ID", clientId) { clientId = it }
                    ThemeTextField("Redirect URI", redirectUri) { redirectUri = it }
                    Text(
                        "Добавьте gptsound://oauth/callback в Redirect URI вашего приложения на SoundCloud Developers.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    GlowButton(
                        text = if (connected) "Переподключить SoundCloud" else "Подключить SoundCloud",
                        profile = theme,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onSaveAuth(AuthSettings(clientId, redirectUri))
                            onConnect()?.let { uri ->
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            }
                        },
                    )
                    if (connected) {
                        TextButton(onClick = onLogout) { Text("Отключить аккаунт") }
                    }
                }
            }
        }

        item {
            GlassCard(theme, Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Чистая лента", fontWeight = FontWeight.Bold)
                            Text(
                                "Скрывает треки с явными рекламными метками в названии или описании.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(checked = cleanFeed, onCheckedChange = onCleanFeedChange)
                    }
                    Text(
                        "GPTsound не показывает собственной рекламы. Рекламу, доставляемую SoundCloud через официальный плеер, приложение не блокирует и не изменяет.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item {
            Text(
                "GPTsound — независимый open-source проект. Он не является официальным приложением SoundCloud.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 6.dp),
            )
        }
    }
}

@Composable
private fun ThemeTextField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
    )
}
