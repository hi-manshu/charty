package com.himanshoe.charty.line.internal.line

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.util.fastMapIndexed
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.line.data.LineData

/**
 * Calculate point positions for all data points
 */
internal fun ChartContext.calculatePointPositions(
    dataList: List<LineData>,
): List<Offset> {
    return dataList.fastMapIndexed { index, point ->
        Offset(
            x = calculateCenteredXPosition(index, dataList.size),
            y = convertValueToYPosition(point.value),
        )
    }
}

