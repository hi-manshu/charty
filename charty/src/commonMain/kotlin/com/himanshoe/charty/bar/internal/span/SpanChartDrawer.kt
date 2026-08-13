package com.himanshoe.charty.bar.internal.span

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.draw.drawReferenceBand
import com.himanshoe.charty.common.draw.drawReferenceLine

internal fun DrawScope.drawSpans(params: SpanDrawParams) {
    val range = params.maxValue - params.minValue
    if (range == 0f) {
        return
    }

    params.dataList.fastForEachIndexed { index, span ->
        val spanChartyColor = span.color ?: params.colors
        val barHeight = params.chartContext.height / params.dataList.size
        val barY = params.chartContext.top + (barHeight * index)
        val barThickness = barHeight * params.barConfig.barWidthFraction
        val centeredBarY = barY + (barHeight - barThickness) / 2

        val startNormalized = (span.startValue - params.minValue) / range
        val endNormalized = (span.endValue - params.minValue) / range
        val startX =
            params.chartContext.left +
                params.axisOffset + (startNormalized * (params.chartContext.width - params.axisOffset))
        val endX =
            params.chartContext.left +
                params.axisOffset + (endNormalized * (params.chartContext.width - params.axisOffset))

        val fullSpanWidth = endX - startX
        val animatedSpanWidth = fullSpanWidth * params.animationProgress

        if (params.recordBounds && animatedSpanWidth > 0) {
            params.onSpanBoundCalculated(
                Rect(
                    left = startX,
                    top = centeredBarY,
                    right = startX + animatedSpanWidth,
                    bottom = centeredBarY + barThickness,
                ) to span,
            )
        }

        val brush =
            Brush.horizontalGradient(
                colors = spanChartyColor.value,
                startX = startX,
                endX = endX,
            )

        drawRoundedSpan(
            brush = brush,
            x = startX,
            y = centeredBarY,
            width = animatedSpanWidth,
            height = barThickness,
            cornerRadius = params.barConfig.cornerRadius.value,
        )
    }
}

/**
 * Draws the configured reference band behind the spans, or nothing when none is configured. A span
 * chart plots its values along the horizontal axis, so the band renders as a vertical stripe between
 * its two values, aligned with the chart's value-axis labels and grid lines.
 *
 * @param barConfig Supplies the optional band.
 * @param chartContext The chart's coordinate context.
 * @param textMeasurer Measures the band's label.
 */
internal fun DrawScope.drawSpanReferenceBandIfNeeded(
    barConfig: BarChartConfig,
    chartContext: ChartContext,
    textMeasurer: TextMeasurer,
) {
    barConfig.referenceBand?.let { band ->
        drawReferenceBand(
            chartContext = chartContext,
            orientation = ChartOrientation.HORIZONTAL,
            config = band,
            textMeasurer = textMeasurer,
        )
    }
}

/**
 * Draws the configured reference line over the spans, or nothing when none is configured. A span
 * chart plots its values along the horizontal axis, so the line renders vertically at its value,
 * aligned with the chart's value-axis labels and grid lines.
 *
 * @param barConfig Supplies the optional line.
 * @param chartContext The chart's coordinate context.
 * @param textMeasurer Measures the line's label.
 */
internal fun DrawScope.drawSpanReferenceLineIfNeeded(
    barConfig: BarChartConfig,
    chartContext: ChartContext,
    textMeasurer: TextMeasurer,
) {
    barConfig.referenceLine?.let { line ->
        drawReferenceLine(
            chartContext = chartContext,
            orientation = ChartOrientation.HORIZONTAL,
            config = line,
            textMeasurer = textMeasurer,
        )
    }
}

/**
 * Helper function to draw a span (horizontal bar) with fully rounded corners
 */
internal fun DrawScope.drawRoundedSpan(
    brush: Brush,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    cornerRadius: Float,
) {
    val path =
        Path().apply {
            addRoundRect(
                RoundRect(
                    left = x,
                    top = y,
                    right = x + width,
                    bottom = y + height,
                    topLeftCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    topRightCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    bottomLeftCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    bottomRightCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                ),
            )
        }
    drawPath(path, brush)
}
