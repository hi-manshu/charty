package com.himanshoe.charty.bar.internal.bar

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.StreamingLayout
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.gesture.AnimatedCrosshair
import com.himanshoe.charty.common.gesture.CROSSHAIR_DASH_EFFECT
import com.himanshoe.charty.common.gesture.ChartCrosshair
import com.himanshoe.charty.common.gesture.ChartCrosshairConfig
import com.himanshoe.charty.common.gesture.ChartCrosshairHost
import com.himanshoe.charty.common.gesture.CrosshairManager
import com.himanshoe.charty.common.gesture.CrosshairState
import com.himanshoe.charty.common.gesture.chartCrosshairHandler
import com.himanshoe.charty.common.gesture.drawCrosshairDot
import com.himanshoe.charty.common.gesture.drawCrosshairValueLabel
import com.himanshoe.charty.common.gesture.rememberChartCrosshair
import com.himanshoe.charty.common.streamingPan
import com.himanshoe.charty.common.theme.orThemeCrosshair
import com.himanshoe.charty.common.tooltip.ChartTooltip
import com.himanshoe.charty.common.tooltip.ChartTooltipHost
import com.himanshoe.charty.common.tooltip.TooltipManager

/**
 * Everything a bar-family chart needs to run a crosshair: the gesture manager, the smoothed position
 * read during the draw pass, the anchor points the gesture snaps to, and the configuration the
 * crosshair was enabled with. Obtain one with [rememberBarCrosshair]; when the chart has no crosshair
 * the scope is still created but [isActive] is `false` and every helper is a no-op.
 *
 * @property manager Owns the live crosshair state, or `null` when the crosshair is off.
 * @property bounds One anchor point per drawn bar, refreshed on every draw pass.
 * @property config The chart configuration with the crosshair styling merged in.
 * @property crosshair The crosshair whose label composable is hosted over the chart, or `null`.
 * @property orientation The host chart's orientation, deciding the snapping and guide-line axis.
 */
@Immutable
internal class BarCrosshairScope(
    val manager: CrosshairManager<BarData>?,
    private val animated: AnimatedCrosshair?,
    val bounds: MutableList<Pair<Offset, BarData>>,
    val config: BarChartConfig,
    val crosshair: ChartCrosshair<BarData>?,
    val orientation: ChartOrientation,
) {
    /** Whether the chart has a crosshair configured at all. */
    val isActive: Boolean get() = manager != null

    /** The smoothed crosshair position to draw this frame, or `null` when nothing is snapped. */
    fun currentState(): CrosshairState? = animated?.resolve()
}

/**
 * Builds the [BarCrosshairScope] for a bar-family chart, accepting the crosshair either as an
 * explicit [crosshair] or as a [BarChartConfig.crosshairConfig] already set on [barConfig].
 *
 * @param barConfig The chart's configuration.
 * @param crosshair The crosshair passed to the chart, or `null` when none was.
 * @param interactionConfig Supplies the viewport or streaming geometry used for cross-chart syncing.
 * @param orientation The chart's orientation.
 * @return The scope the chart threads through its modifier, draw pass, and overlays.
 */
@Composable
internal fun rememberBarCrosshair(
    barConfig: BarChartConfig,
    crosshair: ChartCrosshair<BarData>?,
    interactionConfig: ChartInteractionConfig,
    orientation: ChartOrientation = ChartOrientation.VERTICAL,
): BarCrosshairScope {
    val themedCrosshairConfig = crosshair?.config.orThemeCrosshair()
    val effectiveConfig = crosshair?.let { barConfig.copy(crosshairConfig = themedCrosshairConfig) } ?: barConfig
    val activeCrosshair = crosshair ?: barConfig.crosshairConfig?.let { ChartCrosshair<BarData>(config = it) }
    val bounds = remember { mutableListOf<Pair<Offset, BarData>>() }
    val (manager, animated) =
        rememberChartCrosshair<BarData>(
            enabled = effectiveConfig.crosshairConfig != null,
            interactionConfig = interactionConfig,
        )
    return BarCrosshairScope(
        manager = manager,
        animated = animated,
        bounds = bounds,
        config = effectiveConfig,
        crosshair = activeCrosshair,
        orientation = orientation,
    )
}

/**
 * Chains the crosshair drag gesture onto this modifier when [crosshair] is active, leaving any
 * co-installed click or scrub handler intact so tapping a bar still raises its tooltip. Returns the
 * receiver unchanged when the chart has no crosshair.
 *
 * @param crosshair The chart's crosshair scope.
 * @param dataList The bars currently drawn, used as the gesture's recomposition key.
 * @return This modifier with the crosshair handler chained on, or this modifier itself.
 */
internal fun Modifier.barCrosshairHandler(
    crosshair: BarCrosshairScope,
    dataList: List<BarData>,
): Modifier {
    val manager = crosshair.manager ?: return this
    return this.chartCrosshairHandler(
        dataList = dataList,
        pointBounds = crosshair.bounds,
        onCrosshairUpdate = { state, item -> manager.update(newState = state, item = item) },
        labelFormatter = { barData -> crosshair.config.tooltipFormatter(barData) },
        dismissOnRelease = crosshair.config.crosshairConfig?.dismissOnRelease ?: true,
        orientation = crosshair.orientation,
    )
}

/**
 * The streaming scrollback pan for a bar-family chart, suppressed while a crosshair is configured
 * because the two gestures would compete for the same drag.
 *
 * @param streaming The active streaming layout, or `null` when the chart is not streaming.
 * @param crosshair The chart's crosshair scope.
 * @return The pan modifier, or an empty [Modifier].
 */
internal fun ChartInteractionConfig.barStreamingPan(
    streaming: StreamingLayout?,
    crosshair: BarCrosshairScope,
): Modifier =
    if (crosshair.isActive) {
        Modifier
    } else {
        streamingPan(streaming = streaming, orientation = crosshair.orientation)
    }

/**
 * Refreshes the crosshair's anchor points from the bars drawn this frame and draws the guide line,
 * highlight dot, and value label for the snapped bar. Does nothing when the chart has no crosshair.
 *
 * Anchors match the value-label markers: the top centre of each bar on a vertical chart, the centre
 * of each bar's value end on a horizontal one.
 *
 * @param crosshair The chart's crosshair scope.
 * @param dataList The bars the crosshair reports, in drawn order.
 * @param displayList The same bars carrying the values actually drawn this frame.
 * @param chartContext The chart's coordinate context.
 * @param color The chart's colour, used to fill the highlight dot.
 * @param textMeasurer Measures the value label.
 */
internal fun DrawScope.drawBarCrosshair(
    crosshair: BarCrosshairScope,
    dataList: List<BarData>,
    displayList: List<BarData>,
    chartContext: ChartContext,
    color: ChartyColor,
    textMeasurer: TextMeasurer,
) {
    if (!crosshair.isActive) {
        return
    }
    recordBarCrosshairBounds(
        bounds = crosshair.bounds,
        dataList = dataList,
        values = displayList.fastMap { it.value },
        chartContext = chartContext,
        orientation = crosshair.orientation,
    )
    drawBarCrosshairOverlay(
        state = crosshair.currentState() ?: return,
        barConfig = crosshair.config,
        chartContext = chartContext,
        textMeasurer = textMeasurer,
        chartColor = color,
        orientation = crosshair.orientation,
    )
}

/**
 * Hosts the tap tooltip and, when one is configured, the crosshair label over the chart canvas.
 *
 * @param tooltip How the tap tooltip is rendered.
 * @param tooltipManager Holds the tapped bar and its anchor.
 * @param crosshair The chart's crosshair scope.
 */
@Composable
internal fun BoxScope.BarChartOverlays(
    tooltip: ChartTooltip<BarData>,
    tooltipManager: TooltipManager<Rect, BarData>,
    crosshair: BarCrosshairScope,
) {
    ChartTooltipHost(
        tooltip = tooltip,
        item = tooltipManager.selectedItem,
        anchor = tooltipManager.tooltipState,
        modifier = Modifier.matchParentSize(),
    )
    crosshair.crosshair?.let { activeCrosshair ->
        ChartCrosshairHost(
            crosshair = activeCrosshair,
            item = crosshair.manager?.selectedItem,
            state = crosshair.currentState(),
            modifier = Modifier.matchParentSize(),
        )
    }
}

/**
 * Draws the bar-family crosshair: a dashed guide line running across the plot at the snapped bar, a
 * highlight dot on that bar's anchor, and an optional value label. The guide runs vertically for a
 * [ChartOrientation.VERTICAL] chart and horizontally for a [ChartOrientation.HORIZONTAL] one, with
 * the cross line, when enabled, drawn along the other axis.
 *
 * @param state The crosshair position and label to draw.
 * @param barConfig Supplies the crosshair styling; nothing is drawn when it carries no crosshair.
 * @param chartContext The chart's coordinate context.
 * @param textMeasurer Measures the value label.
 * @param chartColor The chart's colour, used to fill the highlight dot.
 * @param orientation The chart's orientation, deciding which way the guide line runs.
 */
internal fun DrawScope.drawBarCrosshairOverlay(
    state: CrosshairState,
    barConfig: BarChartConfig,
    chartContext: ChartContext,
    textMeasurer: TextMeasurer,
    chartColor: ChartyColor,
    orientation: ChartOrientation,
) {
    val config = barConfig.crosshairConfig ?: return
    val guideIsVertical = orientation == ChartOrientation.VERTICAL

    drawBarCrosshairGuide(
        state = state,
        chartContext = chartContext,
        isVertical = guideIsVertical,
        config = config,
    )
    if (config.showHorizontalLine) {
        drawBarCrosshairGuide(
            state = state,
            chartContext = chartContext,
            isVertical = !guideIsVertical,
            config = config,
        )
    }

    drawCrosshairDot(
        center = Offset(x = state.x, y = state.y),
        config = config,
        fill = Brush.linearGradient(chartColor.value),
    )

    if (config.showLabel) {
        drawCrosshairValueLabel(
            state = state,
            config = config,
            chartContext = chartContext,
            textMeasurer = textMeasurer,
        )
    }
}

private fun DrawScope.drawBarCrosshairGuide(
    state: CrosshairState,
    chartContext: ChartContext,
    isVertical: Boolean,
    config: ChartCrosshairConfig,
) {
    val colors =
        if (isVertical) {
            config.verticalLineColor.value
        } else {
            config.horizontalLineColor.value
        }
    val start =
        if (isVertical) {
            Offset(x = state.x, y = chartContext.top)
        } else {
            Offset(x = chartContext.left, y = state.y)
        }
    val end =
        if (isVertical) {
            Offset(x = state.x, y = chartContext.bottom)
        } else {
            Offset(x = chartContext.right, y = state.y)
        }
    val brush =
        if (isVertical) {
            Brush.verticalGradient(colors)
        } else {
            Brush.horizontalGradient(colors)
        }
    drawLine(
        brush = brush,
        start = start,
        end = end,
        strokeWidth = config.lineWidth,
        pathEffect = CROSSHAIR_DASH_EFFECT,
    )
}
