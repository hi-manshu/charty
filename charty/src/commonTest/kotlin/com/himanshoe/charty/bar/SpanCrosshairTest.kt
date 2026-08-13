package com.himanshoe.charty.bar

import androidx.compose.ui.geometry.Offset
import com.himanshoe.charty.bar.data.SpanData
import com.himanshoe.charty.bar.internal.bar.recordSeriesCrosshairBounds
import com.himanshoe.charty.bar.internal.span.formatSpanCrosshairLabel
import com.himanshoe.charty.bar.internal.span.spanCrosshairAnchors
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.gesture.findNearestPointAlongCategoryAxis
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SpanCrosshairTest {
    private val chartContext =
        ChartContext(
            left = 0f,
            top = 0f,
            right = 100f,
            bottom = 100f,
            minValue = 0f,
            maxValue = 100f,
        )

    private val spans =
        listOf(
            SpanData(label = "span0", startValue = 10f, endValue = 20f),
            SpanData(label = "span1", startValue = 30f, endValue = 80f),
        )

    @Test
    fun anchors_sitAtEachSpansEndAndRowCentre() {
        val anchors = anchors()
        assertEquals(Offset(x = 20f, y = 25f), anchors[0])
        assertEquals(Offset(x = 80f, y = 75f), anchors[1])
    }

    @Test
    fun anchors_shiftRightByTheAxisInset() {
        val inset =
            spanCrosshairAnchors(
                chartContext = chartContext,
                endValues = listOf(0f, 100f),
                minValue = 0f,
                maxValue = 100f,
                axisOffset = 20f,
            )
        assertEquals(20f, inset[0].x)
        assertEquals(100f, inset[1].x)
    }

    @Test
    fun anchors_collapseToTheAxisWhenEveryValueIsTheSame() {
        val flat =
            spanCrosshairAnchors(
                chartContext = chartContext,
                endValues = listOf(50f, 50f, 50f),
                minValue = 50f,
                maxValue = 50f,
                axisOffset = 0f,
            )
        assertTrue(flat.all { it.x == 0f })
        assertTrue(flat[0].y < flat[1].y && flat[1].y < flat[2].y)
    }

    @Test
    fun snapping_runsAlongYBecauseTheChartIsHorizontal() {
        val snapped =
            findNearestPointAlongCategoryAxis(
                position = Offset(x = 0f, y = 74f),
                pointBounds = bounds(),
                orientation = ChartOrientation.HORIZONTAL,
            )
        assertEquals("span1", snapped?.second?.label)
    }

    @Test
    fun snapping_ignoresTheXPosition() {
        val snapped =
            findNearestPointAlongCategoryAxis(
                position = Offset(x = 1000f, y = 25f),
                pointBounds = bounds(),
                orientation = ChartOrientation.HORIZONTAL,
            )
        assertEquals("span0", snapped?.second?.label)
    }

    @Test
    fun snapping_clampsAboveAndBelowThePlot() {
        assertEquals(
            "span0",
            findNearestPointAlongCategoryAxis(
                position = Offset(x = 0f, y = -500f),
                pointBounds = bounds(),
                orientation = ChartOrientation.HORIZONTAL,
            )?.second?.label,
        )
        assertEquals(
            "span1",
            findNearestPointAlongCategoryAxis(
                position = Offset(x = 0f, y = 500f),
                pointBounds = bounds(),
                orientation = ChartOrientation.HORIZONTAL,
            )?.second?.label,
        )
    }

    @Test
    fun snapping_resolvesAnExactTieToTheEarlierSpan() {
        val snapped =
            findNearestPointAlongCategoryAxis(
                position = Offset(x = 0f, y = 50f),
                pointBounds = bounds(),
                orientation = ChartOrientation.HORIZONTAL,
            )
        assertEquals("span0", snapped?.second?.label)
    }

    @Test
    fun snapping_returnsNothingWhenNoSpansHaveBeenDrawn() {
        assertNull(
            findNearestPointAlongCategoryAxis(
                position = Offset(x = 10f, y = 10f),
                pointBounds = emptyList<Pair<Offset, SpanData>>(),
                orientation = ChartOrientation.HORIZONTAL,
            ),
        )
    }

    @Test
    fun label_readsTheWholeRange() {
        assertEquals("span1: 30 - 80", formatSpanCrosshairLabel(spans[1]))
    }

    private fun anchors(): List<Offset> =
        spanCrosshairAnchors(
            chartContext = chartContext,
            endValues = spans.map { it.endValue },
            minValue = 0f,
            maxValue = 100f,
            axisOffset = 0f,
        )

    private fun bounds(): List<Pair<Offset, SpanData>> {
        val bounds = mutableListOf<Pair<Offset, SpanData>>()
        recordSeriesCrosshairBounds(bounds = bounds, anchors = anchors(), items = spans)
        return bounds
    }
}
