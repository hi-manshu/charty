package com.himanshoe.charty.common.draw

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.axis.formatAxisLabel
import com.himanshoe.charty.common.config.ReferenceLineConfig
import com.himanshoe.charty.common.config.ReferenceLineLabelPosition
import com.himanshoe.charty.common.config.ReferenceLineStrokeStyle

/**
 * Internal drawing helpers for reference / target / average lines.
 * These are chart-agnostic utilities that rely on [ChartContext] for coordinate mapping.
 */

private const val DEFAULT_DASH_INTERVAL = 10f
private const val DASH_PHASE_OFFSET = 0f
private const val CENTER_DIVISOR = 2f

private fun referenceValueWithinRange(
    value: Float,
    minValue: Float,
    maxValue: Float,
): Boolean {
    if (minValue == maxValue) return false
    return value in minValue..maxValue
}

/**
 * Draw a reference line for cartesian charts (bar/line/point/combo) that use [ChartContext].
 *
 * @param orientation Orientation of the chart (VERTICAL or HORIZONTAL)
 * @param config Configuration of the reference line
 * @param textMeasurer TextMeasurer for laying out the label text
 */
fun DrawScope.drawReferenceLine(
    chartContext: ChartContext,
    orientation: ChartOrientation,
    config: ReferenceLineConfig,
    textMeasurer: TextMeasurer,
) {
    if (!config.isEnabled) return
    if (!referenceValueWithinRange(config.value, chartContext.minValue, chartContext.maxValue)) return

    when (orientation) {
        ChartOrientation.VERTICAL -> drawHorizontalReferenceLine(chartContext, config, textMeasurer)
        ChartOrientation.HORIZONTAL -> drawVerticalReferenceLine(chartContext, config, textMeasurer)
    }
}

private fun DrawScope.drawHorizontalReferenceLine(
    chartContext: ChartContext,
    config: ReferenceLineConfig,
    textMeasurer: TextMeasurer,
) {
    val y = chartContext.convertValueToYPosition(config.value)
    val start = Offset(chartContext.left, y)
    val end = Offset(chartContext.right, y)

    val pathEffect =
        when (config.strokeStyle) {
            ReferenceLineStrokeStyle.SOLID -> null
            ReferenceLineStrokeStyle.DASHED -> {
                val intervals = config.dashIntervals ?: floatArrayOf(DEFAULT_DASH_INTERVAL, DEFAULT_DASH_INTERVAL)
                PathEffect.dashPathEffect(intervals, DASH_PHASE_OFFSET)
            }
        }

    drawLine(
        color = config.color,
        start = start,
        end = end,
        strokeWidth = config.strokeWidth,
        pathEffect = pathEffect,
    )

    val labelText =
        when {
            config.label != null -> config.label
            config.showValueInLabelWhenNoText -> formatAxisLabel(config.value)
            else -> null
        } ?: return

    val textLayoutResult: TextLayoutResult =
        textMeasurer.measure(
            text = labelText,
            style = config.labelTextStyle,
        )

    val textWidth = textLayoutResult.size.width.toFloat()
    val textHeight = textLayoutResult.size.height.toFloat()

    val x =
        when (config.labelPosition) {
            ReferenceLineLabelPosition.START -> chartContext.left
            ReferenceLineLabelPosition.CENTER, ReferenceLineLabelPosition.ABOVE, ReferenceLineLabelPosition.BELOW ->
                chartContext.left + (chartContext.width - textWidth) / CENTER_DIVISOR

            ReferenceLineLabelPosition.END -> chartContext.right - textWidth
        }

    val yText =
        when (config.labelPosition) {
            ReferenceLineLabelPosition.ABOVE, ReferenceLineLabelPosition.START, ReferenceLineLabelPosition.END ->
                y - config.labelOffset - textHeight

            ReferenceLineLabelPosition.BELOW -> y + config.labelOffset
            ReferenceLineLabelPosition.CENTER -> y - textHeight / CENTER_DIVISOR
        }

    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(x, yText),
    )
}

private fun calculateVerticalLabelY(
    labelPosition: ReferenceLineLabelPosition,
    chartContext: ChartContext,
    textHeight: Float,
): Float = when (labelPosition) {
    ReferenceLineLabelPosition.START -> chartContext.bottom - textHeight
    ReferenceLineLabelPosition.CENTER, ReferenceLineLabelPosition.ABOVE, ReferenceLineLabelPosition.BELOW ->
        chartContext.top + (chartContext.height - textHeight) / CENTER_DIVISOR

    ReferenceLineLabelPosition.END -> chartContext.top
}

private fun calculateVerticalLabelX(
    labelPosition: ReferenceLineLabelPosition,
    x: Float,
    textWidth: Float,
    labelOffset: Float,
): Float = when (labelPosition) {
    ReferenceLineLabelPosition.ABOVE, ReferenceLineLabelPosition.START, ReferenceLineLabelPosition.END ->
        x - textWidth - labelOffset

    ReferenceLineLabelPosition.BELOW -> x + labelOffset
    ReferenceLineLabelPosition.CENTER -> x - textWidth / CENTER_DIVISOR
}

private fun DrawScope.drawVerticalLineWithLabel(
    x: Float,
    chartContext: ChartContext,
    config: ReferenceLineConfig,
    textMeasurer: TextMeasurer,
    pathEffect: PathEffect?,
) {
    drawLine(
        color = config.color,
        start = Offset(x, chartContext.top),
        end = Offset(x, chartContext.bottom),
        strokeWidth = config.strokeWidth,
        pathEffect = pathEffect,
    )

    val labelText = when {
        config.label != null -> config.label
        config.showValueInLabelWhenNoText -> formatAxisLabel(config.value)
        else -> null
    } ?: return

    val textLayoutResult = textMeasurer.measure(labelText, config.labelTextStyle)
    val textWidth = textLayoutResult.size.width.toFloat()
    val textHeight = textLayoutResult.size.height.toFloat()

    val y = calculateVerticalLabelY(config.labelPosition, chartContext, textHeight)
    val xText = calculateVerticalLabelX(config.labelPosition, x, textWidth, config.labelOffset)

    drawText(textLayoutResult, topLeft = Offset(xText, y))
}

private fun DrawScope.drawVerticalReferenceLine(
    chartContext: ChartContext,
    config: ReferenceLineConfig,
    textMeasurer: TextMeasurer,
) {
    val range = chartContext.maxValue - chartContext.minValue
    if (range == 0f) return

    val normalized = (config.value - chartContext.minValue) / range
    val x = chartContext.left + normalized * chartContext.width

    val pathEffect = when (config.strokeStyle) {
        ReferenceLineStrokeStyle.SOLID -> null
        ReferenceLineStrokeStyle.DASHED -> {
            val intervals = config.dashIntervals ?: floatArrayOf(DEFAULT_DASH_INTERVAL, DEFAULT_DASH_INTERVAL)
            PathEffect.dashPathEffect(intervals, DASH_PHASE_OFFSET)
        }
    }

    drawVerticalLineWithLabel(x, chartContext, config, textMeasurer, pathEffect)
}
