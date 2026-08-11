package com.himanshoe.charty.pie.data

import androidx.compose.runtime.Immutable
import com.himanshoe.charty.color.ChartyColor

private const val PERCENTAGE_MULTIPLIER = 100f
private const val DEGREES_IN_CIRCLE = 360f

/**
 * Data class representing a single slice in a Pie or Donut chart
 *
 * @property label The label/name for this slice (e.g., "Product A", "Sales")
 * @property value The numeric value of this slice (must be positive)
 * @property color Optional custom [ChartyColor] (solid or gradient) for this slice. If null, the
 *   chart's colour scheme is used. Using [ChartyColor] keeps pie slices consistent with every other
 *   chart type and lets a slice be filled with a gradient.
 * @property metadata Optional metadata for custom handling in click listeners
 *
 * Usage:
 * ```kotlin
 * val slice1 = PieData("Product A", 45.5f)
 * val slice2 = PieData("Product B", 30.0f, ChartyColor.Solid(Color.Blue))
 * val slice3 = PieData("Product C", 24.5f, metadata = mapOf("category" to "Electronics"))
 * ```
 */
@Immutable
data class PieData(
    val label: String,
    val value: Float,
    val color: ChartyColor? = null,
    val metadata: Map<String, Any>? = null,
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
    fun calculatePercentage(total: Float): Float =
        if (total >
            0f
        ) {
            (value / total) * PERCENTAGE_MULTIPLIER
        } else {
            0f
        }

    /**
     * Calculates the sweep angle for this slice in degrees
     * @param total The sum of all slice values
     * @return Angle in degrees (0.0 to 360.0)
     */
    fun calculateSweepAngle(total: Float): Float =
        if (total > 0f) {
            (value / total) * DEGREES_IN_CIRCLE
        } else {
            0f
        }
}
