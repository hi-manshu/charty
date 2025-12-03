package com.himanshoe.charty.line.internal.stackedarea

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.util.fastMapIndexed
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.line.data.LineGroup

/**
 * Calculate cumulative positions for a series
 */
internal fun ChartContext.calculateCumulativePositions(
    dataList: List<LineGroup>,
    seriesIndex: Int,
): List<Offset> {
    return dataList.fastMapIndexed { index, group ->
        var cumulativeValue = 0f
        for (i in 0..seriesIndex) {
            cumulativeValue += group.values.getOrNull(i) ?: 0f
        }
        Offset(
            x = calculateCenteredXPosition(index, dataList.size),
            y = convertValueToYPosition(cumulativeValue),
        )
    }
}

/**
 * Calculate lower bound positions (previous series cumulative or baseline)
 */
internal fun ChartContext.calculateLowerPositions(
    dataList: List<LineGroup>,
    seriesIndex: Int,
    baselineY: Float,
): List<Offset> {
    return if (seriesIndex > 0) {
        dataList.fastMapIndexed { index, group ->
            var cumulativeValue = 0f
            for (i in 0 until seriesIndex) {
                cumulativeValue += group.values.getOrNull(i) ?: 0f
            }
            Offset(
                x = calculateCenteredXPosition(index, dataList.size),
                y = convertValueToYPosition(cumulativeValue),
            )
        }
    } else {
        dataList.fastMapIndexed { index, _ ->
            Offset(
                x = calculateCenteredXPosition(index, dataList.size),
                y = baselineY,
            )
        }
    }
}

/**
 * Calculate cumulative value up to a series index
 */
internal fun LineGroup.calculateCumulativeValue(seriesIndex: Int): Float {
    var cumulativeValue = 0f
    for (i in 0..seriesIndex) {
        cumulativeValue += values.getOrNull(i) ?: 0f
    }
    return cumulativeValue
}

