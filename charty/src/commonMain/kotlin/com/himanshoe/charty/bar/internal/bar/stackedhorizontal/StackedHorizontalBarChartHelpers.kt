package com.himanshoe.charty.bar.internal.bar.stackedhorizontal

import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.constants.ChartConstants

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
        steps = ChartConstants.DEFAULT_AXIS_STEPS,
    )
