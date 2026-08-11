package com.himanshoe.charty.common.viewport

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val MIN_VISIBLE_FRACTION = 0.1f
private const val FLING_FRICTION = 1.5f
private const val EDGE_EPSILON = 0.001f
private const val SCROLL_ANIMATION_MILLIS = 600

/**
 * Tracks the currently visible data window for a zoomable/pannable chart.
 *
 * The viewport is expressed in normalised fractions `[0, 1]` over the full dataset:
 * `startFraction = 0f` and `endFraction = 1f` shows everything. Pinch-to-zoom narrows
 * the window around a focus point; drag pans it; releasing with velocity launches an
 * inertial fling that decelerates smoothly.
 *
 * The chart reads [visibleIndices] each frame to determine which data subset to render.
 *
 * Use [rememberViewPortState] to create an instance bound to the composition lifecycle.
 */
class ViewPortState {
    var startFraction by mutableFloatStateOf(0f)
        private set
    var endFraction by mutableFloatStateOf(1f)
        private set

    /** Pixel x-coordinate of the chart's left edge — set by ChartScaffold each draw. */
    internal var chartLeft by mutableFloatStateOf(0f)

    /** Pixel width of the chart drawing area — set by ChartScaffold each draw. */
    internal var chartWidth by mutableFloatStateOf(0f)

    /** Total number of data items — set by the chart before gesture handling. */
    internal var dataSize by mutableIntStateOf(0)

    private val decay = exponentialDecay<Float>(frictionMultiplier = FLING_FRICTION)
    private var flingJob: Job? = null
    private var coroutineScope: CoroutineScope? = null

    /**
     * The fraction of the dataset currently shown, equal to `endFraction - startFraction`.
     * A value of `1f` means the full dataset is visible; `0.5f` means half is visible.
     */
    val visibleFraction: Float get() = endFraction - startFraction

    /**
     * Returns the range of data indices currently visible in the viewport.
     *
     * The result is always non-empty and clamped to `[0, totalItems)`.
     *
     * @param totalItems The total number of items in the full dataset.
     * @return An [IntRange] of indices whose data should be rendered.
     */
    fun visibleIndices(totalItems: Int): IntRange {
        val start = (startFraction * totalItems).toInt().coerceAtLeast(0)
        val end = (endFraction * totalItems).toInt().coerceAtMost(totalItems)
        return start until end.coerceAtLeast(start + 1).coerceAtMost(totalItems)
    }

    /**
     * Applies a zoom gesture centred on [focusFraction].
     *
     * @param focusFraction The focal point of the zoom expressed as a fraction of the chart
     *   width, where `0f` is the left edge and `1f` is the right edge.
     * @param scaleFactor Multiplicative zoom factor. Values greater than `1f` zoom in;
     *   values less than `1f` zoom out. The minimum visible window is capped at 10 % of
     *   the dataset to prevent over-zooming.
     */
    internal fun zoom(
        focusFraction: Float,
        scaleFactor: Float,
    ) {
        val newWidth = (visibleFraction / scaleFactor).coerceIn(MIN_VISIBLE_FRACTION, 1f)
        val focusAbsolute = startFraction + focusFraction * visibleFraction
        val newStart = (focusAbsolute - focusFraction * newWidth).coerceIn(0f, 1f - newWidth)
        startFraction = newStart
        endFraction = newStart + newWidth
    }

    /**
     * Applies a pan gesture by shifting the viewport window.
     *
     * @param deltaFraction The amount to shift, expressed as a fraction of the full dataset
     *   width. Positive values pan right (towards later data); negative values pan left.
     *   The window is clamped so it never extends beyond the dataset boundaries.
     */
    internal fun pan(deltaFraction: Float) {
        val newStart = (startFraction + deltaFraction).coerceIn(0f, 1f - visibleFraction)
        startFraction = newStart
        endFraction = newStart + visibleFraction
    }

    /**
     * Launches an inertial fling that continues panning after a drag gesture ends.
     *
     * The fling decelerates using exponential decay with a friction multiplier of
     * [FLING_FRICTION]. Calling this while a fling is already running replaces it.
     *
     * @param initialVelocityX Pixel-per-second velocity along the x-axis at gesture release.
     *   Negative values fling towards later data; positive values fling towards earlier data.
     */
    internal fun fling(initialVelocityX: Float) {
        flingJob?.cancel()
        val scope = coroutineScope ?: return
        val cw = chartWidth.coerceAtLeast(1f)
        flingJob =
            scope.launch {
                var prevValue = 0f
                Animatable(0f).animateDecay(
                    initialVelocity = initialVelocityX,
                    animationSpec = decay,
                ) {
                    val delta = value - prevValue
                    prevValue = value
                    pan(delta / cw * visibleFraction)
                }
            }
    }

    /**
     * Cancels any in-progress fling animation immediately. Call this at the start of a new
     * gesture so the fling does not fight the user's input.
     */
    internal fun cancelFling() {
        flingJob?.cancel()
        flingJob = null
    }

    /**
     * `true` when the window's left edge is at the start of the dataset (there is no earlier data
     * scrolled off to the left).
     */
    val isAtStart: Boolean get() = startFraction <= EDGE_EPSILON

    /**
     * `true` when the window's right edge is at the end of the dataset (there is no later data
     * scrolled off to the right).
     */
    val isAtEnd: Boolean get() = endFraction >= 1f - EDGE_EPSILON

    /**
     * Moves the window to the end of the dataset, keeping the current [visibleFraction], so the most
     * recent data becomes visible. No-op effect when already showing the full dataset.
     */
    fun scrollToEnd() {
        cancelFling()
        val width = visibleFraction
        startFraction = (1f - width).coerceAtLeast(0f)
        endFraction = 1f
    }

    /**
     * Like [scrollToEnd] but glides the window to the end with a smooth tween instead of jumping.
     * Falls back to an instant [scrollToEnd] if no coroutine scope is bound yet.
     */
    fun animateScrollToEnd() {
        cancelFling()
        val width = visibleFraction
        val targetStart = (1f - width).coerceAtLeast(0f)
        val scope = coroutineScope
        if (scope == null) {
            scrollToEnd()
            return
        }
        flingJob =
            scope.launch {
                Animatable(startFraction).animateTo(
                    targetValue = targetStart,
                    animationSpec = tween(durationMillis = SCROLL_ANIMATION_MILLIS),
                ) {
                    startFraction = value
                    endFraction = (value + width).coerceAtMost(1f)
                }
            }
    }

    /**
     * Resets the viewport to show the full dataset by setting [startFraction] to `0f`
     * and [endFraction] to `1f`.
     */
    fun reset() {
        cancelFling()
        startFraction = 0f
        endFraction = 1f
    }

    internal fun bindCoroutineScope(scope: CoroutineScope) {
        coroutineScope = scope
    }
}

/**
 * Creates and remembers a [ViewPortState] instance bound to the current composition.
 *
 * Pass the returned state to a chart's `viewPortState` parameter inside
 * [com.himanshoe.charty.common.config.ChartInteractionConfig] to enable pinch-to-zoom,
 * drag-to-pan, and inertial fling. Call [ViewPortState.reset] to restore the full-data
 * view at any time.
 */
@Composable
fun rememberViewPortState(): ViewPortState {
    val scope = rememberCoroutineScope()
    return remember { ViewPortState() }.also { it.bindCoroutineScope(scope) }
}
