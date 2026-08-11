package com.himanshoe.charty.common.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AxisRangeTest {
    @Test
    fun niceAxisStep_nonPositive_returnsOne() {
        assertEquals(1f, niceAxisStep(0f))
        assertEquals(1f, niceAxisStep(-5f))
    }

    @Test
    fun niceAxisStep_roundsToOneTwoFiveTimesPowerOfTen() {
        assertEquals(1f, niceAxisStep(1f))
        assertEquals(2f, niceAxisStep(3f))
        assertEquals(5f, niceAxisStep(7f))
        assertEquals(10f, niceAxisStep(9f))
        assertEquals(20f, niceAxisStep(20f))
        assertEquals(0.5f, niceAxisStep(0.5f))
    }

    @Test
    fun niceAxisRange_simpleDomain_startsAtZero() {
        val (min, max, steps) = calculateNiceAxisRange(rawMin = 0f, rawMax = 100f, targetSteps = 5)
        assertEquals(0f, min)
        assertEquals(100f, max)
        assertEquals(5, steps)
    }

    @Test
    fun niceAxisRange_flatDomain_expandsAroundValue() {
        val (min, max, steps) = calculateNiceAxisRange(rawMin = 5f, rawMax = 5f, targetSteps = 4)
        assertEquals(0f, min)
        assertEquals(10f, max)
        assertEquals(2, steps)
    }

    @Test
    fun niceAxisRange_spanningZero_keepsZeroOnAStepBoundary() {
        val (min, max, steps) = calculateNiceAxisRange(rawMin = -30f, rawMax = 70f, targetSteps = 5)
        assertTrue(min <= -30f, "min $min should contain -30")
        assertTrue(max >= 70f, "max $max should contain 70")
        val step = (max - min) / steps
        val zeroOffsetInSteps = (0f - min) / step
        assertEquals(kotlin.math.round(zeroOffsetInSteps), zeroOffsetInSteps, absoluteTolerance = 1e-3f)
    }
}
