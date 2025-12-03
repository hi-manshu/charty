package com.himanshoe.charty.bar.internal.span

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.util.fastForEachIndexed

internal fun DrawScope.drawSpans(params: SpanDrawParams) {
    val range = params.maxValue - params.minValue

    params.dataList.fastForEachIndexed { index, span ->
        val spanChartyColor = span.color ?: params.colors
        val barHeight = params.chartContext.height / params.dataList.size
        val barY = params.chartContext.top + (barHeight * index)
        val barThickness = barHeight * params.barConfig.barWidthFraction
        val centeredBarY = barY + (barHeight - barThickness) / 2

        val startNormalized = (span.startValue - params.minValue) / range
        val endNormalized = (span.endValue - params.minValue) / range
        val startX = params.chartContext.left +
            params.axisOffset + (startNormalized * (params.chartContext.width - params.axisOffset))
        val endX = params.chartContext.left +
            params.axisOffset + (endNormalized * (params.chartContext.width - params.axisOffset))

        val fullSpanWidth = endX - startX
        val animatedSpanWidth = fullSpanWidth * params.animationProgress

        if (params.onSpanClick != null && animatedSpanWidth > 0) {
            params.onSpanBoundCalculated(
                Rect(
                    left = startX,
                    top = centeredBarY,
                    right = startX + animatedSpanWidth,
                    bottom = centeredBarY + barThickness,
                ) to span,
            )
        }

        val brush = Brush.horizontalGradient(
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
    val path = Path().apply {
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

