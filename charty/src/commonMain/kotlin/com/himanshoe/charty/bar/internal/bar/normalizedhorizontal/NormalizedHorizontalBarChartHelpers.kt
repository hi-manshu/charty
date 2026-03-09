package com.himanshoe.charty.bar.internal.bar.normalizedhorizontal

import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig

/**
 * Builds the [AxisConfig] for the horizontal value axis.
 *
 * The axis always spans 0 % – 100 % because every bar is normalised to fill the full width.
 */
internal fun createNormalizedHorizontalAxisConfig(): AxisConfig = AxisConfig(
    minValue = 0f,
    maxValue = NORMALIZED_HORIZONTAL_MAX_PERCENT,
    steps = NORMALIZED_HORIZONTAL_AXIS_STEPS,
)

/**
 * Returns the left-side pixel offset reserved for value-axis tick labels.
 */
internal fun calculateNormalizedHorizontalAxisOffset(scaffoldConfig: ChartScaffoldConfig): Float =
    if (scaffoldConfig.showAxis) {
        scaffoldConfig.axisThickness * NORMALIZED_HORIZONTAL_AXIS_OFFSET_MULTIPLIER
    } else {
        0f
    }
