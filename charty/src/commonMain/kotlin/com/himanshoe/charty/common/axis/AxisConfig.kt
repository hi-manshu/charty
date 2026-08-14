package com.himanshoe.charty.common.axis

import com.himanshoe.charty.common.constants.ChartConstants
import kotlin.math.round

/**
 * A data class that holds the configuration for a chart axis.
 *
 * @property minValue The minimum value to be displayed on the axis.
 * @property maxValue The maximum value to be displayed on the axis.
 * @property steps The number of steps or divisions to be shown on the axis.
 * @property drawAxisAtZero If `true` and the data spans across zero, the x-axis will be drawn at the zero-line (centered). If `false`, the x-axis will always be at the bottom.
 * @property valueFormatter Converts each axis tick value into its displayed label. Defaults to
 *   [formatAxisLabel] (integers without decimals, floats trimmed to two places). Override it to
 *   render dates, currency, percentages, or any custom units — e.g. `{ "$${'$'}{it.toInt()}" }`.
 */

data class AxisConfig(
    val minValue: Float = 0f,
    val maxValue: Float = 100f,
    val steps: Int = 5,
    val drawAxisAtZero: Boolean = true,
    val valueFormatter: (Float) -> String = ::formatAxisLabel,
)

private const val ROUNDING_MULTIPLIER = 100f
private const val MAX_DECIMAL_PLACES = 2
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
internal fun formatAxisLabel(value: Float): String {
    if (value % MODULO_CHECK_ZERO == ZERO_VALUE) {
        return value.toLong().toString()
    }

    val rounded = round(value * ROUNDING_MULTIPLIER) / ROUNDING_MULTIPLIER
    val str = rounded.toString()

    return when {
        str.any { it == 'e' || it == 'E' } -> rounded.toLong().toString()
        else -> {
            val dotIndex = str.indexOf('.')
            if (dotIndex >= 0 && str.length > dotIndex + 1 + MAX_DECIMAL_PLACES) {
                str.take(dotIndex + 1 + MAX_DECIMAL_PLACES).trimEnd('0').trimEnd('.')
            } else {
                str.trimEnd('0').trimEnd('.')
            }
        }
    }
}

/**
 * The value axis a chart puts against its data range.
 *
 * Four packages had each declared a `createAxisConfig` of their own — same three lines, same purpose,
 * differing only in whether [drawAxisAtZero] was a parameter or hard-coded, and in which of six
 * spellings of "six steps" they happened to import. Same-named functions in different packages are
 * worse than no sharing at all: a reader who finds one has no way to know the others exist.
 */
internal fun valueAxisConfig(
    minValue: Float,
    maxValue: Float,
    drawAxisAtZero: Boolean,
): AxisConfig =
    AxisConfig(
        minValue = minValue,
        maxValue = maxValue,
        steps = ChartConstants.DEFAULT_AXIS_STEPS,
        drawAxisAtZero = drawAxisAtZero,
    )
