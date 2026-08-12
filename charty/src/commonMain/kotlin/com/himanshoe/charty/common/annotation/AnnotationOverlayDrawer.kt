package com.himanshoe.charty.common.annotation

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext

/**
 * Draws a [ChartAnnotation.Callout]: a rounded bubble carrying the callout text, joined by a
 * connector to the resolved anchor point. The bubble is measured, placed on the side chosen by
 * [resolveCalloutSide] and clamped so it never leaves the plot bounds.
 *
 * @param annotation The callout to draw.
 * @param chartContext The chart's drawing context providing coordinate utilities.
 * @param totalItems The number of data items currently laid out across the plot.
 * @param textMeasurer A [TextMeasurer] for measuring and drawing the bubble text.
 */
internal fun DrawScope.drawCalloutAnnotation(
    annotation: ChartAnnotation.Callout,
    chartContext: ChartContext,
    totalItems: Int,
    textMeasurer: TextMeasurer,
) {
    val style = annotation.style
    val anchor =
        resolveAnchorInContext(anchor = annotation.anchor, chartContext = chartContext, totalItems = totalItems)
    val layout = textMeasurer.measure(text = annotation.text, style = style.textStyle)
    val bubbleWidth = layout.size.width + style.paddingHorizontal * 2
    val bubbleHeight = layout.size.height + style.paddingVertical * 2
    val side =
        resolveCalloutSide(
            preferredSide = style.preferredSide,
            anchorY = anchor.y,
            bubbleHeight = bubbleHeight,
            pointerLength = style.pointerLengthPx,
            plotTop = chartContext.top,
        )
    val bubbleLeft =
        calloutBubbleLeft(
            anchorX = anchor.x,
            bubbleWidth = bubbleWidth,
            plotLeft = chartContext.left,
            plotRight = chartContext.right,
        )
    val bubbleTop =
        calloutBubbleTop(
            anchorY = anchor.y,
            bubbleHeight = bubbleHeight,
            side = side,
            pointerLength = style.pointerLengthPx,
            plotTop = chartContext.top,
            plotBottom = chartContext.bottom,
        )

    val connectorY =
        if (side == CalloutSide.ABOVE) {
            bubbleTop + bubbleHeight
        } else {
            bubbleTop
        }
    drawLine(
        brush = style.connectorColor.toAnnotationBrush(),
        start = anchor,
        end =
            Offset(
                x = anchor.x.coerceIn(minimumValue = bubbleLeft, maximumValue = bubbleLeft + bubbleWidth),
                y = connectorY,
            ),
        strokeWidth = style.connectorWidth,
    )
    drawRoundRect(
        brush = style.bubbleColor.toAnnotationBrush(),
        topLeft = Offset(x = bubbleLeft, y = bubbleTop),
        size = Size(width = bubbleWidth, height = bubbleHeight),
        cornerRadius = CornerRadius(style.cornerRadius),
    )
    drawText(
        textLayoutResult = layout,
        topLeft = Offset(x = bubbleLeft + style.paddingHorizontal, y = bubbleTop + style.paddingVertical),
    )
}

/**
 * Draws a [ChartAnnotation.Shape]: a rectangle, ellipse or line spanning the two resolved anchors.
 *
 * @param annotation The shape to draw.
 * @param chartContext The chart's drawing context providing coordinate utilities.
 * @param totalItems The number of data items currently laid out across the plot.
 */
internal fun DrawScope.drawShapeAnnotation(
    annotation: ChartAnnotation.Shape,
    chartContext: ChartContext,
    totalItems: Int,
) {
    val start = resolveAnchorInContext(anchor = annotation.from, chartContext = chartContext, totalItems = totalItems)
    val end = resolveAnchorInContext(anchor = annotation.to, chartContext = chartContext, totalItems = totalItems)
    val brush = annotation.color.toAnnotationBrush()
    val topLeft = Offset(x = minOf(start.x, end.x), y = minOf(start.y, end.y))
    val size =
        Size(
            width = maxOf(start.x, end.x) - topLeft.x,
            height = maxOf(start.y, end.y) - topLeft.y,
        )
    val drawStyle =
        if (annotation.filled) {
            Fill
        } else {
            Stroke(width = annotation.strokeWidth)
        }
    when (annotation.kind) {
        AnnotationShapeKind.RECT ->
            drawRect(brush = brush, topLeft = topLeft, size = size, style = drawStyle)

        AnnotationShapeKind.ELLIPSE ->
            drawOval(brush = brush, topLeft = topLeft, size = size, style = drawStyle)

        AnnotationShapeKind.LINE ->
            drawLine(brush = brush, start = start, end = end, strokeWidth = annotation.strokeWidth)
    }
}

/**
 * Converts a [ChartyColor] into the [Brush] used to paint annotation overlays.
 *
 * @return A solid brush for [ChartyColor.Solid], a vertical gradient for [ChartyColor.Gradient].
 */
private fun ChartyColor.toAnnotationBrush(): Brush =
    when (this) {
        is ChartyColor.Solid -> SolidColor(color)
        is ChartyColor.Gradient -> Brush.verticalGradient(colors)
    }
