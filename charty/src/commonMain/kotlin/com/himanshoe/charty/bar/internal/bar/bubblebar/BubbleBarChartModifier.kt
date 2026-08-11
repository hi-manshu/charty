package com.himanshoe.charty.bar.internal.bar.bubblebar

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import com.himanshoe.charty.bar.config.BubbleBarChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.common.gesture.rectangularChartClickHandler
import com.himanshoe.charty.common.gesture.rectangularChartScrubHandler
import com.himanshoe.charty.common.tooltip.TooltipState

/**
 * Modifier and input handling for BubbleBarChart. When [enableScrub] is `true`, a
 * drag-to-track tooltip gesture is layered on top of the click handler.
 */

internal fun createBubbleChartModifier(
    onBarClick: ((BarData) -> Unit)?,
    dataList: List<BarData>,
    bubbleConfig: BubbleBarChartConfig,
    barBounds: List<Pair<Rect, BarData>>,
    onTooltipUpdate: (TooltipState?, BarData?) -> Unit,
    modifier: Modifier = Modifier,
    enableScrub: Boolean = false,
): Modifier {
    val tooltipContentBuilder = { barData: BarData, rect: Rect ->
        TooltipState(
            content = bubbleConfig.tooltipFormatter(barData),
            x = rect.left,
            y = rect.top,
            barWidth = rect.width,
            position = bubbleConfig.tooltipPosition,
        )
    }
    val clickModifier =
        modifier.rectangularChartClickHandler(
            dataList = dataList,
            bounds = barBounds,
            onItemClick = onBarClick,
            onTooltipStateChange = onTooltipUpdate,
            createTooltipContent = tooltipContentBuilder,
        )
    return if (enableScrub) {
        clickModifier.rectangularChartScrubHandler(
            dataList = dataList,
            bounds = barBounds,
            onTooltipStateChange = onTooltipUpdate,
            createTooltipContent = tooltipContentBuilder,
        )
    } else {
        clickModifier
    }
}
