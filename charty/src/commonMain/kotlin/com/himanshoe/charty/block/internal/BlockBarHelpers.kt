package com.himanshoe.charty.block.internal

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.block.config.BlockBarChartConfig
import com.himanshoe.charty.block.data.BlockData
import com.himanshoe.charty.color.ChartyColor

/**
 * Internal drawing logic for BlockBarChart.
 * Renders a horizontal segmented bar with rounded corners on the outer edges.
 */
internal fun DrawScope.drawBlockBar(
    blocks: List<BlockData>,
    config: BlockBarChartConfig,
) {
    if (blocks.isEmpty()) return

    val totalValue = blocks.sumOf { it.value.toDouble() }.toFloat()
    if (totalValue <= 0f) return

    val canvasWidth = size.width
    val canvasHeight = size.height
    val gapPx = config.gapBetweenBlocks.toPx()
    val radiusPx = config.cornerRadius.value

    val totalGaps = gapPx * (blocks.size - 1).coerceAtLeast(0)
    val usableWidth = (canvasWidth - totalGaps).coerceAtLeast(0f)

    var cursorX = 0f

    blocks.fastForEachIndexed { index, block ->
        val blockWidth = usableWidth * (block.value / totalValue)
        val isFirstBlock = index == 0
        val isLastBlock = index == blocks.lastIndex

        drawSegment(
            startX = cursorX,
            width = blockWidth,
            height = canvasHeight,
            radius = radiusPx,
            roundStart = isFirstBlock,
            roundEnd = isLastBlock,
            color = block.color,
        )

        cursorX += blockWidth + gapPx
    }
}

/**
 * Draws a single rounded segment of the block bar.
 *
 * @param startX Left edge X coordinate.
 * @param width Segment width.
 * @param height Segment height.
 * @param radius Corner radius value (in pixels).
 * @param roundStart Whether to round the left corners.
 * @param roundEnd Whether to round the right corners.
 * @param color Color or gradient for this segment.
 */
private fun DrawScope.drawSegment(
    startX: Float,
    width: Float,
    height: Float,
    radius: Float,
    roundStart: Boolean,
    roundEnd: Boolean,
    color: ChartyColor,
) {
    if (width <= 0f) return

    val leftCornerRadius = if (roundStart) CornerRadius(radius) else CornerRadius.Zero
    val rightCornerRadius = if (roundEnd) CornerRadius(radius) else CornerRadius.Zero

    val segmentRect =
        RoundRect(
            left = startX,
            top = 0f,
            right = startX + width,
            bottom = height,
            topLeftCornerRadius = leftCornerRadius,
            bottomLeftCornerRadius = leftCornerRadius,
            topRightCornerRadius = rightCornerRadius,
            bottomRightCornerRadius = rightCornerRadius,
        )

    val colors = color.value
    val topLeft = Offset(segmentRect.left, segmentRect.top)
    val segmentSize = Size(segmentRect.width, segmentRect.height)

    when {
        colors.size == 1 -> {
            drawRoundRect(
                color = colors.first(),
                topLeft = topLeft,
                size = segmentSize,
                cornerRadius = CornerRadius(radius),
            )
        }
        else -> {
            drawRoundRect(
                brush = Brush.horizontalGradient(colors),
                topLeft = topLeft,
                size = segmentSize,
                cornerRadius = CornerRadius(radius),
            )
        }
    }
}
