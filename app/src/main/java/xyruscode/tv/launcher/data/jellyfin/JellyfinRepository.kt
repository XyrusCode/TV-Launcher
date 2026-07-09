package xyruscode.tv.launcher.data.jellyfin

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.extensions.imageApi
import org.jellyfin.sdk.api.client.extensions.libraryApi
import org.jellyfin.sdk.api.client.extensions.showApi
import org.jellyfin.sdk.api.client.extensions.userApi
import org.jellyfin.sdk.api.client.extensions.userViewApi
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.ImageType
import xyruscode.tv.launcher.data.model.JellyfinHome
import xyruscode.tv.launcher.data.model.JellyfinSession
import xyruscode.tv.launcher.data.model.MediaItem
import xyruscode.tv.launcher.data.prefs.SettingsDataStore

/**
 * Talks to a Jellyfin server through the official Kotlin SDK. All row endpoints infer the user
 * from the access token, so we never have to pass a userId around.
 */
class JellyfinRepository(
    private val clientProvider: JellyfinClientProvider,
    private val settings: SettingsDataStore,
) {

    val session: Flow<JellyfinSession?> = settings.session

    /** Logs in and persists the session. Returns failure with a user-safe message on error. */
    suspend fun authenticate(rawUrl: String, username: String, password: String): Result<Unit> =
        runCatching {
            val url = normalizeUrl(rawUrl)
            val api = clientProvider.createApi(url)
            val auth = api.userApi
                .authenticateUserByName(username = username, password = password)
                .content
            val token = requireNotNull(auth.accessToken) { "Server returned no access token" }
            val userId = requireNotNull(auth.user?.id) { "Server returned no user" }.toString()
            settings.saveSession(JellyfinSession(url, token, userId))
        }

    /** Loads the four home rows in parallel; a failing row degrades to empty rather than crashing. */
    suspend fun loadHome(session: JellyfinSession): JellyfinHome = coroutineScope {
        val api = clientProvider.createApi(session.serverUrl, session.accessToken)

        val resume = async { safeItems { api.libraryApi.getResumeItems(limit = 20).content.items } }
        val nextUp = async { safeItems { api.showApi.getNextUp(limit = 20).content.items } }
        val latest = async { safeItems { api.libraryApi.getLatestMedia(limit = 20).content } }
        val views = async { safeItems { api.userViewApi.getUserViews(includeHidden = false).content.items } }

        JellyfinHome(
            continueWatching = resume.await().toMediaItems(api),
            nextUp = nextUp.await().toMediaItems(api),
            recentlyAdded = latest.await().toMediaItems(api),
            libraries = views.await().toMediaItems(api),
        )
    }

    suspend fun logout() = settings.clearSession()

    private inline fun safeItems(block: () -> List<BaseItemDto>): List<BaseItemDto> =
        runCatching(block).getOrDefault(emptyList())

    private fun List<BaseItemDto>.toMediaItems(api: ApiClient): List<MediaItem> = map { item ->
        val imageUrl = runCatching {
            api.imageApi.getItemImageUrl(
                itemId = item.id,
                imageType = ImageType.PRIMARY,
                fillWidth = 400,
                fillHeight = 600,
            )
        }.getOrNull()
        MediaItem(
            id = item.id.toString(),
            title = item.name ?: item.seriesName ?: "Untitled",
            subtitle = item.seriesName?.takeIf { it != item.name },
            imageUrl = imageUrl,
        )
    }

    private fun normalizeUrl(raw: String): String {
        val trimmed = raw.trim().trimEnd('/')
        return when {
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            else -> "http://$trimmed"
        }
    }
}
