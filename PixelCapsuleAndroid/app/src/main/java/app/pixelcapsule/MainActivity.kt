package app.pixelcapsule

import android.content.Intent
import android.net.Uri
import android.provider.Settings
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PixelCapsuleTheme {
                PixelCapsuleApp(
                    onStartOverlay = { startOverlay() },
                    onStopOverlay = { stopService(Intent(this, CapsuleOverlayService::class.java)) },
                    onRequestCallPermissions = { requestCallPermissions() },
                    onRequestCameraPermission = { requestCameraPermission() },
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
        ContextCompat.startForegroundService(this, intent)
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
