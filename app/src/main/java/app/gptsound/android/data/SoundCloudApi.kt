package app.gptsound.android.data

import app.gptsound.android.domain.ApiPage
import app.gptsound.android.domain.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

class SoundCloudApi(
    private val client: OkHttpClient = defaultClient(),
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val baseUrl = "https://api.soundcloud.com/".toHttpUrl()

    suspend fun searchTracks(token: String, query: String, limit: Int = 30): ApiPage {
        val url = baseUrl.newBuilder()
            .addPathSegment("tracks")
            .addQueryParameter("q", query.trim())
            .addQueryParameter("access", "playable")
            .addQueryParameter("limit", limit.coerceIn(1, 50).toString())
            .addQueryParameter("linked_partitioning", "true")
            .build()
        return getTrackPage(url, token)
    }

    suspend fun relatedTracks(token: String, trackUrn: String, limit: Int = 40): ApiPage {
        require(trackUrn.startsWith("soundcloud:tracks:")) { "SoundCloud track URN required" }
        val url = baseUrl.newBuilder()
            .addPathSegment("tracks")
            .addPathSegment(trackUrn)
            .addPathSegment("related")
            .addQueryParameter("access", "playable")
            .addQueryParameter("limit", limit.coerceIn(1, 50).toString())
            .addQueryParameter("linked_partitioning", "true")
            .build()
        return getTrackPage(url, token)
    }

    private suspend fun getTrackPage(url: HttpUrl, token: String): ApiPage = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "OAuth $token")
            .header("Accept", "application/json; charset=utf-8")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            when (response.code) {
                401 -> throw SoundCloudException.AuthenticationRequired
                429 -> throw SoundCloudException.RateLimited(
                    retryAfterSeconds = response.header("Retry-After")?.toLongOrNull(),
                )
            }
            if (!response.isSuccessful) {
                throw SoundCloudException.Http(response.code, body.take(500))
            }
            val page = runCatching { json.decodeFromString(ApiTrackPage.serializer(), body) }
                .getOrElse {
                    val items = json.decodeFromString(
                        kotlinx.serialization.builtins.ListSerializer(ApiTrack.serializer()),
                        body,
                    )
                    ApiTrackPage(collection = items)
                }
            ApiPage(page.collection.map(ApiTrack::toDomain), page.nextHref)
        }
    }

    companion object {
        private fun defaultClient() = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}

sealed class SoundCloudException(message: String) : IOException(message) {
    data object AuthenticationRequired : SoundCloudException("Нужно заново подключить SoundCloud")
    data class RateLimited(val retryAfterSeconds: Long?) : SoundCloudException(
        retryAfterSeconds?.let { "Лимит API. Повторите через $it с" } ?: "Достигнут лимит SoundCloud API",
    )
    data class Http(val status: Int, val payload: String) : SoundCloudException(
        "SoundCloud API вернул HTTP $status",
    )
}

@Serializable
private data class ApiTrackPage(
    val collection: List<ApiTrack> = emptyList(),
    @SerialName("next_href") val nextHref: String? = null,
)

@Serializable
private data class ApiTrack(
    val urn: String? = null,
    val id: Long? = null,
    val title: String = "Без названия",
    val user: ApiUser? = null,
    @SerialName("artwork_url") val artworkUrl: String? = null,
    @SerialName("permalink_url") val permalinkUrl: String? = null,
    val duration: Long = 0,
    val genre: String? = null,
    val description: String? = null,
    val access: String? = null,
) {
    fun toDomain() = Track(
        urn = urn ?: id?.let { "soundcloud:tracks:$it" }.orEmpty(),
        title = title,
        artist = user?.username ?: "Неизвестный автор",
        artworkUrl = artworkUrl?.replace("-large.", "-t500x500."),
        permalinkUrl = permalinkUrl.orEmpty(),
        durationMs = duration,
        genre = genre.orEmpty(),
        description = description.orEmpty(),
        access = access ?: "playable",
    )
}

@Serializable
private data class ApiUser(
    val urn: String? = null,
    val username: String = "Неизвестный автор",
)
