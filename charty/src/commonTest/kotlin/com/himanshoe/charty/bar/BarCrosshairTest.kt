package com.himanshoe.charty.bar

import androidx.compose.ui.geometry.Offset
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.internal.bar.barCrosshairAnchors
import com.himanshoe.charty.bar.internal.bar.recordBarCrosshairBounds
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.gesture.findNearestPointAlongCategoryAxis
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BarCrosshairTest {
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
    fun verticalAnchors_sitAtTheTopCentreOfEachBar() {
        val anchors =
            barCrosshairAnchors(
                chartContext = chartContext,
                values = listOf(0f, 50f, 100f),
                orientation = ChartOrientation.VERTICAL,
            )
        assertEquals(Offset(x = 100f / 6f, y = 100f), anchors[0])
        assertEquals(Offset(x = 50f, y = 50f), anchors[1])
        assertEquals(Offset(x = 500f / 6f, y = 0f), anchors[2])
    }

    @Test
    fun horizontalAnchors_sitAtTheValueEndAndRowCentre() {
        val anchors =
            barCrosshairAnchors(
                chartContext = chartContext,
                values = listOf(20f, 80f),
                orientation = ChartOrientation.HORIZONTAL,
            )
        assertEquals(Offset(x = 20f, y = 25f), anchors[0])
        assertEquals(Offset(x = 80f, y = 75f), anchors[1])
    }

    @Test
    fun verticalAnchors_shareOneRowOfDistinctColumns() {
        val anchors =
            barCrosshairAnchors(
                chartContext = chartContext,
                values = listOf(10f, 10f, 10f),
                orientation = ChartOrientation.VERTICAL,
            )
        assertTrue(anchors[0].x < anchors[1].x && anchors[1].x < anchors[2].x)
        assertTrue(anchors.all { it.y == anchors[0].y })
    }

    @Test
    fun horizontalAnchors_shareOneColumnOfDistinctRows() {
        val anchors =
            barCrosshairAnchors(
                chartContext = chartContext,
                values = listOf(10f, 10f, 10f),
                orientation = ChartOrientation.HORIZONTAL,
            )
        assertTrue(anchors[0].y < anchors[1].y && anchors[1].y < anchors[2].y)
        assertTrue(anchors.all { it.x == anchors[0].x })
    }

    @Test
    fun recordedBounds_pairEachAnchorWithItsBar() {
        val bounds = mutableListOf<Pair<Offset, BarData>>()
        val dataList = bars(10f, 20f, 30f)
        recordBarCrosshairBounds(
            bounds = bounds,
            dataList = dataList,
            values = listOf(10f, 20f, 30f),
            chartContext = chartContext,
            orientation = ChartOrientation.VERTICAL,
        )
        assertEquals(3, bounds.size)
        assertEquals(dataList, bounds.map { it.second })
    }

    @Test
    fun recordedBounds_areReplacedRatherThanAppendedOnEveryDrawPass() {
        val bounds = mutableListOf<Pair<Offset, BarData>>()
        repeat(3) {
            recordBarCrosshairBounds(
                bounds = bounds,
                dataList = bars(10f, 20f),
                values = listOf(10f, 20f),
                chartContext = chartContext,
                orientation = ChartOrientation.VERTICAL,
            )
        }
        assertEquals(2, bounds.size)
    }

    @Test
    fun snapping_onAVerticalChartIgnoresTheYPosition() {
        val bounds = verticalBounds()
        val snapped =
            findNearestPointAlongCategoryAxis(
                position = Offset(x = 51f, y = 0f),
                pointBounds = bounds,
                orientation = ChartOrientation.VERTICAL,
            )
        assertEquals("bar1", snapped?.second?.label)
    }

    @Test
    fun snapping_onAHorizontalChartRunsAlongY() {
        val bounds = horizontalBounds()
        val snapped =
            findNearestPointAlongCategoryAxis(
                position = Offset(x = 0f, y = 74f),
                pointBounds = bounds,
                orientation = ChartOrientation.HORIZONTAL,
            )
        assertEquals("bar1", snapped?.second?.label)
    }

    @Test
    fun snapping_onAHorizontalChartIgnoresTheXPosition() {
        val bounds = horizontalBounds()
        val farRight =
            findNearestPointAlongCategoryAxis(
                position = Offset(x = 1000f, y = 25f),
                pointBounds = bounds,
                orientation = ChartOrientation.HORIZONTAL,
            )
        assertEquals("bar0", farRight?.second?.label)
    }

    @Test
    fun snapping_clampsToTheFirstItemBeforeTheChart() {
        assertEquals(
            "bar0",
            findNearestPointAlongCategoryAxis(
                position = Offset(x = -500f, y = -500f),
                pointBounds = verticalBounds(),
                orientation = ChartOrientation.VERTICAL,
            )?.second?.label,
        )
    }

    @Test
    fun snapping_clampsToTheLastItemBeyondTheChart() {
        assertEquals(
            "bar1",
            findNearestPointAlongCategoryAxis(
                position = Offset(x = 500f, y = 500f),
                pointBounds = horizontalBounds(),
                orientation = ChartOrientation.HORIZONTAL,
            )?.second?.label,
        )
    }

    @Test
    fun snapping_resolvesAnExactTieToTheEarlierBar() {
        val bounds =
            listOf(
                Offset(x = 0f, y = 0f) to BarData(label = "bar0", value = 1f),
                Offset(x = 100f, y = 0f) to BarData(label = "bar1", value = 1f),
            )
        val snapped =
            findNearestPointAlongCategoryAxis(
                position = Offset(x = 50f, y = 0f),
                pointBounds = bounds,
                orientation = ChartOrientation.VERTICAL,
            )
        assertEquals("bar0", snapped?.second?.label)
    }

    @Test
    fun snapping_returnsNothingWhenNoBarsHaveBeenDrawn() {
        assertNull(
            findNearestPointAlongCategoryAxis(
                position = Offset(x = 10f, y = 10f),
                pointBounds = emptyList<Pair<Offset, BarData>>(),
                orientation = ChartOrientation.VERTICAL,
            ),
        )
    }

    private fun verticalBounds(): List<Pair<Offset, BarData>> {
        val bounds = mutableListOf<Pair<Offset, BarData>>()
        recordBarCrosshairBounds(
            bounds = bounds,
            dataList = bars(10f, 90f),
            values = listOf(10f, 90f),
            chartContext = chartContext,
            orientation = ChartOrientation.VERTICAL,
        )
        return bounds
    }

    private fun horizontalBounds(): List<Pair<Offset, BarData>> {
        val bounds = mutableListOf<Pair<Offset, BarData>>()
        recordBarCrosshairBounds(
            bounds = bounds,
            dataList = bars(10f, 90f),
            values = listOf(10f, 90f),
            chartContext = chartContext,
            orientation = ChartOrientation.HORIZONTAL,
        )
        return bounds
    }
}
