package com.himanshoe.charty.bar.config

import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.CornerRadius
import com.himanshoe.charty.common.config.ReferenceLineConfig
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.common.tooltip.TooltipPosition

/**
 * Represents a single segment within a stacked bar that was clicked
 *
 * @param barGroup The entire bar group that contains this segment
 * @param segmentIndex The index of the clicked segment within the bar
 * @param segmentValue The value of the clicked segment
 */
data class StackedBarSegment(
    val barGroup: BarGroup,
    val segmentIndex: Int,
    val segmentValue: Float,
)

/**
 * Configuration for Stacked Bar Chart appearance and behavior
 *
 * @param barWidthFraction Fraction of available space that each bar occupies (0.0f - 1.0f)
 * @param barSpacing Spacing between bars in pixels
 * @param topCornerRadius Corner radius for the top segment of stacked bars
 * @param animation Animation configuration (Disabled or Enabled with duration)
 * @param referenceLine Optional configuration for a reference line (e.g., target or average line) shared across all bars
 * @param tooltipConfig Configuration for tooltip appearance when a segment is clicked
 * @param tooltipPosition Preferred position for tooltips (ABOVE, BELOW, or AUTO)
 */
data class StackedBarChartConfig(
    val barWidthFraction: Float = 0.6f,
    val barSpacing: Float = 0f,
    val topCornerRadius: CornerRadius = CornerRadius.Medium,
    val animation: Animation = Animation.Default,
    val referenceLine: ReferenceLineConfig? = null,
    val tooltipConfig: TooltipConfig = TooltipConfig(),
    val tooltipPosition: TooltipPosition = TooltipPosition.AUTO,
    val tooltipFormatter: (StackedBarSegment) -> String = { segment ->
        "${segment.barGroup.label} [${segment.segmentIndex}]: ${segment.segmentValue}"
    },
) {
    init {
        require(barWidthFraction in 0f..1f) { "Bar width fraction must be between 0 and 1" }
        require(barSpacing >= 0) { "Bar spacing must be non-negative" }
    }
}
