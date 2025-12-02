package com.himanshoe.charty.bar.internal.bar.waterfall

import com.himanshoe.charty.bar.data.BarData

/**
 * Calculate cumulative values for waterfall chart
 */
internal fun calculateCumulativeValues(items: List<BarData>): List<Float> {
    val result = mutableListOf<Float>()
    var running = 0f
    items.forEach { bar ->
        running += bar.value
        result += running
    }
    return result
}

/**
 * Calculate min and max values for waterfall chart axis
 */
internal fun calculateWaterfallRange(cumulativeValues: List<Float>): Pair<Float, Float> {
    val minValue = (cumulativeValues.minOrNull() ?: 0f).coerceAtMost(0f)
    val maxValue = (cumulativeValues.maxOrNull() ?: 0f).coerceAtLeast(0f)
    return minValue to maxValue
}

