package com.himanshoe.charty.common.tooltip

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.dp

/**
 * Calculates the optimal position for a tooltip to ensure it stays within chart bounds
 *
 * @param tooltipState The current tooltip state with position information
 * @param tooltipWidth Width of the tooltip
 * @param tooltipHeight Height of the tooltip
 * @param chartWidth Total width of the chart
 * @param chartTop Top boundary of the chart
 * @param chartBottom Bottom boundary of the chart
 * @param config Tooltip configuration
 * @return Pair of (x, y) coordinates for the tooltip and final position (ABOVE or BELOW)
 */
private fun calculateTooltipPosition(
    tooltipState: TooltipState,
    tooltipWidth: Float,
    tooltipHeight: Float,
    chartWidth: Float,
    chartTop: Float,
    chartBottom: Float,
    config: TooltipConfig,
    offsetYPx: Float,
    minDistanceFromEdgePx: Float,
    arrowSizePx: Float,
): Pair<Offset, TooltipPosition> {
    val barCenterX = tooltipState.x + (tooltipState.barWidth / 2f)
    var tooltipX = barCenterX - (tooltipWidth / 2f)
    if (tooltipX < minDistanceFromEdgePx) {
        tooltipX = minDistanceFromEdgePx
    } else if (tooltipX + tooltipWidth > chartWidth - minDistanceFromEdgePx) {
        tooltipX = chartWidth - tooltipWidth - minDistanceFromEdgePx
    }
    val totalOffset = offsetYPx + if (config.showArrow) arrowSizePx else 0f
    val tooltipAboveY = tooltipState.y - tooltipHeight - totalOffset
    val tooltipBelowY = tooltipState.y + totalOffset

    val finalPosition = when (tooltipState.position) {
        TooltipPosition.ABOVE -> {
            if (tooltipAboveY >= chartTop + minDistanceFromEdgePx) {
                TooltipPosition.ABOVE
            } else {
                TooltipPosition.BELOW
            }
        }
        TooltipPosition.BELOW -> {
            if (tooltipBelowY + tooltipHeight <= chartBottom - minDistanceFromEdgePx) {
                TooltipPosition.BELOW
            } else {
                TooltipPosition.ABOVE
            }
        }
        TooltipPosition.AUTO -> {
            if (tooltipAboveY >= chartTop + minDistanceFromEdgePx) {
                TooltipPosition.ABOVE
            } else if (tooltipBelowY + tooltipHeight <= chartBottom - minDistanceFromEdgePx) {
                TooltipPosition.BELOW
            } else {
                val spaceAbove = tooltipState.y - chartTop
                val spaceBelow = chartBottom - tooltipState.y
                if (spaceAbove > spaceBelow) TooltipPosition.ABOVE else TooltipPosition.BELOW
            }
        }
    }

    val tooltipY = if (finalPosition == TooltipPosition.ABOVE) {
        tooltipAboveY
    } else {
        tooltipBelowY
    }

    return Offset(tooltipX, tooltipY) to finalPosition
}

/**
 * Draws a tooltip with arrow on the canvas
 *
 * @param tooltipState The tooltip state with content and position
 * @param config Tooltip configuration
 * @param textMeasurer Text measurer for calculating text dimensions
 * @param chartWidth Width of the chart
 * @param chartTop Top boundary of the chart
 * @param chartBottom Bottom boundary of the chart
 */
@OptIn(ExperimentalTextApi::class)
internal fun DrawScope.drawTooltip(
    tooltipState: TooltipState,
    config: TooltipConfig,
    textMeasurer: TextMeasurer,
    chartWidth: Float,
    chartTop: Float,
    chartBottom: Float,
) {
    val textLayoutResult = textMeasurer.measure(
        text = tooltipState.content,
        style = config.textStyle,
    )

    val horizontalPaddingPx = config.padding.horizontal.toPx()
    val verticalPaddingPx = config.padding.vertical.toPx()
    val tooltipWidth = textLayoutResult.size.width + (horizontalPaddingPx * 2)
    val tooltipHeight = textLayoutResult.size.height + (verticalPaddingPx * 2)
    val offsetYPx = config.offsetY.toPx()
    val minDistanceFromEdgePx = config.minDistanceFromEdge.toPx()
    val arrowSizePx = config.arrowSize.toPx()

    val (tooltipOffset, finalPosition) = calculateTooltipPosition(
        tooltipState = tooltipState,
        tooltipWidth = tooltipWidth,
        tooltipHeight = tooltipHeight,
        chartWidth = chartWidth,
        chartTop = chartTop,
        chartBottom = chartBottom,
        config = config,
        offsetYPx = offsetYPx,
        minDistanceFromEdgePx = minDistanceFromEdgePx,
        arrowSizePx = arrowSizePx,
    )

    if (config.elevation > 0.dp) {
        val shadowPath = Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = tooltipOffset.x - 1f,
                    top = tooltipOffset.y - 1f,
                    right = tooltipOffset.x + tooltipWidth + 1f,
                    bottom = tooltipOffset.y + tooltipHeight + 1f,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                ),
            )
        }
        drawPath(
            path = shadowPath,
            color = config.backgroundColor.copy(alpha = 0.3f),
        )
    }

    val tooltipPath = Path().apply {
        addRoundRect(
            androidx.compose.ui.geometry.RoundRect(
                left = tooltipOffset.x,
                top = tooltipOffset.y,
                right = tooltipOffset.x + tooltipWidth,
                bottom = tooltipOffset.y + tooltipHeight,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
            ),
        )
    }
    drawPath(
        path = tooltipPath,
        color = config.backgroundColor,
    )

    if (config.showArrow) {
        val barCenterX = tooltipState.x + (tooltipState.barWidth / 2f)
        val tooltipLeft = tooltipOffset.x
        val tooltipRight = tooltipOffset.x + tooltipWidth
        val arrowMargin = arrowSizePx.coerceAtLeast(8f)
        val arrowBaseLeft = (barCenterX - arrowSizePx).coerceIn(tooltipLeft + arrowMargin, tooltipRight - arrowMargin - (arrowSizePx * 2))
        val arrowBaseRight = arrowBaseLeft + (arrowSizePx * 2)

        if (finalPosition == TooltipPosition.ABOVE) {
            val arrowPath = Path().apply {
                moveTo(barCenterX, tooltipState.y - offsetYPx)
                lineTo(arrowBaseLeft, tooltipOffset.y + tooltipHeight)
                lineTo(arrowBaseRight, tooltipOffset.y + tooltipHeight)
                close()
            }
            drawPath(
                path = arrowPath,
                color = config.backgroundColor,
            )
        } else {
            val arrowPath = Path().apply {
                moveTo(barCenterX, tooltipState.y + offsetYPx)
                lineTo(arrowBaseLeft, tooltipOffset.y)
                lineTo(arrowBaseRight, tooltipOffset.y)
                close()
            }
            drawPath(
                path = arrowPath,
                color = config.backgroundColor,
            )
        }
    }

    config.borderColor?.let { borderColor ->
        drawPath(
            path = tooltipPath,
            color = borderColor,
            style = Stroke(width = config.borderWidth.toPx()),
        )

        if (config.showArrow) {
            val barCenterX = tooltipState.x + (tooltipState.barWidth / 2f)
            val tooltipLeft = tooltipOffset.x
            val tooltipRight = tooltipOffset.x + tooltipWidth
            val arrowMargin = arrowSizePx.coerceAtLeast(8f)
            val arrowBaseLeft = (barCenterX - arrowSizePx).coerceIn(tooltipLeft + arrowMargin, tooltipRight - arrowMargin - (arrowSizePx * 2))
            val arrowBaseRight = arrowBaseLeft + (arrowSizePx * 2)
            val arrowBorderPath = Path().apply {
                if (finalPosition == TooltipPosition.ABOVE) {
                    moveTo(barCenterX, tooltipState.y - offsetYPx)
                    lineTo(arrowBaseLeft, tooltipOffset.y + tooltipHeight)
                    moveTo(barCenterX, tooltipState.y - offsetYPx)
                    lineTo(arrowBaseRight, tooltipOffset.y + tooltipHeight)
                } else {
                    moveTo(barCenterX, tooltipState.y + offsetYPx)
                    lineTo(arrowBaseLeft, tooltipOffset.y)
                    moveTo(barCenterX, tooltipState.y + offsetYPx)
                    lineTo(arrowBaseRight, tooltipOffset.y)
                }
            }
            drawPath(
                path = arrowBorderPath,
                color = borderColor,
                style = Stroke(width = config.borderWidth.toPx()),
            )
        }
    }
    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(
            x = tooltipOffset.x + horizontalPaddingPx,
            y = tooltipOffset.y + verticalPaddingPx,
        ),
    )
}

