package com.himanshoe.charty.bar

import androidx.compose.ui.geometry.Offset
import com.himanshoe.charty.bar.config.BubbleBarChartConfig
import com.himanshoe.charty.bar.config.WavyChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.internal.bar.wavy.populateWavyBarBounds
import com.himanshoe.charty.bar.internal.bar.wavy.wavyBarHitRects
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.baselineY
import com.himanshoe.charty.common.gesture.findClickedItemWithBounds
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val TOLERANCE = 0.001f

class WavyTooltipTest {
    private val context =
        ChartContext(left = 0f, top = 0f, right = 100f, bottom = 100f, minValue = 0f, maxValue = 100f)

    private val bars =
        listOf(
            BarData(label = "Mon", value = 100f),
            BarData(label = "Tue", value = 50f),
        )

    @Test
    fun hitRects_spanTheFullColumnSlotOfEachWave() {
        val rects =
            wavyBarHitRects(
                chartContext = context,
                values = listOf(100f, 50f),
                minValue = 0f,
                strokeWidthPx = 0f,
            )
        assertEquals(2, rects.size)
        assertEquals(0f, rects[0].left, TOLERANCE)
        assertEquals(50f, rects[0].right, TOLERANCE)
        assertEquals(50f, rects[1].left, TOLERANCE)
        assertEquals(100f, rects[1].right, TOLERANCE)
    }

    @Test
    fun hitRects_spanValueToBaselineAndGrowByTheStroke() {
        val rects =
            wavyBarHitRects(
                chartContext = context,
                values = listOf(100f),
                minValue = 0f,
                strokeWidthPx = 4f,
            )
        assertEquals(-2f, rects[0].top, TOLERANCE)
        assertEquals(102f, rects[0].bottom, TOLERANCE)
    }

    @Test
    fun hitRects_measureFromTheZeroLineWhenValuesGoNegative() {
        val negativeContext =
            ChartContext(left = 0f, top = 0f, right = 100f, bottom = 100f, minValue = -100f, maxValue = 100f)
        assertEquals(50f, negativeContext.baselineY(minValue = -100f, drawNegativesInPlace = true), TOLERANCE)
        val rects =
            wavyBarHitRects(
                chartContext = negativeContext,
                values = listOf(-100f),
                minValue = -100f,
                strokeWidthPx = 0f,
            )
        assertEquals(50f, rects[0].top, TOLERANCE)
        assertEquals(100f, rects[0].bottom, TOLERANCE)
    }

    @Test
    fun aTapInsideAColumnResolvesToThatWave() {
        val bounds = mutableListOf<Pair<androidx.compose.ui.geometry.Rect, BarData>>()
        populateWavyBarBounds(
            chartContext = context,
            dataList = bars,
            enabled = true,
            minValue = 0f,
            strokeWidthPx = 2f,
            barBounds = bounds,
        )
        assertEquals(2, bounds.size)
        val hit = findClickedItemWithBounds(offset = Offset(x = 60f, y = 80f), bounds = bounds)
        assertEquals(bars[1], hit?.second)
    }

    @Test
    fun aTapAboveEveryWaveHitsNothing() {
        val bounds = mutableListOf<Pair<androidx.compose.ui.geometry.Rect, BarData>>()
        populateWavyBarBounds(
            chartContext = context,
            dataList = listOf(BarData(label = "Tue", value = 20f)),
            enabled = true,
            minValue = 0f,
            strokeWidthPx = 2f,
            barBounds = bounds,
        )
        assertNull(findClickedItemWithBounds(offset = Offset(x = 50f, y = 0f), bounds = bounds))
    }

    @Test
    fun boundsAreNotCollectedWhenNothingHitTestsThem() {
        val bounds = mutableListOf<Pair<androidx.compose.ui.geometry.Rect, BarData>>()
        populateWavyBarBounds(
            chartContext = context,
            dataList = bars,
            enabled = false,
            minValue = 0f,
            strokeWidthPx = 2f,
            barBounds = bounds,
        )
        assertTrue(bounds.isEmpty())
    }

    @Test
    fun wavyDefaultFormatterReadsLabelAndValue() {
        assertEquals("Mon: 100", WavyChartConfig().tooltipFormatter(bars[0]))
        assertEquals("Tue: 12.5", WavyChartConfig().tooltipFormatter(BarData(label = "Tue", value = 12.5f)))
    }

    @Test
    fun bubbleBarDefaultFormatterReadsLabelAndValue() {
        assertEquals("Mon: 100", BubbleBarChartConfig().tooltipFormatter(bars[0]))
    }
}
