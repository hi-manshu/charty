package com.himanshoe.charty.block.config

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.himanshoe.charty.common.config.CornerRadius

/**
 * Configuration for [com.himanshoe.charty.block.BlockBarChart].
 *
 * Controls the visual appearance of the segmented bar including corner rounding,
 * spacing between segments, and overall bar height.
 *
 * @property cornerRadius Corner radius applied to the start and end blocks. Use a value
 * close to `barHeight / 2` for a fully pill-shaped bar.
 * @property gapBetweenBlocks Horizontal spacing between adjacent block segments.
 * @property barHeight Total height of the bar.
 */
data class BlockBarChartConfig(
    val cornerRadius: CornerRadius = CornerRadius.Small,
    val gapBetweenBlocks: Dp = 4.dp,
    val barHeight: Dp = 16.dp,
)
