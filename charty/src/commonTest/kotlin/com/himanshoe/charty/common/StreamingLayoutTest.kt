package com.himanshoe.charty.common

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val TOL = 0.001f

class StreamingLayoutTest {
    private fun layout(
        from: Int = 0,
        scroll: Float = 0f,
        windowSize: Int = 4,
    ) = StreamingLayout(from = from, scroll = scroll, windowSize = windowSize)

    private fun context(streaming: StreamingLayout) =
        ChartContext(
            left = 0f,
            top = 0f,
            right = 100f,
            bottom = 100f,
            minValue = 0f,
            maxValue = 100f,
            streaming = streaming,
        )

    @Test
    fun atRest_theWindowFillsThePlotExactly() {
        val streaming = layout()
        val chartContext = context(streaming)
        assertEquals(chartContext.top, chartContext.calculateSlotTopPosition(index = 0, totalItems = 4), TOL)
        assertEquals(25f, chartContext.calculateSlotHeight(totalItems = 4), TOL)
        assertEquals(12.5f, chartContext.calculateCenteredXPosition(index = 0, totalItems = 4), TOL)
        assertEquals(87.5f, chartContext.calculateCenteredXPosition(index = 3, totalItems = 4), TOL)
    }

    @Test
    fun slotWidthIsIndependentOfTheItemCountPassedIn() {
        val chartContext = context(layout())
        assertEquals(
            chartContext.calculateBarWidth(totalBars = 4, widthFraction = 1f),
            chartContext.calculateBarWidth(totalBars = 400, widthFraction = 1f),
            TOL,
        )
    }

    @Test
    fun scrollingByOneIndex_movesEveryItemOneSlotTowardsTheLeadingEdge() {
        val restingX = context(layout(scroll = 0f)).calculateCenteredXPosition(index = 1, totalItems = 4)
        val scrolledX = context(layout(scroll = 1f)).calculateCenteredXPosition(index = 1, totalItems = 4)
        assertEquals(25f, restingX - scrolledX, TOL)
    }

    @Test
    fun itemsScrolledPastTheLeadingEdge_landOutsideThePlot() {
        val chartContext = context(layout(from = 0, scroll = 2f))
        assertTrue(
            chartContext.calculateCenteredXPosition(index = 0, totalItems = 4) < chartContext.left,
            "an item two slots behind the window must be clipped off the leading edge",
        )
    }

    @Test
    fun aPartialSlideLeavesTheIncomingItemBeyondTheTrailingEdge() {
        val chartContext = context(layout(from = 0, scroll = 0.25f, windowSize = 4))
        assertTrue(
            chartContext.calculateCenteredXPosition(index = 4, totalItems = 5) > chartContext.right,
            "the incoming item must still be off the trailing edge part-way through the slide",
        )
    }
}
