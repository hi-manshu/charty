package com.himanshoe.charty.bar.internal.bar.barchart

import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.axis.AxisConfig

/**
 * Calculate the baseline Y position for bars, considering negative values and axis position
 */
internal fun calculateBarBaselineY(minValue: Float, isBelowAxisMode: Boolean, chartContext: ChartContext): Float =
    if (minValue < 0f && isBelowAxisMode) {
        chartContext.convertValueToYPosition(0f)
    } else {
        chartContext.bottom
    }

/**
 * Helper function to create the axis configuration for the Y axis
 */
internal fun createBarAxisConfig(minValue: Float, maxValue: Float, isBelowAxisMode: Boolean): AxisConfig {
    return AxisConfig(
        minValue = minValue,
        maxValue = maxValue,
        steps = 6,
        drawAxisAtZero = isBelowAxisMode,
    )
}

