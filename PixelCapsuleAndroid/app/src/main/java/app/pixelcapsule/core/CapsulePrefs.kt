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
    private const val KEY_CONTACTS = "contact_shortcuts"
    private const val KEY_WIDGET_TEXT = "custom_widget_text"
    private const val KEY_ACCENT = "accent_color_argb"
    private const val KEY_TAP = "gesture_tap"
    private const val KEY_DOUBLE_TAP = "gesture_double_tap"
    private const val KEY_LONG_PRESS = "gesture_long_press"
    private const val KEY_SWIPE_LEFT = "gesture_swipe_left"
    private const val KEY_SWIPE_RIGHT = "gesture_swipe_right"
    private const val KEY_SWIPE_UP = "gesture_swipe_up"
    private const val KEY_SWIPE_DOWN = "gesture_swipe_down"

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
            quickApps = p.getStringSet(KEY_QUICK_APPS, emptySet())?.toSet().orEmpty(),
            contacts = p.getStringSet(KEY_CONTACTS, emptySet())?.toSet().orEmpty(),
            widgetText = p.getString(KEY_WIDGET_TEXT, "").orEmpty(),
            accentColorArgb = p.getLong(KEY_ACCENT, 0xFFB9D69AL),
            tapAction = p.getString(KEY_TAP, "toggle") ?: "toggle",
            doubleTapAction = p.getString(KEY_DOUBLE_TAP, "next_section") ?: "next_section",
            longPressAction = p.getString(KEY_LONG_PRESS, "settings") ?: "settings",
            swipeLeftAction = p.getString(KEY_SWIPE_LEFT, "next_section") ?: "next_section",
            swipeRightAction = p.getString(KEY_SWIPE_RIGHT, "previous_section") ?: "previous_section",
            swipeUpAction = p.getString(KEY_SWIPE_UP, "expand") ?: "expand",
            swipeDownAction = p.getString(KEY_SWIPE_DOWN, "collapse") ?: "collapse"
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
            .putStringSet(KEY_CONTACTS, next.contacts.toSet())
            .putString(KEY_WIDGET_TEXT, next.widgetText.take(500))
            .putLong(KEY_ACCENT, next.accentColorArgb)
            .putString(KEY_TAP, next.tapAction)
            .putString(KEY_DOUBLE_TAP, next.doubleTapAction)
            .putString(KEY_LONG_PRESS, next.longPressAction)
            .putString(KEY_SWIPE_LEFT, next.swipeLeftAction)
            .putString(KEY_SWIPE_RIGHT, next.swipeRightAction)
            .putString(KEY_SWIPE_UP, next.swipeUpAction)
            .putString(KEY_SWIPE_DOWN, next.swipeDownAction)
            .apply()
    }

    fun addContact(context: Context, uri: String, name: String) {
        val entry = uri + "|" + name.replace("|", " ").take(80)
        val p = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        val contacts = p.getStringSet(KEY_CONTACTS, emptySet())?.toMutableSet() ?: mutableSetOf()
        contacts.removeAll { it.substringBefore('|') == uri }
        contacts.add(entry)
        p.edit().putStringSet(KEY_CONTACTS, contacts.toSet()).apply()
    }

    fun removeContact(context: Context, uri: String) {
        val p = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        val contacts = p.getStringSet(KEY_CONTACTS, emptySet())?.filterNot { it.substringBefore('|') == uri }?.toSet().orEmpty()
        p.edit().putStringSet(KEY_CONTACTS, contacts).apply()
    }

    fun setQuickApps(context: Context, packages: Set<String>) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit()
            .putStringSet(KEY_QUICK_APPS, packages.toSet())
            .apply()
    }
}
