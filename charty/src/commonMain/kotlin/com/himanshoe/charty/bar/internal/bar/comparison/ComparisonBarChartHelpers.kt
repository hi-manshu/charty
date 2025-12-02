package com.himanshoe.charty.bar.internal.bar.comparison

import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.axis.AxisConfig

internal fun createComparisonAxisConfig(
    minValue: Float,
    maxValue: Float,
    isBelowAxisMode: Boolean,
): AxisConfig {
    return AxisConfig(
        minValue = minValue,
        maxValue = maxValue,
        steps = COMPARISON_DEFAULT_AXIS_STEPS,
        drawAxisAtZero = isBelowAxisMode,
    )
}

internal fun calculateComparisonBaselineY(
    minValue: Float,
    isBelowAxisMode: Boolean,
    chartContext: ChartContext,
): Float {
    return if (minValue < 0f && isBelowAxisMode) {
        chartContext.convertValueToYPosition(0f)
    } else {
        chartContext.bottom
    }
}

internal fun calculateComparisonBarX(
    chartContext: ChartContext,
    groupWidth: Float,
    groupIndex: Int,
    barWidth: Float,
    barIndex: Int,
): Float {
    return chartContext.left +
        groupWidth * groupIndex +
        barWidth * barIndex +
        groupWidth * COMPARISON_GROUP_PADDING_FRACTION
}

internal fun calculateComparisonBarDimensions(
    value: Float,
    baselineY: Float,
    barValueY: Float,
): Pair<Float, Float> {
    val isNegative = value < 0f

    return if (isNegative) {
        val barTop = baselineY
        val barHeight = barValueY - baselineY
        barTop to barHeight
    } else {
        val barHeight = baselineY - barValueY
        val barTop = baselineY - barHeight
        barTop to barHeight
    }
}

