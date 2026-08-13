package com.himanshoe.charty.candlestick

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.himanshoe.charty.candlestick.config.CandlestickChartConfig
import com.himanshoe.charty.candlestick.data.CandleData
import com.himanshoe.charty.candlestick.internal.candleHitRects
import com.himanshoe.charty.candlestick.internal.formatCandleTooltip
import com.himanshoe.charty.candlestick.internal.populateCandleBounds
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.gesture.findClickedItemWithBounds
import com.himanshoe.charty.common.util.toChartLabel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val TOLERANCE = 0.001f

class CandlestickTooltipTest {
    private val context =
        ChartContext(left = 0f, top = 0f, right = 100f, bottom = 100f, minValue = 0f, maxValue = 100f)

    private val candles =
        listOf(
            CandleData(label = "Mon", open = 20f, high = 40f, low = 10f, close = 30f),
            CandleData(label = "Tue", open = 70f, high = 90f, low = 60f, close = 65f),
        )

    @Test
    fun defaultFormatterShowsAllFourPrices() {
        assertEquals("Mon  O 20 H 40 L 10 C 30", formatCandleTooltip(candles[0]))
        assertEquals("Mon  O 20 H 40 L 10 C 30", CandlestickChartConfig().tooltipFormatter(candles[0]))
    }

    @Test
    fun defaultFormatterRoundsFractionalPrices() {
        val candle = CandleData(label = "Wed", open = 1.24f, high = 2.5f, low = 1f, close = 2f)
        assertEquals("Wed  O 1.2 H 2.5 L 1 C 2", formatCandleTooltip(candle))
    }

    @Test
    fun customFormatterReceivesTheWholeCandle() {
        val config =
            CandlestickChartConfig(tooltipFormatter = { candle ->
                "${candle.label} ${candle.close.toChartLabel()}"
            })
        assertEquals("Mon 30", config.tooltipFormatter(candles[0]))
    }

    @Test
    fun hitRectsSpanHighToLowSoBodyAndWicksShareOneArea() {
        val rects = candleHitRects(chartContext = context, dataList = candles, candleWidthFraction = 1f)
        assertEquals(2, rects.size)
        assertEquals(60f, rects[0].top, TOLERANCE)
        assertEquals(90f, rects[0].bottom, TOLERANCE)
        assertEquals(0f, rects[0].left, TOLERANCE)
        assertEquals(50f, rects[0].right, TOLERANCE)
    }

    @Test
    fun aTapOnAWickResolvesToItsCandle() {
        val bounds = mutableListOf<Pair<Rect, CandleData>>()
        populateCandleBounds(
            chartContext = context,
            dataList = candles,
            enabled = true,
            candleWidthFraction = 1f,
            candleBounds = bounds,
        )
        val hit = findClickedItemWithBounds(offset = Offset(x = 75f, y = 15f), bounds = bounds)
        assertEquals(candles[1], hit?.second)
    }

    @Test
    fun aTapBelowEveryCandleHitsNothing() {
        val bounds = mutableListOf<Pair<Rect, CandleData>>()
        populateCandleBounds(
            chartContext = context,
            dataList = candles,
            enabled = true,
            candleWidthFraction = 1f,
            candleBounds = bounds,
        )
        assertNull(findClickedItemWithBounds(offset = Offset(x = 75f, y = 100f), bounds = bounds, hitSlop = 0f))
    }

    @Test
    fun boundsAreNotCollectedWhenNothingHitTestsThem() {
        val bounds = mutableListOf<Pair<Rect, CandleData>>()
        populateCandleBounds(
            chartContext = context,
            dataList = candles,
            enabled = false,
            candleWidthFraction = 1f,
            candleBounds = bounds,
        )
        assertTrue(bounds.isEmpty())
    }
}
