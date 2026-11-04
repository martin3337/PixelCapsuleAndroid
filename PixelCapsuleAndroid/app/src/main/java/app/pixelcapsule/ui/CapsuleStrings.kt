package app.pixelcapsule.ui

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.google.android.gms.tasks.Task
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * UI translations are resolved from Android resources first (English fallback and
 * native Slovak strings). Other supported system languages are translated on-device
 * with ML Kit. Translation model downloads are requested only on Wi-Fi.
 */
@Composable
fun capsuleString(@StringRes resourceId: Int, vararg formatArgs: Any): String {
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]
    val targetLanguage = locale.language.lowercase()
    val raw = stringResource(resourceId, *formatArgs)
    var resolved by remember(raw, targetLanguage) { mutableStateOf(raw) }

    LaunchedEffect(raw, targetLanguage) {
        resolved = UiTranslations.translate(context.applicationContext, raw, targetLanguage)
    }
    return resolved
}

private object UiTranslations {
    private val translators = ConcurrentHashMap<String, Translator>()
    private val locks = ConcurrentHashMap<String, Mutex>()
    private val readyModels = ConcurrentHashMap.newKeySet<String>()

    suspend fun translate(context: Context, text: String, targetTag: String): String {
        if (text.isBlank()) return text
        val source = if (targetTag == "sk") TranslateLanguage.SLOVAK else TranslateLanguage.ENGLISH
        val target = TranslateLanguage.fromLanguageTag(targetTag) ?: return text
        if (source == target) return text

        val key = "$source-$target"
        val cache = context.getSharedPreferences("pixel_capsule_ui_translations", Context.MODE_PRIVATE)
        val cacheKey = "v1_${target}_${text.hashCode().toUInt().toString(16)}"
        cache.getString(cacheKey, null)?.let { return it }

        val lock = locks.getOrPut(key) { Mutex() }
        return lock.withLock {
            cache.getString(cacheKey, null)?.let { return@withLock it }
            val translator = translators.getOrPut(key) {
                val options = TranslatorOptions.Builder()
                    .setSourceLanguage(source)
                    .setTargetLanguage(target)
                    .build()
                Translation.getClient(options)
            }

            try {
                if (!readyModels.contains(key)) {
                    val conditions = DownloadConditions.Builder().requireWifi().build()
                    translator.downloadModelIfNeeded(conditions).awaitTask()
                    readyModels.add(key)
                }
                val translated = translator.translate(text).awaitTask()
                if (translated.isNotBlank()) {
                    cache.edit().putString(cacheKey, translated).apply()
                    translated
                } else text
            } catch (_: Exception) {
                // Keep the UI responsive and readable when offline or when the
                // requested language is not supported by the installed ML Kit release.
                text
            }
        }
    }
}

private suspend fun <T> Task<T>.awaitTask(): T =
    suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            if (continuation.isActive) continuation.resume(result)
        }
        addOnFailureListener { error ->
            if (continuation.isActive) continuation.resumeWithException(error)
        }
        addOnCanceledListener {
            if (continuation.isActive) continuation.cancel()
        }
    }
