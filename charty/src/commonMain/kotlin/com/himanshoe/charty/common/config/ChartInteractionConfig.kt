package com.himanshoe.charty.common.config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import com.himanshoe.charty.common.annotation.ChartAnnotation
import com.himanshoe.charty.common.brush.BrushSelectionState
import com.himanshoe.charty.common.streaming.StreamingState
import com.himanshoe.charty.common.viewport.ViewPortState

/**
 * Bundles the optional interaction and accessibility parameters that are common to all charts.
 *
 * @property viewPortState When non-null, enables pinch-to-zoom and pan over the data.
 * @property brushSelectionState When non-null, enables drag-to-select brush gestures.
 * @property onRangeSelect Called with (startIndex, endIndex) when a brush selection completes.
 * @property annotations Annotation markers rendered on top of the chart content.
 * @property accessibilityDescription Overrides the auto-generated screen-reader description.
 *   Pass an empty string to suppress it entirely.
 * @property dragTooltipEnabled When `true`, dragging a finger across a rectangular chart (such as a
 *   bar chart) tracks the item under the finger and shows its tooltip, dismissing on release. This
 *   is automatically suppressed while [viewPortState] or [brushSelectionState] is active, since
 *   those also consume drag gestures.
 * @property autoScrollToLatest When `true` and [viewPortState] is set, the viewport follows the end
 *   of the data: whenever the dataset grows the window scrolls to reveal the newest points (keeping
 *   the current zoom level). Has no effect without a [viewPortState].
 * @property edgeFade When non-null and [viewPortState] is set, draws a scrim at the leading/trailing
 *   edges while data is scrolled off-screen, hinting there is more to pan to (see
 *   [ScrollEdgeFadeConfig]).
 * @property streamingState Enables scrollback on a chart with a rolling `visibleWindow`: the reader
 *   can drag back through history while new data accumulates, and jump back to the newest point.
 *   `null` (the default) keeps the window pinned to the newest data. Scrollback needs the chart's
 *   horizontal drag, so it is unavailable when a `crosshair` is configured on the same chart: the
 *   crosshair owns the drag and the window simply keeps following the newest data. Tap-to-tooltip
 *   works alongside either of them.
 * @property jumpToLatest Your "jump to latest" control, rendered over the bottom centre of the plot
 *   while the window is detached and hidden again as soon as it follows the newest data. It receives
 *   the same [StreamingState] you passed as [streamingState], so it can label itself with
 *   [StreamingState.pendingCount] and call [StreamingState.jumpToLatest] when tapped. Use
 *   [com.himanshoe.charty.common.streaming.ChartJumpToLatestPill] for a ready-made one, or write your
 *   own composable. Requires both [streamingState] and a rolling `visibleWindow`; `null` (the default)
 *   shows no control.
 */
@Stable
data class ChartInteractionConfig(
    val viewPortState: ViewPortState? = null,
    val brushSelectionState: BrushSelectionState? = null,
    val onRangeSelect: ((startIndex: Int, endIndex: Int) -> Unit)? = null,
    val annotations: List<ChartAnnotation> = emptyList(),
    val accessibilityDescription: String? = null,
    val dragTooltipEnabled: Boolean = false,
    val autoScrollToLatest: Boolean = false,
    val edgeFade: ScrollEdgeFadeConfig? = null,
    val streamingState: StreamingState? = null,
    val jumpToLatest: (@Composable (StreamingState) -> Unit)? = null,
)
