package com.himanshoe.charty.circular.rings

import com.himanshoe.charty.circular.rings.config.ActivityRingsConfig
import com.himanshoe.charty.circular.rings.data.RingData
import kotlin.test.Test
import kotlin.test.assertFailsWith

class ActivityRingsValidationTest {
    @Test
    fun ringData_acceptsValidValues() {
        RingData(label = "Move", value = 0f, target = 100f)
        RingData(label = "Move", value = 150f, target = 100f)
    }

    @Test
    fun ringData_rejectsNonPositiveTarget() {
        assertFailsWith<IllegalArgumentException> { RingData(label = "Move", value = 10f, target = 0f) }
        assertFailsWith<IllegalArgumentException> { RingData(label = "Move", value = 10f, target = -5f) }
    }

    @Test
    fun ringData_rejectsNegativeValue() {
        assertFailsWith<IllegalArgumentException> { RingData(label = "Move", value = -1f, target = 100f) }
    }

    @Test
    fun config_defaults_areValid() {
        ActivityRingsConfig()
    }

    @Test
    fun config_rejectsThicknessFractionOutsideOpenUnitInterval() {
        assertFailsWith<IllegalArgumentException> { ActivityRingsConfig(ringThicknessFraction = 0f) }
        assertFailsWith<IllegalArgumentException> { ActivityRingsConfig(ringThicknessFraction = 1f) }
        assertFailsWith<IllegalArgumentException> { ActivityRingsConfig(ringThicknessFraction = -0.1f) }
    }

    @Test
    fun config_rejectsGapFractionOutOfRange() {
        assertFailsWith<IllegalArgumentException> { ActivityRingsConfig(ringGapFraction = -0.1f) }
        assertFailsWith<IllegalArgumentException> { ActivityRingsConfig(ringGapFraction = 1f) }
    }

    @Test
    fun config_allowsZeroGapFraction() {
        ActivityRingsConfig(ringGapFraction = 0f)
    }

    @Test
    fun config_rejectsTrackAlphaOutOfRange() {
        assertFailsWith<IllegalArgumentException> { ActivityRingsConfig(trackAlpha = -0.1f) }
        assertFailsWith<IllegalArgumentException> { ActivityRingsConfig(trackAlpha = 1.1f) }
    }
}
