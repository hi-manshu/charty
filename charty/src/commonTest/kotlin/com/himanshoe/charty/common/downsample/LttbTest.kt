package com.himanshoe.charty.common.downsample

import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class LttbTest {
    private fun series(
        size: Int,
        f: (Int) -> Float,
    ): List<Float> = List(size) { f(it) }

    @Test
    fun returnsSameList_whenThresholdAtLeastSize() {
        val data = series(10) { it.toFloat() }
        assertSame(data, lttbDownsample(data = data, threshold = 10) { it })
        assertSame(data, lttbDownsample(data = data, threshold = 50) { it })
    }

    @Test
    fun returnsSameList_whenThresholdBelowMinimum() {
        val data = series(100) { it.toFloat() }
        assertSame(data, lttbDownsample(data = data, threshold = 2) { it })
        assertSame(data, lttbDownsample(data = data, threshold = 0) { it })
    }

    @Test
    fun reducesToExactThresholdSize() {
        val data = series(1000) { sin(it / 10.0).toFloat() }
        val reduced = lttbDownsample(data = data, threshold = 100) { it }
        assertEquals(100, reduced.size)
    }

    @Test
    fun preservesFirstAndLastPoints() {
        val data = series(500) { it.toFloat() }
        val reduced = lttbDownsample(data = data, threshold = 50) { it }
        assertEquals(data.first(), reduced.first())
        assertEquals(data.last(), reduced.last())
    }

    @Test
    fun keepsOriginalOrderAndMembership() {
        val data = series(300) { sin(it / 7.0).toFloat() }
        val reduced = lttbDownsample(data = data, threshold = 40) { it }
        var prevIndex = -1
        reduced.forEach { point ->
            val idx = data.indexOf(point)
            assertTrue(idx > prevIndex, "points must stay in source order")
            prevIndex = idx
        }
    }

    @Test
    fun retainsAGlobalPeak() {
        val spikeIndex = 250
        val data = series(500) { if (it == spikeIndex) 100f else 0f }
        val reduced = lttbDownsample(data = data, threshold = 20) { it }
        assertTrue(reduced.any { it == 100f }, "the global peak must survive downsampling")
    }

    @Test
    fun smallestUsefulThreshold_keepsBothEndpointsAndOneMiddlePoint() {
        val data = series(4) { it.toFloat() }
        val reduced = lttbDownsample(data = data, threshold = 3) { it }
        assertEquals(3, reduced.size)
        assertEquals(data.first(), reduced.first())
        assertEquals(data.last(), reduced.last())
    }

    @Test
    fun allEqualValues_stillReducesToTheThresholdWithoutRepeatingAPoint() {
        val data = List(200) { index -> index to 5f }
        val reduced = lttbDownsample(data = data, threshold = 20) { it.second }
        assertEquals(20, reduced.size)
        assertEquals(reduced.size, reduced.distinct().size, "a flat series must not collapse onto one point")
    }

    @Test
    fun worksOnCustomPointTypes() {
        data class P(
            val x: Int,
            val y: Float,
        )
        val data = List(200) { P(it, (it % 5).toFloat()) }
        val reduced = lttbDownsample(data = data, threshold = 25) { it.y }
        assertEquals(25, reduced.size)
        assertEquals(data.first(), reduced.first())
        assertEquals(data.last(), reduced.last())
    }
}
