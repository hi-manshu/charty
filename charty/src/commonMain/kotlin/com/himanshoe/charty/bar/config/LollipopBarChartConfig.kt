package com.himanshoe.charty.bar.config

import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.config.Animation

/**
 * Configuration for [com.himanshoe.charty.bar.LollipopBarChart]
 *
 * @param barWidthFraction Fraction of horizontal space each lollipop stem occupies (0f..1f)
 * @param stemThickness Thickness of the vertical stem in pixels
 * @param circleRadius Radius of the lollipop circle in pixels
 * @param circleStrokeWidth Optional stroke width for drawing the circle as a ring; 0f for filled
 * @param circleColor Optional override color for the circle head. If null, falls back to the bar color.
 * @param animation Animation configuration for growing stems from baseline
 */
data class LollipopBarChartConfig(
    val barWidthFraction: Float = 0.2f,
    val stemThickness: Float = 6f,
    val circleRadius: Float = 14f,
    val circleStrokeWidth: Float = 0f,
    val circleColor: ChartyColor? = null,
    val animation: Animation = Animation.Enabled()
) {
    init {
        require(barWidthFraction in 0f..1f) { "Bar width fraction must be between 0 and 1" }
        require(stemThickness > 0f) { "Stem thickness must be positive" }
        require(circleRadius > 0f) { "Circle radius must be positive" }
        require(circleStrokeWidth >= 0f) { "Circle stroke width cannot be negative" }
    }
}

