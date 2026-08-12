package com.himanshoe.charty.export

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val EXPORT_DIRECTORY_NAME = "charty-exports"
private const val PNG_QUALITY = 100
private const val MISSING_CONTEXT_REASON =
    "No Android Context available; call rememberChartExporter() in composition and use its export()."

private var applicationContext: Context? = null

/**
 * Writes the PNG into the app's cache directory and returns its path.
 *
 * Handing the file to `ACTION_SEND` would require the consuming app to declare a `FileProvider` in
 * its manifest, which this library cannot do on the app's behalf, so the Android export saves rather
 * than shares. Apps that already own a `FileProvider` can share the returned path themselves.
 *
 * @param bitmap The captured chart image.
 * @param fileName The desired file name; it is normalised before use.
 * @return [ChartExportResult.Saved] with the absolute path, or [ChartExportResult.Failed].
 */
actual suspend fun exportChartImage(
    bitmap: ImageBitmap,
    fileName: String,
): ChartExportResult {
    val context = applicationContext ?: return ChartExportResult.Failed(reason = MISSING_CONTEXT_REASON)
    return writeChartPng(context = context, bitmap = bitmap, fileName = fileName)
}

/**
 * Remembers an exporter bound to the application [Context] of the current composition, and registers
 * that context so the top-level [exportChartImage] works too.
 *
 * @return A [ChartExporter] that writes PNGs into the app's cache directory.
 */
@Composable
actual fun rememberChartExporter(): ChartExporter {
    val context = LocalContext.current.applicationContext
    applicationContext = context
    return remember(context) {
        ChartExporter { bitmap, fileName -> writeChartPng(context = context, bitmap = bitmap, fileName = fileName) }
    }
}

private suspend fun writeChartPng(
    context: Context,
    bitmap: ImageBitmap,
    fileName: String,
): ChartExportResult =
    withContext(Dispatchers.Default) {
        val target = File(File(context.cacheDir, EXPORT_DIRECTORY_NAME), sanitizeExportFileName(fileName))
        runCatching {
            target.parentFile?.mkdirs()
            FileOutputStream(target).use { stream ->
                bitmap.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, PNG_QUALITY, stream)
            }
            target.absolutePath
        }.fold(
            onSuccess = { path -> ChartExportResult.Saved(location = path) },
            onFailure = { error -> ChartExportResult.Failed(reason = error.message ?: "PNG export failed") },
        )
    }
