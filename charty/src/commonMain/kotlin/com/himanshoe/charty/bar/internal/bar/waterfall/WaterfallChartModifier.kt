package com.himanshoe.charty.bar.internal.bar.waterfall

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import com.himanshoe.charty.bar.config.WaterfallChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.common.gesture.createRectangularTooltipState
import com.himanshoe.charty.common.gesture.rectangularChartClickHandler
import com.himanshoe.charty.common.tooltip.TooltipState

/**
 * Create click modifier for waterfall chart
 */
internal fun createWaterfallClickModifier(
    items: List<BarData>,
    config: WaterfallChartConfig,
    barBounds: List<Pair<Rect, BarData>>,
    onBarClick: ((BarData) -> Unit)?,
    onTooltipUpdate: (TooltipState?) -> Unit,
): Modifier {
    return Modifier.rectangularChartClickHandler(
        dataList = items,
        bounds = barBounds,
        onItemClick = onBarClick,
        onTooltipStateChange = onTooltipUpdate,
        createTooltipContent = { barData, rect ->
            createRectangularTooltipState(
                content = config.tooltipFormatter(barData),
                rect = rect,
                position = config.tooltipPosition,
            )
        }
    )
}

