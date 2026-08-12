package com.himanshoe.charty.line.internal.multiline

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.gesture.CrosshairManager
import com.himanshoe.charty.common.gesture.chartCrosshairHandler
import com.himanshoe.charty.common.gesture.createPointTooltipState
import com.himanshoe.charty.common.gesture.pointChartClickHandler
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineGroup
import com.himanshoe.charty.line.data.MultilinePoint

/**
 * Add tap gesture detection for multiline chart points
 */
internal fun Modifier.multilineChartClickHandler(
    dataList: List<LineGroup>,
    lineConfig: LineChartConfig,
    pointBounds: List<Pair<Offset, MultilinePoint>>,
    onPointClick: (MultilinePoint) -> Unit,
    onTooltipStateChange: (TooltipState?, MultilinePoint?) -> Unit,
): Modifier =
    this.pointChartClickHandler(
        dataList = dataList,
        pointBounds = pointBounds,
        tapRadius = lineConfig.pointRadius * MultilineChartConstants.TAP_RADIUS_MULTIPLIER,
        onPointClick = onPointClick,
        onTooltipStateChange = onTooltipStateChange,
        createTooltipContent = { point, position ->
            createPointTooltipState(
                content =
                    point.lineGroup.label +
                        " Line ${
                            point.seriesIndex +
                                MultilineChartConstants.SERIES_INDEX_OFFSET
                        }: ${point.value}",
                position = position,
                pointRadius = lineConfig.pointRadius,
                tooltipPosition = lineConfig.tooltipPosition,
                pointRadiusMultiplier = MultilineChartConstants.POINT_RADIUS_MULTIPLIER,
            )
        },
    )

/**
 * Chains the multiline chart's pointer handling: the crosshair drag wins when a crosshair is
 * enabled, otherwise tap-to-tooltip is installed when a click listener is set, and brush selection
 * plus zoom/pan are layered on top by [buildInteractionModifier].
 *
 * @param base The modifier the handlers are chained onto.
 * @param crosshairManager The crosshair state holder, or `null` when the crosshair is off.
 * @param dataList The groups currently drawn, which the handlers hit-test against.
 * @param lineConfig Supplies the crosshair's dismiss behaviour and the tap radius.
 * @param pointBounds The per-point bounds the tap handler tests.
 * @param crosshairBounds The first-series bounds the crosshair snaps to.
 * @param onPointClick Invoked when a point is tapped, or `null` when taps are ignored.
 * @param onTooltipStateChange Receives the tooltip raised by a tap, and the point it belongs to.
 * @param interactionConfig Supplies the brush-selection and viewport state holders.
 * @return The chained [Modifier] to apply to the chart.
 */
internal fun buildMultilineModifier(
    base: Modifier,
    crosshairManager: CrosshairManager<MultilinePoint>?,
    dataList: List<LineGroup>,
    lineConfig: LineChartConfig,
    pointBounds: MutableList<Pair<Offset, MultilinePoint>>,
    crosshairBounds: MutableList<Pair<Offset, MultilinePoint>>,
    onPointClick: ((MultilinePoint) -> Unit)?,
    onTooltipStateChange: (TooltipState?, MultilinePoint?) -> Unit,
    interactionConfig: ChartInteractionConfig,
): Modifier {
    val mod: Modifier =
        when {
            crosshairManager != null ->
                Modifier.chartCrosshairHandler(
                    dataList = dataList,
                    pointBounds = crosshairBounds,
                    onCrosshairUpdate = crosshairManager::update,
                    labelFormatter = { point ->
                        point.lineGroup.values
                            .mapIndexed { i, v -> "L${i + 1}: $v" }
                            .joinToString("  ")
                    },
                    dismissOnRelease = lineConfig.crosshairConfig?.dismissOnRelease ?: true,
                )
            onPointClick != null ->
                Modifier.multilineChartClickHandler(
                    dataList = dataList,
                    lineConfig = lineConfig,
                    pointBounds = pointBounds,
                    onPointClick = onPointClick,
                    onTooltipStateChange = onTooltipStateChange,
                )
            else -> Modifier
        }
    return buildInteractionModifier(base = base.then(mod), interactionConfig = interactionConfig, dataList = dataList)
}
