package com.himanshoe.charty.bar.internal.bar.bubblebar

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import com.himanshoe.charty.bar.config.BubbleBarChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.common.tooltip.TooltipState

/**
 * Modifier and input handling for BubbleBarChart
 */

@Composable
internal fun createBubbleChartModifier(
    onBarClick: ((BarData) -> Unit)?,
    dataList: List<BarData>,
    bubbleConfig: BubbleBarChartConfig,
    barBounds: List<Pair<Rect, BarData>>,
    onTooltipUpdate: (TooltipState?) -> Unit,
    modifier: Modifier = Modifier,
): Modifier {
    return if (onBarClick != null) {
        modifier.pointerInput(dataList, bubbleConfig, onBarClick) {
            detectTapGestures { offset ->
                handleBubbleBarClick(offset, barBounds, onBarClick, bubbleConfig, onTooltipUpdate)
            }
        }
    } else {
        modifier
    }
}

internal fun handleBubbleBarClick(
    offset: Offset,
    barBounds: List<Pair<Rect, BarData>>,
    onBarClick: (BarData) -> Unit,
    bubbleConfig: BubbleBarChartConfig,
    onTooltipUpdate: (TooltipState?) -> Unit,
) {
    val clickedBar = barBounds.find { (rect, _) -> rect.contains(offset) }

    clickedBar?.let { (rect, barData) ->
        onBarClick.invoke(barData)
        onTooltipUpdate(
            TooltipState(
                content = bubbleConfig.tooltipFormatter(barData),
                x = rect.left,
                y = rect.top,
                barWidth = rect.width,
                position = bubbleConfig.tooltipPosition,
            ),
        )
    } ?: onTooltipUpdate(null)
}

