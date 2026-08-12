package com.himanshoe.charty.common.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.himanshoe.charty.common.config.Animation

/**
 * Linearly interpolates each element of [from] toward [to] by [fraction] (`0f` yields [from], `1f`
 * yields [to]).
 *
 * When the two lists differ in size there is no sensible per-index correspondence, so the [to] list
 * is returned unchanged — a data set that added or removed entries snaps to its new shape instead of
 * tweening. [fraction] is coerced into `0f..1f`.
 *
 * @param from The starting values (typically the previously displayed data).
 * @param to The target values (the new data).
 * @param fraction How far to interpolate from [from] to [to].
 * @return A new list of interpolated values, or [to] when the sizes differ.
 */
fun lerpValues(
    from: List<Float>,
    to: List<Float>,
    fraction: Float,
): List<Float> {
    if (from.size != to.size) {
        return to
    }
    val clamped = fraction.coerceIn(0f, 1f)
    return List(to.size) { index ->
        val start = from[index]
        start + (to[index] - start) * clamped
    }
}

/**
 * Tweens a chart's values whenever the data changes, so bars or points glide from their previous
 * heights to the new ones instead of jumping.
 *
 * On the first composition, and whenever [enabled] is `false` or [animation] is
 * [Animation.Disabled], the [targetValues] are returned as-is. When the data later changes, a `0f..1f`
 * progress runs with [animation]'s spec and the returned list is [lerpValues] between the previously
 * displayed values and [targetValues]. A change in list size snaps to the new values (see
 * [lerpValues]).
 *
 * Every hook runs on every composition — the pass-through case is a branch in the returned value
 * rather than an early return — so a chart that toggles [enabled] or swaps its [animation] keeps a
 * stable composition structure and does not discard the values it was mid-way through tweening.
 *
 * @param targetValues The current data values to display.
 * @param animation The animation configuration driving the transition.
 * @param enabled Opt-in switch; when `false` the [targetValues] pass through untouched.
 * @return The values to draw this frame — interpolated while a change is animating, otherwise
 *   [targetValues].
 */
@Composable
fun rememberAnimatedValues(
    targetValues: List<Float>,
    animation: Animation,
    enabled: Boolean,
): List<Float> {
    val tweening = enabled && animation.isAnimated
    val previous = remember { PreviousValues(targetValues) }
    val progress = remember { Animatable(1f) }

    LaunchedEffect(targetValues, animation, tweening) {
        if (!tweening) {
            previous.values = targetValues
            progress.snapTo(1f)
            return@LaunchedEffect
        }
        if (previous.values == targetValues) {
            return@LaunchedEffect
        }
        val startValues = previous.values
        progress.snapTo(0f)
        try {
            progress.animateTo(targetValue = 1f, animationSpec = animation.toFloatSpec())
        } finally {
            previous.values =
                if (progress.value == 1f) {
                    targetValues
                } else {
                    lerpValues(from = startValues, to = targetValues, fraction = progress.value)
                }
        }
    }

    return if (tweening) {
        lerpValues(from = previous.values, to = targetValues, fraction = progress.value)
    } else {
        targetValues
    }
}

/** Non-observable holder for the previously displayed values; recomposition is driven by the progress [Animatable]. */
private class PreviousValues(
    var values: List<Float>,
)
