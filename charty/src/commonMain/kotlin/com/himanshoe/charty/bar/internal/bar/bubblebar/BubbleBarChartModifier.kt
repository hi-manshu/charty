package com.himanshoe.charty.bar.internal.bar.bubblebar

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import com.himanshoe.charty.bar.config.BubbleBarChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.common.gesture.rectangularChartClickHandler
import com.himanshoe.charty.common.tooltip.TooltipState

/**
 * Modifier and input handling for BubbleBarChart
 */

internal fun createBubbleChartModifier(
    onBarClick: ((BarData) -> Unit)?,
    dataList: List<BarData>,
    bubbleConfig: BubbleBarChartConfig,
    barBounds: List<Pair<Rect, BarData>>,
    onTooltipUpdate: (TooltipState?) -> Unit,
    modifier: Modifier = Modifier,
): Modifier {
    return modifier.rectangularChartClickHandler(
        dataList = dataList,
        bounds = barBounds,
        onItemClick = onBarClick,
        onTooltipStateChange = onTooltipUpdate,
        createTooltipContent = { barData, rect ->
            TooltipState(
                content = bubbleConfig.tooltipFormatter(barData),
                x = rect.left,
                y = rect.top,
                barWidth = rect.width,
                position = bubbleConfig.tooltipPosition,
            )
        },
    )
}

