package xyruscode.tv.launcher.data.jellyfin

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.extensions.authenticateUserByName
import org.jellyfin.sdk.api.client.extensions.authenticateWithQuickConnect
import org.jellyfin.sdk.api.client.extensions.imageApi
import org.jellyfin.sdk.api.client.extensions.itemsApi
import org.jellyfin.sdk.api.client.extensions.quickConnectApi
import org.jellyfin.sdk.api.client.extensions.tvShowsApi
import org.jellyfin.sdk.api.client.extensions.userApi
import org.jellyfin.sdk.api.client.extensions.userLibraryApi
import org.jellyfin.sdk.api.client.extensions.userViewsApi
import org.jellyfin.sdk.model.api.BaseItemDto
import org.jellyfin.sdk.model.api.ImageType
import xyruscode.tv.launcher.data.model.DiscoveredServer
import xyruscode.tv.launcher.data.model.JellyfinHome
import xyruscode.tv.launcher.data.model.JellyfinSession
import xyruscode.tv.launcher.data.model.MediaItem
import xyruscode.tv.launcher.data.model.QuickConnectStart
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

    /**
     * Broadcasts on the LAN and returns the Jellyfin servers that answer within the SDK's
     * discovery timeout. Deduped by address; empty (never throws) if none respond.
     */
    suspend fun discoverServers(): List<DiscoveredServer> = withContext(Dispatchers.IO) {
        runCatching {
            clientProvider.discoverLocalServers()
                .toList()
                .map { DiscoveredServer(name = it.name, address = it.address) }
                .distinctBy { normalizeUrl(it.address) }
        }.getOrDefault(emptyList())
    }

    /**
     * Initiates Quick Connect on [rawUrl]. Returns the code to show the user plus the secret to
     * poll. Fails when the server has Quick Connect disabled — the UI falls back to password login.
     */
    suspend fun startQuickConnect(rawUrl: String): Result<QuickConnectStart> = runCatching {
        val api = clientProvider.createApi(normalizeUrl(rawUrl))
        val result = api.quickConnectApi.initiateQuickConnect().content
        QuickConnectStart(code = result.code, secret = result.secret)
    }

    /** True once the user has approved the Quick Connect request from another Jellyfin client. */
    suspend fun pollQuickConnect(rawUrl: String, secret: String): Result<Boolean> = runCatching {
        val api = clientProvider.createApi(normalizeUrl(rawUrl))
        api.quickConnectApi.getQuickConnectState(secret).content.authenticated
    }

    /** Exchanges an approved Quick Connect secret for a token and persists the session. */
    suspend fun finishQuickConnect(rawUrl: String, secret: String): Result<Unit> = runCatching {
        val url = normalizeUrl(rawUrl)
        val api = clientProvider.createApi(url)
        val auth = api.userApi.authenticateWithQuickConnect(secret = secret).content
        val token = requireNotNull(auth.accessToken) { "Server returned no access token" }
        val userId = requireNotNull(auth.user?.id) { "Server returned no user" }.toString()
        settings.saveSession(JellyfinSession(url, token, userId))
    }

    /** Loads the four home rows in parallel; a failing row degrades to empty rather than crashing. */
    suspend fun loadHome(session: JellyfinSession): JellyfinHome = coroutineScope {
        val api = clientProvider.createApi(session.serverUrl, session.accessToken)

        val resume = async { safeItems { api.itemsApi.getResumeItems(limit = 20).content.items } }
        val nextUp = async { safeItems { api.tvShowsApi.getNextUp(limit = 20).content.items } }
        val latest = async { safeItems { api.userLibraryApi.getLatestMedia(limit = 20).content } }
        val views = async { safeItems { api.userViewsApi.getUserViews(includeHidden = false).content.items } }

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
