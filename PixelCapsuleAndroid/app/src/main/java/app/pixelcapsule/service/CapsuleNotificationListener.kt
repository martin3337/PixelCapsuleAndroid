package app.pixelcapsule.service

import android.content.ComponentName
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import app.pixelcapsule.core.CapsuleRuntime
import app.pixelcapsule.core.MediaInfo
import app.pixelcapsule.core.NotificationInfo

class CapsuleNotificationListener : NotificationListenerService() {
    private val posted = LinkedHashMap<String, NotificationInfo>()
    private var activeController: MediaController? = null
    private var sessionListenerRegistered = false
    private val sessionListener = MediaSessionManager.OnActiveSessionsChangedListener { refreshActiveMedia() }

    private val mediaCallback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) = publishMedia(activeController)
        override fun onPlaybackStateChanged(state: PlaybackState?) = publishMedia(activeController)
        override fun onSessionDestroyed() = refreshActiveMedia()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        CapsuleRuntime.setNotificationDismissHandler { key -> cancelNotification(key) }
        runCatching {
            val manager = getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager
            manager.addOnActiveSessionsChangedListener(
                sessionListener,
                ComponentName(this, CapsuleNotificationListener::class.java)
            )
            sessionListenerRegistered = true
        }
        runCatching { activeNotifications?.forEach(::onNotificationPosted) }
        refreshActiveMedia()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        activeController?.unregisterCallback(mediaCallback)
        if (sessionListenerRegistered) {
            runCatching {
                (getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager)
                    .removeOnActiveSessionsChangedListener(sessionListener)
            }
        }
        sessionListenerRegistered = false
        activeController = null
        CapsuleRuntime.setNotificationDismissHandler(null)
        CapsuleRuntime.setMedia(null, null)
        requestRebind(ComponentName(this, CapsuleNotificationListener::class.java))
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName == packageName) return
        val notification = sbn.notification
        val extras = notification.extras
        val title = extras.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(android.app.Notification.EXTRA_BIG_TEXT)?.toString()
            ?: extras.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString().orEmpty()
        val label = runCatching {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(sbn.packageName, 0)).toString()
        }.getOrDefault(sbn.packageName)
        val hasProgress = extras.containsKey(android.app.Notification.EXTRA_PROGRESS)
        val max = extras.getInt(android.app.Notification.EXTRA_PROGRESS_MAX, 0).takeIf { hasProgress && it > 0 }
        val progress = extras.getInt(android.app.Notification.EXTRA_PROGRESS, 0).takeIf { hasProgress && max != null }
        val canReply = notification.actions?.any { !it.remoteInputs.isNullOrEmpty() } == true

        val item = NotificationInfo(
            key = sbn.key,
            packageName = sbn.packageName,
            appName = label,
            title = title.ifBlank { label },
            text = text,
            postedAt = sbn.postTime,
            progress = progress,
            progressMax = max,
            indeterminate = extras.getBoolean(android.app.Notification.EXTRA_PROGRESS_INDETERMINATE, false),
            canReply = canReply
        )
        synchronized(posted) {
            posted[sbn.key] = item
            while (posted.size > 8) posted.remove(posted.keys.first())
            publishNotificationsLocked()
        }
        CapsuleRuntime.putNotificationActions(sbn.key, notification.actions, notification.contentIntent)
        refreshActiveMedia()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        synchronized(posted) {
            posted.remove(sbn.key)
            publishNotificationsLocked()
        }
        CapsuleRuntime.removeNotification(sbn.key)
    }

    private fun publishNotificationsLocked() {
        CapsuleRuntime.setNotifications(posted.values.sortedByDescending { it.postedAt })
    }

    private fun refreshActiveMedia() {
        try {
            val manager = getSystemService(MEDIA_SESSION_SERVICE) as MediaSessionManager
            val component = ComponentName(this, CapsuleNotificationListener::class.java)
            val sessions = manager.getActiveSessions(component)
            val chosen = sessions.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING }
                ?: sessions.firstOrNull { it.metadata != null }
            if (chosen?.sessionToken != activeController?.sessionToken) {
                activeController?.unregisterCallback(mediaCallback)
                activeController = chosen
                activeController?.registerCallback(mediaCallback)
            }
            publishMedia(activeController)
        } catch (_: SecurityException) {
            CapsuleRuntime.setMedia(null, null)
        } catch (_: IllegalStateException) {
            CapsuleRuntime.setMedia(null, null)
        }
    }

    private fun publishMedia(controller: MediaController?) {
        if (controller == null) {
            CapsuleRuntime.setMedia(null, null)
            return
        }
        val metadata = controller.metadata
        if (metadata == null) {
            CapsuleRuntime.setMedia(null, controller)
            return
        }
        val playback = controller.playbackState
        val state = playback?.state
        val appLabel = runCatching {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(controller.packageName, 0)).toString()
        }.getOrDefault(controller.packageName)
        val artwork = metadata.getBitmap(MediaMetadata.METADATA_KEY_ART)
            ?: metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
        val info = MediaInfo(
            packageName = controller.packageName,
            appName = appLabel,
            title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE).orEmpty().ifBlank { getString(R.string.playing_media) },
            artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST).orEmpty(),
            album = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM).orEmpty(),
            durationMs = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION).coerceAtLeast(0L),
            positionMs = playback?.position?.coerceAtLeast(0L) ?: 0L,
            playing = state == PlaybackState.STATE_PLAYING,
            artwork = artwork
        )
        CapsuleRuntime.setMedia(info, controller)
    }
}
