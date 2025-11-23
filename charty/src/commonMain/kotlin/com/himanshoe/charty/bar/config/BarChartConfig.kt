package com.himanshoe.charty.bar.config

import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.CornerRadius

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
    FROM_MIN_VALUE
}

/**
 * Configuration for Bar Chart appearance and behavior
 *
 * @param barWidthFraction Fraction of available space that each bar occupies (0.0f - 1.0f)
 * @param barSpacing Spacing between bars in pixels
 * @param cornerRadius Corner radius for bar corners (None, Small, Medium, Large, ExtraLarge, or Custom)
 * @param negativeValuesDrawMode How to draw negative values (BELOW_AXIS or FROM_MIN_VALUE)
 * @param animation Animation configuration (Disabled or Enabled with duration)
 */
data class BarChartConfig(
    val barWidthFraction: Float = 0.6f,
    val barSpacing: Float = 0f,
    val cornerRadius: CornerRadius = CornerRadius.Medium,
    val negativeValuesDrawMode: NegativeValuesDrawMode = NegativeValuesDrawMode.BELOW_AXIS,
    val animation: Animation = Animation.Default
) {
    init {
        require(barWidthFraction in 0f..1f) { "Bar width fraction must be between 0 and 1" }
        require(barSpacing >= 0) { "Bar spacing must be non-negative" }
    }
}

