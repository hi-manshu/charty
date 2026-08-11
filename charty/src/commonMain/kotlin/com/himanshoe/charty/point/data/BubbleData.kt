package com.himanshoe.charty.point.data

import androidx.compose.runtime.Immutable

/**
 * Data class for Bubble Chart points
 *
 * @property label The category or label for this bubble
 * @property xValue The X-axis value (optional, defaults to index position)
 * @property yValue The Y-axis value
 * @property size The size/magnitude of the bubble (will be normalized for rendering)
 */
@Immutable
data class BubbleData(
    val label: String,
    val xValue: Float? = null,
    val yValue: Float,
    val size: Float,
) {
    init {
        require(yValue.isFinite()) { "yValue must be a finite number" }
        require(size > 0f) { "Bubble size must be positive" }
    }
}
