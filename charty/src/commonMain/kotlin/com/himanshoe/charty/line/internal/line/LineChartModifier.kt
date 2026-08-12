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
 * Applies the interaction [Modifier]s the line chart is configured for.
 *
 * Each handler is installed independently, because they answer to different gestures: the tap
 * handler fires on a press that never travels past touch slop, while the crosshair and brush
 * handlers only engage once it does. A chart configured with both a crosshair and [onPointClick]
 * therefore shows a tooltip on tap *and* scrubs a crosshair on drag.
 *
 * @param dataList The chart's data list, used as a recomposition key for all gesture handlers.
 * @param lineConfig The line chart configuration, providing tap radius and formatter settings.
 * @param pointBounds The list of canvas pixel positions paired with their [LineData]; populated
 *   each draw frame by the chart's canvas pass.
 * @param onPointClick Optional callback invoked when the user taps a data point.
 * @param onTooltipStateChange Callback to push a new [com.himanshoe.charty.common.tooltip.TooltipState]
 *   (or `null` to dismiss) when the tap handler is active.
 * @param crosshairManager When non-null, adds a draggable crosshair on top of tap interaction.
 * @param brushSelectionState When non-null, enables a horizontal drag-to-select gesture.
 * @param onRangeSelect Called with `(startIndex, endIndex)` when a brush drag completes.
 */
internal fun Modifier.lineChartInteractionHandler(
    dataList: List<LineData>,
    lineConfig: LineChartConfig,
    pointBounds: List<Pair<Offset, LineData>>,
    onPointClick: ((LineData) -> Unit)?,
    onTooltipStateChange: (TooltipState?, LineData?) -> Unit,
    crosshairManager: CrosshairManager<LineData>?,
    brushSelectionState: BrushSelectionState?,
    onRangeSelect: ((startIndex: Int, endIndex: Int) -> Unit)?,
): Modifier {
    var result = this

    if (onPointClick != null) {
        result =
            result.pointChartClickHandler(
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

    if (crosshairManager != null) {
        result =
            result.chartCrosshairHandler(
                dataList = dataList,
                pointBounds = pointBounds,
                onCrosshairUpdate = crosshairManager::update,
                labelFormatter = lineConfig.tooltipFormatter,
                dismissOnRelease = lineConfig.crosshairConfig?.dismissOnRelease ?: true,
            )
    }

    if (brushSelectionState != null) {
        result =
            result.chartBrushSelectionHandler(
                dataList = dataList,
                brushState = brushSelectionState,
                onRangeSelect = onRangeSelect,
            )
    }

    return result
}
