package com.himanshoe.charty.common.draw

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.himanshoe.charty.color.ChartyColor

/**
 * Strokes [pointPositions] segment by segment, revealing the line as [animationProgress] runs.
 *
 * The final segment is drawn partially, so the line grows out of the last vertex rather than snapping
 * whole into place a segment at a time. Takes the stroke width and cap directly rather than a config,
 * because the combo chart draws the same line from a different one. It lives here rather than in the
 * line package for the same reason: a combo chart importing its line drawing from `line.internal`
 * reads as a chart borrowing another chart's private code, and invites the next person to copy it
 * instead.
 */
internal fun DrawScope.drawAnimatedLineSegments(
    pointPositions: List<Offset>,
    color: ChartyColor,
    lineWidth: Float,
    strokeCap: StrokeCap,
    animationProgress: Float,
) {
    val segmentsToDraw = ((pointPositions.size - 1) * animationProgress).toInt()
    val segmentProgress = ((pointPositions.size - 1) * animationProgress) - segmentsToDraw
    val brush = Brush.linearGradient(color.value)

    for (i in 0 until segmentsToDraw) {
        drawLine(
            brush = brush,
            start = pointPositions[i],
            end = pointPositions[i + 1],
            strokeWidth = lineWidth,
            cap = strokeCap,
        )
    }

    if (segmentsToDraw < pointPositions.size - 1 && segmentProgress > 0) {
        val start = pointPositions[segmentsToDraw]
        val end = pointPositions[segmentsToDraw + 1]
        val partialEnd =
            Offset(
                x = start.x + (end.x - start.x) * segmentProgress,
                y = start.y + (end.y - start.y) * segmentProgress,
            )
        drawLine(
            brush = brush,
            start = start,
            end = partialEnd,
            strokeWidth = lineWidth,
            cap = strokeCap,
        )
    }
}
