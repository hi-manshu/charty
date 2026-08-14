package com.himanshoe.charty.common

/**
 * The number a fraction is multiplied by to become a percentage.
 *
 * Five packages had each declared this privately — as `MAX_PERCENTAGE`, `PERCENTAGE_MULTIPLIER` and
 * `NORMALIZED_HORIZONTAL_MAX_PERCENT` — which is three names for one fact and no way to tell, from
 * any one of them, that the others existed.
 */
internal const val PERCENT_SCALE = 100f

/**
 * [value] as a percentage of [total], or zero when there is no total to compare against.
 *
 * The guard belongs here rather than at each call site: a pie slice's total is the sum of the data
 * it was handed, so an empty or all-zero series reaches this with nothing to divide by, and a slice
 * of nothing is nothing rather than NaN. Callers whose divisor is validated at construction pay only
 * a comparison for saying so once.
 */
internal fun percentOf(
    value: Float,
    total: Float,
): Float =
    if (total > 0f) {
        (value / total) * PERCENT_SCALE
    } else {
        0f
    }
