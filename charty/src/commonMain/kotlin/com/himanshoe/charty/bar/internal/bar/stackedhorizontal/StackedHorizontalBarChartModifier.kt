package com.himanshoe.charty.bar.internal.bar.stackedhorizontal

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import com.himanshoe.charty.bar.config.StackedHorizontalBarChartConfig
import com.himanshoe.charty.bar.config.StackedHorizontalBarSegment
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.common.gesture.createRectangularTooltipState
import com.himanshoe.charty.common.gesture.rectangularChartClickHandler
import com.himanshoe.charty.common.gesture.rectangularChartScrubHandler
import com.himanshoe.charty.common.tooltip.TooltipState

/**
 * Attaches pointer-input handling for segment click detection and tooltip state management.
 *
 * Returns [Modifier] unchanged when [onSegmentClick] is null, so there is no gesture
 * overhead for read-only charts. When [enableScrub] is `true`, a drag-to-track tooltip
 * gesture is layered on top.
 */
internal fun createStackedHorizontalBarChartModifier(
    dataList: List<BarGroup>,
    config: StackedHorizontalBarChartConfig,
    onSegmentClick: ((StackedHorizontalBarSegment) -> Unit)?,
    segmentBounds: List<Pair<Rect, StackedHorizontalBarSegment>>,
    onTooltipStateChange: (TooltipState?, StackedHorizontalBarSegment?) -> Unit,
    enableScrub: Boolean = false,
): Modifier {
    val tooltipContentBuilder = { segment: StackedHorizontalBarSegment, rect: Rect ->
        createRectangularTooltipState(
            content = config.tooltipFormatter(segment),
            rect = rect,
            position = config.tooltipPosition,
        )
    }
    val clickModifier =
        Modifier.rectangularChartClickHandler(
            dataList = dataList,
            bounds = segmentBounds,
            onItemClick = onSegmentClick,
            onTooltipStateChange = onTooltipStateChange,
            createTooltipContent = tooltipContentBuilder,
        )
    return if (enableScrub) {
        clickModifier.rectangularChartScrubHandler(
            dataList = dataList,
            bounds = segmentBounds,
            onTooltipStateChange = onTooltipStateChange,
            createTooltipContent = tooltipContentBuilder,
        )
    } else {
        clickModifier
    }
}
