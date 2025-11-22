package com.himanshoe.charty.bar.data

/**
 * Data class representing a group of bars in a grouped bar chart
 *
 * @param label The label displayed on X-axis for this group
 * @param values The list of values for bars in this group
 */
data class BarGroup(
    val label: String,
    val values: List<Float>
)