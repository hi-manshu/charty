package com.himanshoe.charty.calendar

import com.himanshoe.charty.calendar.internal.selectNonOverlappingLabels
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LabelCollisionTest {
    @Test
    fun emptyInput_returnsEmptyResult() {
        val keep = selectNonOverlappingLabels(positions = emptyList(), sizes = emptyList(), minimumGap = 4f)
        assertTrue(keep.isEmpty())
    }

    @Test
    fun singleLabel_isAlwaysKept() {
        val keep = selectNonOverlappingLabels(positions = listOf(0f), sizes = listOf(200f), minimumGap = 4f)
        assertEquals(listOf(true), keep)
    }

    @Test
    fun labelsThatAllFit_areAllKept() {
        val keep =
            selectNonOverlappingLabels(
                positions = listOf(0f, 50f, 100f, 150f),
                sizes = listOf(20f, 20f, 20f, 20f),
                minimumGap = 4f,
            )
        assertEquals(listOf(true, true, true, true), keep)
    }

    @Test
    fun labelsThatAllCollide_keepOnlyTheFirst() {
        val keep =
            selectNonOverlappingLabels(
                positions = listOf(0f, 2f, 4f, 6f),
                sizes = listOf(30f, 30f, 30f, 30f),
                minimumGap = 4f,
            )
        assertEquals(listOf(true, false, false, false), keep)
    }

    @Test
    fun alternatingCollisions_keepEveryOtherLabel() {
        val keep =
            selectNonOverlappingLabels(
                positions = listOf(0f, 10f, 40f, 50f, 80f),
                sizes = listOf(20f, 20f, 20f, 20f, 20f),
                minimumGap = 4f,
            )
        assertEquals(listOf(true, false, true, false, true), keep)
    }

    @Test
    fun partialCollision_skipsOnlyTheCrowdedLabel() {
        val keep =
            selectNonOverlappingLabels(
                positions = listOf(0f, 12f, 100f),
                sizes = listOf(20f, 20f, 20f),
                minimumGap = 4f,
            )
        assertEquals(listOf(true, false, true), keep)
    }

    @Test
    fun skippedLabel_doesNotAdvanceTheTrailingEdge() {
        val keep =
            selectNonOverlappingLabels(
                positions = listOf(0f, 10f, 30f),
                sizes = listOf(20f, 100f, 20f),
                minimumGap = 0f,
            )
        assertEquals(listOf(true, false, true), keep)
    }

    @Test
    fun zeroGap_allowsExactlyTouchingLabels() {
        val keep =
            selectNonOverlappingLabels(
                positions = listOf(0f, 20f, 40f),
                sizes = listOf(20f, 20f, 20f),
                minimumGap = 0f,
            )
        assertEquals(listOf(true, true, true), keep)
    }

    @Test
    fun negativeGap_behavesLikeZeroGap() {
        val positions = listOf(0f, 20f, 39f)
        val sizes = listOf(20f, 20f, 20f)
        assertEquals(
            selectNonOverlappingLabels(positions = positions, sizes = sizes, minimumGap = 0f),
            selectNonOverlappingLabels(positions = positions, sizes = sizes, minimumGap = -50f),
        )
    }

    @Test
    fun mismatchedInputSizes_throw() {
        assertFailsWith<IllegalArgumentException> {
            selectNonOverlappingLabels(positions = listOf(0f, 10f), sizes = listOf(10f), minimumGap = 4f)
        }
    }
}
