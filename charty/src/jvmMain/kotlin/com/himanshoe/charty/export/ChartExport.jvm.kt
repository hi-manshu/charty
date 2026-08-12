package com.himanshoe.charty.export

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image

private const val DOWNLOADS_DIRECTORY_NAME = "Downloads"
private const val ENCODE_FAILED_REASON = "Could not encode the chart as PNG"

/**
 * Encodes [bitmap] as a PNG and writes it into the user's `Downloads` folder, falling back to the
 * home directory when that folder does not exist.
 *
 * @param bitmap The captured chart image.
 * @param fileName The desired file name; it is normalised before use.
 * @return [ChartExportResult.Saved] with the absolute path, or [ChartExportResult.Failed].
 */
actual suspend fun exportChartImage(
    bitmap: ImageBitmap,
    fileName: String,
): ChartExportResult =
    withContext(Dispatchers.Default) {
        val bytes = encodeChartPng(bitmap) ?: return@withContext ChartExportResult.Failed(reason = ENCODE_FAILED_REASON)
        runCatching {
            val target = File(resolveExportDirectory(), sanitizeExportFileName(fileName))
            target.writeBytes(bytes)
            target.absolutePath
        }.fold(
            onSuccess = { path -> ChartExportResult.Saved(location = path) },
            onFailure = { error -> ChartExportResult.Failed(reason = error.message ?: ENCODE_FAILED_REASON) },
        )
    }

/**
 * Remembers the desktop exporter, which needs no composition-local state.
 *
 * @return A [ChartExporter] that writes PNGs to the user's Downloads folder.
 */
@Composable
actual fun rememberChartExporter(): ChartExporter =
    remember { ChartExporter { bitmap, fileName -> exportChartImage(bitmap = bitmap, fileName = fileName) } }

private fun resolveExportDirectory(): File {
    val home = File(System.getProperty("user.home") ?: ".")
    val downloads = File(home, DOWNLOADS_DIRECTORY_NAME)
    return if (downloads.isDirectory) downloads else home
}

private fun encodeChartPng(bitmap: ImageBitmap): ByteArray? =
    Image
        .makeFromBitmap(bitmap.asSkiaBitmap())
        .encodeToData()
        ?.bytes
