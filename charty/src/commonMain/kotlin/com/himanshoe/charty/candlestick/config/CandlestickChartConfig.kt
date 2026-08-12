package com.himanshoe.charty.candlestick.config

import androidx.compose.runtime.Stable
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.CornerRadius
import com.himanshoe.charty.common.config.PersistentMarker
import com.himanshoe.charty.common.config.requireValidVisibleWindow

/**
 * Configuration for Candlestick Chart appearance and behavior
 *
 * @property candleWidthFraction Fraction of available space that each candle occupies (0.0f - 1.0f)
 * @property wickWidthFraction Fraction of candle width that the wick line occupies (0.0f - 1.0f)
 * @property minCandleBodyHeight Minimum height for candle body in pixels (for doji candles)
 * @property showWicks Whether to show upper and lower wicks
 * @property cornerRadius Corner radius for all corners of candle body (None, Small, Medium, Large, ExtraLarge, or Custom)
 * @property animation Animation configuration (Disabled or Enabled with duration)
 * @property animateValueChanges When `true`, a candle's open, high, low, and close all tween from
 *   their previous prices to the new ones whenever the data changes (using [animation]); the four
 *   prices share one progress, so a candle stays internally consistent for the whole transition. When
 *   `false` (default) new data appears instantly. Has no effect if [animation] is
 *   [Animation.Disabled].
 * @property markers Persistent markers pinned to specific candles, drawn at all times regardless of
 *   touch (see [PersistentMarker]). A marker is anchored on the candle's close price, centred on the
 *   candle, and its default label is that close. `PersistentMarker(dataIndex = -1)` is the idiomatic
 *   way to label the latest value — the rightmost candle. Empty (the default) draws none.
 * @property visibleWindow Rolling "show last N" window; `null` (default) shows every point and
 *   changes nothing. As data is appended the window advances to the latest. Must be `>= 2`.
 */
@Stable
data class CandlestickChartConfig(
    val candleWidthFraction: Float = 0.7f,
    val wickWidthFraction: Float = 0.1f,
    val minCandleBodyHeight: Float = 2f,
    val showWicks: Boolean = true,
    val cornerRadius: CornerRadius = CornerRadius.None,
    val animation: Animation = Animation.Default,
    val animateValueChanges: Boolean = false,
    val markers: List<PersistentMarker> = emptyList(),
    val visibleWindow: Int? = null,
) {
    init {
        requireValidVisibleWindow(visibleWindow)
        require(candleWidthFraction in 0f..1f) { "Candle width fraction must be between 0 and 1" }
        require(wickWidthFraction in 0f..1f) { "Wick width fraction must be between 0 and 1" }
        require(minCandleBodyHeight >= 0f) { "Minimum candle body height must be non-negative" }
    }
}
