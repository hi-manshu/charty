package com.himanshoe.charty.bar.data

import androidx.compose.runtime.Immutable
import com.himanshoe.charty.color.ChartyColor

/**
 * Data class representing a group of bars in a grouped bar chart
 *
 * @property label The label displayed on X-axis for this group
 * @property values The list of values for bars in this group
 * @property colors Optional list of colors for each value. If provided, must match the size of values.
 *               If null, uses the chart's default color scheme
 */
@Immutable
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
