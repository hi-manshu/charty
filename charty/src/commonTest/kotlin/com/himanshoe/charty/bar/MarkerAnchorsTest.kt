package com.himanshoe.charty.bar

import com.himanshoe.charty.bar.internal.bar.horizontalBarMarkerPositions
import com.himanshoe.charty.bar.internal.bar.verticalBarMarkerPositions
import com.himanshoe.charty.bar.internal.bar.waterfall.calculateWaterfallBarTopValues
import com.himanshoe.charty.bar.internal.span.spanEndMarkerPositions
import com.himanshoe.charty.common.ChartContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MarkerAnchorsTest {
    private val chartContext =
        ChartContext(
            left = 0f,
            top = 0f,
            right = 100f,
            bottom = 100f,
            minValue = 0f,
            maxValue = 100f,
        )

    @Test
    fun verticalPositions_areOneEntryPerValue() {
        val positions = verticalBarMarkerPositions(chartContext = chartContext, values = listOf(10f, 50f, 90f))
        assertEquals(3, positions.size)
    }

    @Test
    fun verticalPositions_placeHigherValuesHigher() {
        val positions = verticalBarMarkerPositions(chartContext = chartContext, values = listOf(10f, 90f))
        assertTrue(positions[1].y < positions[0].y, "a larger value must sit closer to the top")
    }

    @Test
    fun verticalPositions_runLeftToRight() {
        val positions = verticalBarMarkerPositions(chartContext = chartContext, values = listOf(1f, 2f, 3f))
        assertTrue(positions[0].x < positions[1].x && positions[1].x < positions[2].x)
    }

    @Test
    fun verticalPositions_lastEntryIsRightmost() {
        val positions = verticalBarMarkerPositions(chartContext = chartContext, values = listOf(1f, 2f, 3f))
        assertEquals(positions.maxOf { it.x }, positions.last().x)
    }

    @Test
    fun horizontalPositions_placeLargerValuesFurtherRight() {
        val positions =
            horizontalBarMarkerPositions(
                chartContext = chartContext,
                values = listOf(20f, 80f),
                minValue = 0f,
                maxValue = 100f,
            )
        assertEquals(20f, positions[0].x)
        assertEquals(80f, positions[1].x)
    }

    @Test
    fun horizontalPositions_lastEntryIsBottomRow() {
        val positions =
            horizontalBarMarkerPositions(
                chartContext = chartContext,
                values = listOf(10f, 20f, 30f),
                minValue = 0f,
                maxValue = 100f,
            )
        assertEquals(positions.maxOf { it.y }, positions.last().y)
    }

    @Test
    fun horizontalPositions_centreEachRow() {
        val positions =
            horizontalBarMarkerPositions(
                chartContext = chartContext,
                values = listOf(0f, 0f),
                minValue = 0f,
                maxValue = 100f,
            )
        assertEquals(25f, positions[0].y)
        assertEquals(75f, positions[1].y)
    }

    @Test
    fun horizontalPositions_collapseToAxisWhenRangeIsEmpty() {
        val positions =
            horizontalBarMarkerPositions(
                chartContext = chartContext,
                values = listOf(5f, 5f),
                minValue = 5f,
                maxValue = 5f,
            )
        assertTrue(positions.all { it.x == chartContext.left })
    }

    @Test
    fun waterfallTops_useTheHigherEndOfEachStep() {
        val tops = calculateWaterfallBarTopValues(listOf(100f, 140f, 110f))
        assertEquals(listOf(100f, 140f, 140f), tops)
    }

    @Test
    fun waterfallTops_treatTheFirstStepAsStartingFromZero() {
        val tops = calculateWaterfallBarTopValues(listOf(-30f, -10f))
        assertEquals(listOf(0f, -10f), tops)
    }

    @Test
    fun spanPositions_anchorOnTheEndValueAndRowCentre() {
        val positions =
            spanEndMarkerPositions(
                chartContext = chartContext,
                endValues = listOf(50f, 100f),
                minValue = 0f,
                maxValue = 100f,
                axisOffset = 0f,
            )
        assertEquals(50f, positions[0].x)
        assertEquals(100f, positions[1].x)
        assertEquals(25f, positions[0].y)
        assertEquals(75f, positions[1].y)
    }

    @Test
    fun spanPositions_respectTheAxisInset() {
        val positions =
            spanEndMarkerPositions(
                chartContext = chartContext,
                endValues = listOf(0f),
                minValue = 0f,
                maxValue = 100f,
                axisOffset = 20f,
            )
        assertEquals(20f, positions[0].x)
    }
}
