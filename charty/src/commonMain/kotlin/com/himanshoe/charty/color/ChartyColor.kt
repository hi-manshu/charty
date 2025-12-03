package com.himanshoe.charty.color

import androidx.compose.ui.graphics.Color

/**
 * A sealed class representing the color configuration for charts.
 *
 * This class allows for specifying either a solid color or a gradient for chart elements.
 *
 * @property value A list of colors representing the configuration. For [Solid], it's a list with two identical colors. For [Gradient], it's the list of gradient colors.
 */
sealed class ChartyColor {
    abstract val value: List<Color>

    /**
     * Represents a solid color configuration.
     *
     * @param color The solid color to be used.
     */
    data class Solid(
        val color: Color,
    ) : ChartyColor() {
        override val value: List<Color>
            get() = listOf(color, color)
    }

    /**
     * Represents a gradient color configuration.
     *
     * @param colors The list of colors to be used in the gradient.
     */
    data class Gradient(
        val colors: List<Color>,
    ) : ChartyColor() {
        override val value: List<Color>
            get() = colors
    }
}
