package com.himanshoe.charty.bar.internal.bar.comparison

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import com.himanshoe.charty.bar.config.ComparisonBarChartConfig
import com.himanshoe.charty.bar.config.ComparisonBarSegment
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.common.gesture.createRectangularTooltipState
import com.himanshoe.charty.common.gesture.findClickedItemWithBounds
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
                val clickedBar = findClickedItemWithBounds(offset, barBounds)

                clickedBar?.let { (rect, segment) ->
                    onBarClick.invoke(segment)
                    onTooltipUpdate(
                        createRectangularTooltipState(
                            content = comparisonConfig.tooltipFormatter(segment),
                            rect = rect,
                            position = comparisonConfig.tooltipPosition,
                        ),
                    )
                } ?: onTooltipUpdate(null)
            }
        }
    } else {
        modifier
    }
}

