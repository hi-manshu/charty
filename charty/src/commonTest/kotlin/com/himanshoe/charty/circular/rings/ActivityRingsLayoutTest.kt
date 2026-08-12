package com.himanshoe.charty.circular.rings

import androidx.compose.ui.graphics.Color
import com.himanshoe.charty.circular.rings.data.RingData
import com.himanshoe.charty.circular.rings.internal.buildActivityRingsDescription
import com.himanshoe.charty.circular.rings.internal.requiredRadiusFraction
import com.himanshoe.charty.circular.rings.internal.resolveRingColor
import com.himanshoe.charty.circular.rings.internal.ringLayouts
import com.himanshoe.charty.circular.rings.internal.ringScale
import com.himanshoe.charty.circular.rings.internal.ringSweepFraction
import com.himanshoe.charty.color.ChartyColor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val EPSILON = 0.0001f

class ActivityRingsLayoutTest {
    @Test
    fun sweepFraction_isValueOverTarget() {
        assertEquals(0.5f, ringSweepFraction(value = 50f, target = 100f), EPSILON)
        assertEquals(0.25f, ringSweepFraction(value = 25f, target = 100f), EPSILON)
    }

    @Test
    fun sweepFraction_capsAtOneWhenValueExceedsTarget() {
        assertEquals(1f, ringSweepFraction(value = 150f, target = 100f), EPSILON)
        assertEquals(1f, ringSweepFraction(value = 100f, target = 100f), EPSILON)
    }

    @Test
    fun sweepFraction_isZeroForZeroValue() {
        assertEquals(0f, ringSweepFraction(value = 0f, target = 100f), EPSILON)
    }

    @Test
    fun requiredRadiusFraction_sumsThicknessAndGaps() {
        assertEquals(
            3 * 0.1f + 2 * 0.05f,
            requiredRadiusFraction(ringCount = 3, thicknessFraction = 0.1f, ringGapFraction = 0.05f),
            EPSILON,
        )
    }

    @Test
    fun ringScale_isOneWhenRingsFit() {
        assertEquals(
            1f,
            ringScale(ringCount = 3, thicknessFraction = 0.1f, ringGapFraction = 0.05f),
            EPSILON,
        )
    }

    @Test
    fun ringScale_shrinksWhenRingsWouldOverflow() {
        val scale = ringScale(ringCount = 10, thicknessFraction = 0.2f, ringGapFraction = 0.05f)
        assertTrue(scale < 1f)
        val required = requiredRadiusFraction(ringCount = 10, thicknessFraction = 0.2f, ringGapFraction = 0.05f)
        assertEquals(1f, required * scale, EPSILON)
    }

    @Test
    fun ringLayouts_firstRingHugsTheOuterEdge() {
        val layouts = ringLayouts(ringCount = 3, outerRadius = 100f, thicknessFraction = 0.1f, ringGapFraction = 0.05f)
        val outerEdge = layouts.first().centerRadius + layouts.first().strokeWidth / 2f
        assertEquals(100f, outerEdge, EPSILON)
    }

    @Test
    fun ringLayouts_adjacentRingsAreSeparatedByStrokePlusGap() {
        val layouts = ringLayouts(ringCount = 3, outerRadius = 100f, thicknessFraction = 0.1f, ringGapFraction = 0.05f)
        val expectedStep = 100f * 0.1f + 100f * 0.05f
        assertEquals(expectedStep, layouts[0].centerRadius - layouts[1].centerRadius, EPSILON)
        assertEquals(expectedStep, layouts[1].centerRadius - layouts[2].centerRadius, EPSILON)
    }

    @Test
    fun ringLayouts_manyThickRingsStillFitInsideTheRadius() {
        val layouts = ringLayouts(ringCount = 12, outerRadius = 100f, thicknessFraction = 0.3f, ringGapFraction = 0.1f)
        assertEquals(12, layouts.size)
        layouts.forEach { layout ->
            assertTrue(layout.centerRadius - layout.strokeWidth / 2f >= -EPSILON)
            assertTrue(layout.strokeWidth > 0f)
        }
    }

    @Test
    fun ringLayouts_isEmptyForNoRingsOrNoSpace() {
        val noRings = ringLayouts(ringCount = 0, outerRadius = 100f, thicknessFraction = 0.1f, ringGapFraction = 0.05f)
        val noSpace = ringLayouts(ringCount = 3, outerRadius = 0f, thicknessFraction = 0.1f, ringGapFraction = 0.05f)
        assertTrue(noRings.isEmpty())
        assertTrue(noSpace.isEmpty())
    }

    @Test
    fun resolveRingColor_prefersTheRingsOwnColor() {
        val own = ChartyColor.Solid(Color.Red)
        val fallback = ChartyColor.Solid(Color.Blue)
        assertEquals(own, resolveRingColor(ringColor = own, index = 0, fallback = fallback))
    }

    @Test
    fun resolveRingColor_solidFallbackColorsEveryRingTheSame() {
        val fallback = ChartyColor.Solid(Color.Blue)
        assertEquals(fallback, resolveRingColor(ringColor = null, index = 0, fallback = fallback))
        assertEquals(fallback, resolveRingColor(ringColor = null, index = 2, fallback = fallback))
    }

    @Test
    fun resolveRingColor_gradientFallbackCyclesPerRing() {
        val fallback = ChartyColor.Gradient(listOf(Color.Red, Color.Green))
        assertEquals(
            ChartyColor.Solid(Color.Red),
            resolveRingColor(ringColor = null, index = 0, fallback = fallback),
        )
        assertEquals(
            ChartyColor.Solid(Color.Green),
            resolveRingColor(ringColor = null, index = 1, fallback = fallback),
        )
        assertEquals(
            ChartyColor.Solid(Color.Red),
            resolveRingColor(ringColor = null, index = 2, fallback = fallback),
        )
    }

    @Test
    fun description_listsEveryRingWithCappedPercent() {
        val description =
            buildActivityRingsDescription(
                listOf(
                    RingData(label = "Move", value = 75f, target = 100f),
                    RingData(label = "Exercise", value = 45f, target = 30f),
                ),
            )
        assertEquals(
            "Activity rings chart with 2 rings: Move at 75 percent of target, Exercise at 100 percent of target",
            description,
        )
    }
}
