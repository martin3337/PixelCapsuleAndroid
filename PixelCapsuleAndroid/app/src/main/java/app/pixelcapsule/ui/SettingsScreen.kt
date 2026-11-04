package app.pixelcapsule.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.pixelcapsule.core.CapsulePrefs

@Composable
fun SettingsScreen(onBack: () -> Unit, onRestartOverlay: () -> Unit) {
    val context = LocalContext.current
    val initial = remember { CapsulePrefs.read(context) }
    var width by remember { mutableFloatStateOf(initial.widthDp) }
    var expandedWidth by remember { mutableFloatStateOf(initial.expandedWidthDp) }
    var radius by remember { mutableFloatStateOf(initial.cornerRadiusDp) }
    var offsetX by remember { mutableFloatStateOf(initial.offsetXDp) }
    var offsetY by remember { mutableFloatStateOf(initial.offsetYDp) }
    var dynamic by remember { mutableStateOf(initial.dynamicColors) }
    var media by remember { mutableStateOf(initial.showMedia) }
    var notifications by remember { mutableStateOf(initial.showNotifications) }
    var controls by remember { mutableStateOf(initial.showControls) }
    var apps by remember { mutableStateOf(initial.showApps) }
    var widgetText by remember { mutableStateOf(initial.widgetText) }
    var accent by remember { mutableStateOf(initial.accentColorArgb) }
    var tap by remember { mutableStateOf(initial.tapAction) }
    var doubleTap by remember { mutableStateOf(initial.doubleTapAction) }
    var longPress by remember { mutableStateOf(initial.longPressAction) }
    var swipeLeft by remember { mutableStateOf(initial.swipeLeftAction) }
    var swipeRight by remember { mutableStateOf(initial.swipeRightAction) }
    var swipeUp by remember { mutableStateOf(initial.swipeUpAction) }
    var swipeDown by remember { mutableStateOf(initial.swipeDownAction) }

    fun save(restart: Boolean = false) {
        CapsulePrefs.write(context) {
            copy(
                widthDp = width,
                expandedWidthDp = expandedWidth,
                cornerRadiusDp = radius,
                offsetXDp = offsetX,
                offsetYDp = offsetY,
                dynamicColors = dynamic,
                showMedia = media,
                showNotifications = notifications,
                showControls = controls,
                showApps = apps,
                widgetText = widgetText,
                accentColorArgb = accent,
                tapAction = tap,
                doubleTapAction = doubleTap,
                longPressAction = longPress,
                swipeLeftAction = swipeLeft,
                swipeRightAction = swipeRight,
                swipeUpAction = swipeUp,
                swipeDownAction = swipeDown
            )
        }
        if (restart) onRestartOverlay()
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onBack) { Text("← Späť") }
        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Jemne dolaď polohu, rozmery a obsah kapsuly. Poloha výrezu sa zisťuje z geometrie displeja automaticky.", color = MaterialTheme.colorScheme.onSurfaceVariant)

        Card(shape = androidx.compose.foundation.shape.RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.qr_widget), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("Vlož text alebo odkaz. Kapsula z neho vytvorí QR kód; v kapsule ho môžeš skopírovať alebo otvoriť odkaz.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = widgetText,
                    onValueChange = { widgetText = it.take(500); save() },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.text_or_url)) },
                    placeholder = { Text("https://example.com") },
                    minLines = 2,
                    maxLines = 4
                )
                TextButton(onClick = { save(true) }) { Text(stringResource(R.string.save_restart)) }
            }
        }

        Card(shape = androidx.compose.foundation.shape.RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.geometry), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                SliderSetting(stringResource(R.string.collapsed_width), width, 104f..220f, { width = it; save() }, { save(true) }, "${width.toInt()} dp")
                SliderSetting(stringResource(R.string.expanded_width), expandedWidth, 260f..420f, { expandedWidth = it; save() }, { save(true) }, "${expandedWidth.toInt()} dp")
                SliderSetting(stringResource(R.string.corner_radius), radius, 16f..48f, { radius = it; save() }, { save(true) }, "${radius.toInt()} dp")
                SliderSetting(stringResource(R.string.horizontal_offset), offsetX, -120f..120f, { offsetX = it; save() }, { save(true) }, "${offsetX.toInt()} dp")
                SliderSetting(stringResource(R.string.vertical_offset), offsetY, -20f..100f, { offsetY = it; save() }, { save(true) }, "${offsetY.toInt()} dp")
            }
        }

        Card(shape = androidx.compose.foundation.shape.RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.appearance_content), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                SwitchRow(stringResource(R.string.dynamic_colors), stringResource(R.string.wallpaper_colors), dynamic) { dynamic = it; save(true) }
                SwitchRow(stringResource(R.string.media_player), stringResource(R.string.active_track_controls), media) { media = it; save(true) }
                SwitchRow(stringResource(R.string.notification_title), stringResource(R.string.notification_desc), notifications) { notifications = it; save(true) }
                SwitchRow(stringResource(R.string.system_controls), stringResource(R.string.flashlight_brightness_volume), controls) { controls = it; save(true) }
                SwitchRow(stringResource(R.string.favourite_apps), stringResource(R.string.favourite_apps_desc), apps) { apps = it; save(true) }
            }
        }
                Card(shape = androidx.compose.foundation.shape.RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.capsule_colour), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text(stringResource(R.string.accent_desc), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                val palette = listOf(stringResource(R.string.green) to 0xFFB9D69A, stringResource(R.string.blue) to 0xFF9CCBFF, stringResource(R.string.purple) to 0xFFD0B6FF, stringResource(R.string.pink) to 0xFFFFB8D2, stringResource(R.string.orange) to 0xFFFFC58A, stringResource(R.string.turquoise) to 0xFF91D9D0)
                palette.forEach { (name, value) ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.foundation.Canvas(Modifier.padding(end = 10.dp).size(22.dp)) { drawCircle(Color(value)) }
                        Text(name, modifier = Modifier.weight(1f))
                        androidx.compose.material3.RadioButton(selected = accent == value, onClick = { accent = value; save(true) })
                    }
                }
            }
        }
        Card(shape = androidx.compose.foundation.shape.RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.custom_gestures), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("Nastav akciu pre ťuknutie, dvojité ťuknutie, podržanie a všetky smery potiahnutia.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                val actions = listOf("toggle" to stringResource(R.string.gesture_toggle), "expand" to stringResource(R.string.gesture_expand), "collapse" to stringResource(R.string.gesture_collapse), "next_section" to stringResource(R.string.gesture_next), "previous_section" to stringResource(R.string.gesture_previous), "media" to stringResource(R.string.gesture_open_player), "notifications" to stringResource(R.string.gesture_open_notifications), "settings" to stringResource(R.string.gesture_open_settings))
                GestureSetting(stringResource(R.string.gesture_tap), tap, actions) { tap = it; save(true) }
                GestureSetting(stringResource(R.string.gesture_double_tap), doubleTap, actions) { doubleTap = it; save(true) }
                GestureSetting(stringResource(R.string.gesture_long_press), longPress, actions) { longPress = it; save(true) }
                GestureSetting(stringResource(R.string.gesture_swipe_left), swipeLeft, actions) { swipeLeft = it; save(true) }
                GestureSetting(stringResource(R.string.gesture_swipe_right), swipeRight, actions) { swipeRight = it; save(true) }
                GestureSetting(stringResource(R.string.gesture_swipe_up), swipeUp, actions) { swipeUp = it; save(true) }
                GestureSetting(stringResource(R.string.gesture_swipe_down), swipeDown, actions) { swipeDown = it; save(true) }
            }
        }
        Text("Poznámka: Android vykresľuje systémový stavový riadok nad aplikačnými overlay oknami. Kapsula sa preto zarovnáva podľa výrezu a zobrazuje sa v jeho dostupnej oblasti; nemôže nahradiť systémový stavový riadok.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SliderSetting(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    onFinished: () -> Unit,
    valueLabel: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
            Text(valueLabel, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = range, onValueChangeFinished = onFinished)
    }
}

@Composable
private fun SwitchRow(title: String, description: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, fontWeight = FontWeight.Medium)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun GestureSetting(title: String, selected: String, options: List<Pair<String, String>>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val label = options.firstOrNull { it.first == selected }?.second ?: selected
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
        androidx.compose.foundation.layout.Box {
            TextButton(onClick = { expanded = true }) { Text(label) }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { (value, text) -> DropdownMenuItem(text = { Text(text) }, onClick = { onSelect(value); expanded = false }) }
            }
        }
    }
}
