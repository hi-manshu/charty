package com.himanshoe.charty.export

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import org.jetbrains.skia.Image
import platform.Foundation.NSData
import platform.Foundation.create
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIViewController

private const val ENCODE_FAILED_REASON = "Could not encode the chart as PNG"
private const val NO_WINDOW_REASON = "No presenting view controller is available"

/**
 * Encodes [bitmap] as a PNG, wraps it in a `UIImage` and presents the system share sheet for it.
 *
 * @param bitmap The captured chart image.
 * @param fileName Ignored on iOS: `UIActivityViewController` names the shared image itself, and the
 *   destination app decides the final file name.
 * @return [ChartExportResult.Shared] once the sheet is presented, or [ChartExportResult.Failed].
 */
@Suppress("UNUSED_PARAMETER", "ReturnCount")
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
    if (bytes.isEmpty()) {
        return ChartExportResult.Failed(reason = ENCODE_FAILED_REASON)
    }
    val image =
        UIImage.imageWithData(bytes.toNSData()) ?: return ChartExportResult.Failed(reason = ENCODE_FAILED_REASON)
    val presenter = topViewController() ?: return ChartExportResult.Failed(reason = NO_WINDOW_REASON)
    val sheet = UIActivityViewController(activityItems = listOf(image), applicationActivities = null)
    presenter.presentViewController(viewControllerToPresent = sheet, animated = true, completion = null)
    return ChartExportResult.Shared
}

/**
 * Remembers the iOS exporter, which needs no composition-local state.
 *
 * @return A [ChartExporter] that presents the system share sheet.
 */
@Composable
actual fun rememberChartExporter(): ChartExporter =
    remember { ChartExporter { bitmap, fileName -> exportChartImage(bitmap = bitmap, fileName = fileName) } }

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData =
    usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
    }

private fun topViewController(): UIViewController? {
    val root =
        UIApplication.sharedApplication
            .keyWindow
            ?.rootViewController
    var current = root
    while (current?.presentedViewController != null) {
        current = current.presentedViewController
    }
    return current
}
