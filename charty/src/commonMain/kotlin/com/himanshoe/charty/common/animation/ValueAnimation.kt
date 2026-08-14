package com.himanshoe.charty.common.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMapIndexed
import com.himanshoe.charty.common.config.Animation

/**
 * Linearly interpolates each element of [from] toward [to] by [fraction] (`0f` yields [from], `1f`
 * yields [to]).
 *
 * ## Appended values
 * A series that grew — the streaming case, where a point arrives every tick — keeps its shared
 * prefix and tweens each **new** element out of the last value they had in common. That is what
 * makes a fresh point extend the line from where it left off rather than appear whole: at
 * `fraction = 0f` the new point sits exactly on its predecessor, so the segment has no length, and
 * it grows to its real value as the fraction runs. Anything reading the newest point — a marker
 * pinned to it, its label, the crosshair — follows that motion instead of jumping with it.
 *
 * When the lists differ in any other way there is no sensible correspondence between them, so [to]
 * is returned unchanged and the chart snaps to its new shape. [fraction] is coerced into `0f..1f`.
 *
 * @param from The starting values (typically the previously displayed data).
 * @param to The target values (the new data).
 * @param fraction How far to interpolate from [from] to [to].
 * @return A new list of interpolated values, or [to] when the lists cannot be matched up.
 */
fun lerpValues(
    from: List<Float>,
    to: List<Float>,
    fraction: Float,
): List<Float> {
    val clamped = fraction.coerceIn(0f, 1f)
    val startValues = matchedStartValues(from = from, to = to) ?: return to
    return List(to.size) { index ->
        val start = startValues[index]
        start + (to[index] - start) * clamped
    }
}

/**
 * The value each element of [to] should tween out of, or `null` when the two lists cannot be matched
 * up at all.
 *
 * Same length is the ordinary case and pairs off by index. A list that grew by appending — which is
 * what streaming does on every tick — pairs its shared prefix off with itself and starts each new
 * element from the last value the two lists had in common, so a fresh point begins life on top of
 * its predecessor and the segment between them grows out of nothing.
 */
private fun matchedStartValues(
    from: List<Float>,
    to: List<Float>,
): List<Float>? {
    val appended = from.isNotEmpty() && to.size > from.size && to.subList(0, from.size) == from
    return when {
        from.size == to.size -> from
        appended -> {
            val anchor = from.last()
            List(to.size) { index ->
                if (index < from.size) {
                    to[index]
                } else {
                    anchor
                }
            }
        }

        else -> null
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

/**
 * Returns [dataList] with each item's value tweened toward its target whenever the data changes, so a
 * chart glides to its new shape instead of snapping to it.
 *
 * Five charts wrote this out for themselves — bar, point, line, bubble and the bar family's shared
 * helper — in bodies that differed only in the type of the item and which of its fields carried the
 * value. Taking those two as functions leaves one implementation. [valueOf] reads the field being
 * animated and [withValue] returns a copy carrying the tweened one; everything else is identical
 * whatever the chart.
 *
 * @param dataList The items to display.
 * @param animation The animation driving the transition.
 * @param enabled Opt-in switch mirroring each chart config's `animateValueChanges`.
 * @param valueOf Reads the animated field out of an item.
 * @param withValue Returns a copy of an item carrying the tweened value.
 * @return The items to draw this frame.
 */
@Composable
fun <T> rememberAnimatedData(
    dataList: List<T>,
    animation: Animation,
    enabled: Boolean,
    valueOf: (T) -> Float,
    withValue: (T, Float) -> T,
): List<T> {
    val animatedValues =
        rememberAnimatedValues(
            targetValues = dataList.fastMap(valueOf),
            animation = animation,
            enabled = enabled,
        )
    return remember(dataList, animatedValues) {
        dataList.fastMapIndexed { index, item -> withValue(item, animatedValues[index]) }
    }
}
