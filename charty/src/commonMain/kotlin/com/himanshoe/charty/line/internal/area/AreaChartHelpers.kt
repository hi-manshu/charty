package com.himanshoe.charty.line.internal.area

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.line.data.LineData
import com.himanshoe.charty.line.internal.line.calculatePointPositions

/**
 * The plotted position of every point in [dataList], reporting each one to [onPointCalculated] as it
 * is produced so the caller can record hit-test bounds in the same pass. The geometry itself comes
 * from the shared line-chart mapping, so an area's fill can never sit at a different x than the line
 * charts draw.
 */
internal fun calculatePointPositions(
    dataList: List<LineData>,
    chartContext: ChartContext,
    onPointCalculated: (Pair<Offset, LineData>) -> Unit,
): List<Offset> {
    val positions = chartContext.calculatePointPositions(dataList)
    positions.fastForEachIndexed { index, position -> onPointCalculated(position to dataList[index]) }
    return positions
}

/**
 * Calculates baseline Y position for the area fill.
 */
internal fun calculateBaselineY(
    minValue: Float,
    isBelowAxisMode: Boolean,
    chartContext: ChartContext,
): Float =
    if (minValue < 0f && isBelowAxisMode) {
        chartContext.convertValueToYPosition(0f)
    } else {
        chartContext.bottom
    }
