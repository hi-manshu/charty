@file:OptIn(ExperimentalTextApi::class)

package com.himanshoe.charty.common.ext

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.axis.LabelRotation
import com.himanshoe.charty.common.axis.formatAxisLabel
import com.himanshoe.charty.common.config.ChartScaffoldConfig

internal fun DrawScope.drawVerticalChartAxes(
    xLabels: List<String>,
    yAxisConfig: AxisConfig,
    config: ChartScaffoldConfig,
    textMeasurer: TextMeasurer,
    labelStyle: TextStyle,
    leftLabelRotation: LabelRotation,
) {
    // Calculate chart area
    val leftPadding = if (config.showLabels) 60f else 20f
    val rightPadding = 20f
    val topPadding = 20f
    val bottomPadding = if (config.showLabels && xLabels.isNotEmpty()) 50f else 20f

    val chartRight = size.width - rightPadding
    val chartBottom = size.height - bottomPadding
    val chartHeight = chartBottom - topPadding
    val chartWidth = chartRight - leftPadding

    // Draw Y-axis line (left vertical line)
    if (config.showAxis) {
        drawLine(
            color = config.axisColor,
            start = Offset(leftPadding, topPadding),
            end = Offset(leftPadding, chartBottom),
            strokeWidth = config.axisThickness,
        )
    }

    // Draw X-axis line (horizontal line at bottom or zero)
    if (config.showAxis) {
        val xAxisPosition =
            if (yAxisConfig.minValue < 0f && yAxisConfig.maxValue > 0f && yAxisConfig.drawAxisAtZero) {
                // Position at zero when we have both positive and negative values and drawAxisAtZero is true
                val range = yAxisConfig.maxValue - yAxisConfig.minValue
                val zeroNormalized = (0f - yAxisConfig.minValue) / range
                chartBottom - (zeroNormalized * chartHeight)
            } else {
                // Otherwise place the X axis at the bottom (min value)
                chartBottom
            }

        drawLine(
            color = config.axisColor,
            start = Offset(leftPadding, xAxisPosition),
            end = Offset(chartRight, xAxisPosition),
            strokeWidth = config.axisThickness,
        )
    }

    // Draw Y-axis grid and labels
    val steps = yAxisConfig.steps.coerceAtLeast(2)
    for (i in 0..steps) {
        val value =
            yAxisConfig.minValue +
                (yAxisConfig.maxValue - yAxisConfig.minValue) * (i.toFloat() / steps)
        val normalized = (value - yAxisConfig.minValue) / (yAxisConfig.maxValue - yAxisConfig.minValue)
        val y = chartBottom - (normalized * chartHeight)

        // Grid line (horizontal)
        if (config.showGrid && i > 0 && i < steps) {
            drawLine(
                color = config.gridColor,
                start = Offset(leftPadding, y),
                end = Offset(chartRight, y),
                strokeWidth = config.gridThickness,
            )
        }

        // Y-axis label (left side)
        if (config.showLabels) {
            val labelText = formatAxisLabel(value)
            val textLayout = textMeasurer.measure(AnnotatedString(labelText), labelStyle)

            if (leftLabelRotation.degrees != 0f) {
                // Draw rotated label
                drawContext.transform.rotate(
                    degrees = leftLabelRotation.degrees,
                    pivot = Offset(leftPadding - 10f, y),
                )
                drawText(
                    textLayoutResult = textLayout,
                    topLeft =
                    Offset(
                        leftPadding - textLayout.size.width - 10f,
                        y - textLayout.size.height / 2,
                    ),
                )
                drawContext.transform.rotate(
                    degrees = -leftLabelRotation.degrees,
                    pivot = Offset(leftPadding - 10f, y),
                )
            } else {
                // Draw non-rotated label
                drawText(
                    textLayoutResult = textLayout,
                    topLeft =
                    Offset(
                        leftPadding - textLayout.size.width - 10f,
                        y - textLayout.size.height / 2,
                    ),
                )
            }
        }
    }

    // Draw X-axis labels (centered under each position)
    if (config.showLabels && xLabels.isNotEmpty()) {
        xLabels.forEachIndexed { index, label ->
            val centerX = leftPadding + chartWidth * (index + 0.5f) / xLabels.size
            val textLayout = textMeasurer.measure(AnnotatedString(label), labelStyle)

            drawText(
                textLayoutResult = textLayout,
                topLeft =
                Offset(
                    centerX - textLayout.size.width / 2,
                    chartBottom + 10f,
                ),
            )
        }
    }
}

internal fun DrawScope.drawHorizontalChartAxes(
    xLabels: List<String>,
    yAxisConfig: AxisConfig,
    config: ChartScaffoldConfig,
    textMeasurer: TextMeasurer,
    labelStyle: TextStyle,
    leftLabelRotation: LabelRotation,
) {
    // Calculate chart area - more space for category labels on left
    val leftPadding = if (config.showLabels) 100f else 20f
    val rightPadding = 20f
    val topPadding = 20f
    val bottomPadding = if (config.showLabels) 50f else 20f

    val chartRight = size.width - rightPadding
    val chartBottom = size.height - bottomPadding
    val chartWidth = chartRight - leftPadding
    val chartHeight = chartBottom - topPadding

    // Calculate baseline position (X position where bars start/end for zero)
    val baselineX =
        if (yAxisConfig.minValue < 0f && yAxisConfig.drawAxisAtZero) {
            val range = yAxisConfig.maxValue - yAxisConfig.minValue
            val zeroNormalized = (0f - yAxisConfig.minValue) / range
            leftPadding + (zeroNormalized * chartWidth)
        } else {
            leftPadding
        }

    // Draw category axis (vertical line on left)
    if (config.showAxis) {
        drawLine(
            color = config.axisColor,
            start = Offset(leftPadding, topPadding),
            end = Offset(leftPadding, chartBottom),
            strokeWidth = config.axisThickness,
        )
    }

    // Draw value axis (vertical line at baseline)
    if (config.showAxis) {
        drawLine(
            color = config.axisColor,
            start = Offset(baselineX, topPadding),
            end = Offset(baselineX, chartBottom),
            strokeWidth = config.axisThickness,
        )
    }

    // Draw value grid and labels (vertical lines for horizontal chart)
    val steps = yAxisConfig.steps.coerceAtLeast(2)
    for (i in 0..steps) {
        val value =
            yAxisConfig.minValue +
                (yAxisConfig.maxValue - yAxisConfig.minValue) * (i.toFloat() / steps)
        val normalized = (value - yAxisConfig.minValue) / (yAxisConfig.maxValue - yAxisConfig.minValue)
        val x = leftPadding + (normalized * chartWidth)

        // Grid line (vertical)
        if (config.showGrid && i > 0 && i < steps) {
            drawLine(
                color = config.gridColor,
                start = Offset(x, topPadding),
                end = Offset(x, chartBottom),
                strokeWidth = config.gridThickness,
            )
        }

        // Value label (at bottom)
        if (config.showLabels) {
            val labelText = formatAxisLabel(value)
            val textLayout = textMeasurer.measure(AnnotatedString(labelText), labelStyle)

            drawText(
                textLayoutResult = textLayout,
                topLeft =
                Offset(
                    x - textLayout.size.width / 2,
                    chartBottom + 10f,
                ),
            )
        }
    }

    // Draw category labels (left side, vertically centered in each section)
    if (config.showLabels && xLabels.isNotEmpty()) {
        xLabels.forEachIndexed { index, label ->
            val barHeight = chartHeight / xLabels.size
            val centerY = topPadding + barHeight * (index + 0.5f)
            val textLayout = textMeasurer.measure(AnnotatedString(label), labelStyle)

            if (leftLabelRotation.degrees != 0f) {
                // Draw rotated label
                drawContext.transform.rotate(
                    degrees = leftLabelRotation.degrees,
                    pivot = Offset(leftPadding - 10f, centerY),
                )
                drawText(
                    textLayoutResult = textLayout,
                    topLeft =
                    Offset(
                        leftPadding - textLayout.size.width - 10f,
                        centerY - textLayout.size.height / 2,
                    ),
                )
                drawContext.transform.rotate(
                    degrees = -leftLabelRotation.degrees,
                    pivot = Offset(leftPadding - 10f, centerY),
                )
            } else {
                // Draw non-rotated label
                drawText(
                    textLayoutResult = textLayout,
                    topLeft =
                    Offset(
                        leftPadding - textLayout.size.width - 10f,
                        centerY - textLayout.size.height / 2,
                    ),
                )
            }
        }
    }
}
