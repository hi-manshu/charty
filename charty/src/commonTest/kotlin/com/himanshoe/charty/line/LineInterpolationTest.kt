package com.himanshoe.charty.line

import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.config.LineInterpolation
import kotlin.test.Test
import kotlin.test.assertEquals

class LineInterpolationTest {
    @Test
    fun explicitInterpolation_wins() {
        assertEquals(
            LineInterpolation.STEP,
            resolveLineInterpolation(LineChartConfig(interpolation = LineInterpolation.STEP)),
        )
        assertEquals(
            LineInterpolation.SMOOTH,
            resolveLineInterpolation(LineChartConfig(interpolation = LineInterpolation.SMOOTH)),
        )
    }

    @Test
    fun smoothCurveFlag_appliesWhenInterpolationLinear() {
        assertEquals(
            LineInterpolation.SMOOTH,
            resolveLineInterpolation(LineChartConfig(smoothCurve = true)),
        )
    }

    @Test
    fun explicitInterpolation_overridesSmoothCurveFlag() {
        assertEquals(
            LineInterpolation.STEP,
            resolveLineInterpolation(
                LineChartConfig(smoothCurve = true, interpolation = LineInterpolation.STEP),
            ),
        )
    }

    @Test
    fun defaultsToLinear() {
        assertEquals(LineInterpolation.LINEAR, resolveLineInterpolation(LineChartConfig()))
    }
}
