package com.himanshoe.charty.line.config

import androidx.compose.ui.graphics.StrokeCap
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.ReferenceLineConfig

/**
 * Configuration for Line Chart appearance and behavior
 *
 * @param lineWidth Width of the line stroke in pixels
 * @param showPoints Whether to show circular markers at data points
 * @param pointRadius Radius of point markers in pixels (if showPoints is true)
 * @param pointAlpha Alpha (transparency) value for points (0.0f - 1.0f)
 * @param strokeCap The style of line endings (Butt, Round, or Square)
 * @param smoothCurve Whether to draw smooth curves instead of straight lines (future enhancement)
 * @param negativeValuesDrawMode How to draw negative values (BELOW_AXIS or FROM_MIN_VALUE)
 * @param animation Animation configuration (Disabled or Enabled with duration)
 * @param referenceLine Optional configuration for a reference line (e.g., target or average line)
 */
data class LineChartConfig(
    val lineWidth: Float = 3f,
    val showPoints: Boolean = true,
    val pointRadius: Float = 6f,
    val pointAlpha: Float = 1f,
    val strokeCap: StrokeCap = StrokeCap.Round,
    val smoothCurve: Boolean = false,
    val negativeValuesDrawMode: NegativeValuesDrawMode = NegativeValuesDrawMode.BELOW_AXIS,
    val animation: Animation = Animation.Default,
    val referenceLine: ReferenceLineConfig? = null,
) {
    init {
        require(lineWidth > 0) { "Line width must be greater than 0" }
        require(pointRadius > 0) { "Point radius must be greater than 0" }
        require(pointAlpha in 0f..1f) { "Point alpha must be between 0 and 1" }
    }
}
