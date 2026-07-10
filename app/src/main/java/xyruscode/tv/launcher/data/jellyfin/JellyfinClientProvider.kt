package xyruscode.tv.launcher.data.jellyfin

import android.content.Context
import kotlinx.coroutines.flow.Flow
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.createJellyfin
import org.jellyfin.sdk.model.ClientInfo
import org.jellyfin.sdk.model.api.ServerDiscoveryInfo

/** Owns the shared [org.jellyfin.sdk.Jellyfin] instance and mints per-session API clients. */
class JellyfinClientProvider(context: Context) {

    // Captured before the builder lambda: inside createJellyfin { } the receiver has its own
    // `context` property that would otherwise shadow this constructor parameter.
    private val appContext = context.applicationContext

    private val jellyfin = createJellyfin {
        clientInfo = ClientInfo(name = "Xyrus TV", version = "1.1.0")
        this.context = appContext
    }

    fun createApi(baseUrl: String, accessToken: String? = null): ApiClient =
        jellyfin.createApi(baseUrl = baseUrl, accessToken = accessToken)

    /** UDP-broadcasts on the LAN and emits each Jellyfin server that answers. */
    fun discoverLocalServers(): Flow<ServerDiscoveryInfo> =
        jellyfin.discovery.discoverLocalServers()
}
