package com.himanshoe.charty.common.config

private const val CORNER_RADIUS_NONE = 0f
private const val CORNER_RADIUS_SMALL = 4f
private const val CORNER_RADIUS_MEDIUM = 8f
private const val CORNER_RADIUS_LARGE = 12f
private const val CORNER_RADIUS_EXTRA_LARGE = 16f

/**
 * A sealed class that defines the size of corner radii for chart elements.
 *
 * This class provides a set of predefined radius sizes, as well as a way to specify a custom radius.
 *
 * @property value The corner radius value in density-independent pixels (dp).
 */
sealed class CornerRadius(
    val value: Float,
) {
    /** Represents no corner rounding (0 dp). */
    data object None : CornerRadius(CORNER_RADIUS_NONE)

    /** Represents a small corner radius (4 dp). */
    data object Small : CornerRadius(CORNER_RADIUS_SMALL)

    /** Represents a medium corner radius (8 dp). */
    data object Medium : CornerRadius(CORNER_RADIUS_MEDIUM)

    /** Represents a large corner radius (12 dp). */
    data object Large : CornerRadius(CORNER_RADIUS_LARGE)

    /** Represents an extra-large corner radius (16 dp). */
    data object ExtraLarge : CornerRadius(CORNER_RADIUS_EXTRA_LARGE)

    /**
     * Represents a custom corner radius with a user-specified value.
     *
     * @param radius The corner radius value in dp. Must be a non-negative value.
     */
    data class Custom(
        private val radius: Float,
    ) : CornerRadius(radius) {
        init {
            require(radius >= 0f) { "Corner radius must be non-negative" }
        }
    }
}
