package app.pixelcapsule.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.pixelcapsule.core.CapsulePrefs
import app.pixelcapsule.core.CapsuleRuntime
import app.pixelcapsule.system.AppCatalog
import app.pixelcapsule.system.LaunchableApp

@Composable
fun AppsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val launchableApps = remember { AppCatalog.installedLauncherApps(context) }
    var favourites by remember { mutableStateOf(CapsulePrefs.read(context).quickApps) }
    Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = onBack) { Text("← Späť") }
        Text("Rýchle aplikácie", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Vyber aplikácie, ktoré chceš mať po ruke v rozbalenej kapsule. Zobrazujú sa spustiteľné aplikácie; Android obmedzuje prístup k skrytým balíkom.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Vybrané: ${favourites.size}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(launchableApps, key = { it.packageName }) { app ->
                AppChoiceRow(app, favourites.contains(app.packageName)) { checked ->
                    favourites = if (checked) favourites + app.packageName else favourites - app.packageName
                    CapsulePrefs.setQuickApps(context, favourites)
                    CapsuleRuntime.setQuickApps(favourites)
                }
            }
        }
    }
}

@Composable
private fun AppChoiceRow(app: LaunchableApp, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Card(shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(app.label.take(1).uppercase(), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
                Text(app.label, fontWeight = FontWeight.Medium)
                Text(app.packageName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Checkbox(checked = checked, onCheckedChange = onChecked)
        }
    }
}
