package com.himanshoe.charty.candlestick.internal

import androidx.compose.ui.util.fastMapIndexed

/**
 * Calculate the optimized labels for x-axis
 * If there are too many labels, sample them to avoid overcrowding
 */
internal fun calculateOptimizedLabels(allLabels: List<String>): List<String> {
    return if (allLabels.size > CandlestickChartConstants.MAX_LABELS_DISPLAYED) {
        val indices = (0 until CandlestickChartConstants.LABEL_SAMPLE_COUNT).map { i ->
            (i * (allLabels.size - 1)) / CandlestickChartConstants.LABEL_DIVISOR
        }
        allLabels.fastMapIndexed { index, label ->
            if (index in indices) label else ""
        }
    } else {
        allLabels
    }
}

