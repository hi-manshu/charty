package com.himanshoe.charty.diverging

import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.diverging.config.DivergingSideScaling
import com.himanshoe.charty.diverging.data.DivergingData
import com.himanshoe.charty.diverging.data.DivergingSide
import com.himanshoe.charty.diverging.internal.divergingBarRect
import com.himanshoe.charty.diverging.internal.divergingCenterX
import com.himanshoe.charty.diverging.internal.divergingHalfWidth
import com.himanshoe.charty.diverging.internal.divergingScales
import com.himanshoe.charty.diverging.internal.divergingValueRange
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

private const val TOL = 0.001f

private val context =
    ChartContext(left = 0f, top = 0f, right = 200f, bottom = 100f, minValue = -10f, maxValue = 10f)

private val rows =
    listOf(
        DivergingData(label = "a", leftValue = 4f, rightValue = 10f),
        DivergingData(label = "b", leftValue = 8f, rightValue = 2f),
    )

class DivergingGeometryTest {
    @Test
    fun scales_shared_useTheMaximumAcrossBothSides() {
        val scales = divergingScales(dataList = rows, sideScaling = DivergingSideScaling.SHARED)
        assertEquals(10f, scales.left, TOL)
        assertEquals(10f, scales.right, TOL)
    }

    @Test
    fun scales_independent_useEachSideOwnMaximum() {
        val scales = divergingScales(dataList = rows, sideScaling = DivergingSideScaling.INDEPENDENT)
        assertEquals(8f, scales.left, TOL)
        assertEquals(10f, scales.right, TOL)
    }

    @Test
    fun scales_empty_reportZeroForBothSides() {
        val scales = divergingScales(dataList = emptyList(), sideScaling = DivergingSideScaling.SHARED)
        assertEquals(0f, scales.left, TOL)
        assertEquals(0f, scales.right, TOL)
    }

    @Test
    fun valueRange_isSymmetricAroundZero() {
        val (min, max) = divergingValueRange(divergingScales(rows, DivergingSideScaling.SHARED))
        assertEquals(-10f, min, TOL)
        assertEquals(10f, max, TOL)
    }

    @Test
    fun centre_sitsOnThePlotMidpointWhicheverSideIsLarger() {
        assertEquals(100f, divergingCenterX(chartContext = context), TOL)
    }

    @Test
    fun halfWidth_reservesTheCentreGap() {
        assertEquals(100f, divergingHalfWidth(chartContext = context, centerGapFraction = 0f), TOL)
        assertEquals(90f, divergingHalfWidth(chartContext = context, centerGapFraction = 0.1f), TOL)
    }

    @Test
    fun barRect_bothSidesMeetAtTheCentreWithoutAGap() {
        val left = barRect(side = DivergingSide.LEFT, value = 10f, scale = 10f, gap = 0f)
        val right = barRect(side = DivergingSide.RIGHT, value = 10f, scale = 10f, gap = 0f)
        assertEquals(100f, left.right, TOL)
        assertEquals(100f, right.left, TOL)
        assertEquals(0f, left.left, TOL)
        assertEquals(200f, right.right, TOL)
    }

    @Test
    fun barRect_sharedScaleKeepsTheTwoHalvesComparable() {
        val left = barRect(side = DivergingSide.LEFT, value = 5f, scale = 10f, gap = 0f)
        val right = barRect(side = DivergingSide.RIGHT, value = 5f, scale = 10f, gap = 0f)
        assertEquals(left.width, right.width, TOL)
        assertEquals(50f, left.width, TOL)
    }

    @Test
    fun barRect_independentScaleStretchesTheSmallerSideToTheEdge() {
        val left = barRect(side = DivergingSide.LEFT, value = 8f, scale = 8f, gap = 0f)
        assertEquals(100f, left.width, TOL)
    }

    @Test
    fun barRect_centreGapSplitsEvenlyBetweenTheHalves() {
        val left = barRect(side = DivergingSide.LEFT, value = 10f, scale = 10f, gap = 0.1f)
        val right = barRect(side = DivergingSide.RIGHT, value = 10f, scale = 10f, gap = 0.1f)
        assertEquals(90f, left.right, TOL)
        assertEquals(110f, right.left, TOL)
        assertEquals(90f, left.width, TOL)
        assertEquals(90f, right.width, TOL)
    }

    @Test
    fun barRect_zeroScaleDrawsNothingInsteadOfDividingByZero() {
        val rect = barRect(side = DivergingSide.RIGHT, value = 0f, scale = 0f, gap = 0f)
        assertEquals(0f, rect.width, TOL)
    }

    @Test
    fun barRect_animationProgressScalesTheLength() {
        val rect =
            divergingBarRect(
                chartContext = context,
                index = 0,
                rowCount = 2,
                side = DivergingSide.RIGHT,
                value = 10f,
                scale = 10f,
                barWidthFraction = 1f,
                centerGapFraction = 0f,
                animationProgress = 0.5f,
            )
        assertEquals(50f, rect.width, TOL)
    }

    @Test
    fun barRect_rowsSplitThePlotHeightEvenly() {
        val first = barRect(side = DivergingSide.RIGHT, value = 10f, scale = 10f, gap = 0f, index = 0)
        val second = barRect(side = DivergingSide.RIGHT, value = 10f, scale = 10f, gap = 0f, index = 1)
        assertEquals(0f, first.top, TOL)
        assertEquals(50f, first.bottom, TOL)
        assertEquals(50f, second.top, TOL)
        assertEquals(100f, second.bottom, TOL)
    }

    @Test
    fun data_rejectsNegativeMagnitudes() {
        assertFailsWith<IllegalArgumentException> {
            DivergingData(label = "a", leftValue = -1f, rightValue = 1f)
        }
        assertFailsWith<IllegalArgumentException> {
            DivergingData(label = "a", leftValue = 1f, rightValue = -1f)
        }
    }

    private fun barRect(
        side: DivergingSide,
        value: Float,
        scale: Float,
        gap: Float,
        index: Int = 0,
    ) = divergingBarRect(
        chartContext = context,
        index = index,
        rowCount = 2,
        side = side,
        value = value,
        scale = scale,
        barWidthFraction = 1f,
        centerGapFraction = gap,
        animationProgress = 1f,
    )
}
