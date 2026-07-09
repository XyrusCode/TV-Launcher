package xyruscode.tv.launcher

import android.app.Application
import android.content.Context
import xyruscode.tv.launcher.di.AppContainer

class TvLauncherApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

/** Convenience accessor for the DI container from any Context (e.g. inside composables). */
fun Context.appContainer(): AppContainer = (applicationContext as TvLauncherApp).container
