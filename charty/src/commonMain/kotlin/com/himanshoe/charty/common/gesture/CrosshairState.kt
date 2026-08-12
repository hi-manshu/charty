package com.himanshoe.charty.common.gesture

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.currentCompositeKeyHashCode
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.himanshoe.charty.common.PlotBoundsSource
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.streaming.StreamingState
import com.himanshoe.charty.common.viewport.ViewPortState

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
    fun dismiss() = update(newState = null, item = null)

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
 * A position-smoothed crosshair whose animated `x`/`y` are held as [State] rather than read eagerly.
 *
 * Call [resolve] from inside a `DrawScope` block to read the current animated position; because the
 * read then happens in the draw phase, a crosshair drag invalidates draw only and does not recompose
 * the host chart every frame.
 */
@Immutable
internal class AnimatedCrosshair(
    private val animatedX: State<Float>,
    private val animatedY: State<Float>,
    private val label: String,
) {
    fun resolve(): CrosshairState = CrosshairState(x = animatedX.value, y = animatedY.value, label = label)
}

/**
 * Bundles the standard crosshair setup used by every crosshair-capable chart: a nullable
 * [CrosshairManager] (present only when [enabled]) and the position-smoothed [AnimatedCrosshair]
 * derived from it via [rememberAnimatedCrosshairState].
 *
 * ```kotlin
 * val (crosshairManager, animatedCrosshairState) =
 *     rememberChartCrosshair<LineData>(lineConfig.crosshairConfig != null)
 * ```
 *
 * When the chart is inside a [CrosshairSyncScope] and [viewPortState] is supplied, the manager is
 * also enrolled in the synced group: its own gesture is published to the group and, while another
 * chart owns the gesture, it mirrors the shared position.
 *
 * @param enabled Whether the crosshair is configured for this chart (typically
 *   `config.crosshairConfig != null`). When `false`, both returned values are `null`.
 * @param viewPortState The chart's viewport state, used as the source of plot pixel geometry when
 *   syncing across charts.
 * @param streamingState The chart's streaming state, used as the geometry source when the chart has
 *   a rolling window rather than a viewport. Syncing needs one of the two; with neither, this chart
 *   keeps its crosshair to itself.
 * @return A [Pair] of `(manager, animatedCrosshair)`.
 */
@Composable
internal fun <T> rememberChartCrosshair(
    enabled: Boolean,
    viewPortState: ViewPortState? = null,
    streamingState: StreamingState? = null,
): Pair<CrosshairManager<T>?, AnimatedCrosshair?> {
    val manager =
        if (enabled) {
            rememberCrosshairManager<T>()
        } else {
            null
        }
    val sync = LocalCrosshairSync.current
    val ownerId = currentCompositeKeyHashCode.toString()
    val bounds: PlotBoundsSource? = viewPortState ?: streamingState
    if (manager != null && sync != null && bounds != null) {
        CrosshairSyncParticipantEffects(
            sync = sync,
            ownerId = ownerId,
            manager = manager,
            bounds = bounds,
        )
    }
    val animated = rememberAnimatedCrosshairState(manager?.state)
    return manager to animated
}

/**
 * Returns an [AnimatedCrosshair] whose `x`/`y` springs toward [state]'s position, so the crosshair
 * glides between data points during a drag instead of snapping instantly. The animated values are
 * held as [State] and read lazily via [AnimatedCrosshair.resolve]. Returns `null` when [state] is
 * `null`, which also resets the internal animations so the next appearance starts at its target.
 *
 * @param state The target crosshair state to follow, or `null` when the crosshair is hidden.
 * @param animationSpec The spring used to drive the positional motion.
 */
@Composable
internal fun rememberAnimatedCrosshairState(
    state: CrosshairState?,
    animationSpec: AnimationSpec<Float> = spring(stiffness = Spring.StiffnessMediumLow),
): AnimatedCrosshair? {
    state ?: return null
    val animatedX = animateFloatAsState(targetValue = state.x, animationSpec = animationSpec)
    val animatedY = animateFloatAsState(targetValue = state.y, animationSpec = animationSpec)
    return AnimatedCrosshair(animatedX = animatedX, animatedY = animatedY, label = state.label)
}

/**
 * Convenience over [rememberChartCrosshair] that takes the geometry sources straight from
 * [interactionConfig], so a chart enrols in a [CrosshairSyncScope] whether it pans a viewport or
 * rolls a streaming window.
 *
 * @param enabled Whether the crosshair is configured for this chart.
 * @param interactionConfig The chart's interaction config, supplying the viewport or streaming state.
 * @return A [Pair] of `(manager, animatedCrosshair)`.
 */
@Composable
internal fun <T> rememberChartCrosshair(
    enabled: Boolean,
    interactionConfig: ChartInteractionConfig,
): Pair<CrosshairManager<T>?, AnimatedCrosshair?> =
    rememberChartCrosshair(
        enabled = enabled,
        viewPortState = interactionConfig.viewPortState,
        streamingState = interactionConfig.streamingState,
    )
