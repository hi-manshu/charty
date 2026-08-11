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

    @Test
    fun zoom_narrowsWindowAndClampsToMinimum() {
        val state = ViewPortState()
        state.zoom(focusFraction = 0.5f, scaleFactor = 2f)
        assertEquals(0.5f, state.visibleFraction, absoluteTolerance = 1e-4f)
        // Zoom in hard; the visible window never drops below 10% of the data.
        state.zoom(focusFraction = 0.5f, scaleFactor = 100f)
        assertTrue(state.visibleFraction >= 0.1f - 1e-4f)
    }

    @Test
    fun pan_isClampedWithinBounds() {
        val state = ViewPortState()
        state.zoom(focusFraction = 0f, scaleFactor = 2f)
        state.pan(-1f)
        assertEquals(0f, state.startFraction, absoluteTolerance = 1e-4f)
        state.pan(1f)
        assertEquals(1f, state.endFraction, absoluteTolerance = 1e-4f)
    }

    @Test
    fun visibleIndices_areClampedAndNonEmpty() {
        val state = ViewPortState()
        assertEquals(0 until 10, state.visibleIndices(10))
        state.zoom(focusFraction = 1f, scaleFactor = 2f)
        val range = state.visibleIndices(10)
        assertTrue(range.first >= 0)
        assertTrue(range.last < 10)
        assertTrue(range.last >= range.first)
    }

    @Test
    fun reset_restoresFullView() {
        val state = ViewPortState()
        state.zoom(focusFraction = 0.5f, scaleFactor = 3f)
        state.reset()
        assertEquals(0f, state.startFraction)
        assertEquals(1f, state.endFraction)
    }
}
