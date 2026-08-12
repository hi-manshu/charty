package com.himanshoe.charty.gauge

import com.himanshoe.charty.gauge.internal.gaugeAngleForValue
import com.himanshoe.charty.gauge.internal.gaugeBandArc
import com.himanshoe.charty.gauge.internal.gaugeFraction
import com.himanshoe.charty.gauge.internal.gaugeTickValues
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class GaugeMathTest {
    @Test
    fun fraction_clampsBelowMin() {
        assertEquals(expected = 0f, actual = gaugeFraction(value = -25f, minValue = 0f, maxValue = 100f))
    }

    @Test
    fun fraction_clampsAboveMax() {
        assertEquals(expected = 1f, actual = gaugeFraction(value = 250f, minValue = 0f, maxValue = 100f))
    }

    @Test
    fun fraction_interpolatesLinearly() {
        assertEquals(expected = 0.5f, actual = gaugeFraction(value = 50f, minValue = 0f, maxValue = 100f))
        assertEquals(expected = 0.25f, actual = gaugeFraction(value = 30f, minValue = 20f, maxValue = 60f))
    }

    @Test
    fun fraction_endpointsMapToZeroAndOne() {
        assertEquals(expected = 0f, actual = gaugeFraction(value = 20f, minValue = 20f, maxValue = 60f))
        assertEquals(expected = 1f, actual = gaugeFraction(value = 60f, minValue = 20f, maxValue = 60f))
    }

    @Test
    fun fraction_rejectsInvertedRange() {
        assertFailsWith<IllegalArgumentException> {
            gaugeFraction(value = 5f, minValue = 100f, maxValue = 0f)
        }
    }

    @Test
    fun angle_minValueMapsToStartAngle() {
        assertEquals(
            expected = 135f,
            actual =
                gaugeAngleForValue(
                    value = 0f,
                    minValue = 0f,
                    maxValue = 100f,
                    startAngleDegrees = 135f,
                    sweepAngleDegrees = 270f,
                ),
        )
    }

    @Test
    fun angle_maxValueMapsToEndAngle() {
        assertEquals(
            expected = 405f,
            actual =
                gaugeAngleForValue(
                    value = 100f,
                    minValue = 0f,
                    maxValue = 100f,
                    startAngleDegrees = 135f,
                    sweepAngleDegrees = 270f,
                ),
        )
    }

    @Test
    fun angle_midValueMapsToMidSweep() {
        assertEquals(
            expected = 270f,
            actual =
                gaugeAngleForValue(
                    value = 50f,
                    minValue = 0f,
                    maxValue = 100f,
                    startAngleDegrees = 135f,
                    sweepAngleDegrees = 270f,
                ),
        )
    }

    @Test
    fun angle_clampsOutOfRangeValuesToDialEndpoints() {
        assertEquals(
            expected = 135f,
            actual =
                gaugeAngleForValue(
                    value = -50f,
                    minValue = 0f,
                    maxValue = 100f,
                    startAngleDegrees = 135f,
                    sweepAngleDegrees = 270f,
                ),
        )
        assertEquals(
            expected = 405f,
            actual =
                gaugeAngleForValue(
                    value = 500f,
                    minValue = 0f,
                    maxValue = 100f,
                    startAngleDegrees = 135f,
                    sweepAngleDegrees = 270f,
                ),
        )
    }

    @Test
    fun bandArc_computesStartAndSweep() {
        val arc =
            gaugeBandArc(
                fromValue = 20f,
                toValue = 40f,
                minValue = 0f,
                maxValue = 100f,
                startAngleDegrees = 135f,
                sweepAngleDegrees = 270f,
            )
        assertEquals(expected = 189f, actual = arc.startAngleDegrees, absoluteTolerance = 0.001f)
        assertEquals(expected = 54f, actual = arc.sweepAngleDegrees, absoluteTolerance = 0.001f)
    }

    @Test
    fun bandArc_clampsBandEdgesToRange() {
        val arc =
            gaugeBandArc(
                fromValue = 80f,
                toValue = 150f,
                minValue = 0f,
                maxValue = 100f,
                startAngleDegrees = 135f,
                sweepAngleDegrees = 270f,
            )
        assertEquals(expected = 351f, actual = arc.startAngleDegrees, absoluteTolerance = 0.001f)
        assertEquals(expected = 54f, actual = arc.sweepAngleDegrees, absoluteTolerance = 0.001f)
    }

    @Test
    fun bandArc_bandEntirelyOutsideRangeHasZeroSweep() {
        val arc =
            gaugeBandArc(
                fromValue = 150f,
                toValue = 200f,
                minValue = 0f,
                maxValue = 100f,
                startAngleDegrees = 135f,
                sweepAngleDegrees = 270f,
            )
        assertEquals(expected = 0f, actual = arc.sweepAngleDegrees, absoluteTolerance = 0.001f)
    }

    @Test
    fun tickValues_areEvenlySpacedIncludingEndpoints() {
        assertEquals(
            expected = listOf(0f, 25f, 50f, 75f, 100f),
            actual = gaugeTickValues(minValue = 0f, maxValue = 100f, tickCount = 5),
        )
    }

    @Test
    fun tickValues_minimumCountYieldsEndpointsOnly() {
        assertEquals(
            expected = listOf(10f, 90f),
            actual = gaugeTickValues(minValue = 10f, maxValue = 90f, tickCount = 2),
        )
    }

    @Test
    fun tickValues_lastTickIsExactlyMax() {
        val ticks = gaugeTickValues(minValue = 0f, maxValue = 10f, tickCount = 7)
        assertEquals(expected = 10f, actual = ticks.last())
        assertEquals(expected = 7, actual = ticks.size)
    }

    @Test
    fun tickValues_rejectsTooFewTicks() {
        assertFailsWith<IllegalArgumentException> {
            gaugeTickValues(minValue = 0f, maxValue = 100f, tickCount = 1)
        }
    }

    @Test
    fun tickValues_rejectsCollapsedRange() {
        assertFailsWith<IllegalArgumentException> {
            gaugeTickValues(minValue = 50f, maxValue = 50f, tickCount = 5)
        }
    }

    @Test
    fun tickValues_areMonotonicallyIncreasing() {
        val ticks = gaugeTickValues(minValue = -20f, maxValue = 35f, tickCount = 9)
        ticks.zipWithNext { previous, next ->
            assertTrue(next > previous, "ticks must ascend but $next followed $previous")
        }
        assertEquals(expected = -20f, actual = ticks.first())
        assertEquals(expected = 35f, actual = ticks.last())
    }

    @Test
    fun bandArc_spanningWholeRangeCoversTheFullSweep() {
        val arc =
            gaugeBandArc(
                fromValue = 0f,
                toValue = 100f,
                minValue = 0f,
                maxValue = 100f,
                startAngleDegrees = 135f,
                sweepAngleDegrees = 270f,
            )
        assertEquals(expected = 135f, actual = arc.startAngleDegrees, absoluteTolerance = 0.001f)
        assertEquals(expected = 270f, actual = arc.sweepAngleDegrees, absoluteTolerance = 0.001f)
    }

    @Test
    fun bandArc_invertedSpanNeverProducesNegativeSweep() {
        val arc =
            gaugeBandArc(
                fromValue = 80f,
                toValue = 20f,
                minValue = 0f,
                maxValue = 100f,
                startAngleDegrees = 135f,
                sweepAngleDegrees = 270f,
            )
        assertEquals(expected = 0f, actual = arc.sweepAngleDegrees, absoluteTolerance = 0.001f)
    }

    @Test
    fun angle_fullCircleSweepKeepsEndpointsOneRevolutionApart() {
        val start =
            gaugeAngleForValue(
                value = 0f,
                minValue = 0f,
                maxValue = 100f,
                startAngleDegrees = -90f,
                sweepAngleDegrees = 360f,
            )
        val end =
            gaugeAngleForValue(
                value = 100f,
                minValue = 0f,
                maxValue = 100f,
                startAngleDegrees = -90f,
                sweepAngleDegrees = 360f,
            )
        assertEquals(expected = -90f, actual = start)
        assertEquals(expected = 270f, actual = end)
    }
}
