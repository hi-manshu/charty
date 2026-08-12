package com.himanshoe.charty.common.util

import kotlin.math.abs
import kotlin.math.roundToLong

private const val DECIMAL_SCALE = 10f
private const val WHOLE_NUMBER_EPSILON = 0.0001f

/**
 * Formats a chart value the way a reader expects to see it rather than the way a `Float` prints.
 *
 * A whole number loses its decimal part entirely, and anything else is rounded to a single decimal
 * place. Without this, a value produced by arithmetic renders its full binary expansion — a label
 * reading `37.86205291748047` instead of `37.9`, wide enough to overflow the plot it sits in.
 *
 * Charts that need a different precision, a unit or a currency should pass their own formatter
 * through the relevant config instead of relying on this default.
 */
fun Float.toChartLabel(): String {
    val rounded = (this * DECIMAL_SCALE).roundToLong() / DECIMAL_SCALE
    return if (abs(rounded - rounded.toLong()) < WHOLE_NUMBER_EPSILON) {
        rounded.toLong().toString()
    } else {
        rounded.toString()
    }
}
