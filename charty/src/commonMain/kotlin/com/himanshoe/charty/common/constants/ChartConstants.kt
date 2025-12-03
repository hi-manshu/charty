package com.himanshoe.charty.common.constants

/**
 * Common constants used across multiple chart types.
 * Centralizing these values improves maintainability and consistency.
 */
object ChartConstants {

    // ===== Gesture & Touch Constants =====

    /**
     * Multiplier for tap radius relative to item radius
     * Used to make tap targets larger than visual elements for better UX
     */
    const val DEFAULT_TAP_RADIUS_MULTIPLIER = 2.5f

    /**
     * Multiplier for highlight radius relative to item radius
     */
    const val DEFAULT_HIGHLIGHT_RADIUS_MULTIPLIER = 2f

    // ===== Visual Constants =====

    /**
     * Default alpha value for guideline/reference lines
     */
    const val DEFAULT_GUIDELINE_ALPHA = 0.1f

    /**
     * Default stroke width for guidelines
     */
    const val DEFAULT_GUIDELINE_WIDTH = 1.5f

    /**
     * Outer offset for highlight circle decoration
     */
    const val DEFAULT_HIGHLIGHT_OUTER_OFFSET = 3f

    /**
     * Inner offset for highlight circle decoration
     */
    const val DEFAULT_HIGHLIGHT_INNER_OFFSET = 2f

    // ===== Animation Constants =====

    /**
     * Minimum animation progress value (start)
     */
    const val MIN_ANIMATION_PROGRESS = 0f

    /**
     * Maximum animation progress value (end)
     */
    const val MAX_ANIMATION_PROGRESS = 1f

    // ===== Axis Constants =====

    /**
     * Default number of steps/ticks on Y-axis
     */
    const val DEFAULT_AXIS_STEPS = 6

    /**
     * Default step size for value rounding
     */
    const val DEFAULT_STEP_SIZE = 10

    // ===== Layout Constants =====

    /**
     * Common divisor for center calculations
     */
    const val CENTER_DIVISOR = 2f

    /**
     * Minimum edge margin in pixels
     */
    const val MIN_EDGE_MARGIN = 8f
}

