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
    fun lerpValues_appendedPoint_startsOnItsPredecessorSoTheSegmentGrowsFromIt() {
        val before = listOf(10f, 20f, 30f)
        val after = listOf(10f, 20f, 30f, 50f)
        assertEquals(listOf(10f, 20f, 30f, 30f), lerpValues(from = before, to = after, fraction = 0f))
        assertEquals(listOf(10f, 20f, 30f, 40f), lerpValues(from = before, to = after, fraction = 0.5f))
        assertEquals(after, lerpValues(from = before, to = after, fraction = 1f))
    }

    @Test
    fun lerpValues_appendedPoint_leavesTheExistingPointsWhereTheyWere() {
        val before = listOf(1f, 2f, 3f)
        val after = listOf(1f, 2f, 3f, 99f)
        val mid = lerpValues(from = before, to = after, fraction = 0.5f)
        assertEquals(before, mid.take(before.size))
    }

    @Test
    fun lerpValues_severalAppendedPoints_allStartFromTheLastSharedValue() {
        val before = listOf(5f)
        val after = listOf(5f, 15f, 25f)
        assertEquals(listOf(5f, 5f, 5f), lerpValues(from = before, to = after, fraction = 0f))
    }

    @Test
    fun lerpValues_grownButNotByAppending_stillSnaps() {
        val before = listOf(1f, 2f)
        val after = listOf(7f, 1f, 2f)
        assertEquals(after, lerpValues(from = before, to = after, fraction = 0.5f))
    }

    @Test
    fun lerpValues_growingFromEmpty_snapsBecauseThereIsNoPredecessorToGrowFrom() {
        val after = listOf(4f, 5f)
        assertEquals(after, lerpValues(from = emptyList(), to = after, fraction = 0.5f))
    }

    @Test
    fun lerpValues_shrunk_stillSnaps() {
        val before = listOf(1f, 2f, 3f)
        val after = listOf(1f, 2f)
        assertEquals(after, lerpValues(from = before, to = after, fraction = 0.5f))
    }

    @Test
    fun lerpValues_emptyLists_returnEmpty() {
        assertEquals(emptyList(), lerpValues(from = emptyList(), to = emptyList(), fraction = 0.5f))
    }
}
