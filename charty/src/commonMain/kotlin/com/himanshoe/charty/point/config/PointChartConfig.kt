package com.himanshoe.charty.point.config

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.NegativeValuesDrawMode
import com.himanshoe.charty.common.config.PersistentMarker
import com.himanshoe.charty.common.config.ReferenceBandConfig
import com.himanshoe.charty.common.config.ReferenceLineConfig
import com.himanshoe.charty.common.config.requireValidVisibleWindow
import com.himanshoe.charty.common.gesture.ChartCrosshairConfig
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.common.tooltip.TooltipPosition
import com.himanshoe.charty.common.util.toChartLabel
import com.himanshoe.charty.point.data.PointData

private const val DEFAULT_SELECTION_COLUMN_ARGB = 0x142962FF
private const val MIN_DOWNSAMPLE_THRESHOLD = 3

/**
 * Configuration for Point Chart appearance and behavior
 *
 * @property pointRadius Radius of each point in pixels
 * @property pointAlpha Alpha (transparency) value for points (0.0f - 1.0f)
 * @property negativeValuesDrawMode How to draw negative values (BELOW_AXIS or FROM_MIN_VALUE)
 * @property animation Animation configuration (Disabled or Enabled with duration)
 * @property animateValueChanges When `true`, point values tween from their previous positions to the
 *   new ones whenever the data changes (using [animation]); when `false` (default) new data appears
 *   instantly. Has no effect if [animation] is [Animation.Disabled]. In
 *   [com.himanshoe.charty.point.BubbleChart] this tweens the y values; bubble sizes are unaffected.
 * @property referenceLine Optional reference line configuration for reusable target/avg line support,
 *   drawn over the data by [com.himanshoe.charty.point.PointChart] and
 *   [com.himanshoe.charty.point.BubbleChart]
 * @property referenceBand Optional shaded value region drawn behind the points (see
 *   [ReferenceBandConfig]), honoured by [com.himanshoe.charty.point.PointChart] and
 *   [com.himanshoe.charty.point.BubbleChart]
 * @property markers Persistent markers pinned to specific points, always drawn regardless of touch
 *   (see [PersistentMarker]). A marker is anchored on its point's centre.
 *   `PersistentMarker(dataIndex = -1)` is the idiomatic way to label the latest value — the rightmost
 *   point. Empty (the default) draws none.
 * @property tooltipConfig Configuration for tooltip appearance when a point is clicked.
 *   `null`, the default, takes it from the ambient
 *   [ChartyTheme][com.himanshoe.charty.common.theme.ChartyTheme].
 * @property tooltipPosition Preferred position for tooltips (ABOVE, BELOW, or AUTO)
 * @property tooltipFormatter Function to format tooltip content from PointData
 * @property crosshairConfig When non-null, enables a draggable crosshair that tracks the user's finger
 *   and snaps to the nearest point. When set, it replaces the standard tap-to-tooltip interaction.
 *   Applies to [com.himanshoe.charty.point.PointChart] and [com.himanshoe.charty.point.BubbleChart].
 * @property highlightSelectedColumn When `true`, a translucent vertical band is drawn behind the
 *   selected point's column while a tap selection is active. Toggle it off to disable the highlight.
 * @property selectionColumnColor Fill of the selection-column highlight as a [ChartyColor] (solid or
 *   gradient). Use a semi-transparent color — the alpha channel controls the opacity. Only used when
 *   [highlightSelectedColumn] is `true`.
 * @property selectionColumnWidth Width of the highlight band in pixels. When `null` (default) the
 *   per-point column width is used. Only used when [highlightSelectedColumn] is `true`.
 * @property downsampleThreshold When set, the visible points are reduced to at most this many with
 *   the shape-preserving LTTB algorithm before drawing, keeping large series (tens of thousands of
 *   points) at interactive frame rates. `null` (the default) draws every point. Must be `>= 3`.
 * @property visibleWindow Rolling "show last N" window; `null` (default) shows every point and
 *   changes nothing. As data is appended the window advances to the latest. Must be `>= 2`.
 */
@Stable
data class PointChartConfig(
    val pointRadius: Float = 8f,
    val pointAlpha: Float = 1f,
    val negativeValuesDrawMode: NegativeValuesDrawMode = NegativeValuesDrawMode.BELOW_AXIS,
    val animation: Animation = Animation.Default,
    val animateValueChanges: Boolean = false,
    val referenceLine: ReferenceLineConfig? = null,
    val referenceBand: ReferenceBandConfig? = null,
    val markers: List<PersistentMarker> = emptyList(),
    val tooltipConfig: TooltipConfig? = null,
    val tooltipPosition: TooltipPosition = TooltipPosition.AUTO,
    val tooltipFormatter: (PointData) -> String = { pointData ->
        "${pointData.label}: ${pointData.value.toChartLabel()}"
    },
    val crosshairConfig: ChartCrosshairConfig? = null,
    val highlightSelectedColumn: Boolean = false,
    val selectionColumnColor: ChartyColor = ChartyColor.Solid(Color(DEFAULT_SELECTION_COLUMN_ARGB)),
    val selectionColumnWidth: Float? = null,
    val downsampleThreshold: Int? = null,
    val visibleWindow: Int? = null,
) {
    init {
        requireValidVisibleWindow(visibleWindow)
        require(pointRadius > 0) { "Point radius must be greater than 0" }
        require(pointAlpha in 0f..1f) { "Point alpha must be between 0 and 1" }
        require(downsampleThreshold == null || downsampleThreshold >= MIN_DOWNSAMPLE_THRESHOLD) {
            "downsampleThreshold must be null or >= $MIN_DOWNSAMPLE_THRESHOLD"
        }
    }
}
