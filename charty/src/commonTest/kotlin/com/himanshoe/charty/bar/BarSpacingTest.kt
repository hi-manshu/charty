package com.himanshoe.charty.bar

import com.himanshoe.charty.bar.internal.bar.barchart.effectiveBarWidth
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BarSpacingTest {
    @Test
    fun zeroSpacing_leavesWidthUnchanged() {
        assertEquals(40f, effectiveBarWidth(fullBarWidth = 40f, barSpacing = 0f))
    }

    @Test
    fun spacing_shrinksBarWidth() {
        assertEquals(30f, effectiveBarWidth(fullBarWidth = 40f, barSpacing = 10f))
    }

    @Test
    fun largerSpacing_shrinksMore() {
        val small = effectiveBarWidth(fullBarWidth = 40f, barSpacing = 5f)
        val large = effectiveBarWidth(fullBarWidth = 40f, barSpacing = 20f)
        assertTrue(large < small, "more spacing must yield a narrower bar")
    }

    @Test
    fun spacing_neverProducesNonPositiveWidth() {
        assertEquals(1f, effectiveBarWidth(fullBarWidth = 10f, barSpacing = 100f))
    }
}
