package com.himanshoe.charty.bar.config

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.common.tooltip.TooltipPosition

/**
 * Represents a single segment within a mosaic bar that was clicked
 *
 * @property barGroup The entire bar group that contains this segment
 * @property segmentIndex The index of the clicked segment within the bar
 * @property segmentValue The value of the clicked segment
 * @property segmentPercentage The percentage this segment represents of the total
 */
@Immutable
data class MosaicBarSegment(
    val barGroup: BarGroup,
    val segmentIndex: Int,
    val segmentValue: Float,
    val segmentPercentage: Float,
)

/**
 * Configuration for [com.himanshoe.charty.bar.MosaicBarChart]
 *
 * @property barWidthFraction Fraction of available space that each bar occupies (0.0f - 1.0f)
 * @property animation Animation configuration (Disabled or Enabled with duration)
 * @property tooltipConfig Configuration for tooltip appearance when a segment is clicked
 * @property tooltipPosition Preferred position for tooltips (ABOVE, BELOW, or AUTO)
 * @property tooltipFormatter Converts a data point into the string shown in its tooltip.
 */
@Stable
data class MosaicBarChartConfig(
    val barWidthFraction: Float = 0.9f,
    val animation: Animation = Animation.Default,
    val tooltipConfig: TooltipConfig = TooltipConfig(),
    val tooltipPosition: TooltipPosition = TooltipPosition.AUTO,
    val tooltipFormatter: (MosaicBarSegment) -> String = { segment ->
        "${segment.barGroup.label} [${segment.segmentIndex}]: ${segment.segmentPercentage.toInt()}%"
    },
) {
    init {
        require(barWidthFraction in 0f..1f) { "Bar width fraction must be between 0 and 1" }
    }
}
