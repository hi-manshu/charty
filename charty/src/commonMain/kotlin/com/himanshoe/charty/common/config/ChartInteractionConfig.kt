package com.himanshoe.charty.common.config

import androidx.compose.runtime.Stable
import com.himanshoe.charty.common.annotation.ChartAnnotation
import com.himanshoe.charty.common.brush.BrushSelectionState
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
 */
@Stable
class ChartInteractionConfig(
    val viewPortState: ViewPortState? = null,
    val brushSelectionState: BrushSelectionState? = null,
    val onRangeSelect: ((startIndex: Int, endIndex: Int) -> Unit)? = null,
    val annotations: List<ChartAnnotation> = emptyList(),
    val accessibilityDescription: String? = null,
    val dragTooltipEnabled: Boolean = false,
    val autoScrollToLatest: Boolean = false,
    val edgeFade: ScrollEdgeFadeConfig? = null,
)
