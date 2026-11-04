package app.pixelcapsule.system

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import android.telecom.TelecomManager

object DeviceControls {
    data class TorchInfo(val cameraId: String, val maxStrength: Int)

    fun findTorch(context: Context): TorchInfo? {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        return try {
            manager.cameraIdList.firstNotNullOfOrNull { id ->
                val chars = manager.getCameraCharacteristics(id)
                if (chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) != true) return@firstNotNullOfOrNull null
                val isBack = chars.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
                if (!isBack) return@firstNotNullOfOrNull null
                val max = if (Build.VERSION.SDK_INT >= 33) {
                    chars.get(CameraCharacteristics.FLASH_INFO_STRENGTH_MAXIMUM_LEVEL) ?: 1
                } else 1
                TorchInfo(id, max.coerceAtLeast(1))
            }
        } catch (_: Exception) {
            null
        }
    }

    fun setTorch(context: Context, enabled: Boolean, strength: Int = 1): Result<Unit> = runCatching {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            throw SecurityException("Povolenie fotoaparátu ešte nie je udelené.")
        }
        val torch = findTorch(context) ?: throw IllegalStateException("Toto zariadenie nemá dostupný blesk.")
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        if (enabled && Build.VERSION.SDK_INT >= 33 && torch.maxStrength > 1) {
            manager.turnOnTorchWithStrengthLevel(torch.cameraId, strength.coerceIn(1, torch.maxStrength))
        } else {
            manager.setTorchMode(torch.cameraId, enabled)
        }
    }

    fun canModifySettings(context: Context): Boolean = Settings.System.canWrite(context)

    fun brightnessPercent(context: Context): Int {
        val raw = runCatching {
            Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        }.getOrDefault(128)
        return ((raw / 255f) * 100f).toInt().coerceIn(1, 100)
    }

    fun isAutoBrightness(context: Context): Boolean = runCatching {
        Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE) ==
            Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
    }.getOrDefault(false)

    fun setBrightness(context: Context, percent: Int): Result<Unit> = runCatching {
        check(canModifySettings(context)) { "Povoľte Pixel Capsule meniť systémové nastavenia." }
        // Moving the manual slider explicitly switches from automatic to manual brightness.
        check(Settings.System.putInt(
            context.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS_MODE,
            Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
        )) { "Systém odmietol manuálny režim jasu." }
        check(Settings.System.putInt(
            context.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS,
            (percent.coerceIn(1, 100) * 255 / 100).coerceIn(1, 255)
        )) { "Systém odmietol zmenu jasu." }
        Unit
    }

    fun setAutoBrightness(context: Context, enabled: Boolean): Result<Unit> = runCatching {
        check(canModifySettings(context)) { "Povoľte Pixel Capsule meniť systémové nastavenia." }
        check(Settings.System.putInt(
            context.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS_MODE,
            if (enabled) Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC
            else Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
        )) { "Systém odmietol zmenu režimu jasu." }
        Unit
    }

    fun volumePercent(context: Context, stream: Int): Int {
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val max = audio.getStreamMaxVolume(stream).coerceAtLeast(1)
        return (audio.getStreamVolume(stream) * 100f / max).toInt().coerceIn(0, 100)
    }

    fun setVolume(context: Context, stream: Int, percent: Int) {
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val max = audio.getStreamMaxVolume(stream).coerceAtLeast(1)
        val value = (percent.coerceIn(0, 100) * max / 100f).toInt().coerceIn(0, max)
        audio.setStreamVolume(stream, value, 0)
    }

    @Suppress("DEPRECATION")
    fun answerOrRejectCall(context: Context, answer: Boolean): Result<Unit> = runCatching {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
            throw SecurityException("Povolenie na správu hovoru ešte nie je udelené.")
        }
        val telecom = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        if (answer) {
            telecom.acceptRingingCall()
        } else {
            check(telecom.endCall()) { "Android odmietol ukončiť hovor." }
        }
        Unit
    }
}
