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
 * A composable function that displays a block bar chart.
 *
 * A block bar chart is a horizontal segmented bar where each segment's width is proportional to its value.
 * It is ideal for showing the composition or progress of a whole, with each block representing a different category.
 *
 * @param data A lambda function that returns a list of [BlockData]. Each block's width is determined by its value, and non-positive values are filtered out.
 * @param modifier The modifier to be applied to the chart.
 * @param blockBarConfig The configuration for the block bar's appearance, such as colors, spacing, and height, defined by a [BlockBarChartConfig].
 *
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
        drawBlockBar(
            blocks = blocks,
            config = blockBarConfig,
        )
    }
}
