package com.himanshoe.charty.bar.internal.bar.groupedhorizontal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.color.ChartyColor

/**
 * Memoizes the (minValue, maxValue, colorList) triple derived from [dataList] and [colors].
 *
 * - When [negativeValuesDrawMode] is [NegativeValuesDrawMode.BELOW_AXIS] and the data
 *   contains negative values, `minValue` is the actual minimum so that negative bars
 *   extend to the left of the zero axis.
 * - Otherwise `minValue` is clamped to 0 so the baseline starts at the left edge.
 */
@Composable
internal fun rememberGroupedHorizontalState(
    dataList: List<BarGroup>,
    negativeValuesDrawMode: NegativeValuesDrawMode,
    colors: ChartyColor,
): Triple<Float, Float, List<Color>> =
    remember(dataList, negativeValuesDrawMode, colors) {
        val allValues = dataList.flatMap { it.values }
        val rawMin = allValues.minOrNull() ?: 0f
        val rawMax = allValues.maxOrNull() ?: 0f
        val finalMin = if (negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS) {
            if (rawMin >= 0f) 0f else rawMin
        } else {
            rawMin
        }
        Triple(finalMin, rawMax, colors.value)
    }
