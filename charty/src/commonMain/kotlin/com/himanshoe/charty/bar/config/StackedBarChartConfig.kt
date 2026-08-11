package com.himanshoe.charty.bar.config

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.CornerRadius
import com.himanshoe.charty.common.config.ReferenceLineConfig
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.common.tooltip.TooltipPosition

/**
 * Represents a single segment within a stacked bar that was clicked
 *
 * @property barGroup The entire bar group that contains this segment
 * @property segmentIndex The index of the clicked segment within the bar
 * @property segmentValue The value of the clicked segment
 */
@Immutable
data class StackedBarSegment(
    val barGroup: BarGroup,
    val segmentIndex: Int,
    val segmentValue: Float,
)

/**
 * Configuration for Stacked Bar Chart appearance and behavior
 *
 * @property barWidthFraction Fraction of available space that each bar occupies (0.0f - 1.0f)
 * @property barSpacing Spacing between bars in pixels
 * @property topCornerRadius Corner radius for the top segment of stacked bars
 * @property animation Animation configuration (Disabled or Enabled with duration)
 * @property referenceLine Optional configuration for a reference line (e.g., target or average line) shared across all bars
 * @property tooltipConfig Configuration for tooltip appearance when a segment is clicked
 * @property tooltipPosition Preferred position for tooltips (ABOVE, BELOW, or AUTO)
 * @property tooltipFormatter Converts a data point into the string shown in its tooltip.
 * @property showDataLabels Whether to show the stacked total above each bar
 * @property dataLabelFormatter Formats the total value for the data label text
 * @property dataLabelStyle Text style for data labels
 */
@Stable
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
    val showDataLabels: Boolean = false,
    val dataLabelFormatter: (BarGroup) -> String = { group ->
        val total = group.values.sum()
        if (total == total.toLong().toFloat()) {
            total.toLong().toString()
        } else {
            total.toString()
        }
    },
    val dataLabelStyle: TextStyle =
        TextStyle(
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.DarkGray,
        ),
) {
    init {
        require(barWidthFraction in 0f..1f) { "Bar width fraction must be between 0 and 1" }
        require(barSpacing >= 0) { "Bar spacing must be non-negative" }
    }
}
