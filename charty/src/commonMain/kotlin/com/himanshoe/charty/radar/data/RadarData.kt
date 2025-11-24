package com.himanshoe.charty.radar.data

import com.himanshoe.charty.color.ChartyColor

/**
 * Data class representing a single axis/dimension in a radar chart
 *
 * @param label The label for this axis (e.g., "Speed", "Power", "Defense")
 * @param value The value for this axis (should be between 0 and maxValue)
 * @param maxValue Maximum value for this axis (default: 100f)
 */
data class RadarAxisData(
    val label: String,
    val value: Float,
    val maxValue: Float = DEFAULT_MAX_VALUE
) {
    init {
        require(label.isNotBlank()) { "Radar axis label cannot be blank" }
        require(maxValue > 0f) { "Max value must be positive, got: $maxValue" }
        require(value >= 0f) { "Value must be non-negative, got: $value" }
    }

    /**
     * Get the normalized value (0.0 to 1.0)
     */
    fun getNormalizedValue(): Float = (value / maxValue).coerceIn(0f, 1f)

    /** Constants for [RadarAxisData]. */
    companion object {
        private const val DEFAULT_MAX_VALUE = 100f
    }
}

/**
 * Data class representing a complete dataset for radar chart
 * (a polygon connecting all axis values)
 *
 * @param label Label for this dataset (e.g., "Player 1", "Model A")
 * @param axes List of axis data points
 * @param color Color for this dataset's polygon and line
 * @param fillAlpha Alpha transparency for the filled polygon (0.0 to 1.0)
 */
data class RadarDataSet(
    val label: String,
    val axes: List<RadarAxisData>,
    val color: ChartyColor,
    val fillAlpha: Float = DEFAULT_FILL_ALPHA
) {
    init {
        require(label.isNotBlank()) { "Dataset label cannot be blank" }
        require(axes.isNotEmpty()) { "Dataset must have at least one axis" }
        require(axes.size >= MIN_AXES) { "Radar chart requires at least $MIN_AXES axes, got: ${axes.size}" }
        require(fillAlpha in 0f..1f) { "Fill alpha must be between 0 and 1, got: $fillAlpha" }
    }

    /** Constants for [RadarDataSet]. */
    companion object {
        private const val MIN_AXES = 3
        private const val DEFAULT_FILL_ALPHA = 0.3f
    }
}
