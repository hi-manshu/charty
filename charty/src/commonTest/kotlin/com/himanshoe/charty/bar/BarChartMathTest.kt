package com.himanshoe.charty.bar

import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.bar.internal.bar.barchart.barValueRange
import com.himanshoe.charty.bar.internal.bar.barchart.calculateVerticalBarDimensions
import com.himanshoe.charty.bar.internal.bar.horizontal.calculateHorizontalBaselineX
import com.himanshoe.charty.common.ChartContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val TOL = 0.001f

class BarChartMathTest {
    @Test
    fun barValueRange_belowAxis_clampsPositiveMinToZero() {
        val (min, max) = barValueRange(listOf(15f, 65f), NegativeValuesDrawMode.BELOW_AXIS)
        assertEquals(0f, min, TOL)
        assertEquals(70f, max, TOL)
    }

    @Test
    fun barValueRange_fromMinValue_keepsRealMinForPositiveData() {
        val (min, max) = barValueRange(listOf(15f, 65f), NegativeValuesDrawMode.FROM_MIN_VALUE)
        assertEquals(10f, min, TOL)
        assertEquals(70f, max, TOL)
    }

    @Test
    fun barValueRange_negativeData_bothModesKeepNegativeMin() {
        val below = barValueRange(listOf(-5f, 20f), NegativeValuesDrawMode.BELOW_AXIS)
        val fromMin = barValueRange(listOf(-5f, 20f), NegativeValuesDrawMode.FROM_MIN_VALUE)
        assertEquals(-10f, below.first, TOL)
        assertEquals(-10f, fromMin.first, TOL)
    }

    @Test
    fun verticalBarDimensions_belowAxisNegative_drawsDownwardFromBaseline() {
        val (top, height) =
            calculateVerticalBarDimensions(
                isNegative = true,
                isBelowAxisMode = true,
                baselineY = 50f,
                barValueY = 80f,
                animationProgress = 1f,
            )
        assertEquals(50f, top, TOL)
        assertEquals(30f, height, TOL)
    }

    @Test
    fun verticalBarDimensions_fromMinValueNegative_growsUpwardWithPositiveHeight() {
        val (top, height) =
            calculateVerticalBarDimensions(
                isNegative = true,
                isBelowAxisMode = false,
                baselineY = 100f,
                barValueY = 70f,
                animationProgress = 1f,
            )
        assertEquals(70f, top, TOL)
        assertEquals(30f, height, TOL)
        assertTrue(height >= 0f)
    }

    @Test
    fun verticalBarDimensions_positiveBar_scalesWithProgress() {
        val (top, height) =
            calculateVerticalBarDimensions(
                isNegative = false,
                isBelowAxisMode = true,
                baselineY = 100f,
                barValueY = 40f,
                animationProgress = 0.5f,
            )
        assertEquals(70f, top, TOL)
        assertEquals(30f, height, TOL)
    }

    @Test
    fun horizontalBaselineX_zeroRange_returnsLeft() {
        val ctx = ChartContext(left = 0f, top = 0f, right = 100f, bottom = 100f, minValue = 0f, maxValue = 0f)
        assertEquals(
            0f,
            calculateHorizontalBaselineX(drawAxisAtZero = true, minValue = 0f, maxValue = 0f, chartContext = ctx),
            TOL,
        )
    }

    @Test
    fun horizontalBaselineX_spanningZero_placesAxisProportionally() {
        val ctx = ChartContext(left = 0f, top = 0f, right = 100f, bottom = 100f, minValue = -10f, maxValue = 10f)
        assertEquals(
            50f,
            calculateHorizontalBaselineX(drawAxisAtZero = true, minValue = -10f, maxValue = 10f, chartContext = ctx),
            TOL,
        )
    }

    @Test
    fun horizontalBaselineX_notDrawnAtZero_returnsLeft() {
        val ctx = ChartContext(left = 5f, top = 0f, right = 100f, bottom = 100f, minValue = 0f, maxValue = 50f)
        assertEquals(
            5f,
            calculateHorizontalBaselineX(drawAxisAtZero = false, minValue = 0f, maxValue = 50f, chartContext = ctx),
            TOL,
        )
    }
}
