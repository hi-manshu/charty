package com.himanshoe.charty.common

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.himanshoe.charty.common.animation.isAnimated
import com.himanshoe.charty.common.animation.toFloatSpec
import com.himanshoe.charty.common.config.Animation
import kotlin.math.ceil
import kotlin.math.floor

private const val SLOT_CENTER = 0.5f
private const val EDGE_MARGIN_SLOTS = 1

/**
 * Resolution-independent description of a rolling window's scroll along the category axis. Positions
 * are expressed as fractions of the plot's category extent (0 at the leading edge, 1 at the trailing
 * edge), so the very same layout drives the plotted series ([ChartContext]) and the axis labels
 * ([com.himanshoe.charty.common.axis.DrawAxisAndLabels]) identically — the two can never drift.
 *
 * A window slice ([rememberVisibleWindow] returns it alongside this layout) holds
 * `windowData[0]` at global index [from]. Animating [scroll] slides the window: at rest it equals
 * `size - windowSize`, and each appended point advances it by one, sweeping the older points out the
 * leading edge while the new one eases in at the trailing edge.
 *
 * @property from The global index of the first item in the drawn window slice (`windowData[0]`).
 * @property scroll The fractional global index sitting at the plot's leading edge.
 * @property windowSize The number of category slots visible across the plot (the configured
 *   `visibleWindow`, clamped to the data size while the window is still filling).
 */
@Immutable
@ConsistentCopyVisibility
data class StreamingLayout internal constructor(
    val from: Int,
    val scroll: Float,
    val windowSize: Int,
) {
    /** Fraction of the plot's category extent at the center of the window item at [localIndex]. */
    fun centerFraction(localIndex: Int): Float = (from + localIndex - scroll + SLOT_CENTER) / windowSize

    /** Fraction of the plot's category extent at the leading edge of the slot at [localIndex]. */
    fun startFraction(localIndex: Int): Float = (from + localIndex - scroll) / windowSize

    /** Width of one category slot as a fraction of the plot's category extent. */
    val slotFraction: Float get() = 1f / windowSize
}

/**
 * Drives the rolling-window scroll animation for streaming charts and returns the data slice to draw
 * together with the [StreamingLayout] that positions it.
 *
 * The scroll target is `size - windowSize` (the offset that reveals the last [windowSize] points). It
 * is held in an [Animatable]; when new points are appended the target advances and — if [animation]
 * is animated — the scroll eases to it, producing the slide. Retargeting is continuous, so rapid
 * bursts of appends catch up smoothly instead of restarting. When [animation] is disabled the scroll
 * snaps, matching the plain "show last N" behaviour.
 *
 * Only `windowSize + 2` points are ever materialised for drawing regardless of how large the full
 * series grows: the slice spans the visible slots plus one margin slot on each edge so the outgoing
 * and incoming points exist to be clipped.
 *
 * @param fullDataList The complete, append-accumulating series (the source of truth).
 * @param windowSize The configured rolling window size; clamped to at least 1 and to the data size.
 * @param animation Supplies the slide's duration/easing and whether it animates at all.
 * @return The window slice to draw and its [StreamingLayout].
 */
@Composable
internal fun <T> rememberVisibleWindowSlice(
    fullDataList: List<T>,
    windowSize: Int,
    animation: Animation,
): Pair<List<T>, StreamingLayout> {
    val size = fullDataList.size
    val clampedWindow = windowSize.coerceIn(1, maxOf(1, size))
    val target = (size - clampedWindow).coerceAtLeast(0).toFloat()
    val scrollAnimatable = remember { Animatable(target) }

    LaunchedEffect(target, animation) {
        if (animation.isAnimated) {
            scrollAnimatable.animateTo(targetValue = target, animationSpec = animation.toFloatSpec())
        } else {
            scrollAnimatable.snapTo(target)
        }
    }

    val scroll = scrollAnimatable.value
    val from = (floor(scroll).toInt() - EDGE_MARGIN_SLOTS).coerceAtLeast(0)
    val to = (ceil(scroll).toInt() + clampedWindow + EDGE_MARGIN_SLOTS).coerceIn(from, size)
    val windowData = remember(fullDataList, from, to) { fullDataList.subList(from, to) }
    return windowData to StreamingLayout(from = from, scroll = scroll, windowSize = clampedWindow)
}
