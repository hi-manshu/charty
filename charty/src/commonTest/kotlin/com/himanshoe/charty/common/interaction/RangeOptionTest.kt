package com.himanshoe.charty.common.interaction

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class RangeOptionTest {
    @Test
    fun last_buildsOptionWithCountAndLabel() {
        val option = RangeOption.last(count = 7, label = "7D")
        assertEquals("7D", option.label)
        assertEquals(7, option.pointCount)
    }

    @Test
    fun all_showsEverything() {
        assertNull(RangeOption.All.pointCount)
        assertEquals("All", RangeOption.All.label)
    }

    @Test
    fun equality_factoryAndConstructorAgree() {
        assertEquals(RangeOption(label = "30D", pointCount = 30), RangeOption.last(count = 30, label = "30D"))
    }

    @Test
    fun equality_allEqualsConstructedNullOption() {
        assertEquals(RangeOption(label = "All", pointCount = null), RangeOption.All)
    }

    @Test
    fun equality_differsByLabel() {
        assertNotEquals(RangeOption.last(count = 7, label = "7D"), RangeOption.last(count = 7, label = "1W"))
    }

    @Test
    fun equality_differsByPointCount() {
        assertNotEquals(RangeOption.last(count = 7, label = "7D"), RangeOption.last(count = 30, label = "7D"))
    }

    @Test
    fun copy_preservesEqualitySemantics() {
        val original = RangeOption.last(count = 90, label = "90D")
        assertEquals(original, original.copy())
        assertNotEquals(original, original.copy(pointCount = null))
    }

    @Test
    fun blankLabel_throws() {
        assertFailsWith<IllegalArgumentException> { RangeOption(label = " ", pointCount = 7) }
    }

    @Test
    fun pointCountBelowMinimumWindow_throws() {
        assertFailsWith<IllegalArgumentException> { RangeOption(label = "1", pointCount = 1) }
        assertFailsWith<IllegalArgumentException> { RangeOption.last(count = 0, label = "0") }
    }

    @Test
    fun minimumWindowPointCount_constructs() {
        assertEquals(2, RangeOption.last(count = 2, label = "2P").pointCount)
    }
}
