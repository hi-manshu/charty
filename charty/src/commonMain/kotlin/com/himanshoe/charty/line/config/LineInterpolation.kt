package com.himanshoe.charty.line.config

/**
 * How consecutive points of a line chart are connected.
 */
enum class LineInterpolation {
    /** Straight segments between points. */
    LINEAR,

    /** A smooth cubic-bezier curve through the points. */
    SMOOTH,

    /** Horizontal-then-vertical steps: the value holds until the next point, then jumps. */
    STEP,
}
