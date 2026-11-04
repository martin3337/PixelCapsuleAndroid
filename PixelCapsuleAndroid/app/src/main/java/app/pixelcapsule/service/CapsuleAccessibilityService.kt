package app.pixelcapsule.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.view.accessibility.AccessibilityEvent
import app.pixelcapsule.core.CapsuleRuntime

/**
 * Optional helper. It observes only window-state changes and stores the foreground
 * app label. It never requests or reads view hierarchy, text, passwords or page content.
 * It also provides the user-triggered swipe gesture for "scroll to top".
 */
class CapsuleAccessibilityService : AccessibilityService() {
    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val packageName = event.packageName?.toString()?.takeIf { it.isNotBlank() } ?: return
        if (packageName == applicationContext.packageName) return

        val appName = runCatching {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0)).toString()
        }.getOrDefault(packageName)
        CapsuleRuntime.setForegroundApp(packageName, appName)
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        if (instance === this) {
            instance = null
            CapsuleRuntime.setForegroundApp(null, null)
        }
        super.onDestroy()
    }

    companion object {
        @Volatile
        private var instance: CapsuleAccessibilityService? = null

        /** The user explicitly triggers this gesture from the capsule. */
        fun scrollToTop(): Boolean {
            val service = instance ?: return false
            val metrics = service.resources.displayMetrics
            val path = Path().apply {
                moveTo(metrics.widthPixels / 2f, metrics.heightPixels * 0.14f)
                lineTo(metrics.widthPixels / 2f, metrics.heightPixels * 0.84f)
            }
            val gesture = GestureDescription.Builder()
                .addStroke(GestureDescription.StrokeDescription(path, 0L, 260L))
                .build()
            return runCatching { service.dispatchGesture(gesture, null, null) }.getOrDefault(false)
        }
    }
}