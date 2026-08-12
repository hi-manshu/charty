package com.himanshoe.charty.export

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer

/**
 * Holds the [GraphicsLayer] that a chart records itself into so the chart can be turned into an
 * [ImageBitmap] on demand.
 *
 * Obtain one with [rememberChartCaptureController], attach it to the chart with
 * [Modifier.chartCapture], then call [capture] from a coroutine.
 *
 * @property graphicsLayer The layer the wrapped chart records its drawing into.
 */
@Stable
class ChartCaptureController internal constructor(
    internal val graphicsLayer: GraphicsLayer,
)

/**
 * Creates and remembers a [ChartCaptureController] backed by a composition-scoped graphics layer.
 *
 * @return A [ChartCaptureController] that stays valid for as long as it stays in composition.
 */
@Composable
fun rememberChartCaptureController(): ChartCaptureController {
    val graphicsLayer = rememberGraphicsLayer()
    return remember(graphicsLayer) { ChartCaptureController(graphicsLayer = graphicsLayer) }
}

/**
 * Records the wrapped content into [controller]'s graphics layer and then draws that layer, so the
 * chart renders exactly as it would without this modifier while also being capturable.
 *
 * @param controller The controller whose graphics layer receives the recording.
 * @return A [Modifier] that both draws and captures the content it wraps.
 */
fun Modifier.chartCapture(controller: ChartCaptureController): Modifier =
    this.drawWithContent {
        controller.graphicsLayer.record { this@drawWithContent.drawContent() }
        drawLayer(controller.graphicsLayer)
    }

/**
 * Reads back the most recently recorded frame of the chart this controller is attached to.
 *
 * Call it after at least one frame has been drawn; the layer is empty until the chart has composed
 * and drawn once.
 *
 * @return The recorded chart pixels as an [ImageBitmap].
 */
suspend fun ChartCaptureController.capture(): ImageBitmap = graphicsLayer.toImageBitmap()
