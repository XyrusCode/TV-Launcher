package xyruscode.tv.launcher.util

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import xyruscode.tv.launcher.R
import xyruscode.tv.launcher.data.model.AppEntry

private const val JELLYFIN_PACKAGE = "org.jellyfin.androidtv"
private const val JELLYFIN_STARTUP_ACTIVITY = "org.jellyfin.androidtv.ui.startup.StartupActivity"

object Launchers {

    /** Launches an installed app; shows a toast if it can't be started. */
    fun launchApp(context: Context, entry: AppEntry) {
        if (!tryStart(context, entry.launchIntent)) {
            toast(context, R.string.app_launch_failed)
        }
    }

    /**
     * Opens a Jellyfin item. The item deep-link into the official app is UNDOCUMENTED
     * (jellyfin-androidtv discussion #3452), so this falls back progressively:
     * 1) deep-link straight to the item, 2) just open the Jellyfin app, 3) the Play Store.
     */
    fun playJellyfinItem(context: Context, itemId: String) {
        val deepLink = Intent(Intent.ACTION_VIEW).apply {
            component = ComponentName(JELLYFIN_PACKAGE, JELLYFIN_STARTUP_ACTIVITY)
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
            data = Uri.parse(itemId)
            putExtra("source", "30")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (tryStart(context, deepLink)) return

        val open = (context.packageManager.getLeanbackLaunchIntentForPackage(JELLYFIN_PACKAGE)
            ?: context.packageManager.getLaunchIntentForPackage(JELLYFIN_PACKAGE))
            ?.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        if (open != null && tryStart(context, open)) return

        val store = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$JELLYFIN_PACKAGE"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (!tryStart(context, store)) {
            toast(context, R.string.jellyfin_app_missing)
        }
    }

    private fun tryStart(context: Context, intent: Intent): Boolean =
        try {
            context.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        } catch (_: SecurityException) {
            false
        }

    private fun toast(context: Context, resId: Int) {
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show()
    }
}
