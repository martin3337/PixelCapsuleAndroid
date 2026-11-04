package app.pixelcapsule.core

import android.content.Context

object CapsulePrefs {
    private const val FILE = "pixel_capsule_preferences"
    private const val KEY_WIDTH = "width_dp"
    private const val KEY_EXPANDED_WIDTH = "expanded_width_dp"
    private const val KEY_RADIUS = "corner_radius_dp"
    private const val KEY_X = "offset_x_dp"
    private const val KEY_Y = "offset_y_dp"
    private const val KEY_DYNAMIC = "dynamic_colors"
    private const val KEY_MEDIA = "show_media"
    private const val KEY_NOTIFICATIONS = "show_notifications"
    private const val KEY_CONTROLS = "show_controls"
    private const val KEY_APPS = "show_apps"
    private const val KEY_QUICK_APPS = "quick_apps"

    fun read(context: Context): OverlayPreferences {
        val p = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        return OverlayPreferences(
            widthDp = p.getFloat(KEY_WIDTH, 154f),
            expandedWidthDp = p.getFloat(KEY_EXPANDED_WIDTH, 356f),
            cornerRadiusDp = p.getFloat(KEY_RADIUS, 32f),
            offsetXDp = p.getFloat(KEY_X, 0f),
            offsetYDp = p.getFloat(KEY_Y, 0f),
            dynamicColors = p.getBoolean(KEY_DYNAMIC, true),
            showMedia = p.getBoolean(KEY_MEDIA, true),
            showNotifications = p.getBoolean(KEY_NOTIFICATIONS, true),
            showControls = p.getBoolean(KEY_CONTROLS, true),
            showApps = p.getBoolean(KEY_APPS, true),
            quickApps = p.getStringSet(KEY_QUICK_APPS, emptySet())?.toSet().orEmpty()
        )
    }

    fun write(context: Context, update: OverlayPreferences.() -> OverlayPreferences) {
        val current = read(context)
        val next = current.update()
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit()
            .putFloat(KEY_WIDTH, next.widthDp.coerceIn(104f, 220f))
            .putFloat(KEY_EXPANDED_WIDTH, next.expandedWidthDp.coerceIn(260f, 420f))
            .putFloat(KEY_RADIUS, next.cornerRadiusDp.coerceIn(16f, 48f))
            .putFloat(KEY_X, next.offsetXDp.coerceIn(-160f, 160f))
            .putFloat(KEY_Y, next.offsetYDp.coerceIn(-40f, 120f))
            .putBoolean(KEY_DYNAMIC, next.dynamicColors)
            .putBoolean(KEY_MEDIA, next.showMedia)
            .putBoolean(KEY_NOTIFICATIONS, next.showNotifications)
            .putBoolean(KEY_CONTROLS, next.showControls)
            .putBoolean(KEY_APPS, next.showApps)
            .putStringSet(KEY_QUICK_APPS, next.quickApps.toSet())
            .apply()
    }

    fun setQuickApps(context: Context, packages: Set<String>) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit()
            .putStringSet(KEY_QUICK_APPS, packages.toSet())
            .apply()
    }
}
