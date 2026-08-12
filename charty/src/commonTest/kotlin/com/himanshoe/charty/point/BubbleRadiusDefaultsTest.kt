package com.himanshoe.charty.point

import com.himanshoe.charty.point.config.PointChartConfig
import kotlin.test.Test
import kotlin.test.assertTrue

private const val MIN_RADIUS_DIVISOR = 2f

class BubbleRadiusDefaultsTest {
    @Test
    fun defaultMinimumIsSmallerThanTheDefaultMaximum() {
        val maximum = PointChartConfig().pointRadius
        val minimum = maximum / MIN_RADIUS_DIVISOR
        assertTrue(minimum > 0f, "default minimum radius must be positive but was $minimum")
        assertTrue(maximum > minimum, "default maximum $maximum must exceed the default minimum $minimum")
    }

    @Test
    fun theDerivedMinimumStaysValidForAnyConfiguredMaximum() {
        listOf(1f, 4f, 8f, 24f, 100f).forEach { maximum ->
            val minimum = PointChartConfig(pointRadius = maximum).pointRadius / MIN_RADIUS_DIVISOR
            assertTrue(maximum > minimum, "maximum $maximum must exceed derived minimum $minimum")
            assertTrue(minimum > 0f, "derived minimum must stay positive for maximum $maximum")
        }
    }
}
