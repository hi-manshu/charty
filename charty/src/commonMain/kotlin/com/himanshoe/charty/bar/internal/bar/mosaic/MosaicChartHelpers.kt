package com.himanshoe.charty.bar.internal.bar.mosaic

import androidx.compose.ui.graphics.Color
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.PERCENT_SCALE
import com.himanshoe.charty.common.axis.AxisConfig

/**
 * Five divisions of a 0–100% axis, which puts the labels on the twenties. The library's usual six
 * would land them on sixths of a percentage, which reads as noise.
 */
private const val PERCENT_AXIS_STEPS = 5

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
        minValue = 0f,
        maxValue = PERCENT_SCALE,
        steps = PERCENT_AXIS_STEPS,
    )
