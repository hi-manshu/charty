package com.himanshoe.charty.common.tooltip

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.unit.dp

private const val CENTER_DIVISOR = 2f
private const val ARROW_MULTIPLIER = 2f
private const val HORIZONTAL_PADDING_MULTIPLIER = 2f
private const val VERTICAL_PADDING_MULTIPLIER = 2f
private const val SHADOW_OFFSET = 1f
private const val CORNER_RADIUS_DP = 8f
private const val SHADOW_ALPHA = 0.3f
private const val MIN_ARROW_MARGIN = 8f
private const val ELEVATION_THRESHOLD = 0f

private data class TooltipDimensions(
    val width: Float,
    val height: Float,
    val horizontalPadding: Float,
    val verticalPadding: Float,
    val offsetY: Float,
    val minEdgeDistance: Float,
    val arrowSize: Float,
)

private fun calculateTooltipX(
    barCenterX: Float,
    tooltipWidth: Float,
    chartWidth: Float,
    minEdgeDistance: Float,
): Float {
    val centeredX = barCenterX - (tooltipWidth / CENTER_DIVISOR)
    return when {
        centeredX < minEdgeDistance -> minEdgeDistance
        centeredX + tooltipWidth > chartWidth - minEdgeDistance ->
            chartWidth - tooltipWidth - minEdgeDistance
        else -> centeredX
    }
}

private fun determineTooltipPosition(
    requestedPosition: TooltipPosition,
    tooltipAboveY: Float,
    tooltipBelowY: Float,
    tooltipHeight: Float,
    chartTop: Float,
    chartBottom: Float,
    minEdgeDistance: Float,
    barY: Float,
): TooltipPosition = when (requestedPosition) {
    TooltipPosition.ABOVE -> {
        if (tooltipAboveY >= chartTop + minEdgeDistance) {
            TooltipPosition.ABOVE
        } else {
            TooltipPosition.BELOW
        }
    }
    TooltipPosition.BELOW -> {
        if (tooltipBelowY + tooltipHeight <= chartBottom - minEdgeDistance) {
            TooltipPosition.BELOW
        } else {
            TooltipPosition.ABOVE
        }
    }
    TooltipPosition.AUTO -> {
        when {
            tooltipAboveY >= chartTop + minEdgeDistance -> TooltipPosition.ABOVE
            tooltipBelowY + tooltipHeight <= chartBottom - minEdgeDistance -> TooltipPosition.BELOW
            else -> {
                val spaceAbove = barY - chartTop
                val spaceBelow = chartBottom - barY
                if (spaceAbove > spaceBelow) TooltipPosition.ABOVE else TooltipPosition.BELOW
            }
        }
    }
}

private fun calculateTooltipPosition(
    tooltipState: TooltipState,
    dimensions: TooltipDimensions,
    chartWidth: Float,
    chartTop: Float,
    chartBottom: Float,
): Pair<Offset, TooltipPosition> {
    val barCenterX = tooltipState.x + (tooltipState.barWidth / CENTER_DIVISOR)
    val tooltipX = calculateTooltipX(
        barCenterX,
        dimensions.width,
        chartWidth,
        dimensions.minEdgeDistance,
    )

    val totalOffset = dimensions.offsetY + if (dimensions.arrowSize > ELEVATION_THRESHOLD) {
        dimensions.arrowSize
    } else {
        ELEVATION_THRESHOLD
    }

    val tooltipAboveY = tooltipState.y - dimensions.height - totalOffset
    val tooltipBelowY = tooltipState.y + totalOffset

    val finalPosition = determineTooltipPosition(
        tooltipState.position,
        tooltipAboveY,
        tooltipBelowY,
        dimensions.height,
        chartTop,
        chartBottom,
        dimensions.minEdgeDistance,
        tooltipState.y,
    )

    val tooltipY = if (finalPosition == TooltipPosition.ABOVE) tooltipAboveY else tooltipBelowY
    return Offset(tooltipX, tooltipY) to finalPosition
}

private fun DrawScope.drawTooltipShadow(
    tooltipOffset: Offset,
    dimensions: TooltipDimensions,
    config: TooltipConfig,
) {
    val shadowPath = Path().apply {
        addRoundRect(
            androidx.compose.ui.geometry.RoundRect(
                left = tooltipOffset.x - SHADOW_OFFSET,
                top = tooltipOffset.y - SHADOW_OFFSET,
                right = tooltipOffset.x + dimensions.width + SHADOW_OFFSET,
                bottom = tooltipOffset.y + dimensions.height + SHADOW_OFFSET,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(CORNER_RADIUS_DP.dp.toPx()),
            ),
        )
    }
    drawPath(shadowPath, config.backgroundColor.copy(alpha = SHADOW_ALPHA))
}

private fun DrawScope.createTooltipBackgroundPath(
    tooltipOffset: Offset,
    dimensions: TooltipDimensions,
): Path = Path().apply {
    addRoundRect(
        androidx.compose.ui.geometry.RoundRect(
            left = tooltipOffset.x,
            top = tooltipOffset.y,
            right = tooltipOffset.x + dimensions.width,
            bottom = tooltipOffset.y + dimensions.height,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(CORNER_RADIUS_DP.dp.toPx()),
        ),
    )
}

private fun DrawScope.drawArrowWithBorder(
    tooltipState: TooltipState,
    tooltipOffset: Offset,
    dimensions: TooltipDimensions,
    finalPosition: TooltipPosition,
    config: TooltipConfig,
) {
    val barCenterX = tooltipState.x + (tooltipState.barWidth / CENTER_DIVISOR)
    val tooltipLeft = tooltipOffset.x
    val tooltipRight = tooltipOffset.x + dimensions.width
    val arrowMargin = dimensions.arrowSize.coerceAtLeast(MIN_ARROW_MARGIN)
    val arrowBaseLeft = (barCenterX - dimensions.arrowSize).coerceIn(
        tooltipLeft + arrowMargin,
        tooltipRight - arrowMargin - (dimensions.arrowSize * ARROW_MULTIPLIER),
    )
    val arrowBaseRight = arrowBaseLeft + (dimensions.arrowSize * ARROW_MULTIPLIER)

    val arrowPath = Path().apply {
        if (finalPosition == TooltipPosition.ABOVE) {
            moveTo(barCenterX, tooltipState.y - dimensions.offsetY)
            lineTo(arrowBaseLeft, tooltipOffset.y + dimensions.height)
            lineTo(arrowBaseRight, tooltipOffset.y + dimensions.height)
        } else {
            moveTo(barCenterX, tooltipState.y + dimensions.offsetY)
            lineTo(arrowBaseLeft, tooltipOffset.y)
            lineTo(arrowBaseRight, tooltipOffset.y)
        }
        close()
    }
    drawPath(arrowPath, config.backgroundColor)

    config.borderColor?.let { borderColor ->
        val arrowBorderPath = Path().apply {
            if (finalPosition == TooltipPosition.ABOVE) {
                moveTo(barCenterX, tooltipState.y - dimensions.offsetY)
                lineTo(arrowBaseLeft, tooltipOffset.y + dimensions.height)
                moveTo(barCenterX, tooltipState.y - dimensions.offsetY)
                lineTo(arrowBaseRight, tooltipOffset.y + dimensions.height)
            } else {
                moveTo(barCenterX, tooltipState.y + dimensions.offsetY)
                lineTo(arrowBaseLeft, tooltipOffset.y)
                moveTo(barCenterX, tooltipState.y + dimensions.offsetY)
                lineTo(arrowBaseRight, tooltipOffset.y)
            }
        }
        drawPath(arrowBorderPath, borderColor, style = Stroke(config.borderWidth.toPx()))
    }
}

@OptIn(ExperimentalTextApi::class)
internal fun DrawScope.drawTooltip(
    tooltipState: TooltipState,
    config: TooltipConfig,
    textMeasurer: TextMeasurer,
    chartWidth: Float,
    chartTop: Float,
    chartBottom: Float,
) {
    val textLayoutResult = textMeasurer.measure(tooltipState.content, config.textStyle)
    val horizontalPadding = config.padding.horizontal.toPx()
    val verticalPadding = config.padding.vertical.toPx()

    val dimensions = TooltipDimensions(
        width = textLayoutResult.size.width + (horizontalPadding * HORIZONTAL_PADDING_MULTIPLIER),
        height = textLayoutResult.size.height + (verticalPadding * VERTICAL_PADDING_MULTIPLIER),
        horizontalPadding = horizontalPadding,
        verticalPadding = verticalPadding,
        offsetY = config.offsetY.toPx(),
        minEdgeDistance = config.minDistanceFromEdge.toPx(),
        arrowSize = if (config.showArrow) config.arrowSize.toPx() else ELEVATION_THRESHOLD,
    )

    val (tooltipOffset, finalPosition) = calculateTooltipPosition(
        tooltipState = tooltipState,
        dimensions = dimensions,
        chartWidth = chartWidth,
        chartTop = chartTop,
        chartBottom = chartBottom,
    )

    if (config.elevation.value > ELEVATION_THRESHOLD) {
        drawTooltipShadow(tooltipOffset, dimensions, config)
    }

    val tooltipPath = createTooltipBackgroundPath(tooltipOffset, dimensions)
    drawPath(tooltipPath, config.backgroundColor)

    if (config.showArrow) {
        drawArrowWithBorder(tooltipState, tooltipOffset, dimensions, finalPosition, config)
    }

    config.borderColor?.let { borderColor ->
        drawPath(tooltipPath, borderColor, style = Stroke(config.borderWidth.toPx()))
    }

    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(
            tooltipOffset.x + dimensions.horizontalPadding,
            tooltipOffset.y + dimensions.verticalPadding,
        ),
    )
}

