package com.himanshoe.charty.bar.internal.bar.groupedhorizontal

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import com.himanshoe.charty.bar.config.GroupedHorizontalBarChartConfig
import com.himanshoe.charty.bar.config.GroupedHorizontalBarEntry
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.common.gesture.createRectangularTooltipState
import com.himanshoe.charty.common.gesture.rectangularChartClickHandler
import com.himanshoe.charty.common.tooltip.TooltipState

/**
 * Attaches pointer-input handling for bar click detection and tooltip management.
 *
 * Returns [Modifier] unchanged when [onBarClick] is null so there is no gesture overhead
 * on read-only charts.
 */
internal fun createGroupedHorizontalBarChartModifier(
    dataList: List<BarGroup>,
    config: GroupedHorizontalBarChartConfig,
    onBarClick: ((GroupedHorizontalBarEntry) -> Unit)?,
    barBounds: List<Pair<Rect, GroupedHorizontalBarEntry>>,
    onTooltipStateChange: (TooltipState?) -> Unit,
): Modifier = Modifier.rectangularChartClickHandler(
    dataList = dataList,
    bounds = barBounds,
    onItemClick = onBarClick,
    onTooltipStateChange = onTooltipStateChange,
    createTooltipContent = { entry, rect ->
        createRectangularTooltipState(
            content = config.tooltipFormatter(entry),
            rect = rect,
            position = config.tooltipPosition,
        )
    },
)
