package com.himanshoe.charty.common.streaming

import com.himanshoe.charty.common.gesture.panDeltaToIndexDelta
import kotlin.test.Test
import kotlin.test.assertEquals

class StreamingPanTest {
    @Test
    fun dragOfOneSlot_movesExactlyOneIndex() {
        val delta = panDeltaToIndexDelta(dragPixels = -40f, plotExtentPixels = 400f, windowSize = 10)
        assertEquals(1f, delta)
    }

    @Test
    fun draggingContentForward_movesBackInHistory() {
        val delta = panDeltaToIndexDelta(dragPixels = 80f, plotExtentPixels = 400f, windowSize = 10)
        assertEquals(-2f, delta)
    }

    @Test
    fun draggingContentBackward_movesTowardsLatest() {
        val delta = panDeltaToIndexDelta(dragPixels = -80f, plotExtentPixels = 400f, windowSize = 10)
        assertEquals(2f, delta)
    }

    @Test
    fun verticalOrientationExtent_scalesWithHeight() {
        val delta = panDeltaToIndexDelta(dragPixels = -25f, plotExtentPixels = 200f, windowSize = 8)
        assertEquals(1f, delta)
    }

    @Test
    fun zeroDrag_producesNoMovement() {
        val delta = panDeltaToIndexDelta(dragPixels = 0f, plotExtentPixels = 400f, windowSize = 10)
        assertEquals(expected = 0f, actual = delta, absoluteTolerance = 0f)
    }

    @Test
    fun zeroWindow_producesNoMovement() {
        val delta = panDeltaToIndexDelta(dragPixels = 40f, plotExtentPixels = 400f, windowSize = 0)
        assertEquals(0f, delta)
    }

    @Test
    fun negativeWindow_producesNoMovement() {
        val delta = panDeltaToIndexDelta(dragPixels = 40f, plotExtentPixels = 400f, windowSize = -5)
        assertEquals(0f, delta)
    }

    @Test
    fun unmeasuredPlot_producesNoMovement() {
        val delta = panDeltaToIndexDelta(dragPixels = 40f, plotExtentPixels = 0f, windowSize = 10)
        assertEquals(0f, delta)
    }

    @Test
    fun subSlotDrag_producesFractionalIndexDelta() {
        val delta = panDeltaToIndexDelta(dragPixels = -20f, plotExtentPixels = 400f, windowSize = 10)
        assertEquals(0.5f, delta)
    }
}
