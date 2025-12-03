package com.himanshoe.charty.bar.internal.span

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.data.SpanData
import com.himanshoe.charty.common.gesture.rectangularChartClickHandler
import com.himanshoe.charty.common.tooltip.TooltipState

internal fun createSpanChartModifier(
    onSpanClick: ((SpanData) -> Unit)?,
    dataList: List<SpanData>,
    barConfig: BarChartConfig,
    spanBounds: List<Pair<Rect, SpanData>>,
    onTooltipUpdate: (TooltipState?) -> Unit,
    modifier: Modifier = Modifier,
): Modifier {
    return modifier.rectangularChartClickHandler(
        dataList = dataList,
        bounds = spanBounds,
        onItemClick = onSpanClick,
        onTooltipStateChange = onTooltipUpdate,
        createTooltipContent = { spanData, rect ->
            TooltipState(
                content = "${spanData.label}: ${spanData.startValue} - ${spanData.endValue}",
                x = rect.left,
                y = rect.top,
                barWidth = rect.width,
                position = barConfig.tooltipPosition,
            )
        }
    )
}

