package app.pixelcapsule

import android.content.Intent
import android.os.Bundle
import android.os.Build
import android.net.Uri
import android.provider.Settings
import android.provider.ContactsContract
import android.widget.Toast
import app.pixelcapsule.core.CapsulePrefs
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import app.pixelcapsule.service.CapsuleOverlayService
import app.pixelcapsule.ui.PixelCapsuleApp
import app.pixelcapsule.ui.PixelCapsuleTheme

class MainActivity : ComponentActivity() {
    private var permissionRefresh by mutableIntStateOf(0)
    private val permissionResult = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }
    private val contactPicker = registerForActivityResult(ActivityResultContracts.PickContact()) { uri ->
        if (uri != null) {
            val contact = contentResolver.query(uri, arrayOf(ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts._ID), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) to cursor.getString(1) else "Kontakt" to null
            } ?: ("Kontakt" to null)
            val phone = contact.second?.let { contactId ->
                contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER), ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", arrayOf(contactId), null)?.use { cursor ->
                    if (cursor.moveToFirst()) cursor.getString(0) else null
                }
            }
            val shortcutUri = phone?.let { Uri.fromParts("tel", it, null).toString() } ?: uri.toString()
            CapsulePrefs.addContact(this, shortcutUri, contact.first)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PixelCapsuleTheme {
                PixelCapsuleApp(
                    onStartOverlay = { startOverlay() },
                    onStopOverlay = { stopService(Intent(this, CapsuleOverlayService::class.java)) },
                    onRequestCallPermissions = { requestCallPermissions() },
                    onRequestCameraPermission = { requestCameraPermission() },
                    onRequestBluetoothPermission = { requestBluetoothPermission() },
                    onPickContact = { contactPicker.launch(null) },
                    permissionRefreshKey = permissionRefresh
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        permissionRefresh++
    }

    private fun startOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            openOverlaySettings(this)
            return
        }
        val intent = Intent(this, CapsuleOverlayService::class.java).setAction(CapsuleOverlayService.ACTION_START)
        runCatching {
            ContextCompat.startForegroundService(this, intent)
        }.onFailure { error ->
            Toast.makeText(
                this,
                "Could not start Pixel Capsule: ${error.localizedMessage ?: error.javaClass.simpleName}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun requestCallPermissions() {
        permissionResult.launch(
            arrayOf(
                android.Manifest.permission.READ_PHONE_STATE,
                android.Manifest.permission.ANSWER_PHONE_CALLS
            )
        )
    }

    private fun requestCameraPermission() {
        permissionResult.launch(arrayOf(android.Manifest.permission.CAMERA))
    }

    private fun requestBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= 31) {
            permissionResult.launch(arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT))
        }
    }

    companion object {
        fun openOverlaySettings(context: android.content.Context) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }

        fun openNotificationSettings(context: android.content.Context) {
            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }

        fun openWriteSettings(context: android.content.Context) {
            val intent = Intent(
                Settings.ACTION_MANAGE_WRITE_SETTINGS,
                Uri.parse("package:${context.packageName}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}
