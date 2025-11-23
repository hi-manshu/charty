package com.himanshoe.charty.point

/**
 * Data class for Bubble Chart points
 *
 * @param label The category or label for this bubble
 * @param xValue The X-axis value (optional, defaults to index position)
 * @param yValue The Y-axis value
 * @param size The size/magnitude of the bubble (will be normalized for rendering)
 */
data class BubbleData(
    val label: String,
    val xValue: Float? = null,
    val yValue: Float,
    val size: Float
) {
    init {
        require(yValue.isFinite()) { "yValue must be a finite number" }
        require(size > 0f) { "Bubble size must be positive" }
    }
}

