package com.himanshoe.charty.line.internal.multiline

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
    onTooltipStateChange: (TooltipState?) -> Unit,
): Modifier {
    return this.pointChartClickHandler(
        dataList = dataList,
        pointBounds = pointBounds,
        tapRadius = lineConfig.pointRadius * MultilineChartConstants.TAP_RADIUS_MULTIPLIER,
        onPointClick = onPointClick,
        onTooltipStateChange = onTooltipStateChange,
        createTooltipContent = { point, position ->
            createPointTooltipState(
                content = point.lineGroup.label +
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
}

