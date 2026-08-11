package com.himanshoe.charty.common.draw

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.util.fastForEach
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.config.PersistentMarker

private const val MARKER_LABEL_GAP = 6f

/** Formats a value for a marker's default label, dropping the trailing `.0` on whole numbers. */
fun formatMarkerValue(value: Float): String =
    if (value == value.toLong().toFloat()) {
        value.toLong().toString()
    } else {
        value.toString()
    }

/**
 * Places a marker's callout pill so it is horizontally centred on [centerX] but stays fully within
 * `[left, right]`, and sits [gap] pixels above [anchorY] without crossing above [top].
 *
 * @return The top-left corner at which to draw the pill.
 */
internal fun resolveMarkerLabelTopLeft(
    centerX: Float,
    anchorY: Float,
    pillWidth: Float,
    pillHeight: Float,
    left: Float,
    right: Float,
    top: Float,
    gap: Float,
): Offset {
    val rawX = centerX - pillWidth / 2f
    val maxX = (right - pillWidth).coerceAtLeast(left)
    val x = rawX.coerceIn(left, maxX)
    val y = (anchorY - gap - pillHeight).coerceAtLeast(top)
    return Offset(x = x, y = y)
}

/**
 * Draws the persistent [markers] pinned to their data points. For each marker the point position is
 * taken from [pointPositions] by its `dataIndex` (markers outside the drawn range are skipped), and
 * its callout text is either the marker's own label or the value from [valueLabelFor].
 *
 * Call this after the chart's data so markers render on top.
 *
 * @param chartContext The chart's drawing context.
 * @param markers The markers to draw; an empty list draws nothing.
 * @param pointPositions Pixel positions of every drawn data point, indexed the same as the data.
 * @param valueLabelFor Supplies the formatted value text for a data index when a marker has no label.
 * @param textMeasurer Measures the callout label.
 */
fun DrawScope.drawPersistentMarkers(
    chartContext: ChartContext,
    markers: List<PersistentMarker>,
    pointPositions: List<Offset>,
    valueLabelFor: (Int) -> String,
    textMeasurer: TextMeasurer,
) {
    if (markers.isEmpty()) {
        return
    }
    markers.fastForEach { marker ->
        if (marker.dataIndex in pointPositions.indices) {
            drawSingleMarker(
                chartContext = chartContext,
                marker = marker,
                position = pointPositions[marker.dataIndex],
                labelText = marker.label ?: valueLabelFor(marker.dataIndex),
                textMeasurer = textMeasurer,
            )
        }
    }
}

private fun DrawScope.drawSingleMarker(
    chartContext: ChartContext,
    marker: PersistentMarker,
    position: Offset,
    labelText: String,
    textMeasurer: TextMeasurer,
) {
    if (marker.showGuideLine) {
        drawLine(
            color = marker.guideLineColor,
            start = position,
            end = Offset(x = position.x, y = chartContext.bottom),
            strokeWidth = marker.guideLineWidth,
        )
    }
    if (marker.showDot) {
        drawCircle(brush = marker.dotColor.toMarkerBrush(), radius = marker.dotRadius, center = position)
        marker.dotRingColor?.let { ring ->
            drawCircle(
                color = ring,
                radius = marker.dotRadius,
                center = position,
                style = Stroke(width = marker.dotRingWidth),
            )
        }
    }
    if (marker.showLabel && labelText.isNotEmpty()) {
        drawMarkerLabel(
            chartContext = chartContext,
            marker = marker,
            position = position,
            labelText = labelText,
            textMeasurer = textMeasurer,
        )
    }
}

private fun DrawScope.drawMarkerLabel(
    chartContext: ChartContext,
    marker: PersistentMarker,
    position: Offset,
    labelText: String,
    textMeasurer: TextMeasurer,
) {
    val layout = textMeasurer.measure(text = labelText, style = marker.labelTextStyle)
    val pillWidth = layout.size.width + marker.labelPadding * 2f
    val pillHeight = layout.size.height + marker.labelPadding * 2f
    val topLeft =
        resolveMarkerLabelTopLeft(
            centerX = position.x,
            anchorY = position.y - marker.dotRadius,
            pillWidth = pillWidth,
            pillHeight = pillHeight,
            left = chartContext.left,
            right = chartContext.right,
            top = chartContext.top,
            gap = MARKER_LABEL_GAP,
        )
    drawRoundRect(
        brush = marker.labelBackgroundColor.toMarkerBrush(),
        topLeft = topLeft,
        size = Size(width = pillWidth, height = pillHeight),
        cornerRadius = CornerRadius(marker.labelCornerRadius, marker.labelCornerRadius),
    )
    drawText(
        textLayoutResult = layout,
        topLeft = Offset(x = topLeft.x + marker.labelPadding, y = topLeft.y + marker.labelPadding),
    )
}

private fun ChartyColor.toMarkerBrush(): Brush =
    when (this) {
        is ChartyColor.Solid -> SolidColor(color)
        is ChartyColor.Gradient -> Brush.verticalGradient(colors)
    }
