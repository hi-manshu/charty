package com.himanshoe.charty.common.draw

import com.himanshoe.charty.common.config.ReferenceBandConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

private const val TOL = 0.001f

class ReferenceBandTest {
    @Test
    fun bounds_normalRange_returnedAsIs() {
        val (lo, hi) = resolveBandValueBounds(60f, 80f, minValue = 0f, maxValue = 100f)!!
        assertEquals(60f, lo, TOL)
        assertEquals(80f, hi, TOL)
    }

    @Test
    fun bounds_swappedInput_isNormalized() {
        val (lo, hi) = resolveBandValueBounds(80f, 60f, minValue = 0f, maxValue = 100f)!!
        assertEquals(60f, lo, TOL)
        assertEquals(80f, hi, TOL)
    }

    @Test
    fun bounds_partlyOutside_isClamped() {
        assertEquals(0f to 50f, resolveBandValueBounds(-10f, 50f, 0f, 100f))
        assertEquals(50f to 100f, resolveBandValueBounds(50f, 200f, 0f, 100f))
    }

    @Test
    fun bounds_fullyOutside_isNull() {
        assertNull(resolveBandValueBounds(-20f, -5f, 0f, 100f))
        assertNull(resolveBandValueBounds(150f, 200f, 0f, 100f))
    }

    @Test
    fun bounds_zeroAxisRange_isNull() {
        assertNull(resolveBandValueBounds(10f, 20f, minValue = 50f, maxValue = 50f))
    }

    @Test
    fun bounds_degenerateBand_isNull() {
        assertNull(resolveBandValueBounds(50f, 50f, 0f, 100f))
    }

    @Test
    fun config_rejectsFillAlphaOutOfRange() {
        assertFailsWith<IllegalArgumentException> { ReferenceBandConfig(lowValue = 0f, highValue = 1f, fillAlpha = 2f) }
    }

    @Test
    fun config_rejectsNonPositiveBorderWidth() {
        assertFailsWith<IllegalArgumentException> {
            ReferenceBandConfig(
                lowValue = 0f,
                highValue = 1f,
                borderWidth = 0f,
            )
        }
    }
}
