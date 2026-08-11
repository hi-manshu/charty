package com.himanshoe.charty.common.gesture

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Holds the current position and value label for the crosshair overlay.
 *
 * @property x Pixel x-coordinate of the snapped data point on the canvas.
 * @property y Pixel y-coordinate of the snapped data point on the canvas.
 * @property label Pre-formatted value string displayed in the crosshair label bubble.
 */
data class CrosshairState(
    val x: Float,
    val y: Float,
    val label: String,
)

/**
 * Manages the crosshair's visible state across recompositions.
 *
 * Holds the current [CrosshairState] as observable Compose state so that the canvas
 * redraws automatically whenever the crosshair moves. Call [update] on each drag event
 * and [dismiss] when the finger lifts. Use [rememberCrosshairManager] to create an instance.
 *
 * @param T The chart's data/point type, exposed via [selectedItem] so callers can render a
 *   custom composable crosshair label.
 */
class CrosshairManager<T> {
    /**
     * The current crosshair position and label, or `null` when the crosshair is not visible.
     */
    var state: CrosshairState? by mutableStateOf(null)
        private set

    /**
     * The data item the crosshair is currently snapped to, or `null` when not visible.
     * Used by custom composable crosshair overlays.
     */
    var selectedItem: T? by mutableStateOf(null)
        private set

    /**
     * Updates the crosshair to [newState]. Pass `null` to hide the crosshair.
     *
     * @param newState The new crosshair position and label, or `null` to dismiss.
     * @param item The data item the crosshair snapped to, or `null` to clear it.
     */
    fun update(
        newState: CrosshairState?,
        item: T? = null,
    ) {
        state = newState
        selectedItem = item
    }

    /**
     * Hides the crosshair by clearing [state] and [selectedItem].
     */
    fun dismiss() {
        state = null
        selectedItem = null
    }

    /**
     * Returns `true` when the crosshair is currently visible.
     */
    fun isVisible(): Boolean = state != null
}

/**
 * Creates and remembers a [CrosshairManager] instance that survives recomposition.
 */
@Composable
fun <T> rememberCrosshairManager(): CrosshairManager<T> = remember { CrosshairManager() }

/**
 * Bundles the standard crosshair setup used by every crosshair-capable chart: a nullable
 * [CrosshairManager] (present only when [enabled]) and the position-smoothed [CrosshairState]
 * derived from it via [rememberAnimatedCrosshairState].
 *
 * Charts previously repeated these two lines individually, which made it easy to accidentally draw
 * the raw (un-animated) manager state. Prefer this single call:
 * ```kotlin
 * val (crosshairManager, animatedCrosshairState) =
 *     rememberChartCrosshair<LineData>(lineConfig.crosshairConfig != null)
 * ```
 *
 * @param enabled Whether the crosshair is configured for this chart (typically
 *   `config.crosshairConfig != null`). When `false`, both returned values are `null`.
 * @return A [Pair] of `(manager, animatedState)`.
 */
@Composable
internal fun <T> rememberChartCrosshair(enabled: Boolean): Pair<CrosshairManager<T>?, CrosshairState?> {
    val manager = if (enabled) rememberCrosshairManager<T>() else null
    val animatedState = rememberAnimatedCrosshairState(manager?.state)
    return manager to animatedState
}

/**
 * Returns a [CrosshairState] whose `x`/`y` smoothly animate toward [state]'s position, so the
 * crosshair glides between data points during a drag instead of snapping instantly.
 *
 * The [label] is taken directly from [state] (no animation). Returns `null` when [state] is `null`,
 * which also resets the internal animations so the next appearance starts cleanly at its target.
 *
 * @param state The target crosshair state to follow, or `null` when the crosshair is hidden.
 * @param animationSpec The spring used to drive the positional motion. Defaults to a smooth,
 *   non-bouncy spring tuned for responsive snapping.
 */
@Composable
fun rememberAnimatedCrosshairState(
    state: CrosshairState?,
    animationSpec: AnimationSpec<Float> = spring(stiffness = Spring.StiffnessMediumLow),
): CrosshairState? {
    state ?: return null
    val animatedX by animateFloatAsState(targetValue = state.x, animationSpec = animationSpec)
    val animatedY by animateFloatAsState(targetValue = state.y, animationSpec = animationSpec)
    return state.copy(x = animatedX, y = animatedY)
}
