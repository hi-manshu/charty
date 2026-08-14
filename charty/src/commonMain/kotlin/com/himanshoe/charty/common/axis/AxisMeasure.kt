package com.himanshoe.charty.common.axis

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle

private const val MIN_AXIS_STEPS = 2

/** Extra space on either side of a value-axis label, in pixels. */
const val AXIS_LABEL_MARGIN = 20f

/**
 * Measures the width of the widest tick label an [axisConfig] would render, so a chart can size its
 * value-axis gutter to the actual labels (e.g. wide numbers like `1500000`) instead of a hardcoded
 * guess.
 *
 * @param axisConfig The axis whose tick labels are measured (min/max/steps/formatter).
 * @param textMeasurer Used to measure each formatted label.
 * @param labelStyle The text style the labels are drawn with.
 * @return The widest label width in pixels (0 when there are no ticks).
 */
internal fun measureMaxAxisLabelWidth(
    axisConfig: AxisConfig,
    textMeasurer: TextMeasurer,
    labelStyle: TextStyle,
): Float {
    val steps = axisConfig.steps.coerceAtLeast(MIN_AXIS_STEPS)
    val range = axisConfig.maxValue - axisConfig.minValue
    var maxWidth = 0f
    for (i in 0..steps) {
        val value = axisConfig.minValue + range * (i.toFloat() / steps)
        val width =
            textMeasurer
                .measure(text = AnnotatedString(axisConfig.valueFormatter(value)), style = labelStyle)
                .size.width
                .toFloat()
        if (width > maxWidth) {
            maxWidth = width
        }
    }
    return maxWidth
}

/**
 * The value-axis gutter width for [axisConfig]: the widest measured label plus [AXIS_LABEL_MARGIN] on
 * each side. Returns just the margins when [showLabels] is `false`.
 */
internal fun measureAxisGutter(
    axisConfig: AxisConfig,
    textMeasurer: TextMeasurer,
    labelStyle: TextStyle,
    showLabels: Boolean,
): Float {
    if (!showLabels) {
        return AXIS_LABEL_MARGIN
    }
    val labelWidth =
        measureMaxAxisLabelWidth(axisConfig = axisConfig, textMeasurer = textMeasurer, labelStyle = labelStyle)
    return labelWidth + AXIS_LABEL_MARGIN * 2f
}

/**
 * The width of the widest of [labels], for sizing a gutter that holds category names rather than
 * numbers.
 *
 * A horizontal chart puts its categories down the left-hand side, where a long one — "September",
 * "Engineering" — is as wide as several numbers and just as capable of running off the edge.
 */
internal fun measureMaxLabelWidth(
    labels: List<String>,
    textMeasurer: TextMeasurer,
    labelStyle: TextStyle,
): Float {
    var maxWidth = 0f
    labels.forEach { label ->
        val width =
            textMeasurer
                .measure(text = AnnotatedString(label), style = labelStyle)
                .size.width
                .toFloat()
        if (width > maxWidth) {
            maxWidth = width
        }
    }
    return maxWidth
}

/**
 * How far the last tick label on a horizontal value axis reaches past the end of the plot.
 *
 * Bottom-axis labels are centred on their tick, so the final one — the largest number, and therefore
 * usually the widest — hangs half its width beyond the plot's right edge. With only a fixed margin
 * there it was sliced off by the canvas, which is what made a seven-figure value read as a six-figure
 * one.
 */
internal fun measureTrailingLabelOverhang(
    axisConfig: AxisConfig,
    textMeasurer: TextMeasurer,
    labelStyle: TextStyle,
    showLabels: Boolean,
): Float {
    if (!showLabels) {
        return AXIS_LABEL_MARGIN
    }
    val widest =
        measureMaxAxisLabelWidth(axisConfig = axisConfig, textMeasurer = textMeasurer, labelStyle = labelStyle)
    return (widest / 2f + AXIS_LABEL_MARGIN).coerceAtLeast(AXIS_LABEL_MARGIN)
}

/** Space kept between two neighbouring axis labels, in pixels, so they read as separate numbers. */
private const val LABEL_GAP = 12f

/**
 * How many ticks to advance between labels so that neighbouring labels cannot overlap.
 *
 * Returns 1 when every tick fits. Wider labels — long numbers, or a narrow chart — return 2, 3 and so
 * on, which thins the labels while leaving the grid alone.
 */
internal fun labelStrideFor(
    axisConfig: AxisConfig,
    steps: Int,
    axisWidth: Float,
    textMeasurer: TextMeasurer,
    labelStyle: TextStyle,
): Int {
    val spacing =
        if (steps > 0) {
            axisWidth / steps
        } else {
            0f
        }
    if (spacing <= 0f) {
        return 1
    }
    val needed =
        measureMaxAxisLabelWidth(
            axisConfig = axisConfig,
            textMeasurer = textMeasurer,
            labelStyle = labelStyle,
        ) + LABEL_GAP
    var stride = 1
    while (stride < steps && spacing * stride < needed) {
        stride++
    }
    return stride
}
