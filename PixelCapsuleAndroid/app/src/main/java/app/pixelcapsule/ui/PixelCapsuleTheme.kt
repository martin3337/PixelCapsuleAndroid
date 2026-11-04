package app.pixelcapsule.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val CapsuleLight = lightColorScheme(
    primary = Color(0xFF536F3B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD8EDB9),
    onPrimaryContainer = Color(0xFF26351F),
    secondary = Color(0xFF63734F),
    secondaryContainer = Color(0xFFE8EEDB),
    tertiary = Color(0xFF655B83),
    tertiaryContainer = Color(0xFFEAE8F4),
    background = Color(0xFFF2F3EA),
    surface = Color(0xFFFFFEF9),
    surfaceVariant = Color(0xFFE9EDE2),
    onSurface = Color(0xFF283126),
    onSurfaceVariant = Color(0xFF65715E),
    error = Color(0xFFBA1A1A)
)

private val CapsuleDark = darkColorScheme(
    primary = Color(0xFFB9D69A),
    onPrimary = Color(0xFF25351D),
    primaryContainer = Color(0xFF3C532D),
    onPrimaryContainer = Color(0xFFD8EDB9),
    secondary = Color(0xFFC2D1AC),
    secondaryContainer = Color(0xFF424F36),
    tertiary = Color(0xFFD2C8F0),
    tertiaryContainer = Color(0xFF4C4568),
    background = Color(0xFF101510),
    surface = Color(0xFF171D18),
    surfaceVariant = Color(0xFF2B342B),
    onSurface = Color(0xFFF0F2E9),
    onSurfaceVariant = Color(0xFFBEC9B5),
    error = Color(0xFFFFB4AB)
)

@Composable
fun PixelCapsuleTheme(
    dynamicColors: Boolean = true,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colors = when {
        dynamicColors && Build.VERSION.SDK_INT >= 31 && darkTheme -> dynamicDarkColorScheme(context)
        dynamicColors && Build.VERSION.SDK_INT >= 31 -> dynamicLightColorScheme(context)
        darkTheme -> CapsuleDark
        else -> CapsuleLight
    }
    MaterialTheme(colorScheme = colors, content = content)
}
