package com.himanshoe.charty.circular.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.himanshoe.charty.circular.config.CircularProgressConfig
import com.himanshoe.charty.circular.config.RingDirection
import com.himanshoe.charty.circular.data.CircularRingData

/**
 * Draw the background ring (unfilled portion)
 */
internal fun DrawScope.drawRingBackground(
    center: Offset,
    radius: Float,
    ring: CircularRingData,
    config: CircularProgressConfig,
    rotationAngle: Float,
    strokeWidth: Float,
) {
    val topLeft = Offset(center.x - radius, center.y - radius)
    val size = Size(radius * CircularProgressConstants.TWO, radius * CircularProgressConstants.TWO)

    drawArc(
        color = ring.getBackgroundColor(),
        startAngle = config.startAngleDegrees + rotationAngle,
        sweepAngle = CircularProgressConstants.FULL_CIRCLE_DEGREES,
        useCenter = false,
        topLeft = topLeft,
        size = size,
        style = Stroke(
            width = strokeWidth,
            cap = config.strokeCap,
        ),
    )
}

/**
 * Draw the progress ring (filled portion) with optional shadow
 */
internal fun DrawScope.drawRingProgress(
    center: Offset,
    radius: Float,
    ring: CircularRingData,
    progress: Float,
    config: CircularProgressConfig,
    rotationAngle: Float,
    strokeWidth: Float,
) {
    val topLeft = Offset(center.x - radius, center.y - radius)
    val size = Size(radius * CircularProgressConstants.TWO, radius * CircularProgressConstants.TWO)
    val sweepAngle = ((progress / ring.maxValue) * CircularProgressConstants.FULL_CIRCLE_DEGREES)
        .coerceIn(0f, CircularProgressConstants.FULL_CIRCLE_DEGREES)
    val actualSweepAngle = if (config.ringDirection == RingDirection.CLOCKWISE) {
        sweepAngle
    } else {
        -sweepAngle
    }

    if (sweepAngle > 0f) {
        if (config.enableShadows && ring.shadowColor != null && ring.shadowRadius > 0f) {
            drawRingShadow(
                topLeft = topLeft,
                size = size,
                ring = ring,
                config = config,
                rotationAngle = rotationAngle,
                actualSweepAngle = actualSweepAngle,
                strokeWidth = strokeWidth,
            )
        }
        drawArc(
            color = ring.getPrimaryColor(),
            startAngle = config.startAngleDegrees + rotationAngle,
            sweepAngle = actualSweepAngle,
            useCenter = false,
            topLeft = topLeft,
            size = size,
            style = Stroke(
                width = strokeWidth,
                cap = config.strokeCap,
            ),
        )
    }
}

/**
 * Draw shadow layers for the ring
 */
private fun DrawScope.drawRingShadow(
    topLeft: Offset,
    size: Size,
    ring: CircularRingData,
    config: CircularProgressConfig,
    rotationAngle: Float,
    actualSweepAngle: Float,
    strokeWidth: Float,
) {
    for (i in CircularProgressConstants.SHADOW_LAYERS downTo 1) {
        val shadowAlpha = (CircularProgressConstants.SHADOW_BASE_ALPHA / i)
        val shadowExpand = (ring.shadowRadius * i) / CircularProgressConstants.SHADOW_LAYERS.toFloat()

        drawArc(
            color = ring.shadowColor!!.copy(alpha = shadowAlpha),
            startAngle = config.startAngleDegrees + rotationAngle,
            sweepAngle = actualSweepAngle,
            useCenter = false,
            topLeft = Offset(
                topLeft.x - shadowExpand / CircularProgressConstants.TWO,
                topLeft.y - shadowExpand / CircularProgressConstants.TWO,
            ),
            size = Size(size.width + shadowExpand, size.height + shadowExpand),
            style = Stroke(
                width = strokeWidth + shadowExpand,
                cap = config.strokeCap,
            ),
            blendMode = BlendMode.Multiply,
        )
    }
}

