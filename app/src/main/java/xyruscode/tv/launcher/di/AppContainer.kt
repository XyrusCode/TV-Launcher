package xyruscode.tv.launcher.di

import android.content.Context
import xyruscode.tv.launcher.data.apps.AppRepository
import xyruscode.tv.launcher.data.jellyfin.JellyfinClientProvider
import xyruscode.tv.launcher.data.jellyfin.JellyfinRepository
import xyruscode.tv.launcher.data.prefs.SettingsDataStore

/** Tiny manual DI container. Held by [xyruscode.tv.launcher.TvLauncherApp] for the app's lifetime. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val settings: SettingsDataStore by lazy { SettingsDataStore(appContext) }
    val appRepository: AppRepository by lazy { AppRepository(appContext) }

    private val jellyfinClientProvider: JellyfinClientProvider by lazy { JellyfinClientProvider(appContext) }
    val jellyfinRepository: JellyfinRepository by lazy {
        JellyfinRepository(jellyfinClientProvider, settings)
    }
}
