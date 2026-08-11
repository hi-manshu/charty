package com.himanshoe.charty.common.util

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
    fun calculateMinMaxValue_returnsNiceRoundedPair() {
        val (min, max) = calculateMinMaxValue(listOf(15f, 65f))
        assertEquals(10f, min, TOL)
        assertEquals(70f, max, TOL)
    }

    @Test
    fun niceAxisStep_mapsToOneTwoFiveBuckets() {
        assertEquals(1f, niceAxisStep(1f), TOL) // normalized <= 1.5
        assertEquals(2f, niceAxisStep(3f), TOL) // normalized <= 3.5
        assertEquals(5f, niceAxisStep(6f), TOL) // normalized <= 7.5
        assertEquals(10f, niceAxisStep(9f), TOL) // else
        assertEquals(20f, niceAxisStep(20f), TOL) // 2 x 10^1
    }

    @Test
    fun niceAxisStep_nonPositiveRoughStep_isOne() {
        assertEquals(1f, niceAxisStep(0f), TOL)
        assertEquals(1f, niceAxisStep(-5f), TOL)
    }

    @Test
    fun calculateNiceAxisRange_includesZeroAsTickAcrossSignChange() {
        val (niceMin, niceMax, steps) = calculateNiceAxisRange(rawMin = -5f, rawMax = 15f, targetSteps = 4)
        assertEquals(-5f, niceMin, TOL)
        assertEquals(15f, niceMax, TOL)
        assertTrue(steps >= 1)
        val step = (niceMax - niceMin) / steps
        // zero must land exactly on a tick boundary
        assertEquals(0f, (0f - niceMin) % step, TOL)
    }

    @Test
    fun calculateNiceAxisRange_degenerateEqualBounds_expandsAroundValue() {
        val (niceMin, niceMax, steps) = calculateNiceAxisRange(rawMin = 5f, rawMax = 5f, targetSteps = 4)
        assertTrue(niceMin < 5f && niceMax > 5f)
        assertEquals(2, steps)
    }

    @Test
    fun calculateNiceAxisRange_requiresPositiveTargetSteps() {
        assertFailsWith<IllegalArgumentException> {
            calculateNiceAxisRange(rawMin = 0f, rawMax = 10f, targetSteps = 0)
        }
    }
}
