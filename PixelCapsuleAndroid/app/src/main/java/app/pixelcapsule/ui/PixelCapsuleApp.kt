package app.pixelcapsule.ui

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import app.pixelcapsule.MainActivity

@Composable
fun PixelCapsuleApp(
    onStartOverlay: () -> Unit,
    onStopOverlay: () -> Unit,
    onRequestCallPermissions: () -> Unit,
    onRequestCameraPermission: () -> Unit,
    onRequestBluetoothPermission: () -> Unit,
    onPickContact: () -> Unit,
    permissionRefreshKey: Int
) {
    val context = LocalContext.current
    var page by remember { mutableStateOf(AppPage.HOME) }
    Surface(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).imePadding(), color = MaterialTheme.colorScheme.background) {
        when (page) {
            AppPage.HOME -> HomeScreen(
                onStartOverlay = onStartOverlay,
                onStopOverlay = onStopOverlay,
                onSettings = { page = AppPage.SETTINGS },
                onApps = { page = AppPage.APPS },
                onPermissions = { page = AppPage.PERMISSIONS },
                onRequestCallPermissions = onRequestCallPermissions,
                onRequestCameraPermission = onRequestCameraPermission,
                onRequestBluetoothPermission = onRequestBluetoothPermission,
                onPickContact = onPickContact,
                permissionRefreshKey = permissionRefreshKey
            )
            AppPage.SETTINGS -> SettingsScreen(onBack = { page = AppPage.HOME }, onRestartOverlay = onStartOverlay)
            AppPage.APPS -> AppsScreen(onBack = { page = AppPage.HOME })
            AppPage.PERMISSIONS -> PermissionsScreen(
                onBack = { page = AppPage.HOME },
                onRequestCallPermissions = onRequestCallPermissions,
                onRequestCameraPermission = onRequestCameraPermission,
                onRequestBluetoothPermission = onRequestBluetoothPermission,
                permissionRefreshKey = permissionRefreshKey
            )
        }
    }
}

private enum class AppPage { HOME, SETTINGS, APPS, PERMISSIONS }

@Composable
private fun HomeScreen(
    onStartOverlay: () -> Unit,
    onStopOverlay: () -> Unit,
    onSettings: () -> Unit,
    onApps: () -> Unit,
    onPermissions: () -> Unit,
    onRequestCallPermissions: () -> Unit,
    onRequestCameraPermission: () -> Unit,
    onRequestBluetoothPermission: () -> Unit,
    onPickContact: () -> Unit,
    permissionRefreshKey: Int
) {
    val context = LocalContext.current
    // Permission APIs are not Compose state. Re-read them whenever Activity resumes.
    val overlayGranted = remember(permissionRefreshKey) { Settings.canDrawOverlays(context) }
    val notificationGranted = remember(permissionRefreshKey) { isNotificationAccessEnabled(context) }
    val writeGranted = remember(permissionRefreshKey) { Settings.System.canWrite(context) }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Pixel Capsule", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(capsuleString(R.string.home_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(capsuleString(R.string.language_note), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                Icon(Icons.Default.Bolt, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(13.dp).size(28.dp))
            }
        }

        CapsuleStatusCard(overlayGranted = overlayGranted, notificationsGranted = notificationGranted)

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onStartOverlay, enabled = overlayGranted, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(capsuleString(R.string.start_capsule))
            }
            OutlinedButton(onClick = onStopOverlay, modifier = Modifier.weight(1f)) { Text(capsuleString(R.string.stop_capsule)) }
        }
        if (!overlayGranted) {
            Text(capsuleString(R.string.overlay_hint), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Text(capsuleString(R.string.quick_setup), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            QuickActionCard(capsuleString(R.string.appearance), capsuleString(R.string.position_size_colors), Icons.Default.Tune, onSettings, Modifier.weight(1f))
            QuickActionCard(capsuleString(R.string.apps), capsuleString(R.string.favourite_shortcuts), Icons.Default.Apps, onApps, Modifier.weight(1f))
        }

        OutlinedButton(onClick = onPickContact, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Apps, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
            Text(capsuleString(R.string.add_contact))
        }

        Text(capsuleString(R.string.permissions), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        PermissionStatusCard(
            title = capsuleString(R.string.overlay_access),
            description = capsuleString(R.string.overlay_access_desc),
            granted = overlayGranted,
            onClick = { MainActivity.openOverlaySettings(context) }
        )
        PermissionStatusCard(
            title = capsuleString(R.string.notification_access),
            description = capsuleString(R.string.notification_access_desc),
            granted = notificationGranted,
            onClick = { MainActivity.openNotificationSettings(context) }
        )
        PermissionStatusCard(
            title = capsuleString(R.string.modify_settings),
            description = capsuleString(R.string.brightness_desc),
            granted = writeGranted,
            onClick = { MainActivity.openWriteSettings(context) }
        )
        PermissionStatusCard(
            title = capsuleString(R.string.calls),
            description = capsuleString(R.string.calls_desc),
            granted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED,
            onClick = onRequestCallPermissions
        )
        PermissionStatusCard(
            title = capsuleString(R.string.flashlight),
            description = capsuleString(R.string.flashlight_first_use),
            granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED,
            onClick = onRequestCameraPermission
        )
        PermissionStatusCard(
            title = capsuleString(R.string.bluetooth_accessory),
            description = capsuleString(R.string.bluetooth_permission_desc),
            granted = android.os.Build.VERSION.SDK_INT < 31 || ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED,
            onClick = onRequestBluetoothPermission
        )
        TextButton(onClick = onPermissions, modifier = Modifier.align(Alignment.End)) {
            Text(capsuleString(R.string.permission_guide))
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
        Text(
            capsuleString(R.string.privacy_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CapsuleStatusCard(overlayGranted: Boolean, notificationsGranted: Boolean) {
    val completed = (if (overlayGranted) 1 else 0) + (if (notificationsGranted) 1 else 0)
    val ready = completed == 2
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        capsuleString(R.string.setup_status),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        capsuleString(if (ready) R.string.setup_ready else R.string.setup_incomplete),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        capsuleString(R.string.setup_status_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        if (ready) Icons.Default.CheckCircle else Icons.Default.Bolt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(14.dp).size(30.dp)
                    )
                }
            }
            androidx.compose.material3.LinearProgressIndicator(
                progress = { completed / 2f },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SetupStatusChip(
                    label = capsuleString(R.string.overlay_access),
                    enabled = overlayGranted
                )
                SetupStatusChip(
                    label = capsuleString(R.string.notification_access),
                    enabled = notificationsGranted
                )
            }
        }
    }
}

@Composable
private fun SetupStatusChip(label: String, enabled: Boolean) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (enabled) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                if (enabled) Icons.Default.CheckCircle else Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickActionCard(title: String, description: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier, onClick = onClick, shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(25.dp))
            Text(title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSecondaryContainer)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}

@Composable
private fun PermissionStatusCard(title: String, description: String, granted: Boolean, onClick: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            Icon(
                if (granted) Icons.Default.CheckCircle else Icons.Default.ChevronRight,
                contentDescription = if (granted) capsuleString(R.string.granted) else capsuleString(R.string.open_setting),
                tint = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PermissionsScreen(onBack: () -> Unit, onRequestCallPermissions: () -> Unit, onRequestCameraPermission: () -> Unit, onRequestBluetoothPermission: () -> Unit, permissionRefreshKey: Int) {
    val context = LocalContext.current
    val overlayGranted = remember(permissionRefreshKey) { Settings.canDrawOverlays(context) }
    val notificationGranted = remember(permissionRefreshKey) { isNotificationAccessEnabled(context) }
    val writeGranted = remember(permissionRefreshKey) { Settings.System.canWrite(context) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text(capsuleString(R.string.back_arrow)) }
            Text(capsuleString(R.string.permission_guide), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Text(capsuleString(R.string.permission_guide_desc), color = MaterialTheme.colorScheme.onSurfaceVariant)
        PermissionStatusCard(capsuleString(R.string.overlay), capsuleString(R.string.floating_capsule), overlayGranted, { MainActivity.openOverlaySettings(context) })
        PermissionStatusCard(capsuleString(R.string.notifications), capsuleString(R.string.notifications_permission_detail), notificationGranted, { MainActivity.openNotificationSettings(context) })
        PermissionStatusCard(capsuleString(R.string.system_settings), capsuleString(R.string.brightness_auto), writeGranted, { MainActivity.openWriteSettings(context) })
        PermissionStatusCard(capsuleString(R.string.phone), capsuleString(R.string.phone_permission_detail), ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED, onRequestCallPermissions)
        PermissionStatusCard(capsuleString(R.string.camera_flash), capsuleString(R.string.camera_flash_desc), ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED, onRequestCameraPermission)
        PermissionStatusCard(capsuleString(R.string.bluetooth_accessory), capsuleString(R.string.bluetooth_accessory_desc), android.os.Build.VERSION.SDK_INT < 31 || ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED, onRequestBluetoothPermission)
        Text(capsuleString(R.string.accessibility_detail), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

fun isNotificationAccessEnabled(context: Context): Boolean {
    val component = ComponentName(context, app.pixelcapsule.service.CapsuleNotificationListener::class.java)
    val enabled = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners") ?: return false
    return enabled.split(':').any { ComponentName.unflattenFromString(it) == component }
}
