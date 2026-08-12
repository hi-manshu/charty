package com.himanshoe.charty.line.internal.multiline

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.util.fastMapIndexed
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.line.data.LineGroup

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
