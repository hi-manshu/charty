package com.himanshoe.charty.bar.internal.bar.waterfall

import androidx.compose.ui.util.fastForEach
import com.himanshoe.charty.bar.data.BarData

/**
 * Calculate cumulative values for waterfall chart
 */
internal fun calculateCumulativeValues(items: List<BarData>): List<Float> {
    val result = mutableListOf<Float>()
    var running = 0f
    items.fastForEach { bar ->
        running += bar.value
        result += running
    }
    return result
}

/**
 * Values marking the top edge of each floating bar: the higher of the running total before the step
 * and after it, so a marker anchored on it sits above the bar for gains and losses alike.
 *
 * @param cumulativeValues The running totals produced by [calculateCumulativeValues].
 * @return One value per bar, in the same order.
 */
internal fun calculateWaterfallBarTopValues(cumulativeValues: List<Float>): List<Float> =
    List(cumulativeValues.size) { index ->
        val previous =
            if (index == 0) {
                0f
            } else {
                cumulativeValues[index - 1]
            }
        maxOf(previous, cumulativeValues[index])
    }

/**
 * Calculate min and max values for waterfall chart axis
 */
internal fun calculateWaterfallRange(cumulativeValues: List<Float>): Pair<Float, Float> {
    val minValue = (cumulativeValues.minOrNull() ?: 0f).coerceAtMost(0f)
    val maxValue = (cumulativeValues.maxOrNull() ?: 0f).coerceAtLeast(0f)
    return minValue to maxValue
}
