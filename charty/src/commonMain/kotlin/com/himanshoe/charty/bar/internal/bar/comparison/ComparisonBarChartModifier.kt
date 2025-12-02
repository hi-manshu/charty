package com.himanshoe.charty.bar.internal.bar.comparison

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import com.himanshoe.charty.bar.config.ComparisonBarChartConfig
import com.himanshoe.charty.bar.config.ComparisonBarSegment
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.common.tooltip.TooltipState

internal fun createComparisonChartModifier(
    modifier: Modifier,
    onBarClick: ((ComparisonBarSegment) -> Unit)?,
    dataList: List<BarGroup>,
    comparisonConfig: ComparisonBarChartConfig,
    barBounds: List<Pair<Rect, ComparisonBarSegment>>,
    onTooltipUpdate: (TooltipState?) -> Unit,
): Modifier {
    return if (onBarClick != null) {
        modifier.pointerInput(dataList, comparisonConfig, onBarClick) {
            detectTapGestures { offset ->
                handleComparisonBarClick(offset, barBounds, onBarClick, comparisonConfig, onTooltipUpdate)
            }
        }
    } else {
        modifier
    }
}

private fun handleComparisonBarClick(
    offset: Offset,
    barBounds: List<Pair<Rect, ComparisonBarSegment>>,
    onBarClick: (ComparisonBarSegment) -> Unit,
    comparisonConfig: ComparisonBarChartConfig,
    onTooltipUpdate: (TooltipState?) -> Unit,
) {
    val clickedBar = barBounds.find { (rect, _) -> rect.contains(offset) }

    clickedBar?.let { (rect, segment) ->
        onBarClick.invoke(segment)
        onTooltipUpdate(
            TooltipState(
                content = comparisonConfig.tooltipFormatter(segment),
                x = rect.left,
                y = rect.top,
                barWidth = rect.width,
                position = comparisonConfig.tooltipPosition,
            ),
        )
    } ?: onTooltipUpdate(null)
}

