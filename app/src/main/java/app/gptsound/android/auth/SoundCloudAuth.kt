package app.gptsound.android.auth

import android.content.Context
import android.net.Uri
import android.util.Base64
import app.gptsound.android.data.SecretVault
import app.gptsound.android.domain.AuthSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.concurrent.TimeUnit

class SoundCloudAuth(
    context: Context,
    private val vault: SecretVault,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .callTimeout(30, TimeUnit.SECONDS)
        .build(),
) {
    private val pending = context.getSharedPreferences("gptsound_oauth_pending", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    fun authorizationUri(settings: AuthSettings): Uri {
        require(settings.clientId.isNotBlank()) { "Укажите SoundCloud Client ID" }
        require(settings.redirectUri.isNotBlank()) { "Укажите redirect URI" }
        val verifier = randomUrlSafe(64)
        val challenge = Base64.encodeToString(
            MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray()),
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING,
        )
        val state = randomUrlSafe(24)
        pending.edit()
            .putString(KEY_VERIFIER, verifier)
            .putString(KEY_STATE, state)
            .apply()

        return Uri.parse(AUTHORIZE_URL).buildUpon()
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("client_id", settings.clientId.trim())
            .appendQueryParameter("redirect_uri", settings.redirectUri.trim())
            .appendQueryParameter("code_challenge", challenge)
            .appendQueryParameter("code_challenge_method", "S256")
            .appendQueryParameter("state", state)
            .build()
    }

    suspend fun completeCallback(callback: Uri, settings: AuthSettings) = withContext(Dispatchers.IO) {
        validateRedirect(callback, settings.redirectUri)
        callback.getQueryParameter("error")?.let { error ->
            throw OAuthException("SoundCloud отклонил вход: $error")
        }
        val expectedState = pending.getString(KEY_STATE, null)
        val actualState = callback.getQueryParameter("state")
        if (expectedState.isNullOrBlank() || expectedState != actualState) {
            throw OAuthException("OAuth state не совпал. Начните вход заново")
        }
        val verifier = pending.getString(KEY_VERIFIER, null)
            ?: throw OAuthException("Сессия входа устарела")
        val code = callback.getQueryParameter("code")
            ?: throw OAuthException("SoundCloud не вернул код авторизации")

        val body = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("client_id", settings.clientId.trim())
            .add("redirect_uri", settings.redirectUri.trim())
            .add("code_verifier", verifier)
            .add("code", code)
            .build()
        val request = Request.Builder()
            .url(TOKEN_URL)
            .header("Accept", "application/json; charset=utf-8")
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw OAuthException("Ошибка OAuth ${response.code}: ${responseBody.take(180)}")
            }
            val token = json.decodeFromString(TokenResponse.serializer(), responseBody)
            if (token.accessToken.isBlank()) throw OAuthException("Пустой access token")
            vault.saveAccessToken(token.accessToken)
            pending.edit().clear().apply()
        }
    }

    fun logout() {
        vault.clear()
        pending.edit().clear().apply()
    }

    private fun validateRedirect(callback: Uri, expected: String) {
        val expectedUri = Uri.parse(expected)
        if (callback.scheme != expectedUri.scheme || callback.host != expectedUri.host ||
            callback.path != expectedUri.path
        ) {
            throw OAuthException("Получен неизвестный OAuth redirect")
        }
    }

    private fun randomUrlSafe(bytes: Int): String {
        val random = ByteArray(bytes).also(SecureRandom()::nextBytes)
        return Base64.encodeToString(random, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    companion object {
        private const val AUTHORIZE_URL = "https://secure.soundcloud.com/authorize"
        private const val TOKEN_URL = "https://secure.soundcloud.com/oauth/token"
        private const val KEY_VERIFIER = "verifier"
        private const val KEY_STATE = "state"
    }
}

class OAuthException(message: String) : IOException(message)

@Serializable
private data class TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("expires_in") val expiresIn: Long? = null,
)
