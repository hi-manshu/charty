package com.himanshoe.charty.bar.internal.bar.normalizedhorizontal

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import com.himanshoe.charty.bar.config.NormalizedHorizontalBarChartConfig
import com.himanshoe.charty.bar.config.NormalizedHorizontalBarSegment
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.common.gesture.createRectangularTooltipState
import com.himanshoe.charty.common.gesture.rectangularChartClickHandler
import com.himanshoe.charty.common.tooltip.TooltipState

/**
 * Attaches pointer-input handling for segment click detection and tooltip management.
 *
 * Returns [Modifier] unchanged when [onSegmentClick] is null so there is no
 * gesture overhead on read-only charts.
 */
internal fun createNormalizedHorizontalBarChartModifier(
    dataList: List<BarGroup>,
    config: NormalizedHorizontalBarChartConfig,
    onSegmentClick: ((NormalizedHorizontalBarSegment) -> Unit)?,
    segmentBounds: List<Pair<Rect, NormalizedHorizontalBarSegment>>,
    onTooltipStateChange: (TooltipState?) -> Unit,
): Modifier = Modifier.rectangularChartClickHandler(
    dataList = dataList,
    bounds = segmentBounds,
    onItemClick = onSegmentClick,
    onTooltipStateChange = onTooltipStateChange,
    createTooltipContent = { segment, rect ->
        createRectangularTooltipState(
            content = config.tooltipFormatter(segment),
            rect = rect,
            position = config.tooltipPosition,
        )
    },
)
