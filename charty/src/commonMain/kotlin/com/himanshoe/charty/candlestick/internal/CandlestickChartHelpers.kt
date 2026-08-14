package com.himanshoe.charty.candlestick.internal

import androidx.compose.ui.util.fastMapIndexed

/**
 * Blanks out all but a handful of evenly spaced x-axis labels once there are too many candles to
 * label every one.
 *
 * The kept labels are the first, the last, and the quarter-points between — which is why the divisor
 * is one less than the count rather than a number of its own. It used to be a second constant, and a
 * pair like that only stays correct while nobody edits half of it: raising the count alone pushes the
 * final index past the end of the list, where it matches nothing and the label it was meant to keep
 * disappears with no other symptom.
 *
 * Blanks are kept rather than removed so that each label still lines up with the candle it belongs to.
 */
internal fun calculateOptimizedLabels(allLabels: List<String>): List<String> {
    if (allLabels.size <= CandlestickChartConstants.MAX_LABELS_DISPLAYED) {
        return allLabels
    }
    val lastIndex = allLabels.size - 1
    val gaps = CandlestickChartConstants.SAMPLED_LABEL_COUNT - 1
    val kept = (0..gaps).map { step -> (step * lastIndex) / gaps }.toSet()
    return allLabels.fastMapIndexed { index, label ->
        if (index in kept) {
            label
        } else {
            ""
        }
    }
}
