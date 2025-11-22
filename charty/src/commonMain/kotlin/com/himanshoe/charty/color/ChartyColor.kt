package com.himanshoe.charty.color

import androidx.compose.ui.graphics.Color

/**
 * Sealed class representing color configuration for charts.
 *
 * Usage Examples:
 * ```
 * // 1. Solid color - single color for the entire chart
 * val solidBlue = ChartyColor.Solid(Color.Blue)
 * println(solidBlue.value) // [Color.Blue]
 *
 * // 2. Gradient - multiple colors for gradient effects
 * val gradient = ChartyColor.Gradient(
 *     listOf(Color.Red, Color.Orange, Color.Yellow)
 * )
 * println(gradient.value) // [Color.Red, Color.Orange, Color.Yellow]
 *
 * // 3. Using in a chart component
 * @Composable
 * fun MyBarChart(
 *     data: List<Float>,
 *     colors: ChartyColor = ChartyColor.Solid(Color.Blue)
 * ) {
 *     // Access colors using the value property
 *     val colorList = colors.value
 *
 *     // For single bar chart, use first color
 *     val barColor = colorList.first()
 *
 *     // For multi-bar chart, cycle through colors
 *     data.forEachIndexed { index, value ->
 *         val color = colorList[index % colorList.size]
 *         // Draw bar with color
 *     }
 * }
 *
 * // 4. Pattern matching with when
 * when (val chartColor = ChartyColor.Solid(Color.Green)) {
 *     is ChartyColor.Solid -> {
 *         // Single color rendering logic
 *         println("Using solid color: ${chartColor.color}")
 *     }
 *     is ChartyColor.Gradient -> {
 *         // Gradient rendering logic
 *         println("Using gradient with ${chartColor.colors.size} colors")
 *     }
 * }
 * ```
 */
sealed class ChartyColor {
    abstract val value: List<Color>

    /**
     * Solid color configuration.
     * @param color The solid color to use
     */
    data class Solid(val color: Color) : ChartyColor() {
        override val value: List<Color>
            get() = listOf(color,color)
    }

    /**
     * Gradient color configuration.
     * @param colors The list of colors for the gradient
     */
    data class Gradient(val colors: List<Color>) : ChartyColor() {
        override val value: List<Color>
            get() = colors
    }
}

