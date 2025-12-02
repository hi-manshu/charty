package com.himanshoe.charty.bar.internal.bar.waterfall

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.himanshoe.charty.bar.config.WaterfallChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.common.ChartContext


/**
 * Calculate parameters for drawing a waterfall bar
 */
internal fun calculateWaterfallBarParams(
    index: Int,
    bar: BarData,
    items: List<BarData>,
    cumulativeValues: List<Float>,
    config: WaterfallChartConfig,
    chartContext: ChartContext,
    animationProgress: Float,
): WaterfallBarDrawParams {
    val barX = chartContext.calculateBarLeftPosition(index, items.size, config.barWidthFraction)
    val barWidth = chartContext.calculateBarWidth(items.size, config.barWidthFraction)

    val prevTotal = if (index == 0) 0f else cumulativeValues[index - 1]
    val currTotal = cumulativeValues[index]

    val startY = chartContext.convertValueToYPosition(prevTotal)
    val endY = chartContext.convertValueToYPosition(currTotal)

    val isIncrease = bar.value >= 0f
    val fullHeight = kotlin.math.abs(endY - startY)
    val animatedHeight = fullHeight * animationProgress
    val animatedTop = if (isIncrease) startY - animatedHeight else startY

    val baseColor = if (isIncrease) config.positiveColor else config.negativeColor
    val chartyColor = bar.color ?: baseColor
    val brush = Brush.verticalGradient(chartyColor.value)

    val bounds = Rect(
        left = barX,
        top = animatedTop,
        right = barX + barWidth,
        bottom = animatedTop + animatedHeight,
    )

    return WaterfallBarDrawParams(
        x = barX,
        y = animatedTop,
        width = barWidth,
        height = animatedHeight,
        brush = brush,
        bounds = bounds,
    )
}

/**
 * Draw a waterfall bar with rounded corners
 */
internal fun DrawScope.drawWaterfallBar(
    brush: Brush,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    cornerRadius: Float,
) {
    val path = Path().apply {
        addRoundRect(
            RoundRect(
                left = x,
                top = y,
                right = x + width,
                bottom = y + height,
                topLeftCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                topRightCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                bottomLeftCornerRadius = CornerRadius.Zero,
                bottomRightCornerRadius = CornerRadius.Zero,
            ),
        )
    }
    drawPath(path = path, brush = brush)
}

