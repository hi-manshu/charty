package com.himanshoe.charty.bar.internal.bar.stackedhorizontal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.color.ChartyColor

/**
 * Memoizes the maximum group total and the resolved color list derived from [colors].
 *
 * The maximum total is used as the upper bound of the value axis, so every bar
 * is scaled relative to the largest group.
 *
 * @return A [Pair] of (maxTotal, colorList).
 */
@Composable
internal fun rememberStackedHorizontalMaxTotal(
    dataList: List<BarGroup>,
    colors: ChartyColor,
): Pair<Float, List<Color>> =
    remember(dataList, colors) {
        val totals = dataList.fastMap { group -> group.values.sum() }
        (totals.maxOrNull() ?: 0f) to colors.value
    }
