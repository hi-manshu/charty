package com.himanshoe.charty.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class TailWindowTest {
    private val data = (1..10).toList()

    @Test
    fun nullWindow_returnsSameList() {
        assertSame(data, tailWindow(data, null))
    }

    @Test
    fun windowLargerThanData_returnsSameList() {
        assertSame(data, tailWindow(data, 20))
    }

    @Test
    fun windowSmallerThanData_returnsLastN() {
        assertEquals(listOf(8, 9, 10), tailWindow(data, 3))
    }

    @Test
    fun windowEqualToSize_returnsSameContent() {
        assertEquals(data, tailWindow(data, 10))
    }
}
