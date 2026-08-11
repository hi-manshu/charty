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
import com.himanshoe.charty.common.gesture.rectangularChartScrubHandler
import com.himanshoe.charty.common.tooltip.TooltipState

internal fun createComparisonChartModifier(
    modifier: Modifier,
    onBarClick: ((ComparisonBarSegment) -> Unit)?,
    dataList: List<BarGroup>,
    comparisonConfig: ComparisonBarChartConfig,
    barBounds: List<Pair<Rect, ComparisonBarSegment>>,
    onTooltipUpdate: (TooltipState?, ComparisonBarSegment?) -> Unit,
    enableScrub: Boolean = false,
): Modifier {
    val tooltipContentBuilder = { segment: ComparisonBarSegment, rect: Rect ->
        createRectangularTooltipState(
            content = comparisonConfig.tooltipFormatter(segment),
            rect = rect,
            position = comparisonConfig.tooltipPosition,
        )
    }
    val clickModifier =
        if (onBarClick != null) {
            modifier.pointerInput(dataList, comparisonConfig, onBarClick) {
                detectTapGestures { offset ->
                    val clickedBar = findClickedItemWithBounds(offset = offset, bounds = barBounds)

                    clickedBar?.let { (rect, segment) ->
                        onBarClick.invoke(segment)
                        onTooltipUpdate(tooltipContentBuilder(segment, rect), segment)
                    } ?: onTooltipUpdate(null, null)
                }
            }
        } else {
            modifier
        }
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
