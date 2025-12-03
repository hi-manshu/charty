package com.himanshoe.charty.common.constants

/**
 * An object that holds common constants used across multiple chart types in the Charty library.
 *
 * Centralizing these values improves maintainability and ensures consistency throughout the library.
 */
object ChartConstants {

    /**
     * The multiplier for the tap radius relative to an item's radius.
     * This is used to make tap targets larger than the visual elements, improving user experience.
     */
    const val DEFAULT_TAP_RADIUS_MULTIPLIER = 2.5f

    /**
     * The multiplier for the highlight radius relative to an item's radius.
     */
    const val DEFAULT_HIGHLIGHT_RADIUS_MULTIPLIER = 2f

    /**
     * The default alpha value for guideline or reference lines.
     */
    const val DEFAULT_GUIDELINE_ALPHA = 0.1f

    /**
     * The default stroke width for guidelines.
     */
    const val DEFAULT_GUIDELINE_WIDTH = 1.5f

    /**
     * The outer offset for a highlight circle decoration.
     */
    const val DEFAULT_HIGHLIGHT_OUTER_OFFSET = 3f

    /**
     * The inner offset for a highlight circle decoration.
     */
    const val DEFAULT_HIGHLIGHT_INNER_OFFSET = 2f

    /**
     * The minimum progress value for an animation, representing the start point.
     */
    const val MIN_ANIMATION_PROGRESS = 0f

    /**
     * The maximum progress value for an animation, representing the end point.
     */
    const val MAX_ANIMATION_PROGRESS = 1f

    /**
     * The default number of steps or ticks on the y-axis.
     */
    const val DEFAULT_AXIS_STEPS = 6

    /**
     * The default step size used for rounding values.
     */
    const val DEFAULT_STEP_SIZE = 10

    // ===== Layout Constants =====

    /**
     * A common divisor used for center calculations.
     */
    const val CENTER_DIVISOR = 2f

    /**
     * The minimum margin at the edges in pixels.
     */
    const val MIN_EDGE_MARGIN = 8f
}

