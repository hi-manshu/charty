package com.himanshoe.charty.bar.internal.bar.horizontal

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.draw.drawReferenceLine
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.tooltip.drawTooltip

private const val DATA_LABEL_PADDING = 4f

@OptIn(ExperimentalTextApi::class)
internal fun DrawScope.drawHorizontalBars(params: HorizontalBarDrawParams) {
    val range = params.maxValue - params.minValue
    if (range == 0f) return

    params.dataList.fastForEachIndexed { index, bar ->
        val barHeight = params.chartContext.height / params.dataList.size
        val barY = params.chartContext.top + (barHeight * index)
        val barThickness = barHeight * params.barConfig.barWidthFraction
        val centeredBarY = barY + (barHeight - barThickness) / 2

        val valueNormalized = (bar.value - params.minValue) / range
        val barValueX = params.chartContext.left + (valueNormalized * params.chartContext.width)
        val isNegative = bar.value < 0f

        val (barLeft, barWidth) =
            calculateHorizontalBarDimensions(
                isNegative = isNegative,
                isBelowAxisMode = params.isBelowAxisMode,
                baselineX = params.baselineX,
                barValueX = barValueX,
                animationProgress = params.animationProgress,
            )

        if (barWidth <= 0f) return@fastForEachIndexed

        if (params.recordBounds) {
            params.onBarBoundCalculated(
                Rect(
                    left = barLeft,
                    top = centeredBarY,
                    right = barLeft + barWidth,
                    bottom = centeredBarY + barThickness,
                ) to bar,
            )
        }

        val barColor = bar.color ?: params.color
        val brush =
            Brush.horizontalGradient(
                colors = barColor.value,
                startX = params.chartContext.left,
                endX = params.chartContext.right,
            )

        drawRoundedHorizontalBar(
            brush = brush,
            x = barLeft,
            y = centeredBarY,
            width = barWidth,
            height = barThickness,
            isNegative = isNegative,
            isBelowAxisMode = params.isBelowAxisMode,
            cornerRadius = params.barConfig.cornerRadius.value,
        )

        if (params.barConfig.showDataLabels && params.textMeasurer != null && params.animationProgress >= 1f) {
            val labelText = params.barConfig.dataLabelFormatter(bar)
            val textLayout = params.textMeasurer.measure(labelText, params.barConfig.dataLabelStyle)
            val labelY = centeredBarY + (barThickness - textLayout.size.height) / 2f
            val labelX =
                if (isNegative) {
                    (barLeft - textLayout.size.width - DATA_LABEL_PADDING).coerceAtLeast(params.chartContext.left)
                } else {
                    (barLeft + barWidth + DATA_LABEL_PADDING)
                        .coerceAtMost(params.chartContext.right - textLayout.size.width)
                }
            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(labelX, labelY),
            )
        }
    }
}

@OptIn(ExperimentalTextApi::class)
internal fun DrawScope.drawHorizontalReferenceLineIfNeeded(
    barConfig: BarChartConfig,
    chartContext: ChartContext,
    textMeasurer: TextMeasurer,
) {
    barConfig.referenceLine?.let { referenceLineConfig ->
        drawReferenceLine(
            chartContext = chartContext,
            orientation = ChartOrientation.HORIZONTAL,
            config = referenceLineConfig,
            textMeasurer = textMeasurer,
        )
    }
}

@OptIn(ExperimentalTextApi::class)
internal fun DrawScope.drawHorizontalTooltipIfNeeded(
    tooltipState: TooltipState?,
    barConfig: BarChartConfig,
    textMeasurer: TextMeasurer,
    chartContext: ChartContext,
) {
    tooltipState?.let { state ->
        drawTooltip(
            tooltipState = state,
            config = barConfig.tooltipConfig,
            textMeasurer = textMeasurer,
            chartWidth = chartContext.right,
            chartTop = chartContext.top,
            chartBottom = chartContext.bottom,
        )
    }
}

/**
 * Helper function to draw a horizontal bar with rounded corners
 */
private fun DrawScope.drawRoundedHorizontalBar(
    brush: Brush,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    isNegative: Boolean,
    isBelowAxisMode: Boolean,
    cornerRadius: Float,
) {
    val path =
        Path().apply {
            if (isNegative && isBelowAxisMode) {
                addRoundRect(
                    RoundRect(
                        left = x,
                        top = y,
                        right = x + width,
                        bottom = y + height,
                        topLeftCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                        topRightCornerRadius = CornerRadius.Zero,
                        bottomLeftCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                        bottomRightCornerRadius = CornerRadius.Zero,
                    ),
                )
            } else {
                addRoundRect(
                    RoundRect(
                        left = x,
                        top = y,
                        right = x + width,
                        bottom = y + height,
                        topLeftCornerRadius = CornerRadius.Zero,
                        topRightCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                        bottomLeftCornerRadius = CornerRadius.Zero,
                        bottomRightCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    ),
                )
            }
        }

    drawPath(path, brush)
}
