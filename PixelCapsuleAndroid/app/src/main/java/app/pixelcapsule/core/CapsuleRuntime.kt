package app.pixelcapsule.core

import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.media.session.MediaController
import android.app.Notification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Process-local event bus. Message text and notification actions are not written to disk. */
object CapsuleRuntime {
    private val mutableState = MutableStateFlow(CapsuleUiState())
    val state = mutableState.asStateFlow()

    @Volatile
    var activeMediaController: MediaController? = null
        private set

    private val actionLock = Any()
    private val notificationActions = LinkedHashMap<String, Array<Notification.Action>>()
    private val notificationIntents = LinkedHashMap<String, PendingIntent>()

    fun setMedia(info: MediaInfo?, controller: MediaController?) {
        activeMediaController = controller
        mutableState.update { it.copy(media = info) }
    }

    fun setNotifications(items: List<NotificationInfo>) {
        mutableState.update { old ->
            val progressItem = items.firstOrNull { it.progress != null && it.progressMax != null && it.progressMax > 0 }
            val percent = progressItem?.let {
                ((it.progress!!.toFloat() / it.progressMax!!.toFloat()) * 100f).toInt().coerceIn(0, 100)
            }
            old.copy(notifications = items.take(8), downloadPercent = percent)
        }
    }

    fun putNotificationActions(key: String, actions: Array<Notification.Action>?, contentIntent: PendingIntent?) {
        synchronized(actionLock) {
            if (actions.isNullOrEmpty()) notificationActions.remove(key)
            else notificationActions[key] = actions
            if (contentIntent == null) notificationIntents.remove(key)
            else notificationIntents[key] = contentIntent
            while (notificationActions.size > 20) notificationActions.remove(notificationActions.keys.first())
            while (notificationIntents.size > 20) notificationIntents.remove(notificationIntents.keys.first())
        }
    }

    fun removeNotification(key: String) {
        synchronized(actionLock) {
            notificationActions.remove(key)
            notificationIntents.remove(key)
        }
    }

    fun openNotification(key: String): Boolean {
        val pending = synchronized(actionLock) { notificationIntents[key] } ?: return false
        return try {
            pending.send()
            true
        } catch (_: PendingIntent.CanceledException) {
            false
        }
    }

    fun sendQuickReply(context: Context, key: String, message: String): Boolean {
        if (message.isBlank()) return false
        val action = synchronized(actionLock) {
            notificationActions[key]?.firstOrNull { !it.remoteInputs.isNullOrEmpty() }
        } ?: return false
        val inputs = action.remoteInputs ?: return false
        return try {
            val fillIn = Intent()
            val results = Bundle().apply { putCharSequence(inputs.first().resultKey, message) }
            RemoteInput.addResultsToIntent(inputs, fillIn, results)
            action.actionIntent.send(context, 0, fillIn)
            true
        } catch (_: PendingIntent.CanceledException) {
            false
        } catch (_: SecurityException) {
            false
        }
    }

    fun setCallState(state: Int) {
        mutableState.update { old ->
            val previous = old.call
            val start = when {
                state == CallInfo.CALL_OFFHOOK && previous.state != CallInfo.CALL_OFFHOOK -> SystemClock.elapsedRealtime()
                state == CallInfo.CALL_OFFHOOK -> previous.startedElapsedRealtime
                else -> 0L
            }
            old.copy(call = CallInfo(state, start))
        }
    }

    fun setBattery(level: Int, charging: Boolean) {
        mutableState.update { it.copy(batteryPercent = level.coerceIn(0, 100), isCharging = charging) }
    }

    fun setBluetoothConnected(connected: Boolean) {
        mutableState.update { it.copy(bluetoothConnected = connected) }
    }

    fun setQuickApps(packages: Set<String>) {
        mutableState.update { it.copy(quickApps = packages.toSet()) }
    }

    fun clearTransientState() {
        mutableState.update { it.copy(call = CallInfo(), media = null, notifications = emptyList(), downloadPercent = null) }
        activeMediaController = null
        synchronized(actionLock) {
            notificationActions.clear()
            notificationIntents.clear()
        }
    }
}
