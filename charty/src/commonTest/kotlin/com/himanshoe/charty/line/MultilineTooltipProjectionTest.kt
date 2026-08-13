package com.himanshoe.charty.line

import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineGroup
import com.himanshoe.charty.line.data.MultilinePoint
import com.himanshoe.charty.line.internal.multiline.crosshairSeriesLineData
import com.himanshoe.charty.line.internal.multiline.toTooltipLineData
import kotlin.test.Test
import kotlin.test.assertEquals

class MultilineTooltipProjectionTest {
    private val group = LineGroup(label = "Jan", values = listOf(20f, 12f))

    @Test
    fun tappedPointCarriesItsGroupLabelAndOneBasedSeriesNumber() {
        val point = MultilinePoint(lineGroup = group, seriesIndex = 1, dataIndex = 0, value = 12f)
        val projected = point.toTooltipLineData()
        assertEquals("Jan Line 2", projected.label)
        assertEquals(12f, projected.value)
    }

    @Test
    fun crosshairEntryIsLabelledByItsOneBasedSeriesNumber() {
        assertEquals("L1", crosshairSeriesLineData(seriesIndex = 0, value = 20f).label)
        assertEquals("L3", crosshairSeriesLineData(seriesIndex = 2, value = 1f).label)
    }

    @Test
    fun defaultFormatterRendersTheProjectedPoint() {
        val point = MultilinePoint(lineGroup = group, seriesIndex = 0, dataIndex = 0, value = 20f)
        assertEquals("Jan Line 1: 20", LineChartConfig().tooltipFormatter(point.toTooltipLineData()))
    }

    @Test
    fun customFormatterReceivesTheProjectedPoint() {
        val config = LineChartConfig(tooltipFormatter = { data -> "${data.label}=${data.value.toInt()}" })
        val point = MultilinePoint(lineGroup = group, seriesIndex = 1, dataIndex = 0, value = 12f)
        assertEquals("Jan Line 2=12", config.tooltipFormatter(point.toTooltipLineData()))
    }

    @Test
    fun theProjectionCarriesLabelAndValueRatherThanAFormattedString() {
        val point = MultilinePoint(lineGroup = group, seriesIndex = 1, dataIndex = 0, value = 12f)
        val projected = point.toTooltipLineData()
        assertEquals("Jan Line 2", projected.label)
        assertEquals(12f, projected.value)
    }
}
