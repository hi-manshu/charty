package com.himanshoe.charty.common.accessibility

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DataPointDescriptionsTest {
    @Test
    fun buildsOneDescriptionPerPoint_withPositionAndValue() {
        val out = buildDataPointDescriptions(listOf("Jan", "Feb"), listOf(20f, 45f))
        assertEquals(2, out.size)
        assertEquals("Point 1 of 2: Jan, 20.0", out[0])
        assertEquals("Point 2 of 2: Feb, 45.0", out[1])
    }

    @Test
    fun emptyLabels_yieldEmptyList() {
        assertTrue(buildDataPointDescriptions(emptyList(), emptyList()).isEmpty())
    }

    @Test
    fun missingValue_omitsValueGracefully() {
        val out = buildDataPointDescriptions(listOf("A", "B"), listOf(1f))
        assertEquals("Point 2 of 2: B", out[1])
    }
}
