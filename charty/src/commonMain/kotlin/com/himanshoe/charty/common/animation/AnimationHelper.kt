package com.himanshoe.charty.common.animation

import androidx.compose.animation.core.Animatable
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
 * Creates and manages the animation progress for a chart.
 *
 * This composable function automatically handles the animation lifecycle based on the provided [Animation] configuration.
 * It returns an [Animatable] that tracks the animation progress, which can be used to drive animations in the chart's drawing code.
 *
 * @param animation The configuration for the animation, which can be either [Animation.Enabled] or [Animation.Disabled].
 * @param initialValue The starting value for the animation. If `null`, it defaults to `0f` for enabled animations and `targetValue` for disabled animations.
 * @param targetValue The target value for the animation, which defaults to `1f`.
 * @return An [Animatable] that tracks the animation progress from `initialValue` to `targetValue`.
 *
 * @sample
 * val animationProgress = rememberChartAnimation(config.animation)
 * // Use animationProgress.value in drawing code to animate chart elements.
 */
@Composable
fun rememberChartAnimation(
    animation: Animation,
    initialValue: Float? = null,
    targetValue: Float = 1f,
): Animatable<Float, *> {
    val animationProgress = remember(animation) {
        val initial = initialValue ?: if (animation is Animation.Enabled) 0f else targetValue
        Animatable(initial)
    }

    LaunchedEffect(animation) {
        if (animation is Animation.Enabled) {
            animationProgress.animateTo(
                targetValue = targetValue,
                animationSpec = tween(durationMillis = animation.duration),
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
 * @param animation The configuration for the animation, which can be either [Animation.Enabled] or [Animation.Disabled].
 * @param initialValue The starting value for the animation. If `null`, it defaults to `0f` for enabled animations and `1f` for disabled animations.
 * @return An [Animatable] instance that can be used to control the animation state.
 */
@Composable
fun rememberChartAnimationState(
    animation: Animation,
    initialValue: Float? = null,
): Animatable<Float, *> {
    return remember(animation) {
        val initial = initialValue ?: if (animation is Animation.Enabled) 0f else 1f
        Animatable(initial)
    }
}

