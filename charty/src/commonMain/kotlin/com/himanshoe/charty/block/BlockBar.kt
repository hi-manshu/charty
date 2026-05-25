package com.himanshoe.charty.block

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.util.fastFilter
import com.himanshoe.charty.block.config.BlockBarChartConfig
import com.himanshoe.charty.block.data.BlockData
import com.himanshoe.charty.block.internal.drawBlockBar

/**
 * A composable function that displays a block bar chart.
 *
 * A block bar chart is a horizontal segmented bar where each segment's width is proportional to its
 * value. It is ideal for showing the composition or progress of a whole, with each block
 * representing a different category. Non-positive values are automatically filtered out.
 *
 * The chart always fills the full available width via [Modifier.fillMaxWidth], and its height is
 * controlled by [BlockBarChartConfig.barHeight].
 *
 * @param data A lambda function that returns a list of [BlockData]. Each block's width is
 *   determined by its value relative to the total; non-positive values are ignored.
 * @param modifier The modifier to be applied to the chart.
 * @param blockBarConfig The configuration for the block bar's appearance, such as colors, spacing,
 *   and height, defined by a [BlockBarChartConfig].
 * @param accessibilityDescription Overrides the auto-generated screen-reader description. Pass an empty string to suppress it.
 *
 * Example usage:
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
 */
@Composable
fun BlockBarChart(
    data: () -> List<BlockData>,
    modifier: Modifier = Modifier,
    blockBarConfig: BlockBarChartConfig = BlockBarChartConfig(),
    accessibilityDescription: String? = null,
) {
    val blocks = remember(data) { data().fastFilter { it.value > 0f } }
    val chartDescription = remember(blocks, accessibilityDescription) {
        when (accessibilityDescription) {
            "" -> null
            null -> "Block bar chart, ${blocks.size} segments."
            else -> accessibilityDescription
        }
    }
    val semanticsModifier = if (chartDescription != null) {
        Modifier.semantics { contentDescription = chartDescription }
    } else {
        Modifier
    }

    Canvas(
        modifier = modifier
            .then(semanticsModifier)
            .fillMaxWidth()
            .height(blockBarConfig.barHeight),
    ) {
        drawBlockBar(
            blocks = blocks,
            config = blockBarConfig,
        )
    }
}
