package com.himanshoe.charty.pie

import androidx.compose.ui.graphics.Color

/**
 * Data class representing a single slice in a Pie or Donut chart
 *
 * @param label The label/name for this slice (e.g., "Product A", "Sales")
 * @param value The numeric value of this slice (must be positive)
 * @param color Optional custom color for this slice. If null, uses chart's color scheme
 * @param metadata Optional metadata for custom handling in click listeners
 *
 * Usage:
 * ```kotlin
 * val slice1 = PieData("Product A", 45.5f)
 * val slice2 = PieData("Product B", 30.0f, Color.Blue)
 * val slice3 = PieData("Product C", 24.5f, metadata = mapOf("category" to "Electronics"))
 * ```
 */
data class PieData(
    val label: String,
    val value: Float,
    val color: Color? = null,
    val metadata: Map<String, Any>? = null
) {
    init {
        require(value > 0f) { "Pie slice value must be positive, got: $value" }
        require(label.isNotBlank()) { "Pie slice label cannot be blank" }
    }

    /**
     * Calculates the percentage this slice represents of the total
     * @param total The sum of all slice values
     * @return Percentage as a float (0.0 to 100.0)
     */
    fun calculatePercentage(total: Float): Float {
        return if (total > 0f) (value / total) * 100f else 0f
    }

    /**
     * Calculates the sweep angle for this slice in degrees
     * @param total The sum of all slice values
     * @return Angle in degrees (0.0 to 360.0)
     */
    fun calculateSweepAngle(total: Float): Float {
        return if (total > 0f) (value / total) * 360f else 0f
    }
}

