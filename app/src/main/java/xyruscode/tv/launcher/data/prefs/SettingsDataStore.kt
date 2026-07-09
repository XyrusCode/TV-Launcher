package xyruscode.tv.launcher.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import xyruscode.tv.launcher.data.model.JellyfinSession

private val Context.dataStore by preferencesDataStore(name = "tv_launcher_settings")

/** Persists the Jellyfin session and the user's hidden-app list. */
class SettingsDataStore(private val context: Context) {

    private object Keys {
        val SERVER_URL = stringPreferencesKey("jf_server_url")
        val ACCESS_TOKEN = stringPreferencesKey("jf_access_token")
        val USER_ID = stringPreferencesKey("jf_user_id")
        val HIDDEN_PACKAGES = stringSetPreferencesKey("hidden_packages")
    }

    val session: Flow<JellyfinSession?> = context.dataStore.data.map { prefs ->
        val url = prefs[Keys.SERVER_URL]
        val token = prefs[Keys.ACCESS_TOKEN]
        val userId = prefs[Keys.USER_ID]
        if (!url.isNullOrBlank() && !token.isNullOrBlank() && !userId.isNullOrBlank()) {
            JellyfinSession(url, token, userId)
        } else {
            null
        }
    }

    val hiddenPackages: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[Keys.HIDDEN_PACKAGES] ?: emptySet()
    }

    suspend fun saveSession(session: JellyfinSession) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SERVER_URL] = session.serverUrl
            prefs[Keys.ACCESS_TOKEN] = session.accessToken
            prefs[Keys.USER_ID] = session.userId
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.SERVER_URL)
            prefs.remove(Keys.ACCESS_TOKEN)
            prefs.remove(Keys.USER_ID)
        }
    }

    suspend fun setHidden(packages: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HIDDEN_PACKAGES] = packages
        }
    }
}
