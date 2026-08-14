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

    /**
     * How far ahead of the line's own progress the dots are allowed to run.
     *
     * At 1.0 the last dot would land exactly as the line finished drawing, which reads as the dot
     * arriving late — it appears only once the line has already reached it. Letting the reveal run a
     * fifth faster puts each dot on its point just before the stroke gets there.
     */
    const val POINT_REVEAL_LEAD = 1.2f
}
