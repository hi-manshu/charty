package com.himanshoe.charty.common.annotation

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import com.himanshoe.charty.color.toBrush
import com.himanshoe.charty.color.withChartyColor
import com.himanshoe.charty.common.ChartContext

/**
 * Draws a [ChartAnnotation] — a vertical marker line and an optional text label — onto the canvas.
 *
 * This should be called at the end of the chart's draw lambda so that annotations render on top
 * of all chart content.
 *
 * @param annotation The annotation to draw.
 * @param chartContext The chart's drawing context providing coordinate utilities.
 * @param totalItems The total number of data items in the chart (used to calculate x position).
 * @param textMeasurer A [TextMeasurer] for measuring and drawing the label text.
 */
fun DrawScope.drawChartAnnotation(
    annotation: ChartAnnotation,
    chartContext: ChartContext,
    totalItems: Int,
    textMeasurer: TextMeasurer,
) {
    if (totalItems == 0) {
        return
    }
    val x = chartContext.calculateCenteredXPosition(annotation.xIndex, totalItems)

    val pathEffect = annotation.style.dashPathEffect

    drawLine(
        brush = annotation.style.lineColor.toBrush(),
        start = Offset(x, chartContext.top),
        end = Offset(x, chartContext.bottom),
        strokeWidth = annotation.style.lineWidth,
        pathEffect = pathEffect,
    )

    if (annotation.label.isNotEmpty()) {
        val textStyle = annotation.style.labelTextStyle.withChartyColor(annotation.style.labelTextColor)
        val textResult = textMeasurer.measure(text = annotation.label, style = textStyle)
        val labelWidth = textResult.size.width + annotation.style.labelHorizontalPadding * 2
        val labelHeight = textResult.size.height + annotation.style.labelVerticalPadding * 2

        val labelX = (x - labelWidth / 2f).coerceIn(chartContext.left, chartContext.right - labelWidth)
        val labelY =
            when (annotation.style.labelPosition) {
                AnnotationLabelPosition.TOP -> chartContext.top
                AnnotationLabelPosition.BOTTOM -> chartContext.bottom - labelHeight
            }

        drawRoundRect(
            brush = annotation.style.labelBackgroundColor.toBrush(),
            topLeft = Offset(labelX, labelY),
            size = Size(labelWidth, labelHeight),
            cornerRadius = CornerRadius(annotation.style.labelCornerRadius),
        )

        drawText(
            textLayoutResult = textResult,
            topLeft =
                Offset(
                    labelX + annotation.style.labelHorizontalPadding,
                    labelY + annotation.style.labelVerticalPadding,
                ),
        )
    }
}
