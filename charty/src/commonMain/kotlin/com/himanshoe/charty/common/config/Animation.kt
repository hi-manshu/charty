package com.himanshoe.charty.common.config

/**
 * A sealed interface that defines the animation configuration for a chart.
 *
 * This interface allows for enabling or disabling animations, as well as customizing the animation duration.
 */
sealed interface Animation {
    /**
     * Represents a disabled animation state, where the chart appears instantly without any transition.
     */
    data object Disabled : Animation

    /**
     * Represents an enabled animation state with a configurable duration.
     *
     * @param duration The duration of the animation in milliseconds. Must be a positive value.
     */
    data class Enabled(
        val duration: Int = 800,
    ) : Animation {
        init {
            require(duration > 0) { "Animation duration must be positive" }
        }
    }

    /**
     * A companion object that provides common animation presets.
     */
    companion object {
        /**
         * The default animation, with a duration of 800 milliseconds.
         */
        val Default = Enabled()

        /**
         * A fast animation, with a duration of 400 milliseconds.
         */
        val Fast = Enabled(duration = 400)

        /**
         * A slow animation, with a duration of 1200 milliseconds.
         */
        val Slow = Enabled(duration = 1200)
    }
}
