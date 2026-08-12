package com.himanshoe.charty.common.util

import kotlin.test.Test
import kotlin.test.assertEquals

class ChartLabelFormatTest {
    @Test
    fun wholeNumbersDropTheirDecimal() {
        assertEquals("54", 54f.toChartLabel())
        assertEquals("0", 0f.toChartLabel())
        assertEquals("-7", (-7f).toChartLabel())
    }

    @Test
    fun binaryExpansionIsRoundedToOneDecimal() {
        assertEquals("37.9", 37.86205291748047f.toChartLabel())
        assertEquals("71.8", 71.80310010910034f.toChartLabel())
        assertEquals("85.5", 85.50805377960205f.toChartLabel())
    }

    @Test
    fun roundingToAWholeNumberDropsTheDecimal() {
        assertEquals("48", 47.98f.toChartLabel())
        assertEquals("13", 12.999f.toChartLabel())
    }

    @Test
    fun negativeFractionsRoundTowardsTheNearestValue() {
        assertEquals("-3.2", (-3.24f).toChartLabel())
        assertEquals("-3.3", (-3.26f).toChartLabel())
    }
}
