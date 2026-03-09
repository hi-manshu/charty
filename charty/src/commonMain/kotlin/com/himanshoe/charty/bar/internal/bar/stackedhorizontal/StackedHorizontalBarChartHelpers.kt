package com.himanshoe.charty.bar.internal.bar.stackedhorizontal

import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig

/**
 * Builds the [AxisConfig] that drives the horizontal (value) axis.
 *
 * For stacked horizontal bars the value range is always [0, maxTotal] because
 * negative stacked values are not supported.
 */
internal fun createStackedHorizontalAxisConfig(maxTotal: Float): AxisConfig =
    AxisConfig(
        minValue = 0f,
        maxValue = maxTotal,
        steps = STACKED_HORIZONTAL_DEFAULT_AXIS_STEPS,
    )

/**
 * Returns the left-side pixel offset reserved for the value-axis tick labels.
 *
 * Mirrors the logic used in [HorizontalBarChart] so axis label spacing is consistent
 * across all horizontal chart variants.
 */
internal fun calculateStackedHorizontalAxisOffset(scaffoldConfig: ChartScaffoldConfig): Float =
    if (scaffoldConfig.showAxis) {
        scaffoldConfig.axisThickness * STACKED_HORIZONTAL_AXIS_OFFSET_MULTIPLIER
    } else {
        0f
    }
