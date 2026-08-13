package com.himanshoe.charty.line

import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineGroup
import com.himanshoe.charty.line.data.StackedAreaPoint
import com.himanshoe.charty.line.internal.stackedarea.toTooltipLineData
import kotlin.test.Test
import kotlin.test.assertEquals

class StackedAreaTooltipProjectionTest {
    private val group = LineGroup(label = "Jan", values = listOf(10f, 20f, 5f))

    private val segment =
        StackedAreaPoint(
            lineGroup = group,
            seriesIndex = 1,
            dataIndex = 0,
            value = 20f,
            cumulativeValue = 30f,
        )

    @Test
    fun theProjectionCarriesTheGroupLabelAndTheSegmentsOwnValue() {
        val projected = segment.toTooltipLineData()
        assertEquals("Jan", projected.label)
        assertEquals(20f, projected.value)
    }

    @Test
    fun defaultFormatterRendersTheProjectedSegment() {
        assertEquals("Jan: 20", LineChartConfig().tooltipFormatter(segment.toTooltipLineData()))
    }

    @Test
    fun customFormatterReceivesTheProjectedSegment() {
        val config = LineChartConfig(tooltipFormatter = { data -> "${data.label}=${data.value.toInt()}" })
        assertEquals("Jan=20", config.tooltipFormatter(segment.toTooltipLineData()))
    }
}
