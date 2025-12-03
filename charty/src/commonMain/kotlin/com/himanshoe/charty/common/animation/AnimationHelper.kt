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
 * Creates and manages animation progress for charts.
 * Automatically handles the animation lifecycle based on the Animation configuration.
 *
 * @param animation Animation configuration (Disabled or Enabled with duration)
 * @param initialValue Starting value. If null, uses 0f for enabled animations and targetValue for disabled
 * @param targetValue Target value for the animation (default 1f)
 * @return Animatable that tracks animation progress from initialValue to targetValue
 *
 * Example usage:
 * ```kotlin
 * val animationProgress = rememberChartAnimation(config.animation)
 * // Use animationProgress.value in drawing code
 * ```
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
 * Creates an animation progress Animatable without automatic triggering.
 * Useful when you need more control over when the animation starts.
 *
 * @param animation Animation configuration
 * @param initialValue Starting value. If null, uses 0f for enabled animations and 1f for disabled
 * @return Animatable instance
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

