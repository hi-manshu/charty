package com.himanshoe.charty.line.internal.multiline

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.util.fastMapIndexed
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.line.data.LineData
import com.himanshoe.charty.line.data.LineGroup
import com.himanshoe.charty.line.data.MultilinePoint

/**
 * Calculate point positions for a series
 */
internal fun ChartContext.calculateSeriesPointPositions(
    dataList: List<LineGroup>,
    seriesIndex: Int,
): List<Offset> =
    dataList.fastMapIndexed { index, group ->
        val value = group.values.getOrNull(seriesIndex) ?: 0f
        Offset(
            x = calculateCenteredXPosition(index = index, totalItems = dataList.size),
            y = convertValueToYPosition(value),
        )
    }

/**
 * Projects a point onto the [LineData] shape that
 * [com.himanshoe.charty.line.config.LineChartConfig.tooltipFormatter] accepts: the group's x-axis
 * label qualified with the one-based series number, paired with that series' value at this x
 * position. The series number is folded into the label because [LineData] carries no series of its
 * own, and dropping it would leave two series at the same x indistinguishable in the tooltip.
 *
 * @receiver The tapped point, carrying its group, its series, and its value.
 * @return The formatter's input for this point.
 */
internal fun MultilinePoint.toTooltipLineData(): LineData =
    LineData(
        label = "${lineGroup.label} Line ${seriesIndex + MultilineChartConstants.SERIES_INDEX_OFFSET}",
        value = value,
    )

/**
 * Projects one series' value in a group onto the [LineData] shape the tooltip formatter accepts,
 * labelled `L1`, `L2`, … by its one-based series number, for the crosshair's combined label.
 *
 * @param seriesIndex The zero-based series the value belongs to.
 * @param value The series' value at this x position.
 * @return The formatter's input for this series.
 */
internal fun crosshairSeriesLineData(
    seriesIndex: Int,
    value: Float,
): LineData = LineData(label = "L${seriesIndex + MultilineChartConstants.SERIES_INDEX_OFFSET}", value = value)
