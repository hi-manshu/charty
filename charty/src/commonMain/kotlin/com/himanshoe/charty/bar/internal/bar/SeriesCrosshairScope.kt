package com.himanshoe.charty.bar.internal.bar

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.StreamingLayout
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.gesture.AnimatedCrosshair
import com.himanshoe.charty.common.gesture.ChartCrosshair
import com.himanshoe.charty.common.gesture.ChartCrosshairConfig
import com.himanshoe.charty.common.gesture.ChartCrosshairHost
import com.himanshoe.charty.common.gesture.CrosshairManager
import com.himanshoe.charty.common.gesture.CrosshairState
import com.himanshoe.charty.common.gesture.chartCrosshairHandler
import com.himanshoe.charty.common.gesture.rememberChartCrosshair
import com.himanshoe.charty.common.streamingPan

/**
 * Everything a bar-family chart whose items are not plain [com.himanshoe.charty.bar.data.BarData]
 * needs to run a crosshair: the gesture manager, the smoothed position read during the draw pass, the
 * anchor points the gesture snaps to, and the label text for the snapped item. It is the same
 * mechanism [BarCrosshairScope] gives the plain bar charts, generalised over the reported item type.
 * Obtain one with [rememberSeriesCrosshair]; when the chart has no crosshair the scope is still
 * created but [isActive] is `false` and every helper is a no-op.
 *
 * @param T The item type the chart reports to the crosshair label and to [ChartCrosshair].
 * @property manager Owns the live crosshair state, or `null` when the crosshair is off.
 * @property bounds One anchor point per drawn item, refreshed on every draw pass.
 * @property crosshair The crosshair whose label composable is hosted over the chart, or `null`.
 * @property orientation The host chart's orientation, deciding the snapping and guide-line axis.
 * @property labelFormatter Converts a snapped item into the text its label reads.
 */
@Immutable
internal class SeriesCrosshairScope<T>(
    val manager: CrosshairManager<T>?,
    private val animated: AnimatedCrosshair?,
    val bounds: MutableList<Pair<Offset, T>>,
    val crosshair: ChartCrosshair<T>?,
    val orientation: ChartOrientation,
    val labelFormatter: (T) -> String,
) {
    /** Whether the chart has a crosshair configured at all. */
    val isActive: Boolean get() = manager != null

    /**
     * The crosshair styling in the shape the shared bar-family overlay drawer
     * ([drawBarCrosshairOverlay]) takes, so this scope draws exactly what a plain bar chart draws.
     */
    val overlayStyle: BarChartConfig = BarChartConfig(crosshairConfig = crosshair?.config)

    /** The smoothed crosshair position to draw this frame, or `null` when nothing is snapped. */
    fun currentState(): CrosshairState? = animated?.resolve()
}

/**
 * Builds the [SeriesCrosshairScope] for a bar-family chart, accepting the crosshair either as an
 * explicit [crosshair] or as a [ChartCrosshairConfig] already set on the chart's configuration.
 *
 * @param T The item type the chart reports to the crosshair.
 * @param crosshairConfig The crosshair styling set on the chart's configuration, or `null`.
 * @param crosshair The crosshair passed to the chart, or `null` when none was.
 * @param interactionConfig Supplies the viewport or streaming geometry used for cross-chart syncing.
 * @param labelFormatter Converts a snapped item into the text its label reads.
 * @param orientation The chart's orientation.
 * @return The scope the chart threads through its modifier, draw pass, and overlays.
 */
@Composable
internal fun <T> rememberSeriesCrosshair(
    crosshairConfig: ChartCrosshairConfig?,
    crosshair: ChartCrosshair<T>?,
    interactionConfig: ChartInteractionConfig,
    labelFormatter: (T) -> String,
    orientation: ChartOrientation = ChartOrientation.VERTICAL,
): SeriesCrosshairScope<T> {
    val activeCrosshair = crosshair ?: crosshairConfig?.let { ChartCrosshair<T>(config = it) }
    val bounds = remember { mutableListOf<Pair<Offset, T>>() }
    val (manager, animated) =
        rememberChartCrosshair<T>(
            enabled = activeCrosshair != null,
            interactionConfig = interactionConfig,
        )
    return SeriesCrosshairScope(
        manager = manager,
        animated = animated,
        bounds = bounds,
        crosshair = activeCrosshair,
        orientation = orientation,
        labelFormatter = labelFormatter,
    )
}

/**
 * Chains the crosshair drag gesture onto this modifier when [crosshair] is active, leaving any
 * co-installed click or scrub handler intact so tapping an item still raises its tooltip. Returns the
 * receiver unchanged when the chart has no crosshair.
 *
 * @param T The item type the chart reports to the crosshair.
 * @param D The type of the items in [dataList].
 * @param crosshair The chart's crosshair scope.
 * @param dataList The items currently drawn, used as the gesture's recomposition key.
 * @return This modifier with the crosshair handler chained on, or this modifier itself.
 */
internal fun <T, D> Modifier.seriesCrosshairHandler(
    crosshair: SeriesCrosshairScope<T>,
    dataList: List<D>,
): Modifier {
    val manager = crosshair.manager ?: return this
    return this.chartCrosshairHandler(
        dataList = dataList,
        pointBounds = crosshair.bounds,
        onCrosshairUpdate = { state, item -> manager.update(newState = state, item = item) },
        labelFormatter = crosshair.labelFormatter,
        dismissOnRelease = crosshair.crosshair?.config?.dismissOnRelease ?: true,
        orientation = crosshair.orientation,
    )
}

/**
 * The streaming scrollback pan for a bar-family chart, suppressed while a crosshair is configured
 * because the two gestures would compete for the same drag.
 *
 * @param T The item type the chart reports to the crosshair.
 * @param streaming The active streaming layout, or `null` when the chart is not streaming.
 * @param crosshair The chart's crosshair scope.
 * @return The pan modifier, or an empty [Modifier].
 */
internal fun <T> ChartInteractionConfig.seriesStreamingPan(
    streaming: StreamingLayout?,
    crosshair: SeriesCrosshairScope<T>,
): Modifier =
    if (crosshair.isActive) {
        Modifier
    } else {
        streamingPan(streaming = streaming, orientation = crosshair.orientation)
    }

/**
 * Replaces the crosshair's anchor points with [anchors], pairing each with the item it belongs to.
 *
 * @param T The item type the chart reports to the crosshair.
 * @param bounds The list to refill; cleared first.
 * @param anchors The anchor point of each drawn item, in drawn order.
 * @param items The items the crosshair reports, in the same order as [anchors].
 */
internal fun <T> recordSeriesCrosshairBounds(
    bounds: MutableList<Pair<Offset, T>>,
    anchors: List<Offset>,
    items: List<T>,
) {
    bounds.clear()
    val count = minOf(anchors.size, items.size)
    for (index in 0 until count) {
        bounds.add(anchors[index] to items[index])
    }
}

/**
 * Refreshes the crosshair's anchor points from the items drawn this frame and draws the guide line,
 * highlight dot, and label for the snapped item. Does nothing when the chart has no crosshair.
 *
 * @param T The item type the chart reports to the crosshair.
 * @param crosshair The chart's crosshair scope.
 * @param anchors The anchor point of each drawn item, in drawn order.
 * @param items The items the crosshair reports, in the same order as [anchors].
 * @param chartContext The chart's coordinate context.
 * @param color The chart's colour, used to fill the highlight dot.
 * @param textMeasurer Measures the value label.
 */
internal fun <T> DrawScope.drawSeriesCrosshair(
    crosshair: SeriesCrosshairScope<T>,
    anchors: List<Offset>,
    items: List<T>,
    chartContext: ChartContext,
    color: ChartyColor,
    textMeasurer: TextMeasurer,
) {
    if (!crosshair.isActive) {
        return
    }
    recordSeriesCrosshairBounds(bounds = crosshair.bounds, anchors = anchors, items = items)
    drawBarCrosshairOverlay(
        state = crosshair.currentState() ?: return,
        barConfig = crosshair.overlayStyle,
        chartContext = chartContext,
        textMeasurer = textMeasurer,
        chartColor = color,
        orientation = crosshair.orientation,
    )
}

/**
 * Hosts the crosshair's label composable over the chart canvas, alongside whatever tap tooltip the
 * chart already hosts. Draws nothing when the chart has no crosshair or nothing is snapped.
 *
 * @param T The item type the chart reports to the crosshair.
 * @param crosshair The chart's crosshair scope.
 */
@Composable
internal fun <T> BoxScope.SeriesCrosshairHost(crosshair: SeriesCrosshairScope<T>) {
    crosshair.crosshair?.let { activeCrosshair ->
        ChartCrosshairHost(
            crosshair = activeCrosshair,
            item = crosshair.manager?.selectedItem,
            state = crosshair.currentState(),
            modifier = Modifier.matchParentSize(),
        )
    }
}
