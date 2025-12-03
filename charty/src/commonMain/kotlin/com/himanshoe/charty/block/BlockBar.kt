package com.himanshoe.charty.block

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.himanshoe.charty.block.config.BlockBarChartConfig
import com.himanshoe.charty.block.data.BlockData
import com.himanshoe.charty.block.internal.drawBlockBar

/**
 * BlockBarChart - Horizontal segmented bar with proportional blocks.
 *
 * Renders a pill-shaped bar divided into segments, where each segment's width
 * is proportional to its value. Perfect for showing composition or progress with
 * multiple categories in a compact, visually appealing format.
 *
 * Each block can have its own color, or colors are automatically assigned from
 * the config's color palette.
 *
 * Usage:
 * ```kotlin
 * BlockBarChart(
 *     data = {
 *         listOf(
 *             BlockData(1f, ChartyColor.Solid(Color(0xFFFF6B81))),
 *             BlockData(2f, ChartyColor.Solid(Color(0xFFFFE066))),
 *             BlockData(5f, ChartyColor.Solid(Color(0xFF5BE37D))),
 *         )
 *     },
 *     modifier = Modifier.fillMaxWidth()
 * )
 * ```
 *
 * @param data Lambda returning a list of [BlockData]. Each block's width is proportional
 * to its value. Non-positive values are automatically filtered out.
 * @param blockBarConfig Visual configuration for colors, spacing, radius, and height.
 * @param modifier Modifier to size and position the bar.
 */
@Composable
fun BlockBarChart(
    data: () -> List<BlockData>,
    modifier: Modifier = Modifier,
    blockBarConfig: BlockBarChartConfig = BlockBarChartConfig(),
) {
    val blocks = remember(data) { data().filter { it.value > 0f } }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(blockBarConfig.barHeight),
    ) {
        drawBlockBar(blocks = blocks, config = blockBarConfig)
    }
}
