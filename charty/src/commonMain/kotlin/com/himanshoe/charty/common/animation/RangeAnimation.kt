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
 * While [active], the returned min/max ease toward the targets, retargeting continuously as the
 * window keeps moving. Otherwise the targets are returned as-is (and the internal state snaps, so a
 * later activation starts from the truth). All hooks run on every composition, keeping the hook
 * order stable across mode switches.
 *
 * @param minValue The target minimum of the currently visible data.
 * @param maxValue The target maximum of the currently visible data.
 * @param animation Supplies the easing and duration. A disabled animation does **not** make the
 *   rescale snap: [Animation] governs a chart's entry reveal, whereas a rescale is a continuous
 *   response to the window moving, and an axis that teleports under a sliding window reads as a
 *   glitch. A disabled animation therefore falls back to [Animation.Fast] here.
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
    val rescale =
        if (animation.isAnimated) {
            animation
        } else {
            Animation.Fast
        }
    LaunchedEffect(minValue, maxValue, rescale, active) {
        if (active) {
            launch { animatedMin.animateTo(targetValue = minValue, animationSpec = rescale.toFloatSpec()) }
            launch { animatedMax.animateTo(targetValue = maxValue, animationSpec = rescale.toFloatSpec()) }
        } else {
            animatedMin.snapTo(minValue)
            animatedMax.snapTo(maxValue)
        }
    }
    return if (active) animatedMin.value to animatedMax.value else minValue to maxValue
}
