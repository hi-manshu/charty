package com.himanshoe.charty.bar.config

import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.common.config.CornerRadius
import com.himanshoe.charty.common.config.ReferenceLineConfig
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.common.tooltip.TooltipPosition

/**
 * Represents a single bar in a comparison chart that was clicked
 *
 * @param barGroup The bar group that contains this bar
 * @param barIndex The index of the clicked bar within the group
 * @param barValue The value of the clicked bar
 */
data class ComparisonBarSegment(
    val barGroup: BarGroup,
    val barIndex: Int,
    val barValue: Float,
)

/**
 * Configuration for Comparison Bar Chart (formerly Grouped Bar Chart) appearance and behavior
 *
 * @param negativeValuesDrawMode How to draw negative values (BELOW_AXIS or FROM_MIN_VALUE)
 * @param cornerRadius Corner radius for bar corners (None, Small, Medium, Large, ExtraLarge, or Custom)
 * @param referenceLine Optional reference line configuration
 * @param tooltipConfig Configuration for tooltip appearance when a bar is clicked
 * @param tooltipPosition Preferred position for tooltips (ABOVE, BELOW, or AUTO)
 */
data class ComparisonBarChartConfig(
    val negativeValuesDrawMode: NegativeValuesDrawMode = NegativeValuesDrawMode.BELOW_AXIS,
    val cornerRadius: CornerRadius = CornerRadius.Medium,
    val referenceLine: ReferenceLineConfig? = null,
    val tooltipConfig: TooltipConfig = TooltipConfig(),
    val tooltipPosition: TooltipPosition = TooltipPosition.AUTO,
    val tooltipFormatter: (ComparisonBarSegment) -> String = { segment ->
        "${segment.barGroup.label} [${segment.barIndex}]: ${segment.barValue}"
    },
)
