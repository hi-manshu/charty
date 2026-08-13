package com.himanshoe.charty.common.draw

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import com.himanshoe.charty.color.toBrush
import com.himanshoe.charty.color.withChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.config.ReferenceBandConfig

/**
 * Clamps a `[lowValue, highValue]` band (given in either order) to the visible `[minValue, maxValue]`
 * axis range.
 *
 * @return The clamped `(low, high)` pair, or `null` when the band lies entirely outside the axis
 *   range (or collapses to zero width after clamping).
 */
internal fun resolveBandValueBounds(
    lowValue: Float,
    highValue: Float,
    minValue: Float,
    maxValue: Float,
): Pair<Float, Float>? {
    if (minValue == maxValue) {
        return null
    }
    val low = minOf(lowValue, highValue).coerceAtLeast(minValue)
    val high = maxOf(lowValue, highValue).coerceAtMost(maxValue)
    return if (low >= high) {
        null
    } else {
        low to high
    }
}

/**
 * Draws a [ReferenceBandConfig] — a translucent shaded region between two axis values — onto the
 * canvas. Call this **before** drawing the chart's data so the band renders behind it.
 *
 * @param chartContext The chart's drawing context providing coordinate utilities.
 * @param orientation Whether the value axis is vertical (Y) or horizontal (X).
 * @param config The band to draw.
 * @param textMeasurer A [TextMeasurer] used for the optional label.
 */
fun DrawScope.drawReferenceBand(
    chartContext: ChartContext,
    orientation: ChartOrientation,
    config: ReferenceBandConfig,
    textMeasurer: TextMeasurer,
) {
    if (!config.isEnabled) {
        return
    }
    val (low, high) =
        resolveBandValueBounds(
            lowValue = config.lowValue,
            highValue = config.highValue,
            minValue = chartContext.minValue,
            maxValue = chartContext.maxValue,
        ) ?: return

    when (orientation) {
        ChartOrientation.VERTICAL ->
            drawVerticalReferenceBand(
                chartContext = chartContext,
                config = config,
                low = low,
                high = high,
                textMeasurer = textMeasurer,
            )

        ChartOrientation.HORIZONTAL ->
            drawHorizontalReferenceBand(
                chartContext = chartContext,
                config = config,
                low = low,
                high = high,
                textMeasurer = textMeasurer,
            )
    }
}

private fun DrawScope.drawVerticalReferenceBand(
    chartContext: ChartContext,
    config: ReferenceBandConfig,
    low: Float,
    high: Float,
    textMeasurer: TextMeasurer,
) {
    val yHigh = chartContext.convertValueToYPosition(high)
    val yLow = chartContext.convertValueToYPosition(low)
    drawRect(
        brush = config.fill.toBrush(),
        topLeft = Offset(x = chartContext.left, y = yHigh),
        size = Size(width = chartContext.width, height = yLow - yHigh),
        alpha = config.fillAlpha,
    )
    config.borderColor?.let { borderColor ->
        val effect = config.borderPathEffect
        drawLine(
            brush = borderColor.toBrush(),
            start = Offset(x = chartContext.left, y = yHigh),
            end = Offset(x = chartContext.right, y = yHigh),
            strokeWidth = config.borderWidth,
            pathEffect = effect,
        )
        drawLine(
            brush = borderColor.toBrush(),
            start = Offset(x = chartContext.left, y = yLow),
            end = Offset(x = chartContext.right, y = yLow),
            strokeWidth = config.borderWidth,
            pathEffect = effect,
        )
    }
    config.label?.let { label ->
        val layout =
            textMeasurer.measure(
                text = label,
                style = config.labelTextStyle.withChartyColor(config.labelTextColor),
            )
        drawText(
            textLayoutResult = layout,
            topLeft = Offset(x = chartContext.left + config.labelPadding, y = yHigh + config.labelPadding),
        )
    }
}

private fun DrawScope.drawHorizontalReferenceBand(
    chartContext: ChartContext,
    config: ReferenceBandConfig,
    low: Float,
    high: Float,
    textMeasurer: TextMeasurer,
) {
    val range = chartContext.maxValue - chartContext.minValue
    val xLow = chartContext.left + ((low - chartContext.minValue) / range) * chartContext.width
    val xHigh = chartContext.left + ((high - chartContext.minValue) / range) * chartContext.width
    drawRect(
        brush = config.fill.toBrush(),
        topLeft = Offset(x = xLow, y = chartContext.top),
        size = Size(width = xHigh - xLow, height = chartContext.height),
        alpha = config.fillAlpha,
    )
    config.borderColor?.let { borderColor ->
        val effect = config.borderPathEffect
        drawLine(
            brush = borderColor.toBrush(),
            start = Offset(x = xLow, y = chartContext.top),
            end = Offset(x = xLow, y = chartContext.bottom),
            strokeWidth = config.borderWidth,
            pathEffect = effect,
        )
        drawLine(
            brush = borderColor.toBrush(),
            start = Offset(x = xHigh, y = chartContext.top),
            end = Offset(x = xHigh, y = chartContext.bottom),
            strokeWidth = config.borderWidth,
            pathEffect = effect,
        )
    }
    config.label?.let { label ->
        val layout =
            textMeasurer.measure(
                text = label,
                style = config.labelTextStyle.withChartyColor(config.labelTextColor),
            )
        drawText(
            textLayoutResult = layout,
            topLeft = Offset(x = xHigh + config.labelPadding, y = chartContext.top + config.labelPadding),
        )
    }
}
