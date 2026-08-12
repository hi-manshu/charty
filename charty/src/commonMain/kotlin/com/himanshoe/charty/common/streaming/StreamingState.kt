package com.himanshoe.charty.common.streaming

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.himanshoe.charty.common.PlotBoundsSource
import com.himanshoe.charty.common.animation.isAnimated
import com.himanshoe.charty.common.animation.toFloatSpec
import com.himanshoe.charty.common.config.Animation
import kotlin.math.roundToInt

/**
 * Follow-or-browse state for a chart with a rolling `visibleWindow`.
 *
 * A streaming chart is normally **following**: the window stays pinned to the newest point and
 * advances as data arrives. Dragging the plot backwards **detaches** it, and from that moment the
 * window holds the points the reader is looking at — new data keeps accumulating off-screen instead
 * of yanking the view to the end. [pendingCount] counts what arrived while detached, which is what a
 * "jump to latest" affordance shows, and [jumpToLatest] animates back to the newest point and
 * resumes following.
 *
 * Create one with [rememberStreamingState] and hand it to a chart through
 * [com.himanshoe.charty.common.config.ChartInteractionConfig]. Without one, a chart configured with
 * `visibleWindow` simply always follows, which is the behaviour of a chart that has no scrollback.
 *
 * The scroll position is expressed in data indices — `0` shows the very first point at the plot's
 * leading edge, and [maxScroll] shows the newest — so it is independent of pixel size and survives
 * a resize.
 *
 * Scrollback and a crosshair cannot share a chart: both are drag gestures, and the crosshair wins.
 * A chart configured with a crosshair keeps its window following the newest data, and a "jump to
 * latest" control never appears because the window never detaches by dragging. Tap-to-tooltip is
 * unaffected either way.
 */
@Stable
class StreamingState internal constructor() : PlotBoundsSource {
    private var plotLeftState by mutableFloatStateOf(0f)
    private var plotWidthState by mutableFloatStateOf(0f)

    override val plotLeft: Float get() = plotLeftState
    override val plotWidth: Float get() = plotWidthState

    internal fun updatePlotBounds(
        left: Float,
        width: Float,
    ) {
        plotLeftState = left
        plotWidthState = width
    }

    internal val scroll: Animatable<Float, AnimationVector1D> = Animatable(0f)

    internal var crosshairOwnsDrag: Boolean = false

    private var followingState by mutableStateOf(true)
    private var pendingState by mutableIntStateOf(0)
    private var maxScrollState by mutableFloatStateOf(0f)

    /**
     * Whether the window is pinned to the newest point. `true` initially; dragging backwards clears
     * it and [jumpToLatest] restores it.
     */
    val isFollowing: Boolean get() = followingState

    /**
     * How many points have arrived since the window detached, or `0` while following. Use it to
     * label a "jump to latest" control.
     */
    val pendingCount: Int get() = pendingState

    /** The scroll position that shows the newest point, i.e. `dataSize - windowSize`. */
    val maxScroll: Float get() = maxScrollState

    /** The scroll position currently rendered, in data indices. */
    val currentScroll: Float get() = scroll.value

    /**
     * Detaches the window so it stops following new data. Called by the drag gesture; call it
     * directly to detach programmatically, for example when your own control takes over.
     */
    fun detach() {
        followingState = false
    }

    /**
     * Animates the window back to the newest point with [animation] and resumes following, clearing
     * [pendingCount]. A disabled [animation] jumps immediately.
     *
     * Resuming following is what makes the chart re-drive this animation itself, which matters
     * because the overlay that triggered the jump usually disappears the moment following resumes —
     * taking its coroutine scope, and this call, with it. The chart finishes the slide regardless.
     */
    suspend fun jumpToLatest(animation: Animation = Animation.Default) {
        followingState = true
        pendingState = 0
        if (animation.isAnimated) {
            scroll.animateTo(targetValue = maxScrollState, animationSpec = animation.toFloatSpec())
        } else {
            scroll.snapTo(maxScrollState)
        }
    }

    /**
     * Eases the window to the nearest whole data index, so a drag always comes to rest on aligned
     * slots instead of leaving the leading and trailing items sliced in half by the plot edges.
     *
     * Called when a drag ends. While following, the window is already heading for a whole index, so
     * this does nothing and leaves the follow animation undisturbed.
     */
    internal suspend fun settleToNearestIndex(animation: Animation) {
        if (followingState) {
            return
        }
        val nearest =
            scroll.value
                .roundToInt()
                .toFloat()
                .coerceIn(minimumValue = 0f, maximumValue = maxScrollState)
        if (nearest == scroll.value) {
            return
        }
        if (animation.isAnimated) {
            scroll.animateTo(targetValue = nearest, animationSpec = animation.toFloatSpec())
        } else {
            scroll.snapTo(nearest)
        }
    }

    /**
     * Eases the window to the newest point when following has resumed but the scroll has not caught
     * up yet, which happens when the caller's jump was cancelled with its overlay.
     */
    internal suspend fun settleToLatest(animation: Animation) {
        if (!followingState || scroll.value == maxScrollState) {
            return
        }
        if (animation.isAnimated) {
            scroll.animateTo(targetValue = maxScrollState, animationSpec = animation.toFloatSpec())
        } else {
            scroll.snapTo(maxScrollState)
        }
    }

    /**
     * Moves the window by [delta] data indices, detaching it, and clamps the result into
     * `0..maxScroll` so a drag can never scroll past either end of the data.
     */
    internal suspend fun scrollBy(delta: Float) {
        detach()
        scroll.snapTo((scroll.value + delta).coerceIn(minimumValue = 0f, maximumValue = maxScrollState))
    }

    /**
     * Records the newest reachable scroll position and, when the data grew, either follows it or
     * counts the arrivals as pending.
     */
    internal suspend fun onDataChanged(
        newMaxScroll: Float,
        appended: Int,
        animation: Animation,
    ) {
        val previousMax = maxScrollState
        maxScrollState = newMaxScroll
        if (followingState) {
            if (animation.isAnimated) {
                scroll.animateTo(targetValue = newMaxScroll, animationSpec = animation.toFloatSpec())
            } else {
                scroll.snapTo(newMaxScroll)
            }
        } else if (appended > 0 && newMaxScroll > previousMax) {
            pendingState += appended
            scroll.snapTo(scroll.value.coerceIn(minimumValue = 0f, maximumValue = newMaxScroll))
        }
    }
}

/**
 * Creates and remembers a [StreamingState] for a chart with a rolling `visibleWindow`, so the reader
 * can scroll back through history while new data keeps accumulating.
 *
 * ```kotlin
 * val streaming = rememberStreamingState()
 * LineChart(
 *     data = { points },
 *     lineConfig = LineChartConfig(visibleWindow = 20),
 *     interactionConfig = ChartInteractionConfig(streamingState = streaming),
 * )
 * ```
 */
@Composable
fun rememberStreamingState(): StreamingState = remember { StreamingState() }
