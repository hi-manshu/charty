package com.himanshoe.charty.line.internal.stackedarea

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.common.gesture.rectangularChartClickHandler
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineGroup
import com.himanshoe.charty.line.data.StackedAreaPoint

/**
 * Add tap gesture detection for stacked area chart
 */
internal fun Modifier.stackedAreaChartClickHandler(
    dataList: List<LineGroup>,
    lineConfig: LineChartConfig,
    areaSegmentBounds: List<Triple<Rect, Path, StackedAreaPoint>>,
    onAreaClick: (StackedAreaPoint) -> Unit,
    onTooltipStateChange: (TooltipState?) -> Unit,
): Modifier {
    val bounds = areaSegmentBounds.fastMap { (rect, _, point) -> rect to point }

    return this.rectangularChartClickHandler(
        dataList = dataList,
        bounds = bounds,
        onItemClick = onAreaClick,
        onTooltipStateChange = onTooltipStateChange,
        createTooltipContent = { areaPoint, rect ->
            TooltipState(
                content = "${areaPoint.lineGroup.label}: ${areaPoint.value}",
                x = rect.left + rect.width / StackedAreaChartConstants.BEZIER_CONTROL_POINT_2_MULTIPLIER,
                y = rect.top,
                barWidth = rect.width,
                position = lineConfig.tooltipPosition,
            )
        }
    )
}



