package com.himanshoe.charty.line

import androidx.compose.ui.geometry.Offset
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.line.config.SparklineConfig
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SparklineTest {
    @Test
    fun normalize_emptyList_returnsEmpty() {
        assertTrue(normalizeToYFractions(emptyList()).isEmpty())
    }

    @Test
    fun normalize_singleValue_centersAtMidHeight() {
        assertContentEquals(listOf(0.5f), normalizeToYFractions(listOf(42f)))
    }

    @Test
    fun normalize_allEqualValues_centersFlatLine() {
        assertContentEquals(listOf(0.5f, 0.5f, 0.5f, 0.5f), normalizeToYFractions(listOf(7f, 7f, 7f, 7f)))
    }

    @Test
    fun normalize_mapsMaxToTopAndMinToBottom() {
        val fractions = normalizeToYFractions(listOf(0f, 10f, 5f))
        assertEquals(1f, fractions[0])
        assertEquals(0f, fractions[1])
        assertEquals(0.5f, fractions[2])
    }

    @Test
    fun normalize_handlesNegativeValues() {
        val fractions = normalizeToYFractions(listOf(-10f, 0f, 10f))
        assertEquals(1f, fractions[0])
        assertEquals(0.5f, fractions[1])
        assertEquals(0f, fractions[2])
    }

    @Test
    fun normalize_keepsOutputSizeEqualToInputSize() {
        val values = listOf(3f, 8f, 1f, 9f, 4f)
        assertEquals(values.size, normalizeToYFractions(values).size)
    }

    @Test
    fun points_emptyFractions_returnsEmpty() {
        assertTrue(sparklinePoints(yFractions = emptyList(), width = 100f, height = 40f).isEmpty())
    }

    @Test
    fun points_singleFraction_spansFullWidthAtFractionHeight() {
        val points = sparklinePoints(yFractions = listOf(0.5f), width = 100f, height = 40f)
        assertContentEquals(listOf(Offset(x = 0f, y = 20f), Offset(x = 100f, y = 20f)), points)
    }

    @Test
    fun points_spacedEvenlyAcrossFullDrawSize() {
        val points = sparklinePoints(yFractions = listOf(0f, 0.5f, 1f), width = 100f, height = 40f)
        assertEquals(3, points.size)
        assertEquals(0f, points[0].x)
        assertEquals(50f, points[1].x)
        assertEquals(100f, points[2].x)
        assertEquals(0f, points[0].y)
        assertEquals(20f, points[1].y)
        assertEquals(40f, points[2].y)
    }

    @Test
    fun config_defaultsMatchSparklinePreset() {
        val config = SparklineConfig()
        assertEquals(1.5f, config.lineWidth)
        assertTrue(config.showFill)
        assertEquals(0.15f, config.fillAlpha)
        assertTrue(config.showLastPointDot)
        assertEquals(3f, config.lastPointDotRadius)
        assertFalse(config.smoothCurve)
        assertEquals(Animation.Disabled, config.animation)
    }

    @Test
    fun config_rejectsNonPositiveLineWidth() {
        assertFailsWith<IllegalArgumentException> { SparklineConfig(lineWidth = 0f) }
        assertFailsWith<IllegalArgumentException> { SparklineConfig(lineWidth = -1f) }
    }

    @Test
    fun config_rejectsFillAlphaOutsideZeroToOne() {
        assertFailsWith<IllegalArgumentException> { SparklineConfig(fillAlpha = 1.5f) }
        assertFailsWith<IllegalArgumentException> { SparklineConfig(fillAlpha = -0.1f) }
    }

    @Test
    fun config_rejectsNonPositiveDotRadius() {
        assertFailsWith<IllegalArgumentException> { SparklineConfig(lastPointDotRadius = 0f) }
    }

    @Test
    fun description_reportsCountEndpointsAndRange() {
        assertEquals(
            "Sparkline, 4 values from 3 to 9. Range: 1 to 9.",
            buildSparklineDescription(listOf(3f, 1f, 5f, 9f)),
        )
    }

    @Test
    fun description_allEqualValues_reportsAFlatRange() {
        assertEquals(
            "Sparkline, 3 values from 7 to 7. Range: 7 to 7.",
            buildSparklineDescription(listOf(7f, 7f, 7f)),
        )
    }

    @Test
    fun description_emptySeries() {
        assertEquals("Empty sparkline.", buildSparklineDescription(emptyList()))
    }
}
