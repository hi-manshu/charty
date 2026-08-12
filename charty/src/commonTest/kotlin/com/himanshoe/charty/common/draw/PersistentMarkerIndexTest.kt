package com.himanshoe.charty.common.draw

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PersistentMarkerIndexTest {
    @Test
    fun nonNegativeIndexIsUnchanged() {
        assertEquals(0, resolveMarkerIndex(dataIndex = 0, pointCount = 5))
        assertEquals(3, resolveMarkerIndex(dataIndex = 3, pointCount = 5))
    }

    @Test
    fun minusOneResolvesToTheLastDrawnPoint() {
        assertEquals(4, resolveMarkerIndex(dataIndex = -1, pointCount = 5))
    }

    @Test
    fun negativeIndicesCountBackFromTheEnd() {
        assertEquals(3, resolveMarkerIndex(dataIndex = -2, pointCount = 5))
        assertEquals(0, resolveMarkerIndex(dataIndex = -5, pointCount = 5))
    }

    @Test
    fun outOfRangeResultsStayOutOfRangeSoTheyAreSkipped() {
        val indices = 0 until 5
        assertFalse(resolveMarkerIndex(dataIndex = -6, pointCount = 5) in indices)
        assertFalse(resolveMarkerIndex(dataIndex = 9, pointCount = 5) in indices)
        assertTrue(resolveMarkerIndex(dataIndex = -1, pointCount = 5) in indices)
    }

    @Test
    fun emptyPointListYieldsNoValidIndex() {
        assertFalse(resolveMarkerIndex(dataIndex = -1, pointCount = 0) in 0 until 0)
    }
}
