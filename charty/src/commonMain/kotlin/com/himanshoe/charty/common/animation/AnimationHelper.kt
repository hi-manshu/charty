package com.himanshoe.charty.common.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.himanshoe.charty.common.config.Animation

/**
 * Common animation utilities for chart animations.
 * Provides a unified way to handle chart animation state across all chart types.
 */

/**
 * Whether this [Animation] configuration produces a transition. `false` only for [Animation.Disabled].
 */
val Animation.isAnimated: Boolean
    get() = this !is Animation.Disabled

/**
 * Resolves this [Animation] configuration into a Compose [AnimationSpec] for `Float` values.
 *
 * - [Animation.Enabled] maps to a [tween] using its duration and easing.
 * - [Animation.Spring] maps to a physics-based [spring].
 * - [Animation.Disabled] maps to [snap] (instant); callers should guard with [isAnimated] first.
 */
fun Animation.toFloatSpec(): AnimationSpec<Float> =
    when (this) {
        is Animation.Enabled -> tween(durationMillis = duration, easing = easing)
        is Animation.Spring -> spring(dampingRatio = dampingRatio, stiffness = stiffness)
        is Animation.Disabled -> snap()
    }

/**
 * Creates and manages the animation progress for a chart.
 *
 * This composable function automatically handles the animation lifecycle based on the provided [Animation] configuration.
 * It returns an [Animatable] that tracks the animation progress, which can be used to drive animations in the chart's drawing code.
 *
 * @param animation The configuration for the animation ([Animation.Enabled], [Animation.Spring], or [Animation.Disabled]).
 * @param initialValue The starting value for the animation. If `null`, it defaults to `0f` for animated configs and `targetValue` when disabled.
 * @param targetValue The target value for the animation, which defaults to `1f`.
 * @return An [Animatable] that tracks the animation progress from `initialValue` to `targetValue`.
 *
 * val animationProgress = rememberChartAnimation(config.animation)
 * // Use animationProgress.value in drawing code to animate chart elements.
 */
@Composable
fun rememberChartAnimation(
    animation: Animation,
    initialValue: Float? = null,
    targetValue: Float = 1f,
): Animatable<Float, *> {
    val animationProgress =
        remember(animation) {
            val initial = initialValue ?: if (animation.isAnimated) 0f else targetValue
            Animatable(initial)
        }

    LaunchedEffect(animation) {
        if (animation.isAnimated) {
            animationProgress.animateTo(
                targetValue = targetValue,
                animationSpec = animation.toFloatSpec(),
            )
        }
    }

    return animationProgress
}

/**
 * Creates an [Animatable] for tracking animation progress without automatically starting the animation.
 *
 * This function is useful when you need more control over when the animation begins.
 * It provides an [Animatable] instance that can be manually triggered.
 *
 * @param animation The configuration for the animation ([Animation.Enabled], [Animation.Spring], or [Animation.Disabled]).
 * @param initialValue The starting value for the animation. If `null`, it defaults to `0f` for animated configs and `1f` when disabled.
 * @return An [Animatable] instance that can be used to control the animation state.
 */
@Composable
fun rememberChartAnimationState(
    animation: Animation,
    initialValue: Float? = null,
): Animatable<Float, *> =
    remember(animation) {
        val initial = initialValue ?: if (animation.isAnimated) 0f else 1f
        Animatable(initial)
    }
