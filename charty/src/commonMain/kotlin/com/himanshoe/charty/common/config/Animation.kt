package com.himanshoe.charty.common.config

/**
 * Sealed interface for animation configuration
 */
sealed interface Animation {
    /**
     * No animation - chart appears instantly
     */
    data object Disabled : Animation

    /**
     * Animated chart with configurable duration
     *
     * @param duration Animation duration in milliseconds
     */
    data class Enabled(val duration: Int = 800) : Animation {
        init {
            require(duration > 0) { "Animation duration must be positive" }
        }
    }

    /**
     * Companion object providing common animation presets
     */
    companion object {
        /**
         * Default animation with 800ms duration
         */
        val Default = Enabled()

        /**
         * Fast animation with 400ms duration
         */
        val Fast = Enabled(duration = 400)

        /**
         * Slow animation with 1200ms duration
         */
        val Slow = Enabled(duration = 1200)
    }
}

