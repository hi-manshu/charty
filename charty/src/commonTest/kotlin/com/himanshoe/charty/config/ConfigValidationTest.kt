package com.himanshoe.charty.config

import com.himanshoe.charty.common.config.ReferenceLineConfig
import com.himanshoe.charty.line.config.LineChartConfig
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ConfigValidationTest {
    @Test
    fun lineChartConfig_defaults_areValid() {
        LineChartConfig() // must not throw
    }

    @Test
    fun lineChartConfig_rejectsNonPositiveLineWidth() {
        assertFailsWith<IllegalArgumentException> { LineChartConfig(lineWidth = 0f) }
    }

    @Test
    fun lineChartConfig_rejectsNonPositivePointRadius() {
        assertFailsWith<IllegalArgumentException> { LineChartConfig(pointRadius = 0f) }
    }

    @Test
    fun lineChartConfig_rejectsPointAlphaOutOfRange() {
        assertFailsWith<IllegalArgumentException> { LineChartConfig(pointAlpha = 1.5f) }
    }

    @Test
    fun lineChartConfig_rejectsFillAlphaOutOfRange() {
        assertFailsWith<IllegalArgumentException> { LineChartConfig(fillAlpha = 2f) }
    }

    @Test
    fun referenceLineConfig_rejectsNonPositiveStrokeWidth() {
        assertFailsWith<IllegalArgumentException> {
            ReferenceLineConfig(value = 50f, strokeWidth = 0f)
        }
    }

    @Test
    fun referenceLineConfig_rejectsNegativeLabelOffset() {
        assertFailsWith<IllegalArgumentException> {
            ReferenceLineConfig(value = 50f, labelOffset = -1f)
        }
    }
}
