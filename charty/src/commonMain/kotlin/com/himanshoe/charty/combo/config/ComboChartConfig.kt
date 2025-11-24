package com.himanshoe.charty.combo.config

import androidx.compose.ui.graphics.StrokeCap
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.CornerRadius
import com.himanshoe.charty.common.config.ReferenceLineConfig

/**
 * Configuration for Combo Chart appearance and behavior
 *
 * @param barWidthFraction Fraction of available space that each bar occupies (0.0f - 1.0f)
 * @param barCornerRadius Corner radius for bar corners (None, Small, Medium, Large, ExtraLarge, or Custom)
 * @param lineWidth Width of the line stroke in pixels
 * @param showPoints Whether to show circular markers at data points on the line
 * @param pointRadius Radius of point markers in pixels (if showPoints is true)
 * @param pointAlpha Alpha (transparency) value for points (0.0f - 1.0f)
 * @param strokeCap The style of line endings (Butt, Round, or Square)
 * @param smoothCurve Whether to draw smooth curves instead of straight lines
 * @param negativeValuesDrawMode How to draw negative values (BELOW_AXIS or FROM_MIN_VALUE)
 * @param animation Animation configuration (Disabled or Enabled with duration)
 * @param referenceLine Optional reference line configuration to draw a shared target/avg line across the combo chart
 */
data class ComboChartConfig(
    val barWidthFraction: Float = 0.6f,
    val barCornerRadius: CornerRadius = CornerRadius.Medium,
    val lineWidth: Float = 3f,
    val showPoints: Boolean = true,
    val pointRadius: Float = 6f,
    val pointAlpha: Float = 1f,
    val strokeCap: StrokeCap = StrokeCap.Round,
    val smoothCurve: Boolean = false,
    val negativeValuesDrawMode: NegativeValuesDrawMode = NegativeValuesDrawMode.BELOW_AXIS,
    val animation: Animation = Animation.Default,
    val referenceLine: ReferenceLineConfig? = null
) {
    init {
        require(barWidthFraction in 0f..1f) { "Bar width fraction must be between 0 and 1" }
        require(lineWidth > 0) { "Line width must be greater than 0" }
        require(pointRadius > 0) { "Point radius must be greater than 0" }
        require(pointAlpha in 0f..1f) { "Point alpha must be between 0 and 1" }
    }
}
