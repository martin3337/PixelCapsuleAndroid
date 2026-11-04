package app.pixelcapsule.system

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

 data class LaunchableApp(val packageName: String, val label: String)

object AppCatalog {
    fun installedLauncherApps(context: Context): List<LaunchableApp> {
        val pm = context.packageManager
        val query = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val matches = if (Build.VERSION.SDK_INT >= 33) {
            pm.queryIntentActivities(query, PackageManager.ResolveInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(query, 0)
        }
        return matches
            .mapNotNull { info ->
                val packageName = info.activityInfo?.packageName ?: return@mapNotNull null
                val label = info.loadLabel(pm)?.toString()?.takeIf { it.isNotBlank() } ?: packageName
                LaunchableApp(packageName, label)
            }
            .distinctBy { it.packageName }
            .filterNot { it.packageName == context.packageName }
            .sortedBy { it.label.lowercase() }
    }

    fun launch(context: Context, packageName: String): Boolean {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching { context.startActivity(intent) }.isSuccess
    }
}
