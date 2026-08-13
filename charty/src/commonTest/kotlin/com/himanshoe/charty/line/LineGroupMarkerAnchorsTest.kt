package com.himanshoe.charty.line

import com.himanshoe.charty.line.data.LineGroup
import com.himanshoe.charty.line.internal.marker.stackedTotalValues
import com.himanshoe.charty.line.internal.marker.topSeriesValues
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LineGroupMarkerAnchorsTest {
    @Test
    fun emptyData_anchorsNothing() {
        assertTrue(emptyList<LineGroup>().topSeriesValues().isEmpty())
        assertTrue(emptyList<LineGroup>().stackedTotalValues().isEmpty())
    }

    @Test
    fun multilineAnchorsOnTheHighestSeriesAtEachIndex() {
        val data =
            listOf(
                LineGroup(label = "Jan", values = listOf(10f, 20f, 5f)),
                LineGroup(label = "Feb", values = listOf(40f, 3f)),
            )
        assertEquals(listOf(20f, 40f), data.topSeriesValues())
    }

    @Test
    fun stackedAnchorsOnTheGroupTotal() {
        val data =
            listOf(
                LineGroup(label = "Jan", values = listOf(10f, 20f, 5f)),
                LineGroup(label = "Feb", values = listOf(40f, 3f)),
            )
        assertEquals(listOf(35f, 43f), data.stackedTotalValues())
    }

    @Test
    fun groupWithNoSeries_anchorsAtZero() {
        val data = listOf(LineGroup(label = "Jan", values = emptyList()))
        assertEquals(listOf(0f), data.topSeriesValues())
        assertEquals(listOf(0f), data.stackedTotalValues())
    }

    @Test
    fun negativeValues_anchorOnTheLeastNegativeSeries() {
        val data = listOf(LineGroup(label = "Jan", values = listOf(-10f, -2f, -30f)))
        assertEquals(listOf(-2f), data.topSeriesValues())
    }

    @Test
    fun anchorsKeepOneEntryPerGroupSoNegativeMarkerIndicesLandOnTheNewest() {
        val data =
            listOf(
                LineGroup(label = "Jan", values = listOf(1f)),
                LineGroup(label = "Feb", values = listOf(2f)),
                LineGroup(label = "Mar", values = listOf(3f)),
            )
        assertEquals(data.size, data.topSeriesValues().size)
        assertEquals(3f, data.topSeriesValues().last())
    }
}
