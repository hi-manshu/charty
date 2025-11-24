package com.himanshoe.charty.bar.config

import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.common.tooltip.TooltipPosition

/**
 * Represents a single segment within a mosiac bar that was clicked
 *
 * @param barGroup The entire bar group that contains this segment
 * @param segmentIndex The index of the clicked segment within the bar
 * @param segmentValue The value of the clicked segment
 * @param segmentPercentage The percentage this segment represents of the total
 */
data class MosiacBarSegment(
    val barGroup: BarGroup,
    val segmentIndex: Int,
    val segmentValue: Float,
    val segmentPercentage: Float,
)

/**
 * Configuration for [com.himanshoe.charty.bar.MosiacBarChart]
 *
 * @param barWidthFraction Fraction of available space that each bar occupies (0.0f - 1.0f)
 * @param animation Animation configuration (Disabled or Enabled with duration)
 * @param tooltipConfig Configuration for tooltip appearance when a segment is clicked
 * @param tooltipPosition Preferred position for tooltips (ABOVE, BELOW, or AUTO)
 */
data class MosiacBarChartConfig(
    val barWidthFraction: Float = 0.9f,
    val animation: Animation = Animation.Default,
    val tooltipConfig: TooltipConfig = TooltipConfig(),
    val tooltipPosition: TooltipPosition = TooltipPosition.AUTO,
    val tooltipFormatter: (MosiacBarSegment) -> String = { segment ->
        "${segment.barGroup.label} [${segment.segmentIndex}]: ${segment.segmentPercentage.toInt()}%"
    },
) {
    init {
        require(barWidthFraction in 0f..1f) { "Bar width fraction must be between 0 and 1" }
    }
}
