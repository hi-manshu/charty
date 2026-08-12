package com.himanshoe.charty.line.internal.stackedarea

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.util.fastFlatMap
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMapIndexed
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.line.data.LineGroup

/**
 * Calculate cumulative positions for a series
 */
internal fun ChartContext.calculateCumulativePositions(
    dataList: List<LineGroup>,
    seriesIndex: Int,
): List<Offset> =
    dataList.fastMapIndexed { index, group ->
        var cumulativeValue = 0f
        for (i in 0..seriesIndex) {
            cumulativeValue += group.values.getOrNull(i) ?: 0f
        }
        Offset(
            x = calculateCenteredXPosition(index = index, totalItems = dataList.size),
            y = convertValueToYPosition(cumulativeValue),
        )
    }

/**
 * Calculate lower bound positions (previous series cumulative or baseline)
 */
internal fun ChartContext.calculateLowerPositions(
    dataList: List<LineGroup>,
    seriesIndex: Int,
    baselineY: Float,
): List<Offset> =
    if (seriesIndex > 0) {
        dataList.fastMapIndexed { index, group ->
            var cumulativeValue = 0f
            for (i in 0 until seriesIndex) {
                cumulativeValue += group.values.getOrNull(i) ?: 0f
            }
            Offset(
                x = calculateCenteredXPosition(index = index, totalItems = dataList.size),
                y = convertValueToYPosition(cumulativeValue),
            )
        }
    } else {
        dataList.fastMapIndexed { index, _ ->
            Offset(
                x = calculateCenteredXPosition(index = index, totalItems = dataList.size),
                y = baselineY,
            )
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
