package com.himanshoe.charty.combo

import com.himanshoe.charty.combo.config.ComboChartConfig
import com.himanshoe.charty.combo.data.ComboChartData
import kotlin.test.Test
import kotlin.test.assertEquals

class ComboTooltipTest {
    private val point = ComboChartData(label = "Jan", barValue = 20f, lineValue = 12f)

    @Test
    fun defaultFormatterNamesBothSeriesBecauseATapResolvesToTheWholePoint() {
        assertEquals("Jan: Bar=20, Line=12", ComboChartConfig().tooltipFormatter(point))
    }

    @Test
    fun customFormatterReceivesBothValues() {
        val config =
            ComboChartConfig(
                tooltipFormatter = { data -> "${data.label} ${data.barValue.toInt()}/${data.lineValue.toInt()}" },
            )
        assertEquals("Jan 20/12", config.tooltipFormatter(point))
    }
}
