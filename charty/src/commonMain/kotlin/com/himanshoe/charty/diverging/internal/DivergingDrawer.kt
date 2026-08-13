package com.himanshoe.charty.diverging.internal

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.diverging.data.DivergingData
import com.himanshoe.charty.diverging.data.DivergingSelection
import com.himanshoe.charty.diverging.data.DivergingSide

private const val LABEL_GAP = 6f
private const val HALF = 2f

/**
 * Draws every row's two half-bars, growing outwards from the centre axis, plus their value labels
 * when the chart shows them.
 *
 * The two half-plot brushes are built once per frame and reused for every row; only a row that
 * overrides its colour builds one of its own.
 *
 * @param params The frame's drawing inputs.
 */
internal fun DrawScope.drawDivergingBars(params: DivergingDrawParams) {
    val rowCount = params.dataList.size
    if (rowCount == 0) {
        return
    }
    val centerX = divergingCenterX(chartContext = params.chartContext)
    val leftBrush =
        Brush.horizontalGradient(colors = params.leftColor.value, startX = params.chartContext.left, endX = centerX)
    val rightBrush =
        Brush.horizontalGradient(colors = params.rightColor.value, startX = centerX, endX = params.chartContext.right)

    params.dataList.fastForEachIndexed { index, row ->
        drawDivergingHalf(
            params = params,
            row = row,
            index = index,
            side = DivergingSide.LEFT,
            brush =
                row.leftColor?.let { color ->
                    halfBrush(color = color, startX = centerX, endX = params.chartContext.left)
                }
                    ?: leftBrush,
            label = params.labels?.left?.getOrNull(index),
        )
        drawDivergingHalf(
            params = params,
            row = row,
            index = index,
            side = DivergingSide.RIGHT,
            brush =
                row.rightColor?.let { color ->
                    halfBrush(color = color, startX = centerX, endX = params.chartContext.right)
                } ?: rightBrush,
            label = params.labels?.right?.getOrNull(index),
        )
    }
}

private fun halfBrush(
    color: ChartyColor,
    startX: Float,
    endX: Float,
): Brush = Brush.horizontalGradient(colors = color.value, startX = startX, endX = endX)

private fun DrawScope.drawDivergingHalf(
    params: DivergingDrawParams,
    row: DivergingData,
    index: Int,
    side: DivergingSide,
    brush: Brush,
    label: TextLayoutResult?,
) {
    val value =
        when (side) {
            DivergingSide.LEFT -> row.leftValue
            DivergingSide.RIGHT -> row.rightValue
        }
    val rect =
        divergingBarRect(
            chartContext = params.chartContext,
            index = index,
            rowCount = params.dataList.size,
            side = side,
            value = value,
            scale = params.scales.forSide(side),
            barWidthFraction = params.config.barWidthFraction,
            centerGapFraction = params.config.centerGapFraction,
            animationProgress = params.animationProgress,
        )
    if (rect.width > 0f) {
        drawRoundRect(
            brush = brush,
            topLeft = Offset(x = rect.left, y = rect.top),
            size = Size(width = rect.width, height = rect.height),
            cornerRadius = CornerRadius(x = params.config.cornerRadius.value, y = params.config.cornerRadius.value),
        )
        if (params.recordBounds) {
            params.onBarBoundCalculated(rect to DivergingSelection(data = row, side = side))
        }
    }
    label?.let { measured -> drawDivergingLabel(label = measured, rect = rect, side = side) }
}

private fun DrawScope.drawDivergingLabel(
    label: TextLayoutResult,
    rect: Rect,
    side: DivergingSide,
) {
    val x =
        when (side) {
            DivergingSide.LEFT -> rect.left - LABEL_GAP - label.size.width
            DivergingSide.RIGHT -> rect.right + LABEL_GAP
        }
    drawText(
        textLayoutResult = label,
        topLeft = Offset(x = x, y = rect.top + (rect.height - label.size.height) / HALF),
    )
}
