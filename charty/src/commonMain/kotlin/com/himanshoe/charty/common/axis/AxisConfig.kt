package com.himanshoe.charty.common.axis

import kotlin.math.round

/**
 * A data class that holds the configuration for a chart axis.
 *
 * @property minValue The minimum value to be displayed on the axis.
 * @property maxValue The maximum value to be displayed on the axis.
 * @property steps The number of steps or divisions to be shown on the axis.
 * @property label A descriptive label for the axis (e.g., "Sales", "Revenue").
 * @property drawAxisAtZero If `true` and the data spans across zero, the x-axis will be drawn at the zero-line (centered). If `false`, the x-axis will always be at the bottom.
 */
data class AxisConfig(
    val minValue: Float = 0f,
    val maxValue: Float = 100f,
    val steps: Int = 5,
    val label: String = "",
    val drawAxisAtZero: Boolean = true,
)


private const val ROUNDING_MULTIPLIER = 100f
private const val MAX_DECIMAL_DIGITS = 3
private const val MODULO_CHECK_ZERO = 1
private const val ZERO_VALUE = 0f

/**
 * Formats a float value into a string for display on an axis label.
 *
 * This function ensures that integer values are shown without a decimal point,
 * while float values are displayed with a maximum of two decimal places, with trailing zeros trimmed.
 *
 * @param value The float value to be formatted.
 * @return A formatted string representation of the value.
 */
internal fun formatAxisLabel(value: Float): String =
    if (value % MODULO_CHECK_ZERO == ZERO_VALUE) {
        value.toInt().toString()
    } else {
        // Round to 2 decimal places
        val rounded = round(value * ROUNDING_MULTIPLIER) / ROUNDING_MULTIPLIER
        val str = rounded.toString()

        // Ensure max 2 decimal places
        val dotIndex = str.indexOf('.')
        if (dotIndex >= 0 && str.length > dotIndex + MAX_DECIMAL_DIGITS) {
            str.take(dotIndex + MAX_DECIMAL_DIGITS).trimEnd('0').trimEnd('.')
        } else {
            str.trimEnd('0').trimEnd('.')
        }
    }
