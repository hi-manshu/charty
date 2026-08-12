package com.himanshoe.charty.bar

import com.himanshoe.charty.bar.internal.bar.wavy.wavyPointPositions
import com.himanshoe.charty.common.ChartContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val TOL = 0.001f

class WavyPointPositionsTest {
    private val context =
        ChartContext(left = 0f, top = 0f, right = 100f, bottom = 100f, minValue = 0f, maxValue = 100f)

    @Test
    fun emptyValues_producesNoPositions() {
        assertTrue(wavyPointPositions(chartContext = context, values = emptyList()).isEmpty())
    }

    @Test
    fun positionsAreCentredInEvenlySplitSlots() {
        val positions = wavyPointPositions(chartContext = context, values = listOf(0f, 0f))
        assertEquals(25f, positions[0].x, TOL)
        assertEquals(75f, positions[1].x, TOL)
    }

    @Test
    fun singleValue_isCentredInTheChart() {
        val positions = wavyPointPositions(chartContext = context, values = listOf(50f))
        assertEquals(50f, positions[0].x, TOL)
    }

    @Test
    fun yFollowsTheValueScale() {
        val positions = wavyPointPositions(chartContext = context, values = listOf(0f, 100f))
        assertEquals(100f, positions[0].y, TOL)
        assertEquals(0f, positions[1].y, TOL)
    }
}
