package com.himanshoe.charty.bar.data

import com.himanshoe.charty.color.ChartyColor

/**
 * Data class representing a group of bars in a grouped bar chart
 *
 * @param label The label displayed on X-axis for this group
 * @param values The list of values for bars in this group
 * @param colors Optional list of colors for each value. If provided, must match the size of values.
 *               If null, uses the chart's default color scheme
 */
data class BarGroup(
    val label: String,
    val values: List<Float>,
    val colors: List<ChartyColor>? = null,
) {
    init {
        if (colors != null) {
            require(colors.size == values.size) {
                "Colors list size (${colors.size}) must match values list size (${values.size})"
            }
        }
    }
}
