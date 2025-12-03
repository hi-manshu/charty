package com.himanshoe.charty.line.internal.line

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.himanshoe.charty.common.gesture.createPointTooltipState
import com.himanshoe.charty.common.gesture.pointChartClickHandler
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
    return this.pointChartClickHandler(
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
        }
    )
}

