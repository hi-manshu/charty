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
