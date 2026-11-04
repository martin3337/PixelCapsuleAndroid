package app.pixelcapsule.core

import android.graphics.Bitmap

/** Immutable state shared by the activity, notification listener and overlay window. */
data class MediaInfo(
    val packageName: String,
    val appName: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val positionMs: Long,
    val playing: Boolean,
    val artwork: Bitmap? = null
)

data class NotificationInfo(
    val key: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val postedAt: Long,
    val progress: Int? = null,
    val progressMax: Int? = null,
    val indeterminate: Boolean = false,
    val canReply: Boolean = false
)

data class CallInfo(
    val state: Int = CALL_IDLE,
    val startedElapsedRealtime: Long = 0L
) {
    val isRinging: Boolean get() = state == CALL_RINGING
    val isActive: Boolean get() = state == CALL_OFFHOOK
    val isVisible: Boolean get() = isRinging || isActive

    companion object {
        const val CALL_IDLE = 0
        const val CALL_RINGING = 1
        const val CALL_OFFHOOK = 2
    }
}

data class CapsuleUiState(
    val media: MediaInfo? = null,
    val notifications: List<NotificationInfo> = emptyList(),
    val call: CallInfo = CallInfo(),
    val batteryPercent: Int = -1,
    val isCharging: Boolean = false,
    val chargingSource: String = "",
    val bluetoothConnected: Boolean = false,
    val bluetoothBatteryPercent: Int? = null,
    val downloadPercent: Int? = null,
    val quickApps: Set<String> = emptySet(),
    val foregroundPackageName: String? = null,
    val foregroundAppName: String? = null,
    val batteryTemperatureC: Float? = null,
    val batteryVoltageMv: Int? = null,
    val batteryCurrentMa: Int? = null,
    val batteryHealth: String? = null
)

data class ContactShortcut(val uri: String, val name: String)

data class OverlayPreferences(
    val widthDp: Float = 154f,
    val expandedWidthDp: Float = 356f,
    val cornerRadiusDp: Float = 32f,
    val offsetXDp: Float = 0f,
    val offsetYDp: Float = 0f,
    val dynamicColors: Boolean = true,
    val showMedia: Boolean = true,
    val showNotifications: Boolean = true,
    val showControls: Boolean = true,
    val showApps: Boolean = true,
    val quickApps: Set<String> = emptySet(),
    val contacts: Set<String> = emptySet(),
    val widgetText: String = "",
    val accentColorArgb: Long = 0xFFB9D69A,
    val tapAction: String = "toggle",
    val doubleTapAction: String = "next_section",
    val longPressAction: String = "settings",
    val swipeLeftAction: String = "next_section",
    val swipeRightAction: String = "previous_section",
    val swipeUpAction: String = "expand",
    val swipeDownAction: String = "collapse"
)

fun formatDuration(milliseconds: Long): String {
    val totalSeconds = (milliseconds.coerceAtLeast(0L) / 1000L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "%d:%02d".format(minutes, seconds)
}
