package com.himanshoe.charty.candlestick.config

import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.CornerRadius

/**
 * Configuration for Candlestick Chart appearance and behavior
 *
 * @param candleWidthFraction Fraction of available space that each candle occupies (0.0f - 1.0f)
 * @param wickWidthFraction Fraction of candle width that the wick line occupies (0.0f - 1.0f)
 * @param minCandleBodyHeight Minimum height for candle body in pixels (for doji candles)
 * @param showWicks Whether to show upper and lower wicks
 * @param cornerRadius Corner radius for all corners of candle body (None, Small, Medium, Large, ExtraLarge, or Custom)
 * @param animation Animation configuration (Disabled or Enabled with duration)
 */
data class CandlestickChartConfig(
    val candleWidthFraction: Float = 0.7f,
    val wickWidthFraction: Float = 0.1f,
    val minCandleBodyHeight: Float = 2f,
    val showWicks: Boolean = true,
    val cornerRadius: CornerRadius = CornerRadius.None,
    val animation: Animation = Animation.Default
) {
    init {
        require(candleWidthFraction in 0f..1f) { "Candle width fraction must be between 0 and 1" }
        require(wickWidthFraction in 0f..1f) { "Wick width fraction must be between 0 and 1" }
        require(minCandleBodyHeight >= 0f) { "Minimum candle body height must be non-negative" }
    }
}

