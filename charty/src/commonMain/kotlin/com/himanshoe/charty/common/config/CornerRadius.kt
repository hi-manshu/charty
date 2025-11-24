package com.himanshoe.charty.common.config

private const val CORNER_RADIUS_NONE = 0f
private const val CORNER_RADIUS_SMALL = 4f
private const val CORNER_RADIUS_MEDIUM = 8f
private const val CORNER_RADIUS_LARGE = 12f
private const val CORNER_RADIUS_EXTRA_LARGE = 16f

/**
 * Sealed class for corner radius sizes with value parameter
 */
sealed class CornerRadius(val value: Float) {

    /** No corner rounding (0dp) */
    data object None : CornerRadius(CORNER_RADIUS_NONE)

    /** Small corner radius (4dp) */
    data object Small : CornerRadius(CORNER_RADIUS_SMALL)

    /** Medium corner radius (8dp) */
    data object Medium : CornerRadius(CORNER_RADIUS_MEDIUM)

    /** Large corner radius (12dp) */
    data object Large : CornerRadius(CORNER_RADIUS_LARGE)

    /** Extra large corner radius (16dp) */
    data object ExtraLarge : CornerRadius(CORNER_RADIUS_EXTRA_LARGE)

    /**
     * Custom corner radius with user-specified value
     * @param radius The corner radius value in dp
     */
    data class Custom(private val radius: Float) : CornerRadius(radius) {
        init {
            require(radius >= 0f) { "Corner radius must be non-negative" }
        }
    }
}

