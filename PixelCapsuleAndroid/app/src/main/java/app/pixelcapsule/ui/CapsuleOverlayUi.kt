package app.pixelcapsule.ui

import android.media.AudioManager
import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.pixelcapsule.MainActivity
import app.pixelcapsule.core.CallInfo
import app.pixelcapsule.core.CapsulePrefs
import app.pixelcapsule.core.CapsuleRuntime
import app.pixelcapsule.core.MediaInfo
import app.pixelcapsule.core.NotificationInfo
import app.pixelcapsule.core.formatDuration
import app.pixelcapsule.system.AppCatalog
import app.pixelcapsule.system.DeviceControls
import kotlinx.coroutines.delay
import kotlin.math.abs

private enum class CapsuleSection(val label: String) {
    OVERVIEW("Prehľad"), MEDIA("Hudba"), NOTIFICATIONS("Upozornenia"), CONTROLS("Ovládanie"), APPS("Aplikácie"), CONTACTS("Kontakty")
}

@Composable
fun CapsuleOverlayUi(
    onFocusableChanged: (Boolean) -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val snapshot by CapsuleRuntime.state.collectAsState()
    val preferences = remember { CapsulePrefs.read(context) }
    LaunchedEffect(Unit) { CapsuleRuntime.setQuickApps(preferences.quickApps) }
    var expanded by remember { mutableStateOf(false) }
    var section by remember { mutableStateOf(CapsuleSection.OVERVIEW) }
    fun performGesture(action: String) {
        when (action) {
            "toggle" -> expanded = !expanded
            "expand" -> expanded = true
            "collapse" -> expanded = false
            "settings" -> onOpenSettings()
            "media" -> { section = CapsuleSection.MEDIA; expanded = true }
            "notifications" -> { section = CapsuleSection.NOTIFICATIONS; expanded = true }
            "next_section" -> { val all = CapsuleSection.entries; section = all[(all.indexOf(section) + 1) % all.size]; expanded = true }
            "previous_section" -> { val all = CapsuleSection.entries; section = all[(all.indexOf(section) - 1 + all.size) % all.size]; expanded = true }
        }
    }
    val targetWidth = if (expanded) preferences.expandedWidthDp.dp else preferences.widthDp.dp
    val width by animateDpAsState(targetValue = targetWidth, animationSpec = spring(dampingRatio = 0.78f, stiffness = 420f), label = "capsule-width")
    val radius by animateDpAsState(
        targetValue = if (expanded) 30.dp else preferences.cornerRadiusDp.dp,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f), label = "capsule-radius"
    )
    val bg = Color(0xFF111713)
    val fg = Color(0xFFF4F6EE)
    val muted = Color(0xFFB4C0AE)
    val green = Color(preferences.accentColorArgb)

    Box(modifier = Modifier.width(width).widthIn(min = 104.dp, max = 420.dp)) {
        Surface(
            color = bg,
            contentColor = fg,
            shape = RoundedCornerShape(radius),
            shadowElevation = if (expanded) 14.dp else 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(radius))
                .combinedClickable(
                    onClick = { performGesture(preferences.tapAction) },
                    onDoubleClick = { performGesture(preferences.doubleTapAction) },
                    onLongClick = { performGesture(preferences.longPressAction) }
                )
        ) {
            AnimatedContent(targetState = expanded, label = "capsule-expand") { isExpanded ->
                if (!isExpanded) {
                    CollapsedCapsule(
                        media = snapshot.media,
                        call = snapshot.call,
                        battery = snapshot.batteryPercent,
                        charging = snapshot.isCharging,
                        fg = fg,
                        muted = muted,
                        green = green,
                        onSwipeHorizontal = { delta -> performGesture(if (delta < 0f) preferences.swipeLeftAction else preferences.swipeRightAction) },
                        onSwipeUp = { performGesture(preferences.swipeUpAction) }
                    )
                } else {
                    ExpandedCapsule(
                        section = section,
                        onSection = { section = it },
                        snapshotMedia = snapshot.media,
                        notifications = snapshot.notifications,
                        call = snapshot.call,
                        battery = snapshot.batteryPercent,
                        charging = snapshot.isCharging,
                        bluetoothConnected = snapshot.bluetoothConnected,
                        downloadPercent = snapshot.downloadPercent,
                        quickApps = snapshot.quickApps,
                        onFocusableChanged = onFocusableChanged,
                        onCollapse = { expanded = false },
                        onOpenSettings = onOpenSettings,
                        onSwipeHorizontal = { delta -> performGesture(if (delta < 0f) preferences.swipeLeftAction else preferences.swipeRightAction) },
                        onSwipeDown = { performGesture(preferences.swipeDownAction) },
                        green = green,
                        muted = muted,
                        fg = fg
                    )
                }
            }
        }
    }
}

@Composable
private fun CollapsedCapsule(
    media: MediaInfo?,
    call: CallInfo,
    battery: Int,
    charging: Boolean,
    fg: Color,
    muted: Color,
    green: Color,
    onSwipeHorizontal: (Float) -> Unit,
    onSwipeUp: () -> Unit
) {
    var dragX by remember { mutableFloatStateOf(0f) }
    var elapsedNow by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(call.isActive, call.startedElapsedRealtime) {
        while (call.isActive) {
            elapsedNow = SystemClock.elapsedRealtime()
            delay(1000)
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 14.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, amount -> dragX += amount; change.consume() },
                    onDragEnd = { if (abs(dragX) > 28f) onSwipeHorizontal(dragX); dragX = 0f }
                )
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures(onVerticalDrag = { change, amount -> if (amount < -5f) onSwipeUp(); change.consume() })
            },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        when {
            call.isRinging -> {
                Icon(Icons.Default.Call, contentDescription = null, tint = green, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(9.dp))
                Text("Prichádzajúci hovor", color = fg, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, maxLines = 1)
            }
            call.isActive -> {
                Icon(Icons.Default.Call, contentDescription = null, tint = green, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(9.dp))
                Text("Prebieha hovor", color = fg, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, maxLines = 1)
                Spacer(Modifier.weight(1f))
                Text(formatDuration((elapsedNow - call.startedElapsedRealtime).coerceAtLeast(0L)), color = muted, fontSize = 11.sp)
            }
            media != null -> {
                if (media.artwork != null) {
                    Image(media.artwork.asImageBitmap(), contentDescription = "Obal albumu", modifier = Modifier.size(30.dp).clip(RoundedCornerShape(8.dp)))
                } else {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = green, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(9.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                    Text(media.title, color = fg, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(media.artist.ifBlank { media.appName }, color = muted, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(Modifier.width(6.dp))
                EqualizerBars(playing = media.playing, color = green)
            }
            else -> {
                Box(Modifier.size(8.dp).clip(CircleShape).background(green))
                Spacer(Modifier.width(9.dp))
                Text("Pixel Capsule", color = fg, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Spacer(Modifier.weight(1f))
                if (battery >= 0) Text("$battery%", color = muted, fontSize = 10.sp)
                if (charging) Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = green, modifier = Modifier.size(15.dp))
            }
        }
    }
}

@Composable
private fun EqualizerBars(playing: Boolean, color: Color) {
    val transition = rememberInfiniteTransition(label = "equalizer")
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.Bottom, modifier = Modifier.height(22.dp)) {
        repeat(4) { index ->
            val height by transition.animateFloat(
                initialValue = 0.25f,
                targetValue = if (playing) listOf(0.65f, 1f, 0.48f, 0.82f)[index] else 0.25f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 480 + index * 95, delayMillis = index * 60, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "eq-$index"
            )
            Box(Modifier.width(3.dp).height((17f * height).dp).clip(RoundedCornerShape(4.dp)).background(color))
        }
    }
}

@Composable
private fun ExpandedCapsule(
    section: CapsuleSection,
    onSection: (CapsuleSection) -> Unit,
    snapshotMedia: MediaInfo?,
    notifications: List<NotificationInfo>,
    call: CallInfo,
    battery: Int,
    charging: Boolean,
    bluetoothConnected: Boolean,
    downloadPercent: Int?,
    quickApps: Set<String>,
    onFocusableChanged: (Boolean) -> Unit,
    onCollapse: () -> Unit,
    onOpenSettings: () -> Unit,
    onSwipeHorizontal: (Float) -> Unit,
    onSwipeDown: () -> Unit,
    green: Color,
    muted: Color,
    fg: Color
) {
    Column(modifier = Modifier.fillMaxWidth().heightIn(min = 180.dp, max = 510.dp).verticalScroll(rememberScrollState()).padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            var headerDragX by remember { mutableFloatStateOf(0f) }
            Column(
                modifier = Modifier.weight(1f).pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, amount -> headerDragX += amount; change.consume() },
                        onDragEnd = { if (abs(headerDragX) > 28f) onSwipeHorizontal(headerDragX); headerDragX = 0f }
                    )
                }.pointerInput(Unit) {
                    detectVerticalDragGestures(onVerticalDrag = { change, amount -> if (amount > 8f) onSwipeDown(); change.consume() })
                }
            ) {
                Text("PIXEL CAPSULE", color = muted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                Text(
                    when {
                        call.isRinging -> "Prichádzajúci hovor"
                        call.isActive -> "Hovor prebieha"
                        section == CapsuleSection.MEDIA -> "Prehrávač médií"
                        section == CapsuleSection.NOTIFICATIONS -> "Upozornenia"
                        section == CapsuleSection.CONTROLS -> "Rýchle ovládače"
                        section == CapsuleSection.APPS -> "Obľúbené aplikácie"
                        section == CapsuleSection.CONTACTS -> "Kontakty"
                        else -> "Tvoj okamžitý prehľad"
                    },
                    color = fg, fontWeight = FontWeight.SemiBold, fontSize = 15.sp
                )
            }
            IconButton(onClick = onOpenSettings, modifier = Modifier.size(34.dp)) { Icon(Icons.Default.Settings, contentDescription = "Nastavenia", tint = muted, modifier = Modifier.size(18.dp)) }
            IconButton(onClick = onCollapse, modifier = Modifier.size(34.dp)) { Icon(Icons.Default.Close, contentDescription = "Zavrieť", tint = muted, modifier = Modifier.size(18.dp)) }
        }
        if (call.isVisible) {
            CallCard(call = call, green = green)
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(CapsuleSection.entries) { item ->
                AssistChip(
                    onClick = { onSection(item) },
                    label = { Text(item.label, fontSize = 10.sp) },
                    leadingIcon = {
                        Icon(
                            when (item) {
                                CapsuleSection.MEDIA -> Icons.Default.MusicNote
                                CapsuleSection.NOTIFICATIONS -> Icons.Default.Notifications
                                CapsuleSection.CONTROLS -> Icons.Default.Brightness6
                                CapsuleSection.APPS -> Icons.Default.Apps
                                CapsuleSection.CONTACTS -> Icons.Default.Call
                                CapsuleSection.OVERVIEW -> Icons.Default.Info
                            }, null, modifier = Modifier.size(14.dp)
                        )
                    }
                )
            }
        }
        when (section) {
            CapsuleSection.OVERVIEW -> OverviewPanel(snapshotMedia, notifications, battery, charging, bluetoothConnected, downloadPercent, green, muted)
            CapsuleSection.MEDIA -> MediaPanel(snapshotMedia, green, muted)
            CapsuleSection.NOTIFICATIONS -> NotificationsPanel(notifications, onFocusableChanged, muted)
            CapsuleSection.CONTROLS -> ControlsPanel(green, muted)
            CapsuleSection.APPS -> FavouriteAppsPanel(muted, quickApps)
            CapsuleSection.CONTACTS -> ContactsPanel(muted)
        }
        Text("Potiahni do strán na prepnutie • podrž na nastavenia", color = muted, fontSize = 9.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
private fun CallCard(call: CallInfo, green: Color) {
    val context = LocalContext.current
    var elapsedNow by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(call.state, call.startedElapsedRealtime) {
        while (call.isActive) {
            elapsedNow = SystemClock.elapsedRealtime()
            delay(1000)
        }
    }
    val durationText = if (call.isActive) {
        formatDuration((elapsedNow - call.startedElapsedRealtime).coerceAtLeast(0L))
    } else ""
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF243126))) {
        Row(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Call, contentDescription = null, tint = green)
            Column(modifier = Modifier.weight(1f).padding(horizontal = 9.dp)) {
                Text(if (call.isRinging) "Prichádzajúci hovor" else "Aktívny hovor", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                Text(if (call.isRinging) "Vyber akciu" else durationText, color = Color(0xFFC3D0BD), fontSize = 10.sp)
            }
            if (call.isRinging) {
                IconButton(onClick = {
                    DeviceControls.answerOrRejectCall(context, true).onFailure {
                        Toast.makeText(context, it.message ?: "Hovor sa nepodarilo prijať", Toast.LENGTH_SHORT).show()
                    }
                }) { Icon(Icons.Default.Call, contentDescription = "Prijať hovor", tint = green) }
            }
            IconButton(onClick = {
                DeviceControls.answerOrRejectCall(context, false).onFailure {
                    Toast.makeText(context, it.message ?: "Hovor sa nepodarilo ukončiť", Toast.LENGTH_SHORT).show()
                }
            }) { Icon(Icons.Default.CallEnd, contentDescription = "Odmietnuť alebo ukončiť hovor", tint = Color(0xFFFF8A80)) }
        }
    }
}

@Composable
private fun OverviewPanel(
    media: MediaInfo?, notifications: List<NotificationInfo>, battery: Int, charging: Boolean,
    bluetoothConnected: Boolean, downloadPercent: Int?, green: Color, muted: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (media != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.MusicNote, null, tint = green)
                Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
                    Text(media.title, color = Color.White, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp)
                    Text(media.artist.ifBlank { media.appName }, color = muted, fontSize = 10.sp)
                }
                Text(if (media.playing) "PREHRÁVA" else "POZASTAVENÉ", color = green, fontSize = 9.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusTile("Batéria", if (battery >= 0) "$battery%" else "—", if (charging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryChargingFull, Modifier.weight(1f), green)
            StatusTile("Bluetooth", if (bluetoothConnected) "Pripojené" else "Odpojené", Icons.Default.Bluetooth, Modifier.weight(1f), green)
        }
        if (downloadPercent != null) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Sťahovanie • $downloadPercent%", color = Color.White, fontSize = 11.sp)
                LinearProgressIndicator(progress = { (downloadPercent / 100f).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth(), color = green)
            }
        }
        Text("Posledné upozornenia: ${notifications.size}", color = muted, fontSize = 10.sp)
        notifications.firstOrNull()?.let { Text("${it.appName}: ${it.title}", color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 11.sp) }
    }
}

@Composable
private fun StatusTile(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, accent: Color) {
    Surface(modifier = modifier, color = Color(0xFF202A22), shape = RoundedCornerShape(17.dp)) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(18.dp))
            Column(Modifier.padding(start = 8.dp)) {
                Text(title, color = Color(0xFFAEBBA8), fontSize = 9.sp)
                Text(value, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun MediaPanel(media: MediaInfo?, green: Color, muted: Color) {
    if (media == null) {
        Text("Zatiaľ nie je aktívne prehrávanie. Spusti hudbu alebo video a kapsula zachytí dostupnú MediaSession.", color = muted, fontSize = 11.sp)
        return
    }
    var position by remember(media.packageName, media.title) { mutableLongStateOf(media.positionMs) }
    var dragging by remember { mutableStateOf(false) }
    LaunchedEffect(media.playing, media.durationMs, dragging) {
        while (media.playing && !dragging) {
            delay(1000)
            position = (position + 1000L).coerceAtMost(media.durationMs)
        }
    }
    val controller = CapsuleRuntime.activeMediaController
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (media.artwork != null) {
            Image(media.artwork.asImageBitmap(), "Obal albumu", modifier = Modifier.size(58.dp).clip(RoundedCornerShape(13.dp)))
        } else {
            Surface(Modifier.size(58.dp), shape = RoundedCornerShape(13.dp), color = Color(0xFF2B3B2C)) {
                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.MusicNote, null, tint = green, modifier = Modifier.size(28.dp)) }
            }
        }
        Column(Modifier.weight(1f).padding(start = 11.dp)) {
            Text(media.title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(media.artist.ifBlank { media.appName }, color = muted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(media.appName, color = green, fontSize = 9.sp)
        }
    }
    if (media.durationMs > 0) {
        Slider(
            value = position.toFloat().coerceIn(0f, media.durationMs.toFloat()),
            onValueChange = { dragging = true; position = it.toLong() },
            onValueChangeFinished = { controller?.transportControls?.seekTo(position); dragging = false },
            valueRange = 0f..media.durationMs.toFloat(),
            modifier = Modifier.height(28.dp)
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(formatDuration(position), color = muted, fontSize = 9.sp)
            Text(formatDuration(media.durationMs), color = muted, fontSize = 9.sp)
        }
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { controller?.transportControls?.skipToPrevious() }) { Icon(Icons.Default.SkipPrevious, "Predchádzajúca", tint = Color.White, modifier = Modifier.size(28.dp)) }
        IconButton(onClick = {
            if (media.playing) controller?.transportControls?.pause() else controller?.transportControls?.play()
        }, modifier = Modifier.size(52.dp)) {
            Icon(if (media.playing) Icons.Default.Pause else Icons.Default.PlayArrow, "Prehrať alebo pozastaviť", tint = green, modifier = Modifier.size(34.dp))
        }
        IconButton(onClick = { controller?.transportControls?.skipToNext() }) { Icon(Icons.Default.SkipNext, "Ďalšia", tint = Color.White, modifier = Modifier.size(28.dp)) }
    }
}

@Composable
private fun NotificationsPanel(items: List<NotificationInfo>, onFocusableChanged: (Boolean) -> Unit, muted: Color) {
    if (items.isEmpty()) {
        Text("Zatiaľ tu nie sú žiadne upozornenia. Po zapnutí prístupu k notifikáciám sa zobrazia nové udalosti.", color = muted, fontSize = 11.sp)
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        items.take(4).forEach { item ->
            Card(
                onClick = { CapsuleRuntime.openNotification(item.key) },
                shape = RoundedCornerShape(17.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF202A22))
            ) {
                Column(Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(item.appName, color = Color(0xFFB9D69A), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Text(item.title, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (item.text.isNotBlank()) Text(item.text, color = Color(0xFFC5CEC0), fontSize = 10.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    if (item.progress != null && item.progressMax != null) {
                        LinearProgressIndicator(progress = { (item.progress.toFloat() / item.progressMax).coerceIn(0f, 1f) }, color = Color(0xFFB9D69A), modifier = Modifier.fillMaxWidth())
                        Text("${(item.progress * 100 / item.progressMax.coerceAtLeast(1))}%", color = muted, fontSize = 9.sp)
                    }
                    if (item.canReply) NotificationReply(item, onFocusableChanged)
                }
            }
        }
    }
}

@Composable
private fun NotificationReply(item: NotificationInfo, onFocusableChanged: (Boolean) -> Unit) {
    val context = LocalContext.current
    var replying by remember(item.key) { mutableStateOf(false) }
    var message by remember(item.key) { mutableStateOf("") }
    if (!replying) {
        TextButton(onClick = { replying = true; onFocusableChanged(true) }) { Text("Rýchlo odpovedať", color = Color(0xFFB9D69A), fontSize = 10.sp) }
    } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(
                value = message,
                onValueChange = { message = it },
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp),
                modifier = Modifier.weight(1f).onFocusChanged { onFocusableChanged(it.isFocused) },
                decorationBox = { inner ->
                    Box(Modifier.fillMaxWidth().background(Color(0xFF303B31), RoundedCornerShape(14.dp)).padding(horizontal = 10.dp, vertical = 9.dp)) {
                        if (message.isEmpty()) Text("Napíš odpoveď…", color = Color(0xFF9EAA99), fontSize = 11.sp)
                        inner()
                    }
                }
            )
            IconButton(onClick = {
                val sent = CapsuleRuntime.sendQuickReply(context, item.key, message)
                Toast.makeText(context, if (sent) "Odpoveď odoslaná" else "Aplikácia neponúka funkčnú rýchlu odpoveď", Toast.LENGTH_SHORT).show()
                message = ""
                replying = false
                onFocusableChanged(false)
            }) { Icon(Icons.Default.Send, "Odoslať odpoveď", tint = Color(0xFFB9D69A)) }
        }
    }
}

@Composable
private fun ControlsPanel(green: Color, muted: Color) {
    val context = LocalContext.current
    val torch = remember { DeviceControls.findTorch(context) }
    var torchOn by remember { mutableStateOf(false) }
    var torchStrength by remember { mutableFloatStateOf((torch?.maxStrength ?: 1).toFloat()) }
    var brightness by remember { mutableFloatStateOf(DeviceControls.brightnessPercent(context).toFloat()) }
    var mediaVolume by remember { mutableFloatStateOf(DeviceControls.volumePercent(context, AudioManager.STREAM_MUSIC).toFloat()) }
    var ringVolume by remember { mutableFloatStateOf(DeviceControls.volumePercent(context, AudioManager.STREAM_RING).toFloat()) }
    var autoBrightness by remember { mutableStateOf(DeviceControls.isAutoBrightness(context)) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ControlRow("Baterka", "${if (torchOn) "Zapnutá" else "Vypnutá"}${torch?.let { " • ${it.maxStrength} úrovní" } ?: " • nedostupná"}", Icons.Default.FlashOn, green) {
            if (torch == null) return@ControlRow
            val next = !torchOn
            DeviceControls.setTorch(context, next, torchStrength.toInt()).onSuccess { torchOn = next }.onFailure {
                Toast.makeText(context, it.message ?: "Baterku sa nepodarilo ovládať", Toast.LENGTH_SHORT).show()
            }
        }
        if (torch != null && torch.maxStrength > 1) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Intenzita blesku", color = Color.White, fontSize = 11.sp, modifier = Modifier.weight(1f))
                    Text("${torchStrength.toInt()} / ${torch.maxStrength}", color = muted, fontSize = 10.sp)
                }
                Slider(
                    value = torchStrength,
                    onValueChange = { torchStrength = it },
                    valueRange = 1f..torch.maxStrength.toFloat(),
                    onValueChangeFinished = {
                        if (torchOn) DeviceControls.setTorch(context, true, torchStrength.toInt()).onFailure {
                            Toast.makeText(context, it.message ?: "Zmena intenzity zlyhala", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Brightness6, null, tint = green, modifier = Modifier.size(17.dp))
                Text("  Jas displeja", color = Color.White, fontSize = 11.sp, modifier = Modifier.weight(1f))
                Text(if (autoBrightness) "Auto" else "${brightness.toInt()}%", color = muted, fontSize = 10.sp)
            }
            Slider(
                value = brightness,
                onValueChange = { brightness = it },
                onValueChangeFinished = {
                    DeviceControls.setBrightness(context, brightness.toInt()).onSuccess {
                        autoBrightness = false
                    }.onFailure {
                        Toast.makeText(context, it.message ?: "Zmena jasu zlyhala", Toast.LENGTH_SHORT).show()
                        MainActivity.openWriteSettings(context)
                    }
                },
                valueRange = 1f..100f,
                modifier = Modifier.height(26.dp)
            )
            TextButton(onClick = {
                DeviceControls.setAutoBrightness(context, !autoBrightness).onSuccess { autoBrightness = !autoBrightness }.onFailure {
                    Toast.makeText(context, it.message ?: "Zmena automatického jasu zlyhala", Toast.LENGTH_SHORT).show()
                    MainActivity.openWriteSettings(context)
                }
            }) { Text(if (autoBrightness) "Prepnúť na manuálny jas" else "Zapnúť automatický jas", fontSize = 10.sp, color = green) }
        }
        VolumeSlider("Hlasitosť médií", mediaVolume, { mediaVolume = it }, { DeviceControls.setVolume(context, AudioManager.STREAM_MUSIC, mediaVolume.toInt()) }, green, muted)
        VolumeSlider("Hlasitosť zvonenia", ringVolume, { ringVolume = it }, { DeviceControls.setVolume(context, AudioManager.STREAM_RING, ringVolume.toInt()) }, green, muted)
    }
}

@Composable
private fun ControlRow(title: String, status: String, icon: androidx.compose.ui.graphics.vector.ImageVector, accent: Color, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(16.dp), color = Color(0xFF202A22)) {
        Row(Modifier.fillMaxWidth().padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = accent)
            Column(Modifier.weight(1f).padding(start = 9.dp)) {
                Text(title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text(status, color = Color(0xFFB4C0AE), fontSize = 9.sp)
            }
            Text("Zmeniť", color = accent, fontSize = 10.sp)
        }
    }
}

@Composable
private fun VolumeSlider(title: String, value: Float, onChange: (Float) -> Unit, onFinished: () -> Unit, green: Color, muted: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.VolumeUp, null, tint = green, modifier = Modifier.size(16.dp))
            Text("  $title", color = Color.White, fontSize = 11.sp, modifier = Modifier.weight(1f))
            Text("${value.toInt()}%", color = muted, fontSize = 10.sp)
        }
        Slider(value = value, onValueChange = onChange, onValueChangeFinished = onFinished, valueRange = 0f..100f, modifier = Modifier.height(26.dp))
    }
}

@Composable
private fun ContactsPanel(muted: Color) {
    val context = LocalContext.current
    var entries by remember { mutableStateOf(CapsulePrefs.read(context).contacts) }
    val contacts = entries.mapNotNull { raw -> val parts = raw.split('|', limit = 2); if (parts.size == 2) parts[0] to parts[1] else null }
    if (contacts.isEmpty()) {
        Text("Zatiaľ nemáš pridané kontakty. V aplikácii Pixel Capsule vyber možnosť Pridať kontakt do kapsuly.", color = muted, fontSize = 11.sp)
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        contacts.forEach { (uri, name) ->
            Surface(onClick = { runCatching {
                val parsed = Uri.parse(uri)
                val intent = if (parsed.scheme == "tel") Intent(Intent.ACTION_DIAL, parsed) else Intent(Intent.ACTION_VIEW, parsed)
                context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } }, shape = RoundedCornerShape(16.dp), color = Color(0xFF202A22)) {
                Row(Modifier.fillMaxWidth().padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Call, null, tint = Color(0xFFB9D69A), modifier = Modifier.size(18.dp))
                    Text(name, color = Color.White, modifier = Modifier.weight(1f).padding(start = 10.dp), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    TextButton(onClick = { CapsulePrefs.removeContact(context, uri); entries = CapsulePrefs.read(context).contacts }) { Text("Odobrať", color = muted, fontSize = 9.sp) }
                }
            }
        }
    }
}

@Composable
private fun FavouriteAppsPanel(muted: Color, quickApps: Set<String>) {
    val context = LocalContext.current
    val allApps = remember(quickApps) { AppCatalog.installedLauncherApps(context).filter { it.packageName in quickApps } }
    if (allApps.isEmpty()) {
        Text("Zatiaľ nemáš vybrané skratky. Dlhým podržaním kapsuly otvor nastavenia a pridaj aplikácie.", color = muted, fontSize = 11.sp)
        return
    }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        items(allApps.take(8), key = { it.packageName }) { app ->
            Surface(
                onClick = { AppCatalog.launch(context, app.packageName) },
                shape = RoundedCornerShape(17.dp),
                color = Color(0xFF202A22)
            ) {
                Column(Modifier.width(76.dp).padding(9.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(34.dp).clip(CircleShape).background(Color(0xFF354735)), contentAlignment = Alignment.Center) {
                        Text(app.label.take(1).uppercase(), color = Color(0xFFB9D69A), fontWeight = FontWeight.Bold)
                    }
                    Text(app.label, color = Color.White, fontSize = 9.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}
