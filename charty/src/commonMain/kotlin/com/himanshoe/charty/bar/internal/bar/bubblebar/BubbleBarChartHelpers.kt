package com.himanshoe.charty.bar.internal.bar.bubblebar

import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.axis.AxisConfig

/**
 * Helper functions for BubbleBarChart calculations
 */

internal fun createAxisConfig(
    minValue: Float,
    maxValue: Float,
    isBelowAxisMode: Boolean,
): AxisConfig {
    return AxisConfig(
        minValue = minValue,
        maxValue = maxValue,
        steps = 6,
        drawAxisAtZero = isBelowAxisMode,
    )
}

internal fun calculateBaselineY(
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

internal fun calculateBubbleBarDimensions(
    barValue: Float,
    baselineY: Float,
    barValueY: Float,
    animationProgress: Float,
): Pair<Float, Float> {
    val isNegative = barValue < 0f

    return if (isNegative) {
        val barTop = baselineY
        val fullBarHeight = barValueY - baselineY
        val barHeight = fullBarHeight * animationProgress
        barTop to barHeight
    } else {
        val fullBarHeight = baselineY - barValueY
        val animatedBarHeight = fullBarHeight * animationProgress
        val barTop = baselineY - animatedBarHeight
        barTop to animatedBarHeight
    }
}

