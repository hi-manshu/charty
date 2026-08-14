package com.himanshoe.charty.combo.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.combo.config.ComboChartConfig
import com.himanshoe.charty.line.config.LineInterpolation
import com.himanshoe.charty.line.internal.line.drawAnimatedLineSegments
import com.himanshoe.charty.line.internal.path.interpolatedLinePath

/**
 * Draw smooth curve line through points using cubic bezier curves
 */
internal fun DrawScope.drawSmoothCurveLine(
    pointPositions: List<Offset>,
    lineColor: ChartyColor,
    comboConfig: ComboChartConfig,
    animationProgress: Float,
) {
    if (pointPositions.isEmpty()) {
        return
    }
    // The same curve the line chart draws. This used to hand-roll its control points as
    // deltaX/3 and 2 * deltaX/3, which is what `smoothSegments` computes behind
    // `interpolatedLinePath` — two implementations of one curve, kept in step by nobody.
    drawPath(
        path = interpolatedLinePath(points = pointPositions, interpolation = LineInterpolation.SMOOTH),
        brush = Brush.linearGradient(lineColor.value),
        style = Stroke(width = comboConfig.lineWidth, cap = comboConfig.strokeCap),
        alpha = animationProgress,
    )
}

/**
 * Draw straight line segments with animation
 */
internal fun DrawScope.drawStraightLine(
    pointPositions: List<Offset>,
    lineColor: ChartyColor,
    comboConfig: ComboChartConfig,
    animationProgress: Float,
) {
    // Byte-for-byte the line chart's own segment drawer before this delegated to it, down to the
    // partial trailing segment that makes the line grow rather than appear.
    drawAnimatedLineSegments(
        pointPositions = pointPositions,
        color = lineColor,
        lineWidth = comboConfig.lineWidth,
        strokeCap = comboConfig.strokeCap,
        animationProgress = animationProgress,
    )
}

/**
 * Draw points on the line with animation
 */
internal fun DrawScope.drawLinePoints(
    pointPositions: List<Offset>,
    lineColor: ChartyColor,
    comboConfig: ComboChartConfig,
    animationProgress: Float,
) {
    val brush = Brush.linearGradient(lineColor.value)
    pointPositions.fastForEachIndexed { index, position ->
        val pointProgress = index.toFloat() / (pointPositions.size - 1).coerceAtLeast(1)
        if (pointProgress <= animationProgress) {
            drawCircle(
                brush = brush,
                radius = comboConfig.pointRadius,
                center = position,
                alpha = comboConfig.pointAlpha,
            )
        }
    }
}
