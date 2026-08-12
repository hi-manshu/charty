package com.himanshoe.charty.common.annotation

import kotlin.test.Test
import kotlin.test.assertEquals

private const val TOL = 0.001f

class AnnotationAnchorResolverTest {
    private fun resolve(
        anchor: AnnotationAnchor,
        totalItems: Int = 4,
        minValue: Float = 0f,
        maxValue: Float = 100f,
        centeredXOf: ((Int) -> Float)? = null,
    ) = resolveAnnotationAnchor(
        anchor = anchor,
        left = 0f,
        top = 0f,
        right = 100f,
        bottom = 100f,
        minValue = minValue,
        maxValue = maxValue,
        totalItems = totalItems,
        centeredXOf = centeredXOf,
    )

    @Test
    fun dataPoint_resolvesToCategoryCentre_forFourItems() {
        assertEquals(12.5f, resolve(anchor = AnnotationAnchor.DataPoint(index = 0)).x, TOL)
        assertEquals(37.5f, resolve(anchor = AnnotationAnchor.DataPoint(index = 1)).x, TOL)
        assertEquals(87.5f, resolve(anchor = AnnotationAnchor.DataPoint(index = 3)).x, TOL)
    }

    @Test
    fun dataPoint_resolvesToCategoryCentre_forOtherTotals() {
        assertEquals(50f, resolve(anchor = AnnotationAnchor.DataPoint(index = 0), totalItems = 1).x, TOL)
        assertEquals(10f, resolve(anchor = AnnotationAnchor.DataPoint(index = 0), totalItems = 5).x, TOL)
        assertEquals(70f, resolve(anchor = AnnotationAnchor.DataPoint(index = 3), totalItems = 5).x, TOL)
    }

    @Test
    fun dataPoint_usesPlotCentre_whenNoItems() {
        assertEquals(50f, resolve(anchor = AnnotationAnchor.DataPoint(index = 2), totalItems = 0).x, TOL)
    }

    @Test
    fun dataPoint_isVerticallyCentredInPlot() {
        assertEquals(50f, resolve(anchor = AnnotationAnchor.DataPoint(index = 1)).y, TOL)
    }

    @Test
    fun dataPoint_prefersSuppliedLayoutPositions() {
        val resolved =
            resolve(anchor = AnnotationAnchor.DataPoint(index = 2), centeredXOf = { index -> 5f + index * 10f })
        assertEquals(25f, resolved.x, TOL)
    }

    @Test
    fun value_mapsYAcrossTheValueRange() {
        assertEquals(100f, resolve(anchor = AnnotationAnchor.Value(y = 0f)).y, TOL)
        assertEquals(50f, resolve(anchor = AnnotationAnchor.Value(y = 50f)).y, TOL)
        assertEquals(0f, resolve(anchor = AnnotationAnchor.Value(y = 100f)).y, TOL)
    }

    @Test
    fun value_mapsYAcrossAShiftedRange() {
        val resolved = resolve(anchor = AnnotationAnchor.Value(y = 30f), minValue = 20f, maxValue = 40f)
        assertEquals(50f, resolved.y, TOL)
    }

    @Test
    fun value_zeroRange_fallsBackToBottom() {
        val resolved = resolve(anchor = AnnotationAnchor.Value(y = 7f), minValue = 7f, maxValue = 7f)
        assertEquals(100f, resolved.y, TOL)
    }

    @Test
    fun value_nullX_usesPlotCentre() {
        assertEquals(50f, resolve(anchor = AnnotationAnchor.Value(y = 10f)).x, TOL)
    }

    @Test
    fun value_explicitX_usesCategoryScale() {
        assertEquals(37.5f, resolve(anchor = AnnotationAnchor.Value(x = 1f, y = 10f)).x, TOL)
        assertEquals(25f, resolve(anchor = AnnotationAnchor.Value(x = 0.5f, y = 10f)).x, TOL)
    }

    @Test
    fun pixelOffset_passesThroughRelativeToPlotOrigin() {
        val resolved = resolve(anchor = AnnotationAnchor.PixelOffset(x = 20f, y = 30f))
        assertEquals(20f, resolved.x, TOL)
        assertEquals(30f, resolved.y, TOL)
    }

    @Test
    fun pixelOffset_isRelativeToANonZeroPlotOrigin() {
        val resolved =
            resolveAnnotationAnchor(
                anchor = AnnotationAnchor.PixelOffset(x = 20f, y = 30f),
                left = 10f,
                top = 5f,
                right = 110f,
                bottom = 105f,
                minValue = 0f,
                maxValue = 100f,
                totalItems = 4,
            )
        assertEquals(30f, resolved.x, TOL)
        assertEquals(35f, resolved.y, TOL)
    }

    @Test
    fun anchorsAreClampedToThePlotEdges() {
        val below = resolve(anchor = AnnotationAnchor.Value(y = -50f))
        assertEquals(100f, below.y, TOL)
        val above = resolve(anchor = AnnotationAnchor.Value(y = 500f))
        assertEquals(0f, above.y, TOL)
        val offRight = resolve(anchor = AnnotationAnchor.PixelOffset(x = 400f, y = 10f))
        assertEquals(100f, offRight.x, TOL)
        val offLeft = resolve(anchor = AnnotationAnchor.PixelOffset(x = -400f, y = 10f))
        assertEquals(0f, offLeft.x, TOL)
    }

    @Test
    fun dataPointOutsideTheWindow_isClampedToThePlot() {
        assertEquals(100f, resolve(anchor = AnnotationAnchor.DataPoint(index = 40)).x, TOL)
        assertEquals(0f, resolve(anchor = AnnotationAnchor.DataPoint(index = -40)).x, TOL)
    }
}
