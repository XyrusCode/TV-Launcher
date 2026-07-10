package xyruscode.tv.launcher.data.model

import android.content.Intent
import android.graphics.drawable.Drawable

/** An installed, launchable app — including sideloaded ones Google TV hides. */
data class AppEntry(
    val packageName: String,
    val label: String,
    /** Leanback banner (16:9) if the app ships one; else null. */
    val banner: Drawable?,
    /** Square launcher icon; always present. */
    val icon: Drawable?,
    val launchIntent: Intent,
)

/** A Jellyfin library item reduced to what the home rows render. */
data class MediaItem(
    val id: String,
    val title: String,
    val subtitle: String?,
    val imageUrl: String?,
)

/** The four Jellyfin rows shown above the app grid. */
data class JellyfinHome(
    val continueWatching: List<MediaItem> = emptyList(),
    val nextUp: List<MediaItem> = emptyList(),
    val recentlyAdded: List<MediaItem> = emptyList(),
    val libraries: List<MediaItem> = emptyList(),
) {
    val isEmpty: Boolean
        get() = continueWatching.isEmpty() && nextUp.isEmpty() &&
            recentlyAdded.isEmpty() && libraries.isEmpty()
}

/** Persisted Jellyfin session. Null when not connected. */
data class JellyfinSession(
    val serverUrl: String,
    val accessToken: String,
    val userId: String,
)

/** A Jellyfin server found on the local network via UDP auto-discovery. */
data class DiscoveredServer(
    val name: String,
    val address: String,
)

/** An initiated Quick Connect request: [code] is shown to the user, [secret] is polled. */
data class QuickConnectStart(
    val code: String,
    val secret: String,
)
