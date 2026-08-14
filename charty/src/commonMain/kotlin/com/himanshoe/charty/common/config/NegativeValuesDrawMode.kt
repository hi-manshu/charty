package com.himanshoe.charty.common.config

/**
 * How a chart draws values that fall below zero.
 *
 * Bar, line, area, point and combo charts all offer this choice, so it belongs to none of them. It
 * lived in the bar package, which meant a line chart's config had to import from `bar.config` to
 * describe its own axis.
 */
enum class NegativeValuesDrawMode {
    /**
     * Negative values extend below the zero axis line (axis centered)
     * Positive values extend above the zero axis line
     * Best for visualizing profit/loss, gains/losses, etc.
     */
    BELOW_AXIS,

    /**
     * Everything is drawn from the minimum value upward
     * The axis starts at the lowest value instead of zero
     * Best for showing relative differences when all values should appear above baseline
     */
    FROM_MIN_VALUE,
}
