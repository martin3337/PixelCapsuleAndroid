package app.pixelcapsule.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PixelFormat
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.view.Gravity
import android.view.WindowManager
import android.provider.Settings
import android.view.WindowManager.LayoutParams
import android.content.pm.ServiceInfo
import android.bluetooth.BluetoothDevice
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.ViewTreeSavedStateRegistryOwner
import app.pixelcapsule.MainActivity
import app.pixelcapsule.R
import app.pixelcapsule.core.CapsulePrefs
import app.pixelcapsule.core.CapsuleRuntime
import app.pixelcapsule.ui.CapsuleOverlayUi
import app.pixelcapsule.ui.PixelCapsuleTheme

class CapsuleOverlayService : Service(), LifecycleOwner, SavedStateRegistryOwner {
    private val serviceLifecycle = LifecycleRegistry(this)
    private val savedStateController = SavedStateRegistryController.create(this)
    override val lifecycle: Lifecycle get() = serviceLifecycle
    override val savedStateRegistry get() = savedStateController.savedStateRegistry
    private lateinit var windowManager: WindowManager
    private var overlayView: ComposeView? = null
    private var overlayParams: LayoutParams? = null
    private var receiverRegistered = false
    private var phoneListener: PhoneStateListener? = null
    private var telephonyCallback: TelephonyCallback? = null

    private val stateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_BATTERY_CHANGED -> updateBattery(intent)
                BluetoothDevice.ACTION_ACL_CONNECTED -> CapsuleRuntime.setBluetoothConnected(true)
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> CapsuleRuntime.setBluetoothConnected(false)
                ACTION_BLUETOOTH_BATTERY_LEVEL_CHANGED -> {
                    val percent = intent.getIntExtra(EXTRA_BLUETOOTH_BATTERY_LEVEL, -1)
                    CapsuleRuntime.setBluetoothBatteryPercent(percent.takeIf { it in 0..100 })
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        savedStateController.performAttach()
        savedStateController.performRestore(null)
        serviceLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        startAsForeground()
        registerDeviceReceivers()
        registerPhoneState()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }
        registerDeviceReceivers()
        if (phoneListener == null && telephonyCallback == null) registerPhoneState()
        if (overlayView != null) removeOverlay()
        serviceLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)
        serviceLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        addOverlay()
        if (overlayView == null) {
            serviceLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            serviceLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        }
        return START_STICKY
    }

    private fun startAsForeground() {
        val openApp = PendingIntent.getActivity(
            this, 10, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stop = PendingIntent.getBroadcast(
            this, 11, Intent(this, StopOverlayReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(getString(R.string.overlay_running))
            .setContentText("Tap to open Pixel Capsule settings")
            .setContentIntent(openApp)
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Turn off", stop)
            .build()
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.overlay_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = getString(R.string.overlay_channel_description) }
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
    }

    private fun addOverlay() {
        val prefs = CapsulePrefs.read(this)
        val view = ComposeView(this).apply {
            // ComposeView outside an Activity/Fragment requires both tree owners.
            // Missing either owner causes a crash while the overlay composition is created.
            ViewTreeLifecycleOwner.set(this, this@CapsuleOverlayService)
            ViewTreeSavedStateRegistryOwner.set(this, this@CapsuleOverlayService)
            setContent {
                PixelCapsuleTheme(dynamicColors = prefs.dynamicColors) {
                    CapsuleOverlayUi(
                        onFocusableChanged = ::setOverlayFocusable,
                        onOpenSettings = ::openSettings
                    )
                }
            }
        }
        val flags = LayoutParams.FLAG_NOT_FOCUSABLE or LayoutParams.FLAG_NOT_TOUCH_MODAL or
            LayoutParams.FLAG_LAYOUT_IN_SCREEN
        val params = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT,
            LayoutParams.TYPE_APPLICATION_OVERLAY,
            flags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            x = getCutoutCenterOffsetX() + dp(prefs.offsetXDp)
            y = getCutoutAnchorY() + dp(prefs.offsetYDp)
            softInputMode = LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            if (Build.VERSION.SDK_INT >= 30) {
                layoutInDisplayCutoutMode = LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            } else if (Build.VERSION.SDK_INT >= 28) {
                layoutInDisplayCutoutMode = LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
        runCatching {
            windowManager.addView(view, params)
            overlayView = view
            overlayParams = params
        }.onFailure {
            stopSelf()
        }
    }

    private fun getCutoutAnchorY(): Int {
        if (Build.VERSION.SDK_INT >= 30) {
            val cutout = windowManager.currentWindowMetrics.windowInsets.displayCutout
            val top = cutout?.boundingRects?.maxByOrNull { it.bottom }
            if (top != null) return top.top
        }
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) resources.getDimensionPixelSize(resourceId) / 3 else dp(8f)
    }

    private fun getCutoutCenterOffsetX(): Int {
        if (Build.VERSION.SDK_INT < 30) return 0
        val metrics = windowManager.currentWindowMetrics
        val cutout = metrics.windowInsets.displayCutout?.boundingRects?.maxByOrNull { it.bottom } ?: return 0
        return cutout.centerX() - metrics.bounds.centerX()
    }

    private fun removeOverlay() {
        val view = overlayView
        if (view != null) {
            runCatching { windowManager.removeView(view) }
            overlayView = null
            overlayParams = null
            if (serviceLifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                serviceLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
                serviceLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            }
        }
    }

    private fun setOverlayFocusable(focusable: Boolean) {
        val view = overlayView ?: return
        val params = overlayParams ?: return
        params.flags = if (focusable) {
            LayoutParams.FLAG_NOT_TOUCH_MODAL or LayoutParams.FLAG_LAYOUT_IN_SCREEN
        } else {
            LayoutParams.FLAG_NOT_FOCUSABLE or LayoutParams.FLAG_NOT_TOUCH_MODAL or LayoutParams.FLAG_LAYOUT_IN_SCREEN
        }
        runCatching { windowManager.updateViewLayout(view, params) }
    }

    private fun registerDeviceReceivers() {
        if (receiverRegistered) return
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(ACTION_BLUETOOTH_BATTERY_LEVEL_CHANGED)
        }
        val sticky = ContextCompat.registerReceiver(this, stateReceiver, filter, ContextCompat.RECEIVER_EXPORTED)
        receiverRegistered = true
        sticky?.let(::updateBattery)
    }

    private fun updateBattery(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100).coerceAtLeast(1)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
        val source = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_AC -> getString(R.string.charging_adapter)
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> getString(R.string.charging_wireless)
            else -> getString(R.string.charging_source)
        }
        CapsuleRuntime.setBattery(if (level >= 0) (level * 100 / scale) else 0, charging, source)
    }

    @Suppress("DEPRECATION")
    private fun registerPhoneState() {
        if (checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE) != android.content.pm.PackageManager.PERMISSION_GRANTED) return
        val manager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        runCatching {
            if (Build.VERSION.SDK_INT >= 31) {
                val callback = object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                    override fun onCallStateChanged(state: Int) = CapsuleRuntime.setCallState(state)
                }
                manager.registerTelephonyCallback(mainExecutor, callback)
                telephonyCallback = callback
            } else {
                val listener = object : PhoneStateListener() {
                    override fun onCallStateChanged(state: Int, phoneNumber: String?) {
                        CapsuleRuntime.setCallState(state)
                    }
                }
                manager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE)
                phoneListener = listener
            }
        }
    }

    private fun openSettings() {
        val intent = Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }

    private fun dp(value: Float): Int = (value * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        removeOverlay()
        if (serviceLifecycle.currentState != Lifecycle.State.DESTROYED) {
            serviceLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        }
        if (receiverRegistered) runCatching { unregisterReceiver(stateReceiver) }
        receiverRegistered = false
        val manager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        runCatching {
            if (Build.VERSION.SDK_INT >= 31) telephonyCallback?.let(manager::unregisterTelephonyCallback)
            else phoneListener?.let { manager.listen(it, PhoneStateListener.LISTEN_NONE) }
        }
        telephonyCallback = null
        phoneListener = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "app.pixelcapsule.action.START_OVERLAY"
        const val ACTION_STOP = "app.pixelcapsule.action.STOP_OVERLAY"
        private const val CHANNEL_ID = "pixel_capsule_overlay"
        private const val NOTIFICATION_ID = 4242
        private const val ACTION_BLUETOOTH_BATTERY_LEVEL_CHANGED = "android.bluetooth.device.action.BATTERY_LEVEL_CHANGED"
        private const val EXTRA_BLUETOOTH_BATTERY_LEVEL = "android.bluetooth.device.extra.BATTERY_LEVEL"
    }
}

class StopOverlayReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        context.stopService(Intent(context, CapsuleOverlayService::class.java))
    }
}
