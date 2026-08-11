package com.himanshoe.charty.bar.internal.bar.mosaic

import androidx.compose.ui.graphics.Color
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.axis.AxisConfig

private const val MAX_PERCENTAGE = 100f
private const val MIN_PERCENTAGE = 0f
private const val DEFAULT_AXIS_STEPS = 5

/**
 * Default colors for mosaic bar segments.
 */
internal val defaultMosaicColors =
    listOf(
        ChartyColor.Solid(Color(0xFF0B1D3B)),
        ChartyColor.Solid(Color(0xFFD64C66)),
        ChartyColor.Solid(Color(0xFFFFA64D)),
    )

/**
 * Creates axis configuration for mosaic chart (0-100%).
 */
internal fun createMosaicAxisConfig(): AxisConfig =
    AxisConfig(
        minValue = MIN_PERCENTAGE,
        maxValue = MAX_PERCENTAGE,
        steps = DEFAULT_AXIS_STEPS,
    )
