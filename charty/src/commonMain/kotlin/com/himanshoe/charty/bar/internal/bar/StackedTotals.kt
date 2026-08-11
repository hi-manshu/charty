package com.himanshoe.charty.bar.internal.bar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.color.ChartyColor

/**
 * Memoizes the maximum group total and the resolved color list derived from [colors].
 *
 * Each group's total is the sum of its segment values; the largest total becomes the upper bound
 * of the value axis, so every stacked bar is scaled relative to the biggest group. Shared by
 * [com.himanshoe.charty.bar.StackedBarChart] and
 * [com.himanshoe.charty.bar.StackedHorizontalBarChart].
 *
 * @param dataList The stacked groups.
 * @param colors The color scheme to resolve into a per-segment color list.
 * @return A [Pair] of `(maxTotal, colorList)`.
 */
@Composable
internal fun rememberStackedMaxTotal(
    dataList: List<BarGroup>,
    colors: ChartyColor,
): Pair<Float, List<Color>> =
    remember(dataList, colors) {
        val totals = dataList.fastMap { group -> group.values.sum() }
        (totals.maxOrNull() ?: 0f) to colors.value
    }
