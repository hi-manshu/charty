package com.himanshoe.charty.line.internal.stackedarea

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.util.fastFlatMap
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMapIndexed
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.line.data.LineGroup

/**
 * The upper edge of the band for [seriesIndex]: the running total through that series, plotted at
 * each group's centred x.
 */
internal fun ChartContext.calculateCumulativePositions(
    dataList: List<LineGroup>,
    seriesIndex: Int,
): List<Offset> =
    dataList.fastMapIndexed { index, group ->
        Offset(
            x = calculateCenteredXPosition(index = index, totalItems = dataList.size),
            y = convertValueToYPosition(group.calculateCumulativeValue(seriesIndex)),
        )
    }

/**
 * The lower edge of the band for [seriesIndex]: the upper edge of the series beneath it, or
 * [baselineY] for the bottom-most series, which has nothing beneath it to stack on.
 */
internal fun ChartContext.calculateLowerPositions(
    dataList: List<LineGroup>,
    seriesIndex: Int,
    baselineY: Float,
): List<Offset> =
    dataList.fastMapIndexed { index, group ->
        Offset(
            x = calculateCenteredXPosition(index = index, totalItems = dataList.size),
            y =
                if (seriesIndex > 0) {
                    convertValueToYPosition(group.calculateCumulativeValue(seriesIndex - 1))
                } else {
                    baselineY
                },
        )
    }

/**
 * The running total of this group's values through [seriesIndex] inclusive. Missing series count as
 * zero, and a negative [seriesIndex] totals nothing, which is what the bottom-most band's lower edge
 * asks for.
 */
internal fun LineGroup.calculateCumulativeValue(seriesIndex: Int): Float {
    var cumulativeValue = 0f
    for (i in 0..seriesIndex) {
        cumulativeValue += values.getOrNull(i) ?: 0f
    }
    return cumulativeValue
}

/**
 * Flattens [dataList] into every running total it contains: for each group, the cumulative sum
 * after each of its series values. The largest of these is the height the y-axis must cover, since
 * a stacked area's topmost band ends at its group's full total.
 *
 * @param dataList The groups to accumulate, each holding one value per series.
 * @return Every running total, group by group and series by series.
 */
internal fun calculateStackedCumulativeValues(dataList: List<LineGroup>): List<Float> =
    dataList.fastFlatMap { group ->
        val cumulative = mutableListOf<Float>()
        var sum = 0f
        group.values.fastForEach { value ->
            sum += value
            cumulative.add(sum)
        }
        cumulative
    }
