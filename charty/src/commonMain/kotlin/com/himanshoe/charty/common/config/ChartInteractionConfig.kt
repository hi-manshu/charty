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
 */
@Stable
class ChartInteractionConfig(
    val viewPortState: ViewPortState? = null,
    val brushSelectionState: BrushSelectionState? = null,
    val onRangeSelect: ((startIndex: Int, endIndex: Int) -> Unit)? = null,
    val annotations: List<ChartAnnotation> = emptyList(),
    val accessibilityDescription: String? = null,
)
