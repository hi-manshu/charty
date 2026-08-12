package com.himanshoe.charty.common.util

import kotlin.math.round
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

private const val TOL = 0.001f

class ValueCalculationsTest {
    @Test
    fun calculateMaxValue_roundsUpToStep() {
        assertEquals(70f, calculateMaxValue(listOf(10f, 28f, 42f, 65f)), TOL)
        assertEquals(20f, calculateMaxValue(listOf(20f)), TOL)
    }

    @Test
    fun calculateMaxValue_emptyList_isZero() {
        assertEquals(0f, calculateMaxValue(emptyList()), TOL)
    }

    @Test
    fun calculateMinValue_roundsDownToStep() {
        assertEquals(10f, calculateMinValue(listOf(15f, 28f, 42f)), TOL)
        assertEquals(-10f, calculateMinValue(listOf(-5f, 20f)), TOL)
    }

    @Test
    fun calculateMinValue_emptyList_isZero() {
        assertEquals(0f, calculateMinValue(emptyList()), TOL)
    }

    @Test
    fun calculateMinMaxValue_nonPositiveStepSize_isRejected() {
        assertFailsWith<IllegalArgumentException> { calculateMinValue(values = listOf(5f), stepSize = 0) }
        assertFailsWith<IllegalArgumentException> { calculateMaxValue(values = listOf(5f), stepSize = 0) }
        assertFailsWith<IllegalArgumentException> { calculateMaxValue(values = listOf(5f), stepSize = -10) }
    }

    @Test
    fun baselineValueRange_clampsMinToZeroForPositiveData() {
        val (min, max) = baselineValueRange(listOf(15f, 28f, 42f))
        assertEquals(0f, min, TOL)
        assertEquals(50f, max, TOL)
    }

    @Test
    fun baselineValueRange_keepsNegativeMin() {
        val (min, max) = baselineValueRange(listOf(-5f, 20f))
        assertEquals(-10f, min, TOL)
        assertEquals(20f, max, TOL)
    }

    @Test
    fun calculateMinMaxWithPadding_appliesPercentPadding() {
        val (min, max) = calculateMinMaxWithPadding(listOf(10f, 20f), paddingMultiplier = 0.05f)
        assertEquals(9.5f, min, TOL)
        assertEquals(21f, max, TOL)
    }

    @Test
    fun calculateMinMaxWithPadding_padsOutwardsAroundNegativeData() {
        val values = listOf(-20f, -10f)
        val (min, max) = calculateMinMaxWithPadding(values, paddingMultiplier = 0.05f)
        assertTrue(min < values.min(), "padded min $min must sit below the data minimum ${values.min()}")
        assertTrue(max > values.max(), "padded max $max must sit above the data maximum ${values.max()}")
    }

    @Test
    fun calculateMinMaxWithPadding_padsOutwardsAcrossZero() {
        val values = listOf(-8f, 0f, 12f)
        val (min, max) = calculateMinMaxWithPadding(values, paddingMultiplier = 0.1f)
        assertTrue(min < -8f, "padded min $min must contain -8")
        assertTrue(max > 12f, "padded max $max must contain 12")
    }

    @Test
    fun calculateMinMaxValue_returnsNiceRoundedPair() {
        val (min, max) = calculateMinMaxValue(listOf(15f, 65f))
        assertEquals(10f, min, TOL)
        assertEquals(70f, max, TOL)
    }

    @Test
    fun niceAxisStep_mapsToOneTwoFiveBuckets() {
        assertEquals(1f, niceAxisStep(1f), TOL)
        assertEquals(2f, niceAxisStep(3f), TOL)
        assertEquals(5f, niceAxisStep(6f), TOL)
        assertEquals(5f, niceAxisStep(7f), TOL)
        assertEquals(10f, niceAxisStep(9f), TOL)
        assertEquals(20f, niceAxisStep(20f), TOL)
        assertEquals(0.5f, niceAxisStep(0.5f), TOL)
    }

    @Test
    fun niceAxisStep_nonPositiveRoughStep_isOne() {
        assertEquals(1f, niceAxisStep(0f), TOL)
        assertEquals(1f, niceAxisStep(-5f), TOL)
    }

    @Test
    fun calculateNiceAxisRange_alreadyNiceDomain_isLeftAlone() {
        val (niceMin, niceMax, steps) = calculateNiceAxisRange(rawMin = 0f, rawMax = 100f, targetSteps = 5)
        assertEquals(0f, niceMin, TOL)
        assertEquals(100f, niceMax, TOL)
        assertEquals(5, steps)
    }

    @Test
    fun calculateNiceAxisRange_includesZeroAsTickAcrossSignChange() {
        val (niceMin, niceMax, steps) = calculateNiceAxisRange(rawMin = -5f, rawMax = 15f, targetSteps = 4)
        assertEquals(-5f, niceMin, TOL)
        assertEquals(15f, niceMax, TOL)
        assertTrue(steps >= 1)
        val step = (niceMax - niceMin) / steps
        assertEquals(0f, (0f - niceMin) % step, TOL)
    }

    @Test
    fun calculateNiceAxisRange_wideSpanAcrossZero_containsDataAndKeepsZeroOnATick() {
        val (niceMin, niceMax, steps) = calculateNiceAxisRange(rawMin = -30f, rawMax = 70f, targetSteps = 5)
        assertTrue(niceMin <= -30f, "min $niceMin should contain -30")
        assertTrue(niceMax >= 70f, "max $niceMax should contain 70")
        val zeroOffsetInSteps = (0f - niceMin) / ((niceMax - niceMin) / steps)
        assertEquals(round(zeroOffsetInSteps), zeroOffsetInSteps, TOL)
    }

    @Test
    fun calculateNiceAxisRange_degenerateEqualBounds_expandsAroundValue() {
        val (niceMin, niceMax, steps) = calculateNiceAxisRange(rawMin = 5f, rawMax = 5f, targetSteps = 4)
        assertEquals(0f, niceMin, TOL)
        assertEquals(10f, niceMax, TOL)
        assertEquals(2, steps)
    }

    @Test
    fun calculateNiceAxisRange_degenerateZeroBounds_expandsToUnitRange() {
        val (niceMin, niceMax, steps) = calculateNiceAxisRange(rawMin = 0f, rawMax = 0f, targetSteps = 4)
        assertEquals(-1f, niceMin, TOL)
        assertEquals(1f, niceMax, TOL)
        assertEquals(2, steps)
    }

    @Test
    fun calculateNiceAxisRange_requiresPositiveTargetSteps() {
        assertFailsWith<IllegalArgumentException> {
            calculateNiceAxisRange(rawMin = 0f, rawMax = 10f, targetSteps = 0)
        }
    }
}
