package com.himanshoe.charty.common.annotation

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.common.ChartContext

private const val LABEL_HORIZONTAL_PADDING = 8f
private const val LABEL_VERTICAL_PADDING = 4f
private const val LABEL_CORNER_RADIUS = 4f
private val DASH_INTERVALS = floatArrayOf(6f, 3f)
private val DASH_EFFECT = PathEffect.dashPathEffect(DASH_INTERVALS)

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

    val pathEffect =
        if (annotation.style.isDashed) {
            DASH_EFFECT
        } else {
            null
        }

    drawLine(
        color = annotation.style.lineColor,
        start = Offset(x, chartContext.top),
        end = Offset(x, chartContext.bottom),
        strokeWidth = annotation.style.lineWidth,
        pathEffect = pathEffect,
    )

    if (annotation.label.isNotEmpty()) {
        val textStyle = TextStyle(color = annotation.style.labelTextColor, fontSize = 11.sp)
        val textResult = textMeasurer.measure(annotation.label, textStyle)
        val labelWidth = textResult.size.width + LABEL_HORIZONTAL_PADDING * 2
        val labelHeight = textResult.size.height + LABEL_VERTICAL_PADDING * 2

        val labelX = (x - labelWidth / 2f).coerceIn(chartContext.left, chartContext.right - labelWidth)
        val labelY =
            when (annotation.style.labelPosition) {
                AnnotationLabelPosition.TOP -> chartContext.top
                AnnotationLabelPosition.BOTTOM -> chartContext.bottom - labelHeight
            }

        drawRoundRect(
            color = annotation.style.labelBackgroundColor,
            topLeft = Offset(labelX, labelY),
            size = Size(labelWidth, labelHeight),
            cornerRadius = CornerRadius(LABEL_CORNER_RADIUS),
        )

        drawText(
            textLayoutResult = textResult,
            topLeft = Offset(labelX + LABEL_HORIZONTAL_PADDING, labelY + LABEL_VERTICAL_PADDING),
        )
    }
}
