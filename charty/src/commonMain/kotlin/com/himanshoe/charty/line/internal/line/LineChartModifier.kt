package com.himanshoe.charty.line.internal.line

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.himanshoe.charty.common.brush.BrushSelectionState
import com.himanshoe.charty.common.gesture.CrosshairManager
import com.himanshoe.charty.common.gesture.chartBrushSelectionHandler
import com.himanshoe.charty.common.gesture.chartCrosshairHandler
import com.himanshoe.charty.common.gesture.createPointTooltipState
import com.himanshoe.charty.common.gesture.pointChartClickHandler
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineData

/**
 * Applies the appropriate interaction [Modifier]s to the line chart composable.
 *
 * **Priority rules:**
 * 1. When [crosshairManager] is non-null the drag crosshair handler is installed and the
 *    tap-to-tooltip path is skipped entirely.
 * 2. Otherwise, when [onPointClick] is non-null the tap handler is installed.
 * 3. When [brushSelectionState] is non-null the brush handler is applied as an independent
 *    gesture layer on top of whichever single-touch handler above is active.
 *
 * @param dataList The chart's data list, used as a recomposition key for all gesture handlers.
 * @param lineConfig The line chart configuration, providing tap radius and formatter settings.
 * @param pointBounds The list of canvas pixel positions paired with their [LineData]; populated
 *   each draw frame by the chart's canvas pass.
 * @param onPointClick Optional callback invoked when the user taps a data point. Ignored when
 *   [crosshairManager] is non-null.
 * @param onTooltipStateChange Callback to push a new [com.himanshoe.charty.common.tooltip.TooltipState]
 *   (or `null` to dismiss) when the tap handler is active.
 * @param crosshairManager When non-null, replaces tap interaction with a draggable crosshair.
 * @param brushSelectionState When non-null, enables a horizontal drag-to-select gesture.
 * @param onRangeSelect Called with `(startIndex, endIndex)` when a brush drag completes.
 */
internal fun Modifier.lineChartInteractionHandler(
    dataList: List<LineData>,
    lineConfig: LineChartConfig,
    pointBounds: List<Pair<Offset, LineData>>,
    onPointClick: ((LineData) -> Unit)?,
    onTooltipStateChange: (TooltipState?) -> Unit,
    crosshairManager: CrosshairManager?,
    brushSelectionState: BrushSelectionState?,
    onRangeSelect: ((startIndex: Int, endIndex: Int) -> Unit)?,
): Modifier {
    var result = this

    when {
        crosshairManager != null -> {
            result = result.chartCrosshairHandler(
                dataList = dataList,
                pointBounds = pointBounds,
                onCrosshairUpdate = crosshairManager::update,
                labelFormatter = lineConfig.tooltipFormatter,
                dismissOnRelease = lineConfig.crosshairConfig?.dismissOnRelease ?: true,
            )
        }

        onPointClick != null -> {
            result = result.pointChartClickHandler(
                dataList = dataList,
                pointBounds = pointBounds,
                tapRadius = lineConfig.pointRadius * LineChartConstants.TAP_RADIUS_MULTIPLIER,
                onPointClick = onPointClick,
                onTooltipStateChange = onTooltipStateChange,
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
        }
    }

    if (brushSelectionState != null) {
        result = result.chartBrushSelectionHandler(
            dataList = dataList,
            brushState = brushSelectionState,
            onRangeSelect = onRangeSelect,
        )
    }

    return result
}
