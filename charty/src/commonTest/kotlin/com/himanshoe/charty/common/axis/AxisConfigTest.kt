package com.himanshoe.charty.common.axis

import kotlin.test.Test
import kotlin.test.assertEquals

class AxisConfigTest {
    @Test
    fun formatAxisLabel_integerValues_haveNoDecimalPoint() {
        assertEquals("5", formatAxisLabel(5f))
        assertEquals("0", formatAxisLabel(0f))
        assertEquals("-3", formatAxisLabel(-3f))
        assertEquals("100", formatAxisLabel(100f))
    }

    @Test
    fun formatAxisLabel_floatValues_trimmedToTwoPlaces() {
        assertEquals("5.5", formatAxisLabel(5.5f))
        assertEquals("5.25", formatAxisLabel(5.25f))
    }

    @Test
    fun formatAxisLabel_trailingZerosTrimmed() {
        assertEquals("5.5", formatAxisLabel(5.50f))
    }

    @Test
    fun formatAxisLabel_largeIntegerValue_noOverflowNoScientificNotation() {
        // regression: 3e9 exceeds Int.MAX_VALUE (old toInt() overflowed to a negative)
        assertEquals("3000000000", formatAxisLabel(3_000_000_000f))
        assertEquals("150000000", formatAxisLabel(150_000_000f))
    }

    @Test
    fun valueFormatter_defaultsToFormatAxisLabel() {
        val config = AxisConfig(minValue = 0f, maxValue = 100f, steps = 5)
        assertEquals(formatAxisLabel(42f), config.valueFormatter(42f))
        assertEquals("42", config.valueFormatter(42f))
    }

    @Test
    fun valueFormatter_customOverrideIsUsed() {
        val config = AxisConfig(valueFormatter = { "${'$'}${it.toInt()}" })
        assertEquals("\$42", config.valueFormatter(42f))
    }
}
