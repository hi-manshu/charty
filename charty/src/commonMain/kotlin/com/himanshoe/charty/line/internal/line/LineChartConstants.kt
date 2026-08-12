package com.himanshoe.charty.line.internal.line

import com.himanshoe.charty.common.constants.ChartConstants

/**
 * Constants used in line chart calculations, aliased from [ChartConstants] so the line family shares
 * one source of truth with the rest of the library instead of re-declaring the same magic numbers.
 */
internal object LineChartConstants {
    const val TAP_RADIUS_MULTIPLIER = ChartConstants.DEFAULT_TAP_RADIUS_MULTIPLIER
    const val POINT_RADIUS_MULTIPLIER = ChartConstants.DEFAULT_HIGHLIGHT_RADIUS_MULTIPLIER
    const val POINT_HIGHLIGHT_RADIUS_ADDITION = ChartConstants.DEFAULT_HIGHLIGHT_OUTER_OFFSET
    const val POINT_HIGHLIGHT_INNER_RADIUS_ADDITION = ChartConstants.DEFAULT_HIGHLIGHT_INNER_OFFSET
    const val TOOLTIP_LINE_ALPHA = ChartConstants.DEFAULT_GUIDELINE_ALPHA
    const val TOOLTIP_LINE_WIDTH = ChartConstants.DEFAULT_GUIDELINE_WIDTH
}
