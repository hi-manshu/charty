package com.himanshoe.charty.gantt

import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.gantt.data.GanttRow
import com.himanshoe.charty.gantt.data.GanttSegment
import com.himanshoe.charty.gantt.internal.ganttProgressRect
import com.himanshoe.charty.gantt.internal.ganttSegmentRect
import com.himanshoe.charty.gantt.internal.ganttValueRange
import com.himanshoe.charty.gantt.internal.ganttValueToX
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

private const val TOL = 0.001f

private val context =
    ChartContext(left = 0f, top = 0f, right = 100f, bottom = 100f, minValue = 0f, maxValue = 10f)

private val rows =
    listOf(
        GanttRow(
            label = "Design",
            segments =
                listOf(
                    GanttSegment(startValue = 0f, endValue = 3f),
                    GanttSegment(startValue = 5f, endValue = 8f),
                ),
        ),
        GanttRow(label = "Build", segments = listOf(GanttSegment(startValue = 2f, endValue = 10f))),
    )

class GanttGeometryTest {
    @Test
    fun valueRange_spansTheEarliestStartToTheLatestEnd() {
        val (min, max) = ganttValueRange(rows = rows)
        assertEquals(0f, min, TOL)
        assertEquals(10f, max, TOL)
    }

    @Test
    fun valueRange_withoutSegments_fallsBackToAUnitRange() {
        val (min, max) = ganttValueRange(rows = listOf(GanttRow(label = "idle", segments = emptyList())))
        assertEquals(0f, min, TOL)
        assertEquals(1f, max, TOL)
    }

    @Test
    fun valueToX_mapsTheRangeAcrossThePlot() {
        assertEquals(0f, ganttValueToX(value = 0f, chartContext = context, minValue = 0f, maxValue = 10f), TOL)
        assertEquals(50f, ganttValueToX(value = 5f, chartContext = context, minValue = 0f, maxValue = 10f), TOL)
        assertEquals(100f, ganttValueToX(value = 10f, chartContext = context, minValue = 0f, maxValue = 10f), TOL)
    }

    @Test
    fun valueToX_degenerateRange_collapsesOntoTheLeftEdge() {
        assertEquals(0f, ganttValueToX(value = 4f, chartContext = context, minValue = 4f, maxValue = 4f), TOL)
    }

    @Test
    fun segmentRect_placesASegmentAtItsValuePositions() {
        val rect = segmentRect(segment = GanttSegment(startValue = 2f, endValue = 6f), index = 0)
        assertEquals(20f, rect.left, TOL)
        assertEquals(60f, rect.right, TOL)
    }

    @Test
    fun segmentRect_growsFromTheStartEdgeAsTheAnimationRuns() {
        val rect =
            ganttSegmentRect(
                chartContext = context,
                index = 0,
                rowCount = 2,
                segment = GanttSegment(startValue = 2f, endValue = 6f),
                minValue = 0f,
                maxValue = 10f,
                barHeightFraction = 1f,
                animationProgress = 0.25f,
            )
        assertEquals(20f, rect.left, TOL)
        assertEquals(30f, rect.right, TOL)
    }

    @Test
    fun segmentRect_keepsEverySegmentOfARowInThatRowsSlot() {
        val first = segmentRect(segment = GanttSegment(startValue = 0f, endValue = 3f), index = 0)
        val second = segmentRect(segment = GanttSegment(startValue = 5f, endValue = 8f), index = 0)
        val otherRow = segmentRect(segment = GanttSegment(startValue = 0f, endValue = 3f), index = 1)
        assertEquals(first.top, second.top, TOL)
        assertEquals(0f, first.top, TOL)
        assertEquals(50f, first.bottom, TOL)
        assertEquals(50f, otherRow.top, TOL)
    }

    @Test
    fun progressRect_coversTheCompletedHeadOnly() {
        val rect = segmentRect(segment = GanttSegment(startValue = 0f, endValue = 10f), index = 0)
        val completed = ganttProgressRect(rect = rect, progress = 0.25f)
        assertEquals(25f, completed?.width ?: 0f, TOL)
        assertEquals(rect.left, completed?.left ?: -1f, TOL)
    }

    @Test
    fun progressRect_absentProgress_drawsNoOverlay() {
        val rect = segmentRect(segment = GanttSegment(startValue = 0f, endValue = 10f), index = 0)
        assertNull(ganttProgressRect(rect = rect, progress = null))
    }

    @Test
    fun segment_rejectsAZeroOrNegativeLengthRange() {
        assertFailsWith<IllegalArgumentException> { GanttSegment(startValue = 5f, endValue = 5f) }
        assertFailsWith<IllegalArgumentException> { GanttSegment(startValue = 5f, endValue = 4f) }
    }

    @Test
    fun segment_rejectsProgressOutsideZeroToOne() {
        assertFailsWith<IllegalArgumentException> {
            GanttSegment(startValue = 0f, endValue = 1f, progress = 1.5f)
        }
    }

    @Test
    fun row_rejectsOverlappingSegments() {
        assertFailsWith<IllegalArgumentException> {
            GanttRow(
                label = "Design",
                segments =
                    listOf(
                        GanttSegment(startValue = 0f, endValue = 5f),
                        GanttSegment(startValue = 4f, endValue = 8f),
                    ),
            )
        }
    }

    @Test
    fun row_acceptsTouchingSegmentsInAnyOrder() {
        val row =
            GanttRow(
                label = "Design",
                segments =
                    listOf(
                        GanttSegment(startValue = 5f, endValue = 8f),
                        GanttSegment(startValue = 0f, endValue = 5f),
                    ),
            )
        assertEquals(2, row.segments.size)
    }

    private fun segmentRect(
        segment: GanttSegment,
        index: Int,
    ) = ganttSegmentRect(
        chartContext = context,
        index = index,
        rowCount = 2,
        segment = segment,
        minValue = 0f,
        maxValue = 10f,
        barHeightFraction = 1f,
        animationProgress = 1f,
    )
}
