package com.himanshoe.charty.bar.internal.span

import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig

internal fun createAxisConfig(minValue: Float, maxValue: Float): AxisConfig {
    return AxisConfig(
        minValue = minValue,
        maxValue = maxValue,
        steps = DEFAULT_AXIS_STEPS,
        drawAxisAtZero = false,
    )
}

internal fun calculateAxisOffset(scaffoldConfig: ChartScaffoldConfig): Float {
    return if (scaffoldConfig.showAxis) {
        scaffoldConfig.axisThickness * AXIS_OFFSET_MULTIPLIER
    } else {
        0f
    }
}

