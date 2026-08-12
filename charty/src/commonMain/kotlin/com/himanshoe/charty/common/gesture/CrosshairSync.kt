package com.himanshoe.charty.common.gesture

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import com.himanshoe.charty.common.viewport.ViewPortState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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
 * Create one instance per synced group with [rememberCrosshairSync] and share it with
 * [CrosshairSyncScope]; charts inside that scope enrol themselves.
 */
@Stable
class CrosshairSyncState internal constructor() {
    private val sharedState = MutableStateFlow<SharedCrosshair?>(null)

    internal val shared: StateFlow<SharedCrosshair?> = sharedState.asStateFlow()

    /**
     * The shared crosshair position as a fraction in `0f..1f` across the plot width, or `null`
     * when no participant currently shows a crosshair.
     */
    val fraction: Float?
        get() = sharedState.value?.fraction

    /**
     * The id of the participant whose gesture produced the current [fraction], or `null` when no
     * crosshair is active.
     */
    val owner: String?
        get() = sharedState.value?.ownerId

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
        sharedState.update {
            SharedCrosshair(
                ownerId = ownerId,
                fraction = fraction.coerceIn(minimumValue = 0f, maximumValue = 1f),
            )
        }
    }

    /**
     * Clears the shared crosshair if [ownerId] is the current owner. Requests from a participant
     * that does not own the crosshair are ignored, so an observer dismissing its mirrored guide
     * can never cancel the owner's active gesture. The owner check and the clear happen as one
     * atomic update, so a concurrent publish from another chart can never be lost.
     *
     * @param ownerId Stable id of the participant requesting the clear.
     */
    fun clear(ownerId: String) {
        sharedState.update { current ->
            if (current?.ownerId == ownerId) {
                null
            } else {
                current
            }
        }
    }

    /**
     * Returns the fraction an observing participant should mirror, or `null` when there is nothing
     * to mirror: either no crosshair is active, or [observerId] itself owns the current gesture,
     * because a chart never mirrors its own crosshair.
     *
     * @param observerId Stable id of the observing chart within the synced group.
     */
    fun fractionFor(observerId: String): Float? = sharedState.value?.takeIf { it.ownerId != observerId }?.fraction
}

/**
 * The group's active crosshair: which chart owns the gesture and where the guide sits. Held as a
 * single value so the owner and the position can never be observed out of step.
 *
 * @property ownerId Stable id of the chart whose gesture produced [fraction].
 * @property fraction Horizontal position as a fraction in `0f..1f` across the plot width.
 */
@Immutable
internal data class SharedCrosshair(
    val ownerId: String,
    val fraction: Float,
)

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
 * instance to [CrosshairSyncScope] to share it across a group of charts.
 */
@Composable
fun rememberCrosshairSync(): CrosshairSyncState = remember { CrosshairSyncState() }

/**
 * Shares one crosshair position across every crosshair-capable chart composed inside [content].
 *
 * Each participating chart publishes its own crosshair while the user drags it, and every other
 * chart in the scope mirrors that position as a guide line. Ownership is last-writer-wins, so
 * moving to a different chart hands the crosshair over. Charts enrol automatically — no per-chart
 * parameter is needed — but a chart only participates when it has a crosshair configured **and** an
 * [com.himanshoe.charty.common.viewport.ViewPortState] in its `interactionConfig`, which supplies
 * the plot geometry the shared fraction is resolved against.
 *
 * A mirrored guide carries no snapped data item, so no label is drawn for it; the vertical guide
 * line is the shared element.
 *
 * ```kotlin
 * CrosshairSyncScope {
 *     LineChart(data = { revenue }, crosshair = ChartCrosshair(), interactionConfig = topConfig)
 *     LineChart(data = { costs }, crosshair = ChartCrosshair(), interactionConfig = bottomConfig)
 * }
 * ```
 *
 * @param sync The shared group state; defaults to one remembered for this scope.
 * @param content The charts that share the crosshair.
 */
@Composable
fun CrosshairSyncScope(
    sync: CrosshairSyncState = rememberCrosshairSync(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalCrosshairSync provides sync) { content() }
}

/**
 * The synced-crosshair group the current charts belong to, or `null` when they are not inside a
 * [CrosshairSyncScope]. Charts read this internally to enrol themselves.
 */
val LocalCrosshairSync: ProvidableCompositionLocal<CrosshairSyncState?> =
    staticCompositionLocalOf { null }

/**
 * Wires [manager] into [sync] in both directions: publishing this chart's own crosshair to the
 * group and mirroring the group's position while another chart owns the gesture.
 */
@Composable
internal fun <T> CrosshairSyncParticipantEffects(
    sync: CrosshairSyncState,
    ownerId: String,
    manager: CrosshairManager<T>,
    viewPortState: ViewPortState,
) {
    SyncPublishEffect(sync = sync, ownerId = ownerId, manager = manager, viewPortState = viewPortState)
    SyncMirrorEffect(sync = sync, ownerId = ownerId, manager = manager, viewPortState = viewPortState)
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
 * gesture is active. The pixel position is resolved inside the observed snapshot so a plot that
 * resizes while the mirror is showing moves the guide instead of leaving it at a stale pixel.
 */
@Composable
private fun <T> SyncMirrorEffect(
    sync: CrosshairSyncState,
    ownerId: String,
    manager: CrosshairManager<T>,
    viewPortState: ViewPortState,
) {
    val shared by sync.shared.collectAsState()
    LaunchedEffect(sync, ownerId, manager, viewPortState) {
        snapshotFlow {
            shared?.takeIf { it.ownerId != ownerId }?.let { active ->
                crosshairXForFraction(
                    fraction = active.fraction,
                    plotLeft = viewPortState.chartLeft,
                    plotWidth = viewPortState.chartWidth,
                )
            }
        }.collect { mirroredX ->
            if (mirroredX != null) {
                manager.update(
                    newState =
                        CrosshairState(
                            x = mirroredX,
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
