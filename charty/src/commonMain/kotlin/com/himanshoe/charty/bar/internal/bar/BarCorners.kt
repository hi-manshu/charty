package com.himanshoe.charty.bar.internal.bar

/**
 * Which pair of a bar's corners is rounded.
 *
 * A bar is rounded only at the end it grows towards, so the end joined to the axis stays square and
 * the bar reads as sitting on the baseline rather than floating above it. Which pair that is depends
 * on the chart's direction and, for a negative value, on which side of the baseline it is drawn.
 */
internal enum class BarCorners {
    /** Rounded at the top: an upward vertical bar. */
    TOP,

    /** Rounded at the bottom: a downward vertical bar. */
    BOTTOM,

    /** Rounded on the right: a rightward horizontal bar. */
    TRAILING,

    /** Rounded on the left: a leftward horizontal bar. */
    LEADING,

    /** Square: a segment in the middle of a stack, which has a neighbour on both ends. */
    NONE,
}
