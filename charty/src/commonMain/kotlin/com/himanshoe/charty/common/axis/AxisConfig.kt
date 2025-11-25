package com.himanshoe.charty.common.axis

import kotlin.math.round

/**
 * Configuration for chart axis.
 *
 * @param minValue Minimum value on the axis
 * @param maxValue Maximum value on the axis
 * @param steps Number of steps/divisions on the axis
 * @param label Label for the axis (e.g., "Sales", "Revenue")
 * @param drawAxisAtZero When true and data spans zero, the X axis is drawn at zero (centered). When false, the X axis is always drawn at the bottom.
 */
data class AxisConfig(
    val minValue: Float = 0f,
    val maxValue: Float = 100f,
    val steps: Int = 5,
    val label: String = "",
    val drawAxisAtZero: Boolean = true,
)


/**
 * Formats a float value for display on axis labels.
 * - Shows integers without decimal point
 * - Shows floats with max 2 decimal places, trimming trailing zeros
 */
internal fun formatAxisLabel(value: Float): String =
    if (value % 1 == 0f) {
        value.toInt().toString()
    } else {
        // Round to 2 decimal places
        val rounded = round(value * 100) / 100
        val str = rounded.toString()

        // Ensure max 2 decimal places
        val dotIndex = str.indexOf('.')
        if (dotIndex >= 0 && str.length > dotIndex + 3) {
            str.take(dotIndex + 3).trimEnd('0').trimEnd('.')
        } else {
            str.trimEnd('0').trimEnd('.')
        }
    }
