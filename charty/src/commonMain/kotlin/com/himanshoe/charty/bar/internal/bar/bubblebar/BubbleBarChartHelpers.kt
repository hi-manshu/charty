package com.himanshoe.charty.bar.internal.bar.bubblebar

/**
 * Helper functions for BubbleBarChart calculations
 */

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
