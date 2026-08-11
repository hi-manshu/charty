package com.himanshoe.charty.bar.internal.bar.mosaic

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import com.himanshoe.charty.bar.config.MosaicBarChartConfig
import com.himanshoe.charty.bar.config.MosaicBarSegment
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.common.gesture.rectangularChartClickHandler
import com.himanshoe.charty.common.gesture.rectangularChartScrubHandler
import com.himanshoe.charty.common.tooltip.TooltipState

/**
 * Creates a modifier with click handling for mosaic chart segments, and—when [enableScrub]
 * is `true`—a drag-to-track tooltip gesture layered on top.
 */
internal fun createMosaicChartModifier(
    onSegmentClick: ((MosaicBarSegment) -> Unit)?,
    groups: List<BarGroup>,
    config: MosaicBarChartConfig,
    segmentBounds: List<Pair<Rect, MosaicBarSegment>>,
    onTooltipUpdate: (TooltipState?, MosaicBarSegment?) -> Unit,
    modifier: Modifier = Modifier,
    enableScrub: Boolean = false,
): Modifier {
    val tooltipContentBuilder = { segment: MosaicBarSegment, rect: Rect ->
        TooltipState(
            content = config.tooltipFormatter(segment),
            x = rect.left,
            y = rect.top,
            barWidth = rect.width,
            position = config.tooltipPosition,
        )
    }
    val clickModifier =
        modifier.rectangularChartClickHandler(
            dataList = groups,
            bounds = segmentBounds,
            onItemClick = onSegmentClick,
            onTooltipStateChange = onTooltipUpdate,
            createTooltipContent = tooltipContentBuilder,
        )
    return if (enableScrub) {
        clickModifier.rectangularChartScrubHandler(
            dataList = groups,
            bounds = segmentBounds,
            onTooltipStateChange = onTooltipUpdate,
            createTooltipContent = tooltipContentBuilder,
        )
    } else {
        clickModifier
    }
}
