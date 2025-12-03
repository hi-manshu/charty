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

private const val VERTICAL_CHART_LEFT_PADDING_WITH_LABELS = 60f
private const val VERTICAL_CHART_LEFT_PADDING_WITHOUT_LABELS = 20f
private const val VERTICAL_CHART_RIGHT_PADDING = 20f
private const val VERTICAL_CHART_TOP_PADDING = 20f
private const val VERTICAL_CHART_BOTTOM_PADDING_WITH_LABELS = 50f
private const val VERTICAL_CHART_BOTTOM_PADDING_WITHOUT_LABELS = 20f

private const val HORIZONTAL_CHART_LEFT_PADDING_WITH_LABELS = 100f
private const val HORIZONTAL_CHART_LEFT_PADDING_WITHOUT_LABELS = 20f
private const val HORIZONTAL_CHART_RIGHT_PADDING = 20f
private const val HORIZONTAL_CHART_TOP_PADDING = 20f
private const val HORIZONTAL_CHART_BOTTOM_PADDING_WITH_LABELS = 50f
private const val HORIZONTAL_CHART_BOTTOM_PADDING_WITHOUT_LABELS = 20f

private const val LABEL_OFFSET = 10f
private const val CENTER_DIVISOR = 2f
private const val POSITION_OFFSET = 0.5f
private const val MIN_STEPS = 2

private const val ZERO_VALUE = 0f

private data class ChartBounds(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val width: Float,
    val height: Float,
)

private fun calculateVerticalChartBounds(
    size: androidx.compose.ui.geometry.Size,
    showLabels: Boolean,
    hasXLabels: Boolean,
): ChartBounds {
    val leftPadding =
        if (showLabels) VERTICAL_CHART_LEFT_PADDING_WITH_LABELS else VERTICAL_CHART_LEFT_PADDING_WITHOUT_LABELS
    val rightPadding = VERTICAL_CHART_RIGHT_PADDING
    val topPadding = VERTICAL_CHART_TOP_PADDING
    val bottomPadding = if (showLabels && hasXLabels) {
        VERTICAL_CHART_BOTTOM_PADDING_WITH_LABELS
    } else {
        VERTICAL_CHART_BOTTOM_PADDING_WITHOUT_LABELS
    }

    val right = size.width - rightPadding
    val bottom = size.height - bottomPadding

    return ChartBounds(
        left = leftPadding,
        top = topPadding,
        right = right,
        bottom = bottom,
        width = right - leftPadding,
        height = bottom - topPadding,
    )
}

private fun calculateHorizontalAxisPosition(
    yAxisConfig: AxisConfig,
    chartBounds: ChartBounds,
): Float =
    if (yAxisConfig.minValue < ZERO_VALUE && yAxisConfig.maxValue > ZERO_VALUE && yAxisConfig.drawAxisAtZero) {
        val range = yAxisConfig.maxValue - yAxisConfig.minValue
        val zeroNormalized = (ZERO_VALUE - yAxisConfig.minValue) / range
        chartBounds.bottom - (zeroNormalized * chartBounds.height)
    } else {
        chartBounds.bottom
    }

private fun DrawScope.drawRotatedText(
    textLayout: androidx.compose.ui.text.TextLayoutResult,
    topLeft: Offset,
    rotation: Float,
    pivot: Offset,
) {
    drawContext.transform.rotate(rotation, pivot)
    drawText(textLayoutResult = textLayout, topLeft = topLeft)
    drawContext.transform.rotate(-rotation, pivot)
}

/**
 * Draws the axes for a vertical chart, including axis lines, grid lines, and labels.
 *
 * @param xLabels A list of strings for the x-axis labels.
 * @param yAxisConfig The configuration for the y-axis.
 * @param config The general configuration for the chart scaffold.
 * @param textMeasurer A [TextMeasurer] for measuring text.
 * @param labelStyle The [TextStyle] for the labels.
 * @param leftLabelRotation The rotation for the labels on the left axis.
 */
internal fun DrawScope.drawVerticalChartAxes(
    xLabels: List<String>,
    yAxisConfig: AxisConfig,
    config: ChartScaffoldConfig,
    textMeasurer: TextMeasurer,
    labelStyle: TextStyle,
    leftLabelRotation: LabelRotation,
) {
    val bounds = calculateVerticalChartBounds(size, config.showLabels, xLabels.isNotEmpty())
    val valueRange = yAxisConfig.maxValue - yAxisConfig.minValue
    val steps = yAxisConfig.steps.coerceAtLeast(MIN_STEPS)

    if (config.showAxis) {
        drawLine(
            color = config.axisColor,
            start = Offset(bounds.left, bounds.top),
            end = Offset(bounds.left, bounds.bottom),
            strokeWidth = config.axisThickness,
        )

        val xAxisY = calculateHorizontalAxisPosition(yAxisConfig, bounds)
        drawLine(
            color = config.axisColor,
            start = Offset(bounds.left, xAxisY),
            end = Offset(bounds.right, xAxisY),
            strokeWidth = config.axisThickness,
        )
    }

    for (i in 0..steps) {
        val value = yAxisConfig.minValue + valueRange * (i.toFloat() / steps)
        val normalized = (value - yAxisConfig.minValue) / valueRange
        val y = bounds.bottom - (normalized * bounds.height)

        if (config.showGrid && i > 0 && i < steps) {
            drawLine(
                color = config.gridColor,
                start = Offset(bounds.left, y),
                end = Offset(bounds.right, y),
                strokeWidth = config.gridThickness,
            )
        }

        if (config.showLabels) {
            val textLayout = textMeasurer.measure(AnnotatedString(formatAxisLabel(value)), labelStyle)
            val labelX = bounds.left - textLayout.size.width - LABEL_OFFSET
            val labelY = y - textLayout.size.height / CENTER_DIVISOR
            val topLeft = Offset(labelX, labelY)

            if (leftLabelRotation.degrees != ZERO_VALUE) {
                val pivot = Offset(bounds.left - LABEL_OFFSET, y)
                drawRotatedText(textLayout, topLeft, leftLabelRotation.degrees, pivot)
            } else {
                drawText(textLayoutResult = textLayout, topLeft = topLeft)
            }
        }
    }

    if (config.showLabels && xLabels.isNotEmpty()) {
        val labelWidth = bounds.width / xLabels.size
        xLabels.forEachIndexed { index, label ->
            val textLayout = textMeasurer.measure(AnnotatedString(label), labelStyle)
            val centerX = bounds.left + labelWidth * (index + POSITION_OFFSET)
            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(
                    centerX - textLayout.size.width / CENTER_DIVISOR,
                    bounds.bottom + LABEL_OFFSET,
                ),
            )
        }
    }
}

private fun calculateHorizontalChartBounds(
    size: androidx.compose.ui.geometry.Size,
    showLabels: Boolean,
): ChartBounds {
    val leftPadding =
        if (showLabels) HORIZONTAL_CHART_LEFT_PADDING_WITH_LABELS else HORIZONTAL_CHART_LEFT_PADDING_WITHOUT_LABELS
    val rightPadding = HORIZONTAL_CHART_RIGHT_PADDING
    val topPadding = HORIZONTAL_CHART_TOP_PADDING
    val bottomPadding =
        if (showLabels) HORIZONTAL_CHART_BOTTOM_PADDING_WITH_LABELS else HORIZONTAL_CHART_BOTTOM_PADDING_WITHOUT_LABELS

    val right = size.width - rightPadding
    val bottom = size.height - bottomPadding

    return ChartBounds(
        left = leftPadding,
        top = topPadding,
        right = right,
        bottom = bottom,
        width = right - leftPadding,
        height = bottom - topPadding,
    )
}

private fun calculateVerticalAxisPosition(
    yAxisConfig: AxisConfig,
    chartBounds: ChartBounds,
): Float =
    if (yAxisConfig.minValue < ZERO_VALUE && yAxisConfig.drawAxisAtZero) {
        val range = yAxisConfig.maxValue - yAxisConfig.minValue
        val zeroNormalized = (ZERO_VALUE - yAxisConfig.minValue) / range
        chartBounds.left + (zeroNormalized * chartBounds.width)
    } else {
        chartBounds.left
    }

/**
 * Draws the axes for a horizontal chart, including axis lines, grid lines, and labels.
 *
 * @param xLabels A list of strings for the x-axis labels.
 * @param yAxisConfig The configuration for the y-axis.
 * @param config The general configuration for the chart scaffold.
 * @param textMeasurer A [TextMeasurer] for measuring text.
 * @param labelStyle The [TextStyle] for the labels.
 * @param leftLabelRotation The rotation for the labels on the left axis.
 */
internal fun DrawScope.drawHorizontalChartAxes(
    xLabels: List<String>,
    yAxisConfig: AxisConfig,
    config: ChartScaffoldConfig,
    textMeasurer: TextMeasurer,
    labelStyle: TextStyle,
    leftLabelRotation: LabelRotation,
) {
    val bounds = calculateHorizontalChartBounds(size, config.showLabels)
    val baselineX = calculateVerticalAxisPosition(yAxisConfig, bounds)
    val valueRange = yAxisConfig.maxValue - yAxisConfig.minValue
    val steps = yAxisConfig.steps.coerceAtLeast(MIN_STEPS)

    if (config.showAxis) {
        drawLine(
            color = config.axisColor,
            start = Offset(bounds.left, bounds.top),
            end = Offset(bounds.left, bounds.bottom),
            strokeWidth = config.axisThickness,
        )

        drawLine(
            color = config.axisColor,
            start = Offset(baselineX, bounds.top),
            end = Offset(baselineX, bounds.bottom),
            strokeWidth = config.axisThickness,
        )
    }

    for (i in 0..steps) {
        val value = yAxisConfig.minValue + valueRange * (i.toFloat() / steps)
        val normalized = (value - yAxisConfig.minValue) / valueRange
        val x = bounds.left + (normalized * bounds.width)

        if (config.showGrid && i > 0 && i < steps) {
            drawLine(
                color = config.gridColor,
                start = Offset(x, bounds.top),
                end = Offset(x, bounds.bottom),
                strokeWidth = config.gridThickness,
            )
        }

        if (config.showLabels) {
            val textLayout = textMeasurer.measure(AnnotatedString(formatAxisLabel(value)), labelStyle)
            drawText(
                textLayoutResult = textLayout,
                topLeft = Offset(
                    x - textLayout.size.width / CENTER_DIVISOR,
                    bounds.bottom + LABEL_OFFSET,
                ),
            )
        }
    }

    if (config.showLabels && xLabels.isNotEmpty()) {
        val barHeight = bounds.height / xLabels.size
        xLabels.forEachIndexed { index, label ->
            val textLayout = textMeasurer.measure(AnnotatedString(label), labelStyle)
            val centerY = bounds.top + barHeight * (index + POSITION_OFFSET)
            val labelX = bounds.left - textLayout.size.width - LABEL_OFFSET
            val labelY = centerY - textLayout.size.height / CENTER_DIVISOR
            val topLeft = Offset(labelX, labelY)

            if (leftLabelRotation.degrees != ZERO_VALUE) {
                val pivot = Offset(bounds.left - LABEL_OFFSET, centerY)
                drawRotatedText(
                    textLayout = textLayout,
                    topLeft = topLeft,
                    rotation = leftLabelRotation.degrees,
                    pivot = pivot,
                )
            } else {
                drawText(
                    textLayoutResult = textLayout,
                    topLeft = topLeft,
                )
            }
        }
    }
}
