package com.himanshoe.charty.line.internal.area

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.gesture.CrosshairManager
import com.himanshoe.charty.common.gesture.chartBrushSelectionHandler
import com.himanshoe.charty.common.gesture.chartCrosshairHandler
import com.himanshoe.charty.common.gesture.chartZoomAndPan
import com.himanshoe.charty.common.gesture.createPointTooltipState
import com.himanshoe.charty.common.gesture.pointChartClickHandler
import com.himanshoe.charty.common.tooltip.TooltipManager
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineData
import com.himanshoe.charty.line.internal.line.LineChartConstants

/**
 * Creates a modifier with click handling for area chart.
 */
internal fun createAreaChartModifier(
    onPointClick: ((LineData) -> Unit)?,
    dataList: List<LineData>,
    lineConfig: LineChartConfig,
    pointBounds: List<Pair<Offset, LineData>>,
    onTooltipUpdate: (TooltipState?, LineData?) -> Unit,
    modifier: Modifier = Modifier,
): Modifier =
    if (onPointClick != null) {
        modifier.pointChartClickHandler(
            dataList = dataList,
            pointBounds = pointBounds,
            tapRadius = lineConfig.pointRadius * LineChartConstants.TAP_RADIUS_MULTIPLIER,
            onPointClick = onPointClick,
            onTooltipStateChange = onTooltipUpdate,
            createTooltipContent = { lineData, position ->
                createPointTooltipState(
                    content = lineConfig.tooltipFormatter(lineData),
                    position = position,
                    pointRadius = lineConfig.pointRadius,
                    tooltipPosition = lineConfig.tooltipPosition,
                    pointRadiusMultiplier = LineChartConstants.POINT_RADIUS_MULTIPLIER,
                )
            },
        )
    } else {
        modifier
    }

/**
 * Chains the area chart's pointer handling. Tap-to-tooltip and the crosshair drag are installed
 * independently — a tap never travels past touch slop and a crosshair only engages once it does —
 * so a chart configured with both answers to both. Brush selection and zoom/pan are layered on top
 * when their state holders are configured.
 *
 * @param crosshairManager The crosshair state holder, or `null` when the crosshair is off.
 * @param dataList The points currently drawn, which the handlers hit-test against.
 * @param tooltipManager Owns the point bounds the tap handler tests and the tooltip it raises.
 * @param lineConfig Supplies the tooltip formatter and the crosshair's dismiss behaviour.
 * @param onPointClick Invoked when a point is tapped, or `null` when taps are ignored.
 * @param interactionConfig Supplies the brush-selection and viewport state holders.
 * @return The chained [Modifier] to apply to the chart.
 */
internal fun buildAreaModifier(
    crosshairManager: CrosshairManager<LineData>?,
    dataList: List<LineData>,
    tooltipManager: TooltipManager<Offset, LineData>,
    lineConfig: LineChartConfig,
    onPointClick: ((LineData) -> Unit)?,
    interactionConfig: ChartInteractionConfig,
): Modifier {
    var mod: Modifier =
        createAreaChartModifier(
            modifier = Modifier,
            onPointClick = onPointClick,
            dataList = dataList,
            lineConfig = lineConfig,
            pointBounds = tooltipManager.bounds,
            onTooltipUpdate = tooltipManager::updateTooltip,
        )
    if (crosshairManager != null) {
        mod =
            mod.chartCrosshairHandler(
                dataList = dataList,
                pointBounds = tooltipManager.bounds,
                onCrosshairUpdate = crosshairManager::update,
                labelFormatter = lineConfig.tooltipFormatter,
                dismissOnRelease = lineConfig.crosshairConfig?.dismissOnRelease ?: true,
            )
    }
    if (interactionConfig.brushSelectionState != null) {
        mod =
            mod.chartBrushSelectionHandler(
                dataList = dataList,
                brushState = interactionConfig.brushSelectionState,
                onRangeSelect = interactionConfig.onRangeSelect,
            )
    }
    if (interactionConfig.viewPortState != null) {
        mod = mod.chartZoomAndPan(interactionConfig.viewPortState)
    }
    return mod
}
