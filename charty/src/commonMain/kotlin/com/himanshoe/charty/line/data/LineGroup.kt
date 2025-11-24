package com.himanshoe.charty.line.data

/**
 * Data class representing a group of line values at a specific X position
 * Used for multi-line charts and stacked area charts
 *
 * @param label The label displayed on X-axis for this group
 * @param values The list of values for different series at this X position
 */
data class LineGroup(
    val label: String,
    val values: List<Float>,
)
