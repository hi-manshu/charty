package com.himanshoe.charty.common.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.himanshoe.charty.common.config.Animation
import kotlinx.coroutines.launch

/**
 * Smoothly retargets a chart's value range while a rolling window slides, so a new extreme entering
 * (or an old one leaving) the window glides the axis — and every plotted point with it — to the new
 * scale instead of jumping.
 *
 * While [active] and [animation] is animated, the returned min/max ease toward the targets with
 * [animation]'s spec, retargeting continuously as the window keeps moving. Otherwise the targets are
 * returned as-is (and the internal state snaps, so a later activation starts from the truth). All
 * hooks run on every composition, keeping the hook order stable across mode switches.
 *
 * @param minValue The target minimum of the currently visible data.
 * @param maxValue The target maximum of the currently visible data.
 * @param animation Supplies the easing/duration; a disabled animation snaps.
 * @param active Whether smoothing applies — pass the chart's streaming state.
 * @return The min/max to lay out with this frame.
 */
@Composable
internal fun rememberAnimatedRange(
    minValue: Float,
    maxValue: Float,
    animation: Animation,
    active: Boolean,
): Pair<Float, Float> {
    val animatedMin = remember { Animatable(minValue) }
    val animatedMax = remember { Animatable(maxValue) }
    LaunchedEffect(minValue, maxValue, animation, active) {
        if (active && animation.isAnimated) {
            launch { animatedMin.animateTo(targetValue = minValue, animationSpec = animation.toFloatSpec()) }
            launch { animatedMax.animateTo(targetValue = maxValue, animationSpec = animation.toFloatSpec()) }
        } else {
            animatedMin.snapTo(minValue)
            animatedMax.snapTo(maxValue)
        }
    }
    return if (active) animatedMin.value to animatedMax.value else minValue to maxValue
}
