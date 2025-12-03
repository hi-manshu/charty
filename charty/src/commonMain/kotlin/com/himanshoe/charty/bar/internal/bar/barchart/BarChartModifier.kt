package com.himanshoe.charty.bar.internal.bar.barchart

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.common.gesture.createRectangularTooltipState
import com.himanshoe.charty.common.gesture.rectangularChartClickHandler
import com.himanshoe.charty.common.tooltip.TooltipState

/**
 * Helper function to create the chart modifier with tap gestures for bar clicks
 */
internal fun createBarChartModifier(
    onBarClick: ((BarData) -> Unit)?,
    dataList: List<BarData>,
    barConfig: BarChartConfig,
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
            createRectangularTooltipState(
                content = barConfig.tooltipFormatter(barData),
                rect = rect,
                position = barConfig.tooltipPosition,
            )
        }
    )
}

