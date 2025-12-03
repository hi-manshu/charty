package com.himanshoe.charty.bar.internal.bar.stacked

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import com.himanshoe.charty.bar.config.StackedBarChartConfig
import com.himanshoe.charty.bar.config.StackedBarSegment
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.common.gesture.createRectangularTooltipState
import com.himanshoe.charty.common.gesture.rectangularChartClickHandler
import com.himanshoe.charty.common.tooltip.TooltipState

internal fun createStackedBarChartModifier(
    dataList: List<BarGroup>,
    stackedConfig: StackedBarChartConfig,
    onSegmentClick: ((StackedBarSegment) -> Unit)?,
    segmentBounds: List<Pair<Rect, StackedBarSegment>>,
    onTooltipStateChange: (TooltipState?) -> Unit,
): Modifier {
    return Modifier.rectangularChartClickHandler(
        dataList = dataList,
        bounds = segmentBounds,
        onItemClick = onSegmentClick,
        onTooltipStateChange = onTooltipStateChange,
        createTooltipContent = { segment, rect ->
            createRectangularTooltipState(
                content = stackedConfig.tooltipFormatter(segment),
                rect = rect,
                position = stackedConfig.tooltipPosition,
            )
        },
    )
}

