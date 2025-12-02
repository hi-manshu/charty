package com.himanshoe.charty.bar.internal.span

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.data.SpanData
import com.himanshoe.charty.common.tooltip.TooltipState

@Composable
internal fun createSpanChartModifier(
    onSpanClick: ((SpanData) -> Unit)?,
    dataList: List<SpanData>,
    barConfig: BarChartConfig,
    spanBounds: List<Pair<Rect, SpanData>>,
    onTooltipUpdate: (TooltipState?) -> Unit,
    modifier: Modifier = Modifier,
): Modifier {
    return if (onSpanClick != null) {
        modifier.pointerInput(dataList, barConfig, onSpanClick) {
            detectTapGestures { offset ->
                handleSpanClick(offset, spanBounds, onSpanClick, barConfig, onTooltipUpdate)
            }
        }
    } else {
        modifier
    }
}

internal fun handleSpanClick(
    offset: androidx.compose.ui.geometry.Offset,
    spanBounds: List<Pair<Rect, SpanData>>,
    onSpanClick: (SpanData) -> Unit,
    barConfig: BarChartConfig,
    onTooltipUpdate: (TooltipState?) -> Unit
) {
    val clickedSpan = spanBounds.find { (rect, _) -> rect.contains(offset) }

    clickedSpan?.let { (rect, spanData) ->
        onSpanClick.invoke(spanData)
        onTooltipUpdate(
            TooltipState(
                content = "${spanData.label}: ${spanData.startValue} - ${spanData.endValue}",
                x = rect.left,
                y = rect.top,
                barWidth = rect.width,
                position = barConfig.tooltipPosition,
            )
        )
    } ?: onTooltipUpdate(null)
}

