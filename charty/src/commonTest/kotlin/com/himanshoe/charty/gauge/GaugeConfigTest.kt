package com.himanshoe.charty.gauge

import androidx.compose.ui.graphics.Color
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.gauge.config.AngularGaugeConfig
import com.himanshoe.charty.gauge.data.GaugeBand
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GaugeConfigTest {
    @Test
    fun defaultConfig_isValid() {
        val config = AngularGaugeConfig()
        assertEquals(expected = 0f, actual = config.minValue)
        assertEquals(expected = 100f, actual = config.maxValue)
        assertEquals(expected = 135f, actual = config.startAngleDegrees)
        assertEquals(expected = 270f, actual = config.sweepAngleDegrees)
        assertEquals(expected = 5, actual = config.tickCount)
    }

    @Test
    fun config_rejectsMaxNotGreaterThanMin() {
        assertFailsWith<IllegalArgumentException> {
            AngularGaugeConfig(minValue = 100f, maxValue = 100f)
        }
    }

    @Test
    fun config_rejectsNonPositiveSweep() {
        assertFailsWith<IllegalArgumentException> {
            AngularGaugeConfig(sweepAngleDegrees = 0f)
        }
    }

    @Test
    fun config_rejectsSweepBeyondFullCircle() {
        assertFailsWith<IllegalArgumentException> {
            AngularGaugeConfig(sweepAngleDegrees = 361f)
        }
    }

    @Test
    fun config_allowsFullCircleSweep() {
        val config = AngularGaugeConfig(sweepAngleDegrees = 360f)
        assertEquals(expected = 360f, actual = config.sweepAngleDegrees)
    }

    @Test
    fun config_rejectsTooFewTicks() {
        assertFailsWith<IllegalArgumentException> {
            AngularGaugeConfig(tickCount = 1)
        }
    }

    @Test
    fun config_rejectsTrackWidthFractionOutsideOpenUnitInterval() {
        assertFailsWith<IllegalArgumentException> {
            AngularGaugeConfig(trackWidthFraction = 0f)
        }
        assertFailsWith<IllegalArgumentException> {
            AngularGaugeConfig(trackWidthFraction = 1f)
        }
    }

    @Test
    fun config_rejectsPlotBandOutsideRange() {
        assertFailsWith<IllegalArgumentException> {
            AngularGaugeConfig(
                minValue = 0f,
                maxValue = 100f,
                plotBands =
                    listOf(
                        GaugeBand(fromValue = 50f, toValue = 150f, color = ChartyColor.Solid(Color.Red)),
                    ),
            )
        }
    }

    @Test
    fun config_acceptsBandsWithinRange() {
        val config =
            AngularGaugeConfig(
                minValue = 0f,
                maxValue = 100f,
                plotBands =
                    listOf(
                        GaugeBand(fromValue = 0f, toValue = 60f, color = ChartyColor.Solid(Color.Green)),
                        GaugeBand(fromValue = 60f, toValue = 100f, color = ChartyColor.Solid(Color.Red)),
                    ),
            )
        assertEquals(expected = 2, actual = config.plotBands.size)
    }

    @Test
    fun band_rejectsEmptyOrInvertedSpan() {
        assertFailsWith<IllegalArgumentException> {
            GaugeBand(fromValue = 40f, toValue = 40f, color = ChartyColor.Solid(Color.Red))
        }
        assertFailsWith<IllegalArgumentException> {
            GaugeBand(fromValue = 40f, toValue = 20f, color = ChartyColor.Solid(Color.Red))
        }
    }
}
