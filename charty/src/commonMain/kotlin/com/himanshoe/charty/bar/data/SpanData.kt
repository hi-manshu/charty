package com.himanshoe.charty.bar.data

import com.himanshoe.charty.color.ChartyColor

/**
 * Data class representing a span (range) in a span chart
 *
 * @param label The label displayed on Y-axis for this span row
 * @param startValue The starting value of the span
 * @param endValue The ending value of the span
 * @param color Optional color for this specific span. If null, uses the chart's default color scheme
 */
data class SpanData(
    val label: String,
    val startValue: Float,
    val endValue: Float,
    val color: ChartyColor? = null,
) {
    init {
        require(endValue >= startValue) { "endValue must be greater than or equal to startValue" }
    }
}
