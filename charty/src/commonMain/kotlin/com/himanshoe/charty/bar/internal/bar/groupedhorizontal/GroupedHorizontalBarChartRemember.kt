package com.himanshoe.charty.bar.internal.bar.groupedhorizontal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.util.calculateNiceAxisRange

/**
 * Memoized state for [com.himanshoe.charty.bar.GroupedHorizontalBarChart].
 *
 * @property minValue Nice (rounded) minimum value for the value axis.
 * @property maxValue Nice (rounded) maximum value for the value axis.
 * @property axisSteps Number of intervals between axis ticks (computed to ensure zero is a tick
 *   whenever the range spans both negative and positive values).
 * @property colorList Resolved list of bar colours.
 */
internal data class GroupedHorizontalState(
    val minValue: Float,
    val maxValue: Float,
    val axisSteps: Int,
    val colorList: List<Color>,
)

/**
 * Memoizes the axis range and colour list derived from [dataList] and [colors].
 *
 * Uses a D3-style "nice axis" algorithm so that axis ticks land on round numbers and — when the
 * data spans both negative and positive values in [NegativeValuesDrawMode.BELOW_AXIS] mode —
 * zero is guaranteed to coincide with a tick mark.
 */
@Composable
internal fun rememberGroupedHorizontalState(
    dataList: List<BarGroup>,
    negativeValuesDrawMode: NegativeValuesDrawMode,
    colors: ChartyColor,
): GroupedHorizontalState =
    remember(dataList, negativeValuesDrawMode, colors) {
        val allValues = dataList.flatMap { it.values }
        val rawMin = allValues.minOrNull() ?: 0f
        val rawMax = allValues.maxOrNull() ?: 0f

        // In BELOW_AXIS mode, clamp the minimum to 0 when all data is positive so that bars
        // always start from the left edge rather than from the smallest value.
        val axisMin = if (negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS && rawMin >= 0f) {
            0f
        } else {
            rawMin
        }
        val axisMax = if (rawMax < axisMin) axisMin else rawMax

        val (niceMin, niceMax, steps) = calculateNiceAxisRange(axisMin, axisMax, GROUPED_HORIZONTAL_DEFAULT_AXIS_STEPS)

        GroupedHorizontalState(
            minValue = niceMin,
            maxValue = niceMax,
            axisSteps = steps,
            colorList = colors.value,
        )
    }
