package com.himanshoe.charty.common.viewport

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ViewPortStateTest {
    @Test
    fun fullView_isAtStartAndEnd() {
        val state = ViewPortState()
        assertTrue(state.isAtStart)
        assertTrue(state.isAtEnd)
    }

    @Test
    fun scrollToEnd_onFullView_keepsFullView() {
        val state = ViewPortState()
        state.scrollToEnd()
        assertEquals(0f, state.startFraction)
        assertEquals(1f, state.endFraction)
    }

    @Test
    fun scrollToEnd_afterZoom_movesWindowToEndKeepingWidth() {
        val state = ViewPortState()
        state.dataSize = 100
        state.zoom(focusFraction = 0f, scaleFactor = 4f)
        val width = state.visibleFraction
        state.scrollToEnd()
        assertEquals(1f, state.endFraction)
        assertEquals(1f - width, state.startFraction, absoluteTolerance = 1e-4f)
        assertTrue(state.isAtEnd)
        assertFalse(state.isAtStart)
    }
}
