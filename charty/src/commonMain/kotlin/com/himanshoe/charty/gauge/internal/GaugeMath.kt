package com.himanshoe.charty.gauge.internal

/**
 * Normalizes [value] into a `0..1` fraction of the `[minValue, maxValue]` range. Values below
 * [minValue] clamp to `0` and values above [maxValue] clamp to `1`.
 */
internal fun gaugeFraction(
    value: Float,
    minValue: Float,
    maxValue: Float,
): Float {
    require(maxValue > minValue) {
        "maxValue ($maxValue) must be greater than minValue ($minValue)"
    }
    return ((value - minValue) / (maxValue - minValue)).coerceIn(0f, 1f)
}

/**
 * Maps [value] onto the dial: [minValue] maps to [startAngleDegrees], [maxValue] maps to
 * `startAngleDegrees + sweepAngleDegrees`, and intermediate values interpolate linearly. Out-of-range
 * values clamp to the dial's endpoints.
 */
internal fun gaugeAngleForValue(
    value: Float,
    minValue: Float,
    maxValue: Float,
    startAngleDegrees: Float,
    sweepAngleDegrees: Float,
): Float {
    val fraction = gaugeFraction(value = value, minValue = minValue, maxValue = maxValue)
    return startAngleDegrees + fraction * sweepAngleDegrees
}

/**
 * Computes the dial arc covered by a plot band spanning `[fromValue, toValue]`. Band edges outside
 * `[minValue, maxValue]` clamp to the dial's endpoints, so a band entirely outside the range yields
 * a zero sweep.
 */
internal fun gaugeBandArc(
    fromValue: Float,
    toValue: Float,
    minValue: Float,
    maxValue: Float,
    startAngleDegrees: Float,
    sweepAngleDegrees: Float,
): GaugeArc {
    val startFraction = gaugeFraction(value = fromValue, minValue = minValue, maxValue = maxValue)
    val endFraction = gaugeFraction(value = toValue, minValue = minValue, maxValue = maxValue)
    return GaugeArc(
        startAngleDegrees = startAngleDegrees + startFraction * sweepAngleDegrees,
        sweepAngleDegrees = (endFraction - startFraction).coerceAtLeast(0f) * sweepAngleDegrees,
    )
}

/**
 * Produces [tickCount] evenly spaced tick values from [minValue] to [maxValue], both endpoints
 * included. The last tick is exactly [maxValue] regardless of floating-point accumulation.
 */
internal fun gaugeTickValues(
    minValue: Float,
    maxValue: Float,
    tickCount: Int,
): List<Float> {
    require(tickCount >= 2) {
        "tickCount ($tickCount) must be at least 2"
    }
    require(maxValue > minValue) {
        "maxValue ($maxValue) must be greater than minValue ($minValue)"
    }
    val step = (maxValue - minValue) / (tickCount - 1)
    return List(tickCount) { index ->
        if (index == tickCount - 1) {
            maxValue
        } else {
            minValue + step * index
        }
    }
}
