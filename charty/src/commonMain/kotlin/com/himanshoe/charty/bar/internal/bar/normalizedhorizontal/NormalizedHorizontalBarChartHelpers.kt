package com.himanshoe.charty.bar.internal.bar.normalizedhorizontal

import com.himanshoe.charty.common.PERCENT_SCALE
import com.himanshoe.charty.common.axis.AxisConfig

/**
 * Builds the [AxisConfig] for the horizontal value axis.
 *
 * The axis always spans 0 % – 100 % because every bar is normalised to fill the full width.
 */
internal fun createNormalizedHorizontalAxisConfig(): AxisConfig =
    AxisConfig(
        minValue = 0f,
        maxValue = PERCENT_SCALE,
        steps = NORMALIZED_HORIZONTAL_AXIS_STEPS,
    )
