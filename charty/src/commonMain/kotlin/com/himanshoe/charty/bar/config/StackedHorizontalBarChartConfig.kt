package com.himanshoe.charty.bar.config

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.CornerRadius
import com.himanshoe.charty.common.config.ReferenceLineConfig
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.common.tooltip.TooltipPosition

/**
 * Represents a single segment within a stacked horizontal bar that was clicked.
 *
 * @param barGroup The entire bar group that contains this segment.
 * @param segmentIndex The index of the clicked segment within the bar.
 * @param segmentValue The value of the clicked segment.
 */
@Immutable
data class StackedHorizontalBarSegment(
    val barGroup: BarGroup,
    val segmentIndex: Int,
    val segmentValue: Float,
)

/**
 * Configuration for Stacked Horizontal Bar Chart appearance and behavior.
 *
 * @param barWidthFraction Fraction of the available row height that each bar occupies (0.0f – 1.0f).
 * @param barSpacing Spacing between bars in pixels. Must be non-negative.
 * @param rightCornerRadius Corner radius applied to the right (trailing) end of each stacked bar.
 * @param animation Animation configuration ([Animation.Disabled] or [Animation.Enabled] with duration).
 * @param referenceLine Optional configuration for a vertical reference line (e.g., a target or average line).
 * @param tooltipConfig Configuration for tooltip appearance when a segment is clicked.
 * @param tooltipPosition Preferred position for tooltips ([TooltipPosition.ABOVE], [TooltipPosition.BELOW], or [TooltipPosition.AUTO]).
 * @param tooltipFormatter Lambda that formats the tooltip label for a clicked [StackedHorizontalBarSegment].
 */
@Stable
data class StackedHorizontalBarChartConfig(
    val barWidthFraction: Float = 0.6f,
    val barSpacing: Float = 0f,
    val rightCornerRadius: CornerRadius = CornerRadius.Medium,
    val animation: Animation = Animation.Default,
    val referenceLine: ReferenceLineConfig? = null,
    val tooltipConfig: TooltipConfig = TooltipConfig(),
    val tooltipPosition: TooltipPosition = TooltipPosition.AUTO,
    val tooltipFormatter: (StackedHorizontalBarSegment) -> String = { segment ->
        "${segment.barGroup.label} [${segment.segmentIndex}]: ${segment.segmentValue}"
    },
) {
    init {
        require(barWidthFraction in 0f..1f) { "Bar width fraction must be between 0 and 1" }
        require(barSpacing >= 0) { "Bar spacing must be non-negative" }
    }
}
