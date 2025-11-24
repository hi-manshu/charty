package com.himanshoe.charty.candlestick.data

/**
 * Data class representing a single candlestick in a candlestick chart
 * Commonly used for financial/stock market data visualization
 *
 * @param label The label displayed on X-axis (e.g., date/time)
 * @param open Opening price/value
 * @param high Highest price/value in the period
 * @param low Lowest price/value in the period
 * @param close Closing price/value
 * @param volume Optional volume data for this period
 */
data class CandleData(
    val label: String,
    val open: Float,
    val high: Float,
    val low: Float,
    val close: Float,
    val volume: Float? = null
) {
    init {
        require(high >= low) { "High value must be greater than or equal to low value" }
        require(open >= low && open <= high) { "Open value must be between low and high" }
        require(close >= low && close <= high) { "Close value must be between low and high" }
        if (volume != null) {
            require(volume >= 0f) { "Volume must be non-negative" }
        }
    }

    /**
     * Returns true if this candle represents a bullish period (price increased)
     * i.e., close price is higher than open price
     */
    val isBullish: Boolean
        get() = close >= open

    /**
     * Returns true if this candle represents a bearish period (price decreased)
     * i.e., close price is lower than open price
     */
    val isBearish: Boolean
        get() = close < open

    /**
     * Returns the body height (difference between open and close)
     */
    val bodyHeight: Float
        get() = kotlin.math.abs(close - open)

    /**
     * Returns the upper wick length (distance from top of body to high)
     */
    val upperWickLength: Float
        get() = high - maxOf(open, close)

    /**
     * Returns the lower wick length (distance from bottom of body to low)
     */
    val lowerWickLength: Float
        get() = minOf(open, close) - low

    /**
     * Returns true if this is a doji candle (open and close are very close)
     */
    fun isDoji(threshold: Float = 0.01f): Boolean {
        val range = high - low
        return if (range == 0f) true else bodyHeight / range < threshold
    }
}

