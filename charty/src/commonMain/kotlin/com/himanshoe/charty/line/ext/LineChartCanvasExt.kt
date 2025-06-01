package com.himanshoe.charty.line.ext

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.common.ChartColor
import com.himanshoe.charty.line.config.InteractionTooltipConfig
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.model.LineData
import com.himanshoe.charty.line.model.MultiLineData

/**
 * Draws a line curve on the canvas.
 */
internal fun DrawScope.drawLineCurve(
    data: () -> List<LineData>,
    canvasHeight: Float,
    xStep: Float,
    minValue: Float,
    maxValue: Float,
    chartConfig: LineChartConfig,
    lineColor: ChartColor,
    showFilledArea: Boolean = false,
    showLineStroke: Boolean = true,
    fillColor: ChartColor,
    smoothLineCurve: Boolean = true,
) {
    val lineConfig = chartConfig.lineConfig
    val linePath = Path()
    val fillPath = Path()
    val lineData = data()
    if (lineData.isNotEmpty()) {
        val yScale = canvasHeight / (maxValue - minValue)
        val firstPointY = canvasHeight - (lineData[0].yValue - minValue) * yScale
        linePath.moveTo(0f, firstPointY)
        fillPath.moveTo(0f, firstPointY)

        val drawPathSegment: (Int) -> Unit = { i ->
            val currentX = i * xStep
            val currentY = canvasHeight - (lineData[i].yValue - minValue) * yScale
            if (smoothLineCurve) {
                val previousX = (i - 1) * xStep
                val previousY = canvasHeight - (lineData[i - 1].yValue - minValue) * yScale
                val controlPointX = (previousX + currentX) / 2
                linePath.cubicTo(controlPointX, previousY, controlPointX, currentY, currentX, currentY)
                fillPath.cubicTo(controlPointX, previousY, controlPointX, currentY, currentX, currentY)
            } else {
                linePath.lineTo(currentX, currentY)
                fillPath.lineTo(currentX, currentY)
            }
        }

        for (i in 1 until lineData.size) {
            drawPathSegment(i)
        }

        if (showFilledArea) {
            fillPath.lineTo(size.width, size.height)
            fillPath.lineTo(0f, size.height)
            fillPath.close()
            drawPath(
                path = fillPath,
                brush = Brush.linearGradient(
                    fillColor.value.fastMap {
                        it.copy(alpha = 0.2f)
                    }
                )
            )
        }

        if (showLineStroke) {
            drawPath(
                path = linePath,
                brush = Brush.linearGradient(lineColor.value),
                style = Stroke(
                    cap = lineConfig.lineCap,
                    width = lineConfig.lineChartStrokeWidth
                )
            )
        }
    }
}

/**
 * Draws values on the line.
 */
internal fun DrawScope.drawValueOnLine(
    lineData: List<LineData>,
    xStep: Float,
    minValue: Float,
    yRange: Float,
    canvasHeight: Float,
    textMeasurer: TextMeasurer,
    valueTextColor: ChartColor,
    valueTextStyle: TextStyle?
) {
    lineData.forEachIndexed { index, lineDataPoint ->
        val x = index * xStep
        val y = canvasHeight - (lineDataPoint.yValue - minValue) * (canvasHeight / yRange)
        val textBrush = Brush.linearGradient(valueTextColor.value)
        val textLayoutResult = textMeasurer.measure(
            text = lineDataPoint.yValue.toString(),
            style = valueTextStyle?.copy(brush = textBrush)
                ?: TextStyle.Default.copy(brush = textBrush)
        )

        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(
                x = x + 10,
                y = y - textLayoutResult.size.height / 2
            ),
        )
    }
}

/**
 * Draws an indicator line on the canvas.
 */
internal fun DrawScope.drawIndicatorLine(
    xTooltipPosition: Float,
    canvasHeight: Float,
    interactionTooltipConfig: InteractionTooltipConfig
) {
    drawLine(
        brush = Brush.linearGradient(interactionTooltipConfig.indicatorLineColor.value),
        start = Offset(xTooltipPosition, 0f),
        pathEffect = interactionTooltipConfig.indicatorLinePathEffect,
        end = Offset(xTooltipPosition, canvasHeight),
        strokeWidth = interactionTooltipConfig.indicatorLineWidth,
        cap = Stroke.DefaultCap
    )
}

/**
 * Draws a tooltip on the canvas.
 */
private fun DrawScope.drawTooltip(
    lineData: List<LineData>,
    yValue: Float,
    xTooltipPosition: Float,
    index: Int,
    minValue: Float,
    yRange: Float,
    canvasHeight: Float,
    textMeasurer: TextMeasurer,
    interactionTooltipConfig: InteractionTooltipConfig
) {
    val yPosition = canvasHeight - (yValue - minValue) * (canvasHeight / yRange)
    val textBrush = Brush.linearGradient(interactionTooltipConfig.textColor.value)
    val textLayoutResult = textMeasurer.measure(
        text = yValue.toString(),
        style = interactionTooltipConfig.textStyle?.copy(brush = textBrush)
            ?: TextStyle.Default.copy(brush = textBrush)
    )
    val textX = if (index == lineData.size - 1) {
        xTooltipPosition - textLayoutResult.size.width - 10
    } else {
        xTooltipPosition + 10
    }
    drawRect(
        brush = Brush.linearGradient(interactionTooltipConfig.containerColor.value),
        topLeft = Offset(
            x = textX - 5,
            y = yPosition - textLayoutResult.size.height / 2 - 5
        ),
        size = Size(
            width = (textLayoutResult.size.width + 10).toFloat(),
            height = (textLayoutResult.size.height + 10).toFloat()
        )
    )
    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(
            x = textX,
            y = yPosition - textLayoutResult.size.height / 2
        ),
    )
}

/**
 * Draws tooltips for multiple lines on the canvas.
 */
internal fun DrawScope.drawMultiLineChartTooltip(
    multiLineData: List<MultiLineData>,
    xTooltipPosition: Float,
    xScaleFactor: Float,
    minValue: Float,
    yRange: Float,
    canvasHeight: Float,
    onValueChange: (List<LineData>) -> Unit,
    textMeasurer: TextMeasurer,
    interactionTooltipConfig: InteractionTooltipConfig
) {
    multiLineData.fastForEach { lineData ->
        onValueChange(lineData.data)
        val index = (xTooltipPosition / xScaleFactor).toInt().coerceIn(0, lineData.data.size - 1)
        val yValue = lineData.data[index].yValue
        drawTooltip(
            lineData = lineData.data,
            yValue = yValue,
            xTooltipPosition = xTooltipPosition,
            index = index,
            minValue = minValue,
            yRange = yRange,
            canvasHeight = canvasHeight,
            textMeasurer = textMeasurer,
            interactionTooltipConfig = interactionTooltipConfig
        )
    }
}

/**
 * Draws a tooltip for a single line on the canvas.
 */
internal fun DrawScope.drawLineChartTooltip(
    lineData: List<LineData>,
    xTooltipPosition: Float,
    xStep: Float,
    minValue: Float,
    yRange: Float,
    canvasHeight: Float,
    onValueChange: (LineData) -> Unit,
    textMeasurer: TextMeasurer,
    interactionTooltipConfig: InteractionTooltipConfig
) {
    val index = (xTooltipPosition / xStep).toInt().coerceIn(0, lineData.size - 1)
    val yValue = lineData[index].yValue
    onValueChange(lineData[index])

    drawTooltip(
        lineData = lineData,
        yValue = yValue,
        xTooltipPosition = xTooltipPosition,
        index = index,
        minValue = minValue,
        yRange = yRange,
        canvasHeight = canvasHeight,
        textMeasurer = textMeasurer,
        interactionTooltipConfig = interactionTooltipConfig
    )
}
