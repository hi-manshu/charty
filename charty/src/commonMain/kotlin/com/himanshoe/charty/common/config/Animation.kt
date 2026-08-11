package com.himanshoe.charty.common.config

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring as ComposeSpring
import androidx.compose.runtime.Immutable

/**
 * A sealed interface that defines the animation configuration for a chart.
 *
 * This interface allows for enabling or disabling animations, customizing the duration and easing
 * of a tween-based animation, or selecting a physics-based [Spring] animation for a smoother,
 * more natural feel.
 */
@Immutable
sealed interface Animation {
    /**
     * Represents a disabled animation state, where the chart appears instantly without any transition.
     */
    data object Disabled : Animation

    /**
     * Represents a duration-based (tween) animation with a configurable [duration] and [easing].
     *
     * @param duration The duration of the animation in milliseconds. Must be a positive value.
     * @param easing The easing curve applied to the animation. Defaults to [FastOutSlowInEasing].
     */
    @Immutable
    data class Enabled(
        val duration: Int = 800,
        val easing: Easing = FastOutSlowInEasing,
    ) : Animation {
        init {
            require(duration > 0) { "Animation duration must be positive" }
        }
    }

    /**
     * Represents a physics-based spring animation, producing a smooth, natural motion that is not
     * bound to a fixed duration.
     *
     * @param dampingRatio Controls bounciness. Lower values bounce more; defaults to a non-bouncy spring.
     * @param stiffness Controls the speed at which the spring settles. Defaults to a low stiffness for
     *   a gentle, smooth motion.
     */
    @Immutable
    data class Spring(
        val dampingRatio: Float = ComposeSpring.DampingRatioNoBouncy,
        val stiffness: Float = ComposeSpring.StiffnessLow,
    ) : Animation

    /**
     * A companion object that provides common animation presets.
     */
    companion object {
        /**
         * The default animation, a tween with a duration of 800 milliseconds.
         */
        val Default = Enabled()

        /**
         * A fast tween animation, with a duration of 400 milliseconds.
         */
        val Fast = Enabled(duration = 400)

        /**
         * A slow tween animation, with a duration of 1200 milliseconds.
         */
        val Slow = Enabled(duration = 1200)

        /**
         * A smooth, non-bouncy spring animation for natural-feeling transitions.
         */
        val Smooth = Spring()

        /**
         * A spring animation with a gentle bounce.
         */
        val Bouncy = Spring(dampingRatio = ComposeSpring.DampingRatioMediumBouncy)
    }
}
