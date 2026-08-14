package com.himanshoe.charty.candlestick.internal

/**
 * Constants used in candlestick chart calculations
 */
internal object CandlestickChartConstants {
    /** Above this many candles the x-axis is sampled rather than labelled in full. */
    const val MAX_LABELS_DISPLAYED = 10

    /** How many labels a sampled axis keeps: the two ends and the three quarter-points between them. */
    const val SAMPLED_LABEL_COUNT = 5

    const val DEFAULT_BULLISH_COLOR = 0xFF4CAF50
    const val DEFAULT_BEARISH_COLOR = 0xFFF44336
}
