package com.himanshoe.charty.common

import kotlin.test.Test
import kotlin.test.assertEquals

private const val TOL = 0.001f

class ChartContextTest {
    private fun context(
        min: Float = 0f,
        max: Float = 100f,
    ) = ChartContext(left = 0f, top = 0f, right = 100f, bottom = 100f, minValue = min, maxValue = max)

    @Test
    fun width_and_height_areDerived() {
        val c = context()
        assertEquals(100f, c.width, TOL)
        assertEquals(100f, c.height, TOL)
    }

    @Test
    fun convertValueToYPosition_mapsRangeToPixels() {
        val c = context()
        assertEquals(100f, c.convertValueToYPosition(0f), TOL) // min -> bottom
        assertEquals(0f, c.convertValueToYPosition(100f), TOL) // max -> top
        assertEquals(50f, c.convertValueToYPosition(50f), TOL) // mid -> mid
    }

    @Test
    fun convertValueToYPosition_zeroRange_returnsBottom() {
        val c = context(min = 42f, max = 42f)
        assertEquals(100f, c.convertValueToYPosition(42f), TOL)
        assertEquals(100f, c.convertValueToYPosition(0f), TOL)
    }

    @Test
    fun convertValueToYPosition_secondaryRange_usesThatScale() {
        val c = context() // own range 0..100
        assertEquals(50f, c.convertValueToYPosition(value = 5f, rangeMin = 0f, rangeMax = 10f), TOL)
        assertEquals(100f, c.convertValueToYPosition(value = 0f, rangeMin = 0f, rangeMax = 10f), TOL)
        assertEquals(0f, c.convertValueToYPosition(value = 10f, rangeMin = 0f, rangeMax = 10f), TOL)
    }

    @Test
    fun convertValueToYPosition_secondaryZeroRange_returnsBottom() {
        val c = context()
        assertEquals(100f, c.convertValueToYPosition(value = 5f, rangeMin = 5f, rangeMax = 5f), TOL)
    }

    @Test
    fun calculateBarWidth_isFractionOfSection() {
        assertEquals(15f, context().calculateBarWidth(totalBars = 4, widthFraction = 0.6f), TOL)
    }

    @Test
    fun calculateBarLeftPosition_centersBarInSection() {
        val c = context()
        assertEquals(5f, c.calculateBarLeftPosition(index = 0, totalBars = 4, barWidthFraction = 0.6f), TOL)
        assertEquals(30f, c.calculateBarLeftPosition(index = 1, totalBars = 4, barWidthFraction = 0.6f), TOL)
    }

    @Test
    fun calculateCenteredXPosition_centersItemInSlot() {
        val c = context()
        assertEquals(12.5f, c.calculateCenteredXPosition(index = 0, totalItems = 4), TOL)
        assertEquals(37.5f, c.calculateCenteredXPosition(index = 1, totalItems = 4), TOL)
    }
}
