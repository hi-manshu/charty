package com.himanshoe.charty.bar.internal.bar.barchart

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.common.gesture.createRectangularTooltipState
import com.himanshoe.charty.common.gesture.rectangularChartClickHandler
import com.himanshoe.charty.common.gesture.rectangularChartScrubHandler
import com.himanshoe.charty.common.tooltip.TooltipState

/**
 * Helper function to create the chart modifier with tap gestures for bar clicks, and—when
 * [enableScrub] is `true`—a drag-to-track tooltip gesture layered on top.
 */
internal fun createBarChartModifier(
    onBarClick: ((BarData) -> Unit)?,
    dataList: List<BarData>,
    barConfig: BarChartConfig,
    barBounds: List<Pair<Rect, BarData>>,
    onTooltipUpdate: (TooltipState?, BarData?) -> Unit,
    modifier: Modifier = Modifier,
    enableScrub: Boolean = false,
): Modifier {
    val tooltipContentBuilder = { barData: BarData, rect: Rect ->
        createRectangularTooltipState(
            content = barConfig.tooltipFormatter(barData),
            rect = rect,
            position = barConfig.tooltipPosition,
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
