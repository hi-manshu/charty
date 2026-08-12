package com.himanshoe.charty.line

import com.himanshoe.charty.line.data.LineGroup
import com.himanshoe.charty.line.internal.stackedarea.calculateStackedCumulativeValues
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StackedCumulativeValuesTest {
    @Test
    fun emptyData_producesNoValues() {
        assertTrue(calculateStackedCumulativeValues(emptyList()).isEmpty())
    }

    @Test
    fun singleGroup_accumulatesAcrossSeries() {
        val data = listOf(LineGroup(label = "Jan", values = listOf(10f, 20f, 5f)))
        assertEquals(listOf(10f, 30f, 35f), calculateStackedCumulativeValues(data))
    }

    @Test
    fun eachGroupRestartsItsOwnTotal() {
        val data =
            listOf(
                LineGroup(label = "Jan", values = listOf(10f, 20f)),
                LineGroup(label = "Feb", values = listOf(1f, 2f)),
            )
        assertEquals(listOf(10f, 30f, 1f, 3f), calculateStackedCumulativeValues(data))
    }

    @Test
    fun largestValueIsTheTallestGroupTotal() {
        val data =
            listOf(
                LineGroup(label = "Jan", values = listOf(1f, 1f)),
                LineGroup(label = "Feb", values = listOf(4f, 5f)),
            )
        assertEquals(9f, calculateStackedCumulativeValues(data).max())
    }

    @Test
    fun groupWithNoSeries_contributesNothing() {
        val data =
            listOf(
                LineGroup(label = "Jan", values = emptyList()),
                LineGroup(label = "Feb", values = listOf(3f)),
            )
        assertEquals(listOf(3f), calculateStackedCumulativeValues(data))
    }
}
