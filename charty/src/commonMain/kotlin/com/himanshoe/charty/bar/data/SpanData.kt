package com.himanshoe.charty.bar.data

import androidx.compose.runtime.Immutable
import com.himanshoe.charty.color.ChartyColor

/**
 * Data class representing a span (range) in a span chart
 *
 * @property label The label displayed on Y-axis for this span row
 * @property startValue The starting value of the span
 * @property endValue The ending value of the span
 * @property color Optional color for this specific span. If null, uses the chart's default color scheme
 */
@Immutable
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
