package com.himanshoe.charty.bar.internal.bar.mosiac

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import com.himanshoe.charty.bar.config.MosiacBarChartConfig
import com.himanshoe.charty.bar.config.MosiacBarSegment
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.common.gesture.rectangularChartClickHandler
import com.himanshoe.charty.common.tooltip.TooltipState

/**
 * Creates a modifier with click handling for mosiac chart segments.
 */
internal fun createMosiacChartModifier(
    onSegmentClick: ((MosiacBarSegment) -> Unit)?,
    groups: List<BarGroup>,
    config: MosiacBarChartConfig,
    segmentBounds: List<Pair<Rect, MosiacBarSegment>>,
    onTooltipUpdate: (TooltipState?) -> Unit,
    modifier: Modifier = Modifier,
): Modifier {
    return modifier.rectangularChartClickHandler(
        dataList = groups,
        bounds = segmentBounds,
        onItemClick = onSegmentClick,
        onTooltipStateChange = onTooltipUpdate,
        createTooltipContent = { segment, rect ->
            TooltipState(
                content = config.tooltipFormatter(segment),
                x = rect.left,
                y = rect.top,
                barWidth = rect.width,
                position = config.tooltipPosition,
            )
        },
    )
}

