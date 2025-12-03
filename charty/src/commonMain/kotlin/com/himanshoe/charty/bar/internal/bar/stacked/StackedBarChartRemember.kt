package com.himanshoe.charty.bar.internal.bar.stacked

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.color.ChartyColor

@Composable
internal fun rememberStackedMaxTotal(
    dataList: List<BarGroup>,
    colors: ChartyColor
): Pair<Float, List<Color>> {
    return remember(dataList, colors) {
        val totals = dataList.fastMap { group -> group.values.sum() }
        (totals.maxOrNull() ?: 0f) to colors.value
    }
}
