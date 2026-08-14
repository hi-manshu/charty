package com.himanshoe.charty.line.internal.multiline

import com.himanshoe.charty.common.constants.ChartConstants

/**
 * Constants used in multiline chart calculations. The hit-testing and point radii are aliased from
 * [ChartConstants] so every chart family measures a tap the same way; the remaining two describe the
 * staggered per-series reveal that is unique to this chart.
 */
internal object MultilineChartConstants {
    const val TAP_RADIUS_MULTIPLIER = ChartConstants.DEFAULT_TAP_RADIUS_MULTIPLIER
    const val POINT_RADIUS_MULTIPLIER = ChartConstants.DEFAULT_HIGHLIGHT_RADIUS_MULTIPLIER
    const val ANIMATION_PROGRESS_MULTIPLIER = 1.2f
}
