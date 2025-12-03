package com.himanshoe.charty.line.internal.line

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import com.himanshoe.charty.common.gesture.createPointTooltipState
import com.himanshoe.charty.common.gesture.findNearestPoint
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineData

/**
 * Add tap gesture detection for line chart points
 */
internal fun Modifier.lineChartClickHandler(
    dataList: List<LineData>,
    lineConfig: LineChartConfig,
    pointBounds: List<Pair<Offset, LineData>>,
    onPointClick: (LineData) -> Unit,
    onTooltipStateChange: (TooltipState?) -> Unit,
): Modifier {
    return this.pointerInput(dataList, lineConfig, onPointClick) {
        detectTapGestures { offset ->
            val tapRadius = lineConfig.pointRadius * LineChartConstants.TAP_RADIUS_MULTIPLIER
            val nearestPoint = findNearestPoint(
                offset = offset,
                pointBounds = pointBounds,
                tapRadius = tapRadius,
            )

            nearestPoint?.let { (position, lineData) ->
                onPointClick.invoke(lineData)
                onTooltipStateChange(
                    createPointTooltipState(
                        content = lineConfig.tooltipFormatter(lineData),
                        position = position,
                        pointRadius = lineConfig.pointRadius,
                        tooltipPosition = lineConfig.tooltipPosition,
                        pointRadiusMultiplier = LineChartConstants.POINT_RADIUS_MULTIPLIER,
                    ),
                )
            } ?: run {
                onTooltipStateChange(null)
            }
        }
    }
}

