package com.himanshoe.charty.candlestick.internal

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.candlestick.config.CandlestickChartConfig
import com.himanshoe.charty.candlestick.data.CandleData
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.gesture.createRectangularTooltipState
import com.himanshoe.charty.common.gesture.rectangularChartClickHandler
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.util.toChartLabel

/**
 * The click callback the tap handler is installed with. The candlestick chart exposes no click
 * callback of its own — the tap exists purely to raise the tooltip — and a single shared instance
 * keeps the tap handler's `pointerInput` key stable across recompositions.
 */
private val TooltipOnlyTap: (CandleData) -> Unit = {}

/**
 * Default tooltip text for a tapped candle: its label followed by all four prices it encodes, in the
 * conventional open-high-low-close order. A candle is one OHLC record rather than a single value, so
 * showing one of the four would be an arbitrary choice; volume is left out because it is optional and
 * measured in different units.
 *
 * @param candle The tapped candle.
 * @return The tooltip text, for example `Mon  O 100 H 110 L 95 C 105`.
 */
internal fun formatCandleTooltip(candle: CandleData): String =
    "${candle.label}  O ${candle.open.toChartLabel()} H ${candle.high.toChartLabel()} " +
        "L ${candle.low.toChartLabel()} C ${candle.close.toChartLabel()}"

/**
 * The tap hit area of every candle: the candle's own column width horizontally, and its full
 * high-to-low span vertically, so a tap anywhere on the body or either wick resolves to that candle.
 *
 * @param chartContext The pixel bounds and value range of the plotting area.
 * @param dataList The candles currently drawn, in x order.
 * @param candleWidthFraction The fraction of its slot each candle occupies.
 * @return One rect per candle, at the same indices as [dataList].
 */
internal fun candleHitRects(
    chartContext: ChartContext,
    dataList: List<CandleData>,
    candleWidthFraction: Float,
): List<Rect> {
    val candleWidth = chartContext.calculateBarWidth(dataList.size, candleWidthFraction)
    return List(dataList.size) { index ->
        val left = chartContext.calculateBarLeftPosition(index, dataList.size, candleWidthFraction)
        Rect(
            left = left,
            top = chartContext.convertValueToYPosition(dataList[index].high),
            right = left + candleWidth,
            bottom = chartContext.convertValueToYPosition(dataList[index].low),
        )
    }
}

/**
 * Refills [candleBounds] with the current frame's candle hit areas so a tap resolves against where
 * the candles actually are. Clears the bounds and stops there when [enabled] is `false`.
 *
 * @param chartContext The pixel bounds and value range of the plotting area.
 * @param dataList The candles currently drawn.
 * @param enabled Whether anything hit-tests the bounds this frame.
 * @param candleWidthFraction The fraction of its slot each candle occupies.
 * @param candleBounds The bounds list to refill, owned by the chart across frames.
 */
internal fun populateCandleBounds(
    chartContext: ChartContext,
    dataList: List<CandleData>,
    enabled: Boolean,
    candleWidthFraction: Float,
    candleBounds: MutableList<Pair<Rect, CandleData>>,
) {
    candleBounds.clear()
    if (!enabled || dataList.isEmpty()) {
        return
    }
    val rects =
        candleHitRects(
            chartContext = chartContext,
            dataList = dataList,
            candleWidthFraction = candleWidthFraction,
        )
    dataList.fastForEachIndexed { index, candle -> candleBounds.add(rects[index] to candle) }
}

/**
 * Adds the candlestick chart's tap-to-tooltip gesture, or returns the receiver untouched when
 * [enabled] is `false`.
 *
 * @param dataList The candles currently drawn, used as a recomposition key.
 * @param candlestickConfig Supplies the tooltip text, placement, and styling.
 * @param candleBounds The per-candle hit rects the tap handler tests.
 * @param onTooltipUpdate Receives the tooltip raised by a tap, and the candle it belongs to.
 * @param enabled When `false`, no tap handler is installed at all.
 * @return The [Modifier] carrying the tap handler.
 */
internal fun Modifier.candlestickTooltipHandler(
    dataList: List<CandleData>,
    candlestickConfig: CandlestickChartConfig,
    candleBounds: List<Pair<Rect, CandleData>>,
    onTooltipUpdate: (TooltipState?, CandleData?) -> Unit,
    enabled: Boolean,
): Modifier {
    if (!enabled) {
        return this
    }
    return this.rectangularChartClickHandler(
        dataList = dataList,
        bounds = candleBounds,
        onItemClick = TooltipOnlyTap,
        onTooltipStateChange = onTooltipUpdate,
        createTooltipContent = { candle, rect ->
            createRectangularTooltipState(
                content = candlestickConfig.tooltipFormatter(candle),
                rect = rect,
                position = candlestickConfig.tooltipPosition,
            )
        },
    )
}
