package com.himanshoe.charty.bar.internal.bar.stackedhorizontal

import com.himanshoe.charty.common.axis.AxisConfig

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
