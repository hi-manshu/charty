package com.himanshoe.charty.bar

import androidx.compose.ui.geometry.Offset
import com.himanshoe.charty.bar.config.ComparisonBarSegment
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.bar.internal.bar.comparison.comparisonCrosshairAnchors
import com.himanshoe.charty.bar.internal.bar.comparison.comparisonCrosshairSegments
import com.himanshoe.charty.bar.internal.bar.lollipop.lollipopCrosshairAnchors
import com.himanshoe.charty.bar.internal.bar.recordSeriesCrosshairBounds
import com.himanshoe.charty.bar.internal.bar.waterfall.calculateCumulativeValues
import com.himanshoe.charty.bar.internal.bar.waterfall.waterfallCrosshairAnchors
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.gesture.findNearestPointAlongCategoryAxis
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val PIXEL_TOLERANCE = 0.001f

class BarFamilyCrosshairTest {
    private val chartContext =
        ChartContext(
            left = 0f,
            top = 0f,
            right = 100f,
            bottom = 100f,
            minValue = 0f,
            maxValue = 100f,
        )

    private fun bars(vararg values: Float): List<BarData> =
        values.mapIndexed { index, value -> BarData(label = "bar$index", value = value) }

    @Test
    fun lollipopAnchors_sitAtTheCentreOfEachHead() {
        val anchors = lollipopCrosshairAnchors(chartContext = chartContext, values = listOf(0f, 50f, 100f))
        assertEquals(Offset(x = 100f / 6f, y = 100f), anchors[0])
        assertEquals(Offset(x = 50f, y = 50f), anchors[1])
        assertEquals(Offset(x = 500f / 6f, y = 0f), anchors[2])
    }

    @Test
    fun lollipopSnapping_ignoresTheYPosition() {
        val bounds = mutableListOf<Pair<Offset, BarData>>()
        val dataList = bars(10f, 90f)
        recordSeriesCrosshairBounds(
            bounds = bounds,
            anchors = lollipopCrosshairAnchors(chartContext = chartContext, values = listOf(10f, 90f)),
            items = dataList,
        )
        val snapped =
            findNearestPointAlongCategoryAxis(
                position = Offset(x = 80f, y = 0f),
                pointBounds = bounds,
                orientation = ChartOrientation.VERTICAL,
            )
        assertEquals("bar1", snapped?.second?.label)
    }

    @Test
    fun waterfallAnchors_sitOnTheTopEdgeOfEachFloatingBar() {
        val cumulative = calculateCumulativeValues(bars(60f, -20f, 30f))
        val anchors = waterfallCrosshairAnchors(chartContext = chartContext, cumulativeValues = cumulative)
        assertEquals(40f, anchors[0].y, PIXEL_TOLERANCE)
        assertEquals(40f, anchors[1].y, PIXEL_TOLERANCE)
        assertEquals(30f, anchors[2].y, PIXEL_TOLERANCE)
    }

    @Test
    fun waterfallAnchors_shareOneRowOfDistinctColumns() {
        val cumulative = calculateCumulativeValues(bars(10f, 10f, 10f))
        val anchors = waterfallCrosshairAnchors(chartContext = chartContext, cumulativeValues = cumulative)
        assertTrue(anchors[0].x < anchors[1].x && anchors[1].x < anchors[2].x)
    }

    @Test
    fun waterfallSnapping_clampsOutsideThePlot() {
        val dataList = bars(60f, -20f, 30f)
        val bounds = mutableListOf<Pair<Offset, BarData>>()
        recordSeriesCrosshairBounds(
            bounds = bounds,
            anchors =
                waterfallCrosshairAnchors(
                    chartContext = chartContext,
                    cumulativeValues = calculateCumulativeValues(dataList),
                ),
            items = dataList,
        )
        assertEquals(
            "bar0",
            findNearestPointAlongCategoryAxis(
                position = Offset(x = -500f, y = 0f),
                pointBounds = bounds,
                orientation = ChartOrientation.VERTICAL,
            )?.second?.label,
        )
        assertEquals(
            "bar2",
            findNearestPointAlongCategoryAxis(
                position = Offset(x = 500f, y = 0f),
                pointBounds = bounds,
                orientation = ChartOrientation.VERTICAL,
            )?.second?.label,
        )
    }

    @Test
    fun comparisonAnchors_sitLevelWithEachGroupsTallestBar() {
        val anchors = comparisonCrosshairAnchors(chartContext = chartContext, dataList = groups())
        assertEquals(Offset(x = 100f / 6f, y = 20f), anchors[0])
        assertEquals(Offset(x = 50f, y = 60f), anchors[1])
        assertEquals(Offset(x = 500f / 6f, y = 90f), anchors[2])
    }

    @Test
    fun comparisonSegments_reportEachGroupsTallestBar() {
        val segments = comparisonCrosshairSegments(groups())
        assertEquals(listOf(1, 0, 1), segments.map { it.barIndex })
        assertEquals(listOf(80f, 40f, 10f), segments.map { it.barValue })
        assertEquals(listOf("q0", "q1", "q2"), segments.map { it.barGroup.label })
    }

    @Test
    fun comparisonSegments_resolveATieToTheEarlierBar() {
        val tied = listOf(BarGroup(label = "tie", values = listOf(30f, 30f)))
        assertEquals(0, comparisonCrosshairSegments(tied).single().barIndex)
    }

    @Test
    fun comparisonSnapping_landsOnAGroupRatherThanABar() {
        val dataList = groups()
        val bounds = mutableListOf<Pair<Offset, ComparisonBarSegment>>()
        recordSeriesCrosshairBounds(
            bounds = bounds,
            anchors = comparisonCrosshairAnchors(chartContext = chartContext, dataList = dataList),
            items = comparisonCrosshairSegments(dataList),
        )
        val snapped =
            findNearestPointAlongCategoryAxis(
                position = Offset(x = 49f, y = 99f),
                pointBounds = bounds,
                orientation = ChartOrientation.VERTICAL,
            )
        assertEquals("q1", snapped?.second?.barGroup?.label)
        assertEquals(0, snapped?.second?.barIndex)
    }

    @Test
    fun recordedBounds_pairEachAnchorWithItsItemAndAreReplacedEachPass() {
        val bounds = mutableListOf<Pair<Offset, BarData>>()
        val dataList = bars(10f, 20f)
        repeat(3) {
            recordSeriesCrosshairBounds(
                bounds = bounds,
                anchors = lollipopCrosshairAnchors(chartContext = chartContext, values = listOf(10f, 20f)),
                items = dataList,
            )
        }
        assertEquals(2, bounds.size)
        assertEquals(dataList, bounds.map { it.second })
    }

    @Test
    fun recordedBounds_stopAtTheShorterOfAnchorsAndItems() {
        val bounds = mutableListOf<Pair<Offset, BarData>>()
        recordSeriesCrosshairBounds(
            bounds = bounds,
            anchors = lollipopCrosshairAnchors(chartContext = chartContext, values = listOf(10f, 20f, 30f)),
            items = bars(10f, 20f),
        )
        assertEquals(2, bounds.size)
    }

    private fun groups(): List<BarGroup> =
        listOf(
            BarGroup(label = "q0", values = listOf(20f, 80f)),
            BarGroup(label = "q1", values = listOf(40f, 15f)),
            BarGroup(label = "q2", values = listOf(5f, 10f)),
        )
}
