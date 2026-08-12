package com.himanshoe.charty.line

import androidx.compose.ui.geometry.Offset
import com.himanshoe.charty.line.internal.path.smoothSegments
import com.himanshoe.charty.line.internal.path.stepCorner
import com.himanshoe.charty.line.internal.path.stepVertices
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InterpolatedPathTest {
    private val points =
        listOf(
            Offset(x = 0f, y = 10f),
            Offset(x = 10f, y = 4f),
            Offset(x = 20f, y = 8f),
        )

    @Test
    fun stepVertices_emptyInput_producesNoVertices() {
        assertTrue(stepVertices(emptyList()).isEmpty())
    }

    @Test
    fun stepVertices_singlePoint_isUnchanged() {
        val single = listOf(Offset(x = 3f, y = 7f))
        assertEquals(single, stepVertices(single))
    }

    @Test
    fun stepVertices_producesTwoSegmentsPerPointGap() {
        val vertices = stepVertices(points)
        assertEquals(points.size * 2 - 1, vertices.size)
    }

    @Test
    fun stepVertices_turnsAtNextXAndPreviousY() {
        val vertices = stepVertices(points)
        assertEquals(
            listOf(
                Offset(x = 0f, y = 10f),
                Offset(x = 10f, y = 10f),
                Offset(x = 10f, y = 4f),
                Offset(x = 20f, y = 4f),
                Offset(x = 20f, y = 8f),
            ),
            vertices,
        )
    }

    @Test
    fun stepCorner_holdsPreviousValueUntilNextX() {
        assertEquals(
            Offset(x = 20f, y = 4f),
            stepCorner(start = Offset(x = 10f, y = 4f), end = Offset(x = 20f, y = 8f)),
        )
    }

    @Test
    fun smoothSegments_emptyInput_fallsBackToLinear() {
        assertTrue(smoothSegments(emptyList()).isEmpty())
    }

    @Test
    fun smoothSegments_singlePoint_fallsBackToLinear() {
        assertTrue(smoothSegments(listOf(Offset(x = 1f, y = 2f))).isEmpty())
    }

    @Test
    fun smoothSegments_producesOneHopPerPointGap() {
        assertEquals(points.size - 1, smoothSegments(points).size)
    }

    @Test
    fun smoothSegments_placeControlPointsAtThirdsLevelWithTheirEnds() {
        val segment = smoothSegments(points).first()
        assertEquals(Offset(x = 10f / 3f, y = 10f), segment.control1)
        assertEquals(Offset(x = 20f / 3f, y = 4f), segment.control2)
        assertEquals(Offset(x = 10f, y = 4f), segment.end)
    }
}
