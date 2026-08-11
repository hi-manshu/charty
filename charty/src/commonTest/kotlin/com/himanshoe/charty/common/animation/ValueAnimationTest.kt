package com.himanshoe.charty.common.animation

import kotlin.test.Test
import kotlin.test.assertEquals

class ValueAnimationTest {
    @Test
    fun lerpValues_atZeroFraction_returnsFromValues() {
        val from = listOf(0f, 10f, -4f)
        val to = listOf(100f, 20f, 6f)
        assertEquals(from, lerpValues(from = from, to = to, fraction = 0f))
    }

    @Test
    fun lerpValues_atFullFraction_returnsToValues() {
        val from = listOf(0f, 10f, -4f)
        val to = listOf(100f, 20f, 6f)
        assertEquals(to, lerpValues(from = from, to = to, fraction = 1f))
    }

    @Test
    fun lerpValues_atMidFraction_interpolatesEachElement() {
        val from = listOf(0f, 10f, -4f)
        val to = listOf(100f, 20f, 6f)
        assertEquals(listOf(50f, 15f, 1f), lerpValues(from = from, to = to, fraction = 0.5f))
    }

    @Test
    fun lerpValues_clampsFractionBelowZero() {
        val from = listOf(2f)
        val to = listOf(8f)
        assertEquals(from, lerpValues(from = from, to = to, fraction = -1f))
    }

    @Test
    fun lerpValues_clampsFractionAboveOne() {
        val from = listOf(2f)
        val to = listOf(8f)
        assertEquals(to, lerpValues(from = from, to = to, fraction = 2f))
    }

    @Test
    fun lerpValues_sizeMismatch_snapsToTarget() {
        val from = listOf(1f, 2f)
        val to = listOf(9f, 9f, 9f)
        assertEquals(to, lerpValues(from = from, to = to, fraction = 0.5f))
    }

    @Test
    fun lerpValues_emptyLists_returnEmpty() {
        assertEquals(emptyList(), lerpValues(from = emptyList(), to = emptyList(), fraction = 0.5f))
    }
}
