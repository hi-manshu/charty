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
private const val LABEL_FONT_SIZE = 11
private val DASH_INTERVALS = floatArrayOf(6f, 3f)
private val DASH_EFFECT = PathEffect.dashPathEffect(DASH_INTERVALS)

/**
 * Draws a [ChartAnnotation] onto the canvas: a vertical marker line, a text callout bubble, or a
 * geometric shape, depending on the subtype.
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
    when (annotation) {
        is ChartAnnotation.Marker ->
            drawMarkerAnnotation(
                annotation = annotation,
                chartContext = chartContext,
                totalItems = totalItems,
                textMeasurer = textMeasurer,
            )

        is ChartAnnotation.Callout ->
            drawCalloutAnnotation(
                annotation = annotation,
                chartContext = chartContext,
                totalItems = totalItems,
                textMeasurer = textMeasurer,
            )

        is ChartAnnotation.Shape ->
            drawShapeAnnotation(annotation = annotation, chartContext = chartContext, totalItems = totalItems)
    }
}

/**
 * Resolves [anchor] against the live [chartContext], so a data-anchored annotation follows the plot
 * through zoom, pan and rolling-window streaming.
 *
 * @param anchor The anchor to resolve.
 * @param chartContext The chart's drawing context providing coordinate utilities.
 * @param totalItems The number of data items currently laid out across the plot.
 * @return The clamped pixel position the annotation attaches to.
 */
internal fun resolveAnchorInContext(
    anchor: AnnotationAnchor,
    chartContext: ChartContext,
    totalItems: Int,
): Offset {
    val centeredXOf: ((Int) -> Float)? =
        if (totalItems > 0) {
            { index -> chartContext.calculateCenteredXPosition(index = index, totalItems = totalItems) }
        } else {
            null
        }
    return resolveAnnotationAnchor(
        anchor = anchor,
        left = chartContext.left,
        top = chartContext.top,
        right = chartContext.right,
        bottom = chartContext.bottom,
        minValue = chartContext.minValue,
        maxValue = chartContext.maxValue,
        totalItems = totalItems,
        centeredXOf = centeredXOf,
    )
}

private fun DrawScope.drawMarkerAnnotation(
    annotation: ChartAnnotation.Marker,
    chartContext: ChartContext,
    totalItems: Int,
    textMeasurer: TextMeasurer,
) {
    if (totalItems == 0) {
        return
    }
    val x = chartContext.calculateCenteredXPosition(index = annotation.xIndex, totalItems = totalItems)

    val pathEffect =
        if (annotation.style.isDashed) {
            DASH_EFFECT
        } else {
            null
        }

    drawLine(
        color = annotation.style.lineColor,
        start = Offset(x = x, y = chartContext.top),
        end = Offset(x = x, y = chartContext.bottom),
        strokeWidth = annotation.style.lineWidth,
        pathEffect = pathEffect,
    )

    if (annotation.label.isNotEmpty()) {
        val textStyle = TextStyle(color = annotation.style.labelTextColor, fontSize = LABEL_FONT_SIZE.sp)
        val textResult = textMeasurer.measure(text = annotation.label, style = textStyle)
        val labelWidth = textResult.size.width + LABEL_HORIZONTAL_PADDING * 2
        val labelHeight = textResult.size.height + LABEL_VERTICAL_PADDING * 2

        val labelX =
            (x - labelWidth / 2f).coerceIn(
                minimumValue = chartContext.left,
                maximumValue = chartContext.right - labelWidth,
            )
        val labelY =
            when (annotation.style.labelPosition) {
                AnnotationLabelPosition.TOP -> chartContext.top
                AnnotationLabelPosition.BOTTOM -> chartContext.bottom - labelHeight
            }

        drawRoundRect(
            color = annotation.style.labelBackgroundColor,
            topLeft = Offset(x = labelX, y = labelY),
            size = Size(width = labelWidth, height = labelHeight),
            cornerRadius = CornerRadius(LABEL_CORNER_RADIUS),
        )

        drawText(
            textLayoutResult = textResult,
            topLeft = Offset(x = labelX + LABEL_HORIZONTAL_PADDING, y = labelY + LABEL_VERTICAL_PADDING),
        )
    }
}
