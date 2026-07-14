package app.gptsound.android.ui.player

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import app.gptsound.android.domain.ThemeProfile
import app.gptsound.android.domain.Track
import app.gptsound.android.ui.components.GlowButton
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundCloudPlayerSheet(
    track: Track,
    profile: ThemeProfile,
    onDismiss: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Text(track.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(track.artist, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(5.dp))

            if (track.isDemo || track.permalinkUrl.isBlank()) {
                Text(
                    "Это демонстрационная карточка. Подключите SoundCloud API в разделе «Дизайн», чтобы искать и слушать реальные треки.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                SoundCloudWidgetView(track.permalinkUrl, Modifier.fillMaxWidth().height(178.dp))
                GlowButton(
                    text = "Открыть источник в SoundCloud",
                    profile = profile,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { uriHandler.openUri(track.permalinkUrl) },
                )
                Text(
                    "Воспроизведение идёт через официальный SoundCloud Widget и сохраняет атрибуцию и правила платформы.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(22.dp))
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun SoundCloudWidgetView(permalinkUrl: String, modifier: Modifier = Modifier) {
    val encoded = URLEncoder.encode(permalinkUrl, StandardCharsets.UTF_8.toString())
    val html = """
        <!doctype html>
        <html><head><meta name="viewport" content="width=device-width,initial-scale=1"></head>
        <body style="margin:0;background:transparent;overflow:hidden">
          <iframe width="100%" height="166" scrolling="no" frameborder="no" allow="autoplay"
            src="https://w.soundcloud.com/player/?url=$encoded&color=%238be9ff&auto_play=false&show_artwork=true&show_user=true&show_playcount=true&sharing=true&download=false">
          </iframe>
        </body></html>
    """.trimIndent()

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = true
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                loadDataWithBaseURL("https://w.soundcloud.com", html, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            if (webView.tag != permalinkUrl) {
                webView.tag = permalinkUrl
                webView.loadDataWithBaseURL("https://w.soundcloud.com", html, "text/html", "UTF-8", null)
            }
        },
    )
}
