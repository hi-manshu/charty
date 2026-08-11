package com.himanshoe.charty.bar.internal.span

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.data.SpanData
import com.himanshoe.charty.common.gesture.rectangularChartClickHandler
import com.himanshoe.charty.common.gesture.rectangularChartScrubHandler
import com.himanshoe.charty.common.tooltip.TooltipState

internal fun createSpanChartModifier(
    onSpanClick: ((SpanData) -> Unit)?,
    dataList: List<SpanData>,
    barConfig: BarChartConfig,
    spanBounds: List<Pair<Rect, SpanData>>,
    onTooltipUpdate: (TooltipState?, SpanData?) -> Unit,
    modifier: Modifier = Modifier,
    enableScrub: Boolean = false,
): Modifier {
    val tooltipContentBuilder = { spanData: SpanData, rect: Rect ->
        TooltipState(
            content = "${spanData.label}: ${spanData.startValue} - ${spanData.endValue}",
            x = rect.left,
            y = rect.top,
            barWidth = rect.width,
            position = barConfig.tooltipPosition,
        )
    }
    val clickModifier =
        modifier.rectangularChartClickHandler(
            dataList = dataList,
            bounds = spanBounds,
            onItemClick = onSpanClick,
            onTooltipStateChange = onTooltipUpdate,
            createTooltipContent = tooltipContentBuilder,
        )
    return if (enableScrub) {
        clickModifier.rectangularChartScrubHandler(
            dataList = dataList,
            bounds = spanBounds,
            onTooltipStateChange = onTooltipUpdate,
            createTooltipContent = tooltipContentBuilder,
        )
    } else {
        clickModifier
    }
}
