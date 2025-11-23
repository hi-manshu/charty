package com.himanshoe.charty.bar.config

import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.CornerRadius

/**
 * Configuration for Stacked Bar Chart appearance and behavior
 *
 * @param barWidthFraction Fraction of available space that each bar occupies (0.0f - 1.0f)
 * @param barSpacing Spacing between bars in pixels
 * @param topCornerRadius Corner radius for the top segment of stacked bars
 * @param animation Animation configuration (Disabled or Enabled with duration)
 */
data class StackedBarChartConfig(
    val barWidthFraction: Float = 0.6f,
    val barSpacing: Float = 0f,
    val topCornerRadius: CornerRadius = CornerRadius.Medium,
    val animation: Animation = Animation.Default
) {
    init {
        require(barWidthFraction in 0f..1f) { "Bar width fraction must be between 0 and 1" }
        require(barSpacing >= 0) { "Bar spacing must be non-negative" }
    }
}

