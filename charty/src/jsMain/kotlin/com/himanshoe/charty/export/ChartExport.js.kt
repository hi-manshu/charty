package com.himanshoe.charty.export

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skia.Image

private const val ENCODE_FAILED_REASON = "Could not encode the chart as PNG"
private const val PNG_DATA_URL_PREFIX = "data:image/png;base64,"

/**
 * Encodes [bitmap] as a PNG and starts a browser download of it by clicking a synthetic anchor whose
 * `href` is a `data:` URL, which keeps the export free of blob-lifetime bookkeeping.
 *
 * @param bitmap The captured chart image.
 * @param fileName The desired file name; it is normalised before use.
 * @return [ChartExportResult.Saved] with the download file name, or [ChartExportResult.Failed].
 */
actual suspend fun exportChartImage(
    bitmap: ImageBitmap,
    fileName: String,
): ChartExportResult {
    val bytes =
        Image
            .makeFromBitmap(bitmap.asSkiaBitmap())
            .encodeToData()
            ?.bytes
            ?: return ChartExportResult.Failed(reason = ENCODE_FAILED_REASON)
    val safeName = sanitizeExportFileName(fileName)
    return runCatching {
        downloadDataUrl(url = PNG_DATA_URL_PREFIX + bytes.encodeChartBase64(), name = safeName)
        ChartExportResult.Saved(location = safeName)
    }.getOrElse { error -> ChartExportResult.Failed(reason = error.message ?: ENCODE_FAILED_REASON) }
}

/**
 * Remembers the browser exporter, which needs no composition-local state.
 *
 * @return A [ChartExporter] that downloads PNGs through the browser.
 */
@Composable
actual fun rememberChartExporter(): ChartExporter =
    remember { ChartExporter { bitmap, fileName -> exportChartImage(bitmap = bitmap, fileName = fileName) } }

@Suppress("UnusedParameter")
private fun downloadDataUrl(
    url: String,
    name: String,
) {
    js("var a=document.createElement('a');a.href=url;a.download=name;a.click();")
}
