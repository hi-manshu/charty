package com.himanshoe.charty.common.gesture

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import com.himanshoe.charty.common.viewport.ViewPortState

private const val DRIVEN_CROSSHAIR_Y = 0f
private const val DRIVEN_CROSSHAIR_LABEL = ""

/**
 * Shared crosshair coordination for a group of stacked charts.
 *
 * The state holds the crosshair position as a normalised fraction (`0f` = plot left edge,
 * `1f` = plot right edge) together with the id of the participant whose gesture produced it. A
 * chart publishes its own crosshair position via [publish] while its user drags, and every other
 * participant reads the shared fraction via [fractionFor] to mirror the guide line at the same
 * horizontal position. Ownership is last-writer-wins: the moment another chart publishes, it takes
 * over and the previous owner becomes an observer.
 *
 * Create one instance per synced group with [rememberCrosshairSync] and enrol each chart with
 * [rememberParticipant].
 */
@Stable
class CrosshairSyncState internal constructor() {
    private val normalizedX: MutableState<Float?> = mutableStateOf(null)
    private val ownerId: MutableState<String?> = mutableStateOf(null)

    /**
     * The shared crosshair position as a fraction in `0f..1f` across the plot width, or `null`
     * when no participant currently shows a crosshair.
     */
    val fraction: Float?
        get() = normalizedX.value

    /**
     * The id of the participant whose gesture produced the current [fraction], or `null` when no
     * crosshair is active.
     */
    val owner: String?
        get() = ownerId.value

    /**
     * Publishes the crosshair position of the chart identified by [ownerId], making it the active
     * owner. Any previous owner is displaced (last-writer-wins), which is how a drag starting on a
     * second chart takes the crosshair over. The [fraction] is coerced into `0f..1f`.
     *
     * @param ownerId Stable id of the publishing chart, unique within the synced group.
     * @param fraction Horizontal crosshair position as a fraction of the plot width.
     */
    fun publish(
        ownerId: String,
        fraction: Float,
    ) {
        this.ownerId.value = ownerId
        normalizedX.value = fraction.coerceIn(minimumValue = 0f, maximumValue = 1f)
    }

    /**
     * Clears the shared crosshair if [ownerId] is the current owner. Requests from a participant
     * that does not own the crosshair are ignored, so an observer dismissing its mirrored guide
     * can never cancel the owner's active gesture.
     *
     * @param ownerId Stable id of the participant requesting the clear.
     */
    fun clear(ownerId: String) {
        if (this.ownerId.value == ownerId) {
            this.ownerId.value = null
            normalizedX.value = null
        }
    }

    /**
     * Returns the fraction an observing participant should mirror, or `null` when there is nothing
     * to mirror: either no crosshair is active, or [observerId] itself owns the current gesture,
     * because a chart never mirrors its own crosshair.
     *
     * @param observerId Stable id of the observing chart within the synced group.
     */
    fun fractionFor(observerId: String): Float? {
        val activeOwner = ownerId.value ?: return null
        return if (activeOwner == observerId) {
            null
        } else {
            normalizedX.value
        }
    }
}

/**
 * Converts a canvas pixel [x] into the normalised `0f..1f` fraction across a plot that starts at
 * [plotLeft] and spans [plotWidth] pixels. A non-positive [plotWidth] yields `0f`, and the result
 * is coerced into `0f..1f` so pixel rounding at the plot edges never escapes the valid range.
 *
 * @param x Canvas pixel x-coordinate to normalise.
 * @param plotLeft Canvas pixel x-coordinate of the plot's left edge.
 * @param plotWidth Plot width in pixels.
 */
fun normalizedCrosshairFraction(
    x: Float,
    plotLeft: Float,
    plotWidth: Float,
): Float =
    if (plotWidth <= 0f) {
        0f
    } else {
        ((x - plotLeft) / plotWidth).coerceIn(minimumValue = 0f, maximumValue = 1f)
    }

/**
 * Converts a normalised crosshair [fraction] back into a canvas pixel x-coordinate for a plot that
 * starts at [plotLeft] and spans [plotWidth] pixels. The inverse of [normalizedCrosshairFraction]:
 * the [fraction] is coerced into `0f..1f`, and a non-positive [plotWidth] yields [plotLeft].
 *
 * @param fraction Horizontal crosshair position as a fraction of the plot width.
 * @param plotLeft Canvas pixel x-coordinate of the plot's left edge.
 * @param plotWidth Plot width in pixels.
 */
fun crosshairXForFraction(
    fraction: Float,
    plotLeft: Float,
    plotWidth: Float,
): Float =
    if (plotWidth <= 0f) {
        plotLeft
    } else {
        plotLeft + fraction.coerceIn(minimumValue = 0f, maximumValue = 1f) * plotWidth
    }

/**
 * Creates and remembers a [CrosshairSyncState] shared by a group of stacked charts. Pass the same
 * instance to [rememberParticipant] for every chart in the group.
 */
@Composable
fun rememberCrosshairSync(): CrosshairSyncState = remember { CrosshairSyncState() }

/**
 * Enrols one chart in this synced-crosshair group and returns the [ChartCrosshair] to pass to the
 * chart's `crosshair` parameter.
 *
 * The participant owns a [CrosshairManager] wired to the group in both directions. When this
 * chart's own crosshair moves — a [CrosshairState] carrying a snapped data item — its position is
 * published to the group as a normalised fraction of the plot width. While another participant
 * owns the gesture, this chart's manager is driven to show a mirrored guide at the shared
 * fraction. Plot pixel geometry is read from [viewPortState], so the same instance must also be
 * given to the chart via `ChartInteractionConfig(viewPortState = ...)`; the chart scaffold
 * populates its bounds on every draw pass.
 *
 * A mirrored guide carries no snapped data item, so no label is rendered for it and its marker dot
 * rests at the top of the plot; the vertical guide line is the shared element. Until charts adopt
 * an externally supplied manager (a pending `ChartCrosshair.manager` hook honoured by
 * `rememberChartCrosshair`), the returned crosshair behaves exactly like a plain [ChartCrosshair]
 * and the group state stays idle.
 *
 * @param T The chart's data/point type.
 * @param ownerId Stable id for this chart, unique within the synced group.
 * @param viewPortState The chart's viewport state, used as the source of plot pixel geometry.
 * @param config Appearance of the guide line; defaults to a vertical-only guide, which reads best
 *   when mirrored across charts whose y-scales differ.
 * @param label Optional custom label for this chart's own crosshair, as on [ChartCrosshair].
 */
@Composable
fun <T> CrosshairSyncState.rememberParticipant(
    ownerId: String,
    viewPortState: ViewPortState,
    config: ChartCrosshairConfig = ChartCrosshairConfig(showHorizontalLine = false),
    label: (@Composable CrosshairScope<T>.() -> Unit)? = null,
): ChartCrosshair<T> {
    val manager = rememberCrosshairManager<T>()
    SyncPublishEffect(sync = this, ownerId = ownerId, manager = manager, viewPortState = viewPortState)
    SyncMirrorEffect(sync = this, ownerId = ownerId, manager = manager, viewPortState = viewPortState)
    return remember(this, ownerId, config, label) { ChartCrosshair(config = config, label = label) }
}

/**
 * Publishes [manager]'s own crosshair movements to [sync]. Only states carrying a snapped data
 * item are published: gesture-driven updates always snap to an item, while mirrored updates driven
 * by [SyncMirrorEffect] carry `null` and are ignored, which keeps an observer from re-publishing a
 * position it merely mirrors.
 */
@Composable
private fun <T> SyncPublishEffect(
    sync: CrosshairSyncState,
    ownerId: String,
    manager: CrosshairManager<T>,
    viewPortState: ViewPortState,
) {
    LaunchedEffect(sync, ownerId, manager, viewPortState) {
        snapshotFlow { manager.state?.takeIf { manager.selectedItem != null } }
            .collect { ownState ->
                if (ownState != null) {
                    sync.publish(
                        ownerId = ownerId,
                        fraction =
                            normalizedCrosshairFraction(
                                x = ownState.x,
                                plotLeft = viewPortState.chartLeft,
                                plotWidth = viewPortState.chartWidth,
                            ),
                    )
                } else {
                    sync.clear(ownerId = ownerId)
                }
            }
    }
}

/**
 * Drives [manager] to mirror the group's shared fraction while another participant owns the
 * gesture. Mirrored states are pushed with a `null` item so [SyncPublishEffect] never re-publishes
 * them; when the shared fraction clears, the mirrored guide is dismissed unless this chart's own
 * gesture is active.
 */
@Composable
private fun <T> SyncMirrorEffect(
    sync: CrosshairSyncState,
    ownerId: String,
    manager: CrosshairManager<T>,
    viewPortState: ViewPortState,
) {
    LaunchedEffect(sync, ownerId, manager, viewPortState) {
        snapshotFlow { sync.fractionFor(observerId = ownerId) }
            .collect { fraction ->
                if (fraction != null) {
                    manager.update(
                        newState =
                            CrosshairState(
                                x =
                                    crosshairXForFraction(
                                        fraction = fraction,
                                        plotLeft = viewPortState.chartLeft,
                                        plotWidth = viewPortState.chartWidth,
                                    ),
                                y = DRIVEN_CROSSHAIR_Y,
                                label = DRIVEN_CROSSHAIR_LABEL,
                            ),
                        item = null,
                    )
                } else if (manager.selectedItem == null) {
                    manager.dismiss()
                }
            }
    }
}
