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
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
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
    val overlayGranted = Settings.canDrawOverlays(context)
    val notificationGranted = isNotificationAccessEnabled(context)
    val writeGranted = Settings.System.canWrite(context)
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Pixel Capsule", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("Tvoj interaktívny ostrov pre Android", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                Icon(Icons.Default.Bolt, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(13.dp).size(28.dp))
            }
        }

        CapsulePreviewCard()

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onStartOverlay, enabled = overlayGranted, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text("Zapnúť kapsulu")
            }
            OutlinedButton(onClick = onStopOverlay, modifier = Modifier.weight(1f)) { Text("Vypnúť") }
        }
        if (!overlayGranted) {
            Text("Najprv povoľ zobrazenie nad ostatnými aplikáciami.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Text("Rýchle nastavenie", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            QuickActionCard("Vzhľad", "Poloha, veľkosť a farby", Icons.Default.Tune, onSettings, Modifier.weight(1f))
            QuickActionCard("Aplikácie", "Tvoje obľúbené skratky", Icons.Default.Apps, onApps, Modifier.weight(1f))
        }

        OutlinedButton(onClick = onPickContact, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Apps, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
            Text("Pridať kontakt do kapsuly")
        }

        Text("Oprávnenia", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        PermissionStatusCard(
            title = "Zobrazenie nad aplikáciami",
            description = "Zobrazí kapsulu nad otvorenými aplikáciami.",
            granted = overlayGranted,
            onClick = { MainActivity.openOverlaySettings(context) }
        )
        PermissionStatusCard(
            title = "Prístup k notifikáciám a médiám",
            description = "Číta notifikácie a nájde aktívne prehrávače.",
            granted = notificationGranted,
            onClick = { MainActivity.openNotificationSettings(context) }
        )
        PermissionStatusCard(
            title = "Úprava systémových nastavení",
            description = "Umožní meniť jas displeja.",
            granted = writeGranted,
            onClick = { MainActivity.openWriteSettings(context) }
        )
        PermissionStatusCard(
            title = "Hovory",
            description = "Stav hovoru a dostupné ovládanie hovoru.",
            granted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED,
            onClick = onRequestCallPermissions
        )
        PermissionStatusCard(
            title = "Baterka",
            description = "Vyžaduje povolenie fotoaparátu iba pri prvom zapnutí blesku.",
            granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED,
            onClick = onRequestCameraPermission
        )
        PermissionStatusCard(
            title = "Bluetooth príslušenstvo",
            description = "Voliteľné povolenie na zobrazenie úrovne batérie podporovaných slúchadiel a zariadení.",
            granted = android.os.Build.VERSION.SDK_INT < 31 || ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED,
            onClick = onRequestBluetoothPermission
        )
        TextButton(onClick = onPermissions, modifier = Modifier.align(Alignment.End)) {
            Text("Sprievodca oprávneniami")
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
        Text(
            "Súkromie: správy sa spracúvajú lokálne v pamäti. Aplikácia ich neposiela na server ani neukladá do histórie.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CapsulePreviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("NÁHĽAD KAPSULY", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text("MATERIAL 3", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            Box(modifier = Modifier.fillMaxWidth().height(112.dp), contentAlignment = Alignment.TopCenter) {
                Surface(
                    modifier = Modifier.padding(top = 6.dp).fillMaxWidth(0.68f).height(76.dp),
                    color = Color(0xFF101511),
                    shape = RoundedCornerShape(40.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                        Box(Modifier.size(10.dp).clip(RoundedCornerShape(50)).background(Color(0xFFB9D69A)))
                        Spacer(Modifier.size(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Pixel Capsule", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Všetko dôležité na jednom mieste", color = Color(0xFFBCC7B7), fontSize = 10.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.Bottom) {
                            listOf(9, 17, 12, 20).forEachIndexed { index, height ->
                                Box(Modifier.size(width = 3.dp, height = height.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFFB9D69A)))
                            }
                        }
                    }
                }
            }
            Text("Kapsula sa prispôsobí aktívnej hudbe, upozorneniam, hovorom aj rýchlym ovládačom.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
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
                contentDescription = if (granted) "Povolené" else "Otvoriť nastavenie",
                tint = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PermissionsScreen(onBack: () -> Unit, onRequestCallPermissions: () -> Unit, onRequestCameraPermission: () -> Unit, onRequestBluetoothPermission: () -> Unit, permissionRefreshKey: Int) {
    val context = LocalContext.current
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("← Späť") }
            Text("Sprievodca oprávneniami", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Text("Zapni iba tie prístupy, ktoré chceš používať. Každý prístup môžeš neskôr odobrať v nastaveniach Androidu.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        PermissionStatusCard("Overlay", "Plávajúca kapsula na obrazovke.", Settings.canDrawOverlays(context), { MainActivity.openOverlaySettings(context) })
        PermissionStatusCard("Notifikácie", "Texty upozornení a akcie rýchlej odpovede.", isNotificationAccessEnabled(context), { MainActivity.openNotificationSettings(context) })
        PermissionStatusCard("Systémové nastavenia", "Manuálny jas displeja a automatický režim.", Settings.System.canWrite(context), { MainActivity.openWriteSettings(context) })
        PermissionStatusCard("Telefón", "Stav SIM hovoru; pri prijatí/odmietnutí je potrebné samostatné povolenie.", ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED, onRequestCallPermissions)
        PermissionStatusCard("Fotoaparát / blesk", "Povolenie sa používa iba pri ovládaní LED blesku.", ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED, onRequestCameraPermission)
        PermissionStatusCard("Bluetooth príslušenstvo", "Voliteľný prístup k úrovni batérie podporovaných zariadení.", android.os.Build.VERSION.SDK_INT < 31 || ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED, onRequestBluetoothPermission)
        Text("AccessibilityService sa nevyžaduje. Kapsula používa vlastné dotykové gestá a samostatné systémové rozhrania.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

fun isNotificationAccessEnabled(context: Context): Boolean {
    val component = ComponentName(context, app.pixelcapsule.service.CapsuleNotificationListener::class.java)
    val enabled = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners") ?: return false
    return enabled.split(':').any { ComponentName.unflattenFromString(it) == component }
}
