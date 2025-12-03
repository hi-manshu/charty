package com.himanshoe.charty.candlestick.internal

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * Draw a single candlestick with optional rounded corners
 */
internal fun DrawScope.drawCandlestick(params: CandlestickDrawParams) {
    with(params) {
        val bodyLeft = centerX - bodyWidth / CandlestickChartConstants.TWO
        val bodyBottom = bodyTop + bodyHeight

        if (showWicks) {
            if (highY < bodyTop) {
                drawLine(
                    brush = brush,
                    start = Offset(centerX, highY),
                    end = Offset(centerX, bodyTop),
                    strokeWidth = wickWidth,
                )
            }

            if (lowY > bodyBottom) {
                drawLine(
                    brush = brush,
                    start = Offset(centerX, bodyBottom),
                    end = Offset(centerX, lowY),
                    strokeWidth = wickWidth,
                )
            }
        }

        // Draw body
        if (cornerRadius > 0f) {
            drawRoundRect(
                brush = brush,
                topLeft = Offset(bodyLeft, bodyTop),
                size = Size(bodyWidth, bodyHeight),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            )
        } else {
            drawRect(
                brush = brush,
                topLeft = Offset(bodyLeft, bodyTop),
                size = Size(bodyWidth, bodyHeight),
            )
        }
    }
}

