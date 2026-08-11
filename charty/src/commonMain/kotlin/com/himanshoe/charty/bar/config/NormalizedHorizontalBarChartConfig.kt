package com.himanshoe.charty.bar.config

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.CornerRadius
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.common.tooltip.TooltipPosition

/**
 * Identifies a single segment inside a normalized horizontal bar chart that was clicked.
 *
 * @property barGroup The group (row) that contains the tapped segment.
 * @property segmentIndex Zero-based index of the segment within the bar.
 * @property segmentValue The raw value of the tapped segment.
 * @property segmentPercentage The percentage share this segment holds within its group (0–100).
 */
@Immutable
data class NormalizedHorizontalBarSegment(
    val barGroup: BarGroup,
    val segmentIndex: Int,
    val segmentValue: Float,
    val segmentPercentage: Float,
)

/**
 * Configuration for [com.himanshoe.charty.bar.NormalizedHorizontalBarChart].
 *
 * @property barWidthFraction Fraction of each row's height occupied by the bar (0.0f – 1.0f).
 * @property rightCornerRadius Corner radius applied to the trailing (right) end of each full bar.
 * @property animation Animation configuration ([Animation.Disabled] or [Animation.Enabled]).
 * @property tooltipConfig Tooltip appearance settings.
 * @property tooltipPosition Preferred tooltip placement.
 * @property tooltipFormatter Lambda that formats the tooltip text for a tapped [NormalizedHorizontalBarSegment].
 *   Defaults to showing the segment label, index, and percentage.
 */
@Stable
data class NormalizedHorizontalBarChartConfig(
    val barWidthFraction: Float = 0.6f,
    val rightCornerRadius: CornerRadius = CornerRadius.Medium,
    val animation: Animation = Animation.Default,
    val tooltipConfig: TooltipConfig = TooltipConfig(),
    val tooltipPosition: TooltipPosition = TooltipPosition.AUTO,
    val tooltipFormatter: (NormalizedHorizontalBarSegment) -> String = { segment ->
        "${segment.barGroup.label} [${segment.segmentIndex}]: ${segment.segmentPercentage.toInt()}%"
    },
) {
    init {
        require(barWidthFraction in 0f..1f) { "Bar width fraction must be between 0 and 1" }
    }
}
