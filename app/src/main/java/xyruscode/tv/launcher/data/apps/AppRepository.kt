package xyruscode.tv.launcher.data.apps

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyruscode.tv.launcher.data.model.AppEntry

/**
 * Enumerates every launchable app on the device.
 *
 * Google TV's home only lists apps that declare a LEANBACK_LAUNCHER filter, which is why
 * sideloaded apps disappear. We query BOTH the leanback and the standard LAUNCHER categories
 * and merge them — that's what brings the hidden apps back.
 */
class AppRepository(context: Context) {

    private val appContext = context.applicationContext
    private val pm = appContext.packageManager
    private val ownPackage = appContext.packageName

    suspend fun loadApps(hidden: Set<String>): List<AppEntry> = withContext(Dispatchers.IO) {
        // Leanback entries come first so their 16:9 banners win during de-duplication.
        val resolved = queryFor(Intent.CATEGORY_LEANBACK_LAUNCHER) + queryFor(Intent.CATEGORY_LAUNCHER)

        val byPackage = LinkedHashMap<String, AppEntry>()
        for (info in resolved) {
            val pkg = info.activityInfo?.packageName ?: continue
            if (pkg == ownPackage || pkg in hidden || byPackage.containsKey(pkg)) continue

            val launch = (pm.getLeanbackLaunchIntentForPackage(pkg)
                ?: pm.getLaunchIntentForPackage(pkg))
                ?.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
                ?: continue

            byPackage[pkg] = AppEntry(
                packageName = pkg,
                label = info.loadLabel(pm)?.toString()?.ifBlank { pkg } ?: pkg,
                banner = runCatching { info.activityInfo.loadBanner(pm) }.getOrNull(),
                icon = runCatching { info.loadIcon(pm) }.getOrNull(),
                launchIntent = launch,
            )
        }

        byPackage.values.sortedBy { it.label.lowercase() }
    }

    @Suppress("DEPRECATION", "QueryPermissionsNeeded")
    private fun queryFor(category: String): List<ResolveInfo> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(category)
        return runCatching { pm.queryIntentActivities(intent, 0) }.getOrDefault(emptyList())
    }
}
