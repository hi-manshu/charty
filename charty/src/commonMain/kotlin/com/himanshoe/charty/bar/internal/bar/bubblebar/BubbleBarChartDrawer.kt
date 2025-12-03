package com.himanshoe.charty.bar.internal.bar.bubblebar

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.draw.drawReferenceLine
import com.himanshoe.charty.common.tooltip.drawTooltip
import kotlin.math.ceil
import kotlin.math.max

/**
 * Drawing functions for BubbleBarChart
 */

internal fun DrawScope.drawBubbleBars(
    params: BubbleBarDrawParams,
) {
    params.dataList.fastForEachIndexed { index, bar ->
        val barX = params.chartContext.calculateBarLeftPosition(
            index,
            params.dataList.size,
            params.bubbleConfig.barWidthFraction,
        )
        val barWidth = params.chartContext.calculateBarWidth(
            params.dataList.size,
            params.bubbleConfig.barWidthFraction,
        )
        val barValueY = params.chartContext.convertValueToYPosition(bar.value)

        val (barTop, barHeight) = calculateBubbleBarDimensions(
            barValue = bar.value,
            baselineY = params.baselineY,
            barValueY = barValueY,
            animationProgress = params.animationProgress,
        )

        if (params.onBarClick != null) {
            params.barBounds.add(
                Rect(
                    left = barX,
                    top = barTop,
                    right = barX + barWidth,
                    bottom = barTop + barHeight,
                ) to bar,
            )
        }

        val barColor = bar.color ?: params.color

        drawBubbleBar(
            color = barColor,
            x = barX,
            y = barTop,
            width = barWidth,
            height = barHeight,
            bubbleRadius = params.bubbleConfig.bubbleRadius,
            bubbleSpacing = params.bubbleConfig.bubbleSpacing,
        )
    }
}

@OptIn(ExperimentalTextApi::class)
internal fun DrawScope.drawReferenceLineIfNeeded(
    params: BubbleBarDrawParams,
) {
    params.bubbleConfig.referenceLine?.let { referenceLineConfig ->
        drawReferenceLine(
            chartContext = params.chartContext,
            orientation = ChartOrientation.VERTICAL,
            config = referenceLineConfig,
            textMeasurer = params.textMeasurer,
        )
    }
}

@OptIn(ExperimentalTextApi::class)
internal fun DrawScope.drawTooltipIfNeeded(
    params: BubbleBarDrawParams,
    tooltipState: com.himanshoe.charty.common.tooltip.TooltipState?,
) {
    tooltipState?.let { state ->
        drawTooltip(
            tooltipState = state,
            config = params.bubbleConfig.tooltipConfig,
            textMeasurer = params.textMeasurer,
            chartWidth = params.chartContext.right,
            chartTop = params.chartContext.top,
            chartBottom = params.chartContext.bottom,
        )
    }
}

private fun DrawScope.drawBubbleBar(
    color: ChartyColor,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    bubbleRadius: Float,
    bubbleSpacing: Float,
) {
    if (height <= 0f) return

    val centerX = x + width / 2f
    val diameter = bubbleRadius * 2
    val verticalStep = diameter + bubbleSpacing
    val bubbleCount = max(1, ceil(height / verticalStep).toInt())

    for (i in 0 until bubbleCount) {
        val bubbleY = y + height - (i * verticalStep) - bubbleRadius

        if (bubbleY < y - bubbleRadius) break

        val bubbleColor = when (color) {
            is ChartyColor.Solid -> color.color
            is ChartyColor.Gradient -> {
                val colors = color.colors
                val ratio = i.toFloat() / bubbleCount.coerceAtLeast(1)
                val scaledRatio = ratio * (colors.size - 1)
                val index = scaledRatio.toInt().coerceIn(0, colors.size - 2)
                val localRatio = scaledRatio - index
                lerp(colors[index], colors[index + 1], localRatio)
            }
        }

        drawCircle(
            color = bubbleColor,
            radius = bubbleRadius,
            center = Offset(centerX, bubbleY),
        )
    }
}

