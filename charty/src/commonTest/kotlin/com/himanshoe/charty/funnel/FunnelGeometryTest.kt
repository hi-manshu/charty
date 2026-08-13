package com.himanshoe.charty.funnel

import com.himanshoe.charty.funnel.config.FunnelOrientation
import com.himanshoe.charty.funnel.data.FunnelStage
import com.himanshoe.charty.funnel.internal.FunnelBand
import com.himanshoe.charty.funnel.internal.funnelBandBounds
import com.himanshoe.charty.funnel.internal.funnelConversion
import com.himanshoe.charty.funnel.internal.funnelHalfExtent
import com.himanshoe.charty.funnel.internal.funnelStageFractions
import com.himanshoe.charty.funnel.internal.funnelStageRect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

private const val TOL = 0.001f

private val stages =
    listOf(
        FunnelStage(label = "Visited", value = 1000f),
        FunnelStage(label = "Signed up", value = 400f),
        FunnelStage(label = "Purchased", value = 100f),
    )

class FunnelGeometryTest {
    @Test
    fun fractions_areRelativeToTheFirstStage() {
        val fractions = funnelStageFractions(stages = stages)
        assertEquals(1f, fractions[0], TOL)
        assertEquals(0.4f, fractions[1], TOL)
        assertEquals(0.1f, fractions[2], TOL)
    }

    @Test
    fun fractions_zeroFirstStage_areAllZeroRatherThanNaN() {
        val fractions =
            funnelStageFractions(
                stages =
                    listOf(
                        FunnelStage(label = "none", value = 0f),
                        FunnelStage(label = "some", value = 5f),
                    ),
            )
        assertEquals(0f, fractions[0], TOL)
        assertEquals(0f, fractions[1], TOL)
    }

    @Test
    fun fractions_equalStages_allFillTheFullWidth() {
        val fractions =
            funnelStageFractions(
                stages = List(size = 3) { index -> FunnelStage(label = "s$index", value = 7f) },
            )
        fractions.forEach { fraction -> assertEquals(1f, fraction, TOL) }
    }

    @Test
    fun fractions_stageLargerThanTheFirst_isClampedToFullWidth() {
        val fractions =
            funnelStageFractions(
                stages =
                    listOf(
                        FunnelStage(label = "first", value = 10f),
                        FunnelStage(label = "grew", value = 25f),
                    ),
            )
        assertEquals(1f, fractions[1], TOL)
    }

    @Test
    fun fractions_emptyInput_isEmpty() {
        assertEquals(0, funnelStageFractions(stages = emptyList()).size)
    }

    @Test
    fun conversion_reportsTheShareOfThePreviousStage() {
        assertNull(funnelConversion(stages = stages, index = 0))
        assertEquals(0.4f, funnelConversion(stages = stages, index = 1) ?: 0f, TOL)
        assertEquals(0.25f, funnelConversion(stages = stages, index = 2) ?: 0f, TOL)
    }

    @Test
    fun conversion_afterAnEmptyStage_isUndefined() {
        val emptyThenSome =
            listOf(
                FunnelStage(label = "none", value = 0f),
                FunnelStage(label = "some", value = 3f),
            )
        assertNull(funnelConversion(stages = emptyThenSome, index = 1))
    }

    @Test
    fun bandBounds_divideTheFlowAxisEvenly() {
        val band = funnelBandBounds(index = 1, count = 4, flowStart = 0f, flowExtent = 400f, gap = 0f)
        assertEquals(FunnelBand(start = 100f, end = 200f), band)
    }

    @Test
    fun bandBounds_reserveTheGapBetweenNeighbours() {
        val first = funnelBandBounds(index = 0, count = 3, flowStart = 0f, flowExtent = 100f, gap = 5f)
        val second = funnelBandBounds(index = 1, count = 3, flowStart = 0f, flowExtent = 100f, gap = 5f)
        assertEquals(30f, first.extent, TOL)
        assertEquals(35f, second.start, TOL)
        assertEquals(65f, second.end, TOL)
    }

    @Test
    fun bandBounds_noStages_isAnEmptyBandAtTheOrigin() {
        val band = funnelBandBounds(index = 0, count = 0, flowStart = 12f, flowExtent = 100f, gap = 0f)
        assertEquals(0f, band.extent, TOL)
        assertEquals(12f, band.start, TOL)
    }

    @Test
    fun halfExtent_opensOutwardsWithTheAnimation() {
        assertEquals(50f, funnelHalfExtent(fraction = 1f, crossExtent = 100f, animationProgress = 1f), TOL)
        assertEquals(25f, funnelHalfExtent(fraction = 1f, crossExtent = 100f, animationProgress = 0.5f), TOL)
        assertEquals(20f, funnelHalfExtent(fraction = 0.4f, crossExtent = 100f, animationProgress = 1f), TOL)
    }

    @Test
    fun stageRect_verticalFunnel_isCentredOnTheCrossAxis() {
        val rect =
            funnelStageRect(
                band = FunnelBand(start = 0f, end = 50f),
                startHalfExtent = 40f,
                endHalfExtent = 20f,
                crossCenter = 50f,
                orientation = FunnelOrientation.VERTICAL,
            )
        assertEquals(10f, rect.left, TOL)
        assertEquals(90f, rect.right, TOL)
        assertEquals(0f, rect.top, TOL)
        assertEquals(50f, rect.bottom, TOL)
    }

    @Test
    fun stageRect_horizontalFunnel_runsAlongTheFlowAxis() {
        val rect =
            funnelStageRect(
                band = FunnelBand(start = 0f, end = 50f),
                startHalfExtent = 40f,
                endHalfExtent = 20f,
                crossCenter = 50f,
                orientation = FunnelOrientation.HORIZONTAL,
            )
        assertEquals(0f, rect.left, TOL)
        assertEquals(50f, rect.right, TOL)
        assertEquals(10f, rect.top, TOL)
        assertEquals(90f, rect.bottom, TOL)
    }

    @Test
    fun stage_rejectsNegativeValues() {
        assertFailsWith<IllegalArgumentException> { FunnelStage(label = "bad", value = -1f) }
    }
}
