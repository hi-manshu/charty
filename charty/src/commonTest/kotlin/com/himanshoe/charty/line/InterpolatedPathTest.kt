package com.himanshoe.charty.line

import androidx.compose.ui.geometry.Offset
import com.himanshoe.charty.line.internal.path.CubicSegment
import com.himanshoe.charty.line.internal.path.smoothSegments
import com.himanshoe.charty.line.internal.path.stepCorner
import com.himanshoe.charty.line.internal.path.stepVertices
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val TOL = 0.001f

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
    fun smoothSegments_landOnEveryDataPointInOrder() {
        assertEquals(points.drop(1), smoothSegments(points).map { it.end })
    }

    @Test
    fun smoothSegments_neverOvershootThePairOfValuesTheyConnect() {
        points.zipWithNext().zip(smoothSegments(points)).forEach { (pair, segment) ->
            val (start, end) = pair
            val lowest = minOf(start.y, end.y)
            val highest = maxOf(start.y, end.y)
            sampleCurve(start = start, segment = segment).forEach { sample ->
                assertTrue(
                    sample.y >= lowest - TOL && sample.y <= highest + TOL,
                    "curve reached ${sample.y}, outside the $lowest..$highest the points span",
                )
            }
        }
    }

    @Test
    fun smoothSegments_advanceMonotonicallyAlongTheCategoryAxis() {
        points.zipWithNext().zip(smoothSegments(points)).forEach { (pair, segment) ->
            val (start, end) = pair
            val xs = sampleCurve(start = start, segment = segment).map { it.x }
            xs.zipWithNext().forEach { (earlier, later) ->
                assertTrue(later >= earlier - TOL, "a smoothed line must never double back on itself")
            }
        }
    }

    private fun sampleCurve(
        start: Offset,
        segment: CubicSegment,
        samples: Int = 20,
    ): List<Offset> =
        (0..samples).map { step ->
            val t = step.toFloat() / samples
            val inverse = 1f - t
            Offset(
                x =
                    inverse * inverse * inverse * start.x +
                        3f * inverse * inverse * t * segment.control1.x +
                        3f * inverse * t * t * segment.control2.x +
                        t * t * t * segment.end.x,
                y =
                    inverse * inverse * inverse * start.y +
                        3f * inverse * inverse * t * segment.control1.y +
                        3f * inverse * t * t * segment.control2.y +
                        t * t * t * segment.end.y,
            )
        }
}
