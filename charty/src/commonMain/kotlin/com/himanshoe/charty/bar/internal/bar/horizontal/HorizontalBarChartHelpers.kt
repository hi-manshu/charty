package com.himanshoe.charty.bar.internal.bar.horizontal

import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.axis.AxisConfig

internal fun createHorizontalAxisConfig(
    minValue: Float,
    maxValue: Float,
    drawAxisAtZero: Boolean,
): AxisConfig =
    AxisConfig(
        minValue = minValue,
        maxValue = maxValue,
        steps = HORIZONTAL_DEFAULT_AXIS_STEPS,
        drawAxisAtZero = drawAxisAtZero,
    )

internal fun calculateHorizontalBaselineX(
    drawAxisAtZero: Boolean,
    minValue: Float,
    maxValue: Float,
    chartContext: ChartContext,
): Float =
    if (drawAxisAtZero) {
        val range = maxValue - minValue
        val zeroNormalized = (0f - minValue) / range
        chartContext.left + (zeroNormalized * chartContext.width)
    } else {
        chartContext.left
    }

internal fun calculateHorizontalBarDimensions(
    isNegative: Boolean,
    isBelowAxisMode: Boolean,
    baselineX: Float,
    barValueX: Float,
    animationProgress: Float,
): Pair<Float, Float> =
    if (isNegative && isBelowAxisMode) {
        val fullBarWidth = baselineX - barValueX
        val barWidth = fullBarWidth * animationProgress
        val barLeft = barValueX
        barLeft to barWidth
    } else {
        val fullBarWidth = barValueX - baselineX
        val barWidth = fullBarWidth * animationProgress
        val barLeft = baselineX
        barLeft to barWidth
    }
