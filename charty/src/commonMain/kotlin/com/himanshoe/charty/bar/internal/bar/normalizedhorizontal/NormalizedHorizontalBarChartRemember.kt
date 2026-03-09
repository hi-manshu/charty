package com.himanshoe.charty.bar.internal.bar.normalizedhorizontal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.color.ChartyColor

/**
 * Memoizes the resolved colour list from [colors].
 *
 * Kept as a dedicated remember composable for consistency with other chart modules
 * and to allow cheap invalidation when only the colour palette changes.
 */
@Composable
internal fun rememberNormalizedHorizontalColors(
    dataList: List<BarGroup>,
    colors: ChartyColor,
): List<Color> = remember(dataList, colors) { colors.value }
