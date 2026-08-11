package com.himanshoe.charty.bar.config

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.CornerRadius
import com.himanshoe.charty.common.config.ReferenceBandConfig
import com.himanshoe.charty.common.config.ReferenceLineConfig
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.common.tooltip.TooltipPosition

/**
 * Defines how negative values should be drawn in bar charts
 */
enum class NegativeValuesDrawMode {
    /**
     * Negative bars extend below the zero axis line (axis centered)
     * Positive bars extend above the zero axis line
     * Best for visualizing profit/loss, gains/losses, etc.
     */
    BELOW_AXIS,

    /**
     * All bars drawn from the minimum value upward
     * The axis starts at the lowest value instead of zero
     * Best for showing relative differences when all values should appear above baseline
     */
    FROM_MIN_VALUE,
}

/**
 * Configuration for Bar Chart appearance and behavior
 *
 * @property barWidthFraction Fraction of available space that each bar occupies (0.0f - 1.0f)
 * @property barSpacing Spacing between bars in pixels
 * @property cornerRadius Corner radius for bar corners (None, Small, Medium, Large, ExtraLarge, or Custom)
 * @property negativeValuesDrawMode How to draw negative values (BELOW_AXIS or FROM_MIN_VALUE)
 * @property animation Animation configuration (Disabled or Enabled with duration)
 * @property animateValueChanges When `true`, bar heights tween from their previous values to the new
 *   ones whenever the data changes (using [animation]); when `false` (default) new data appears
 *   instantly. Has no effect if [animation] is [Animation.Disabled].
 * @property referenceLine Optional reference line configuration (target/average line)
 * @property referenceBand Optional shaded value region drawn behind the bars (see [ReferenceBandConfig])
 * @property tooltipConfig Configuration for tooltip appearance when a bar is clicked
 * @property tooltipPosition Preferred position for tooltips (ABOVE, BELOW, or AUTO)
 * @property tooltipFormatter Converts a data point into the string shown in its tooltip.
 * @property showDataLabels Whether to show value labels above each bar
 * @property dataLabelFormatter Formats the bar value for the data label text
 * @property dataLabelStyle Text style for data labels
 */
@Stable
data class BarChartConfig(
    val barWidthFraction: Float = 0.6f,
    val barSpacing: Float = 0f,
    val cornerRadius: CornerRadius = CornerRadius.Medium,
    val negativeValuesDrawMode: NegativeValuesDrawMode = NegativeValuesDrawMode.BELOW_AXIS,
    val animation: Animation = Animation.Default,
    val animateValueChanges: Boolean = false,
    val referenceLine: ReferenceLineConfig? = null,
    val referenceBand: ReferenceBandConfig? = null,
    val tooltipConfig: TooltipConfig = TooltipConfig(),
    val tooltipPosition: TooltipPosition = TooltipPosition.AUTO,
    val tooltipFormatter: (BarData) -> String = { barData ->
        "${barData.label}: ${barData.value}"
    },
    val showDataLabels: Boolean = false,
    val dataLabelFormatter: (BarData) -> String = { barData ->
        val v = barData.value
        if (v == v.toLong().toFloat()) {
            v.toLong().toString()
        } else {
            v.toString()
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
