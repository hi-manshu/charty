package com.himanshoe.charty.line.config

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.PersistentMarker
import com.himanshoe.charty.common.config.ReferenceBandConfig
import com.himanshoe.charty.common.config.ReferenceLineConfig
import com.himanshoe.charty.common.config.requireValidVisibleWindow
import com.himanshoe.charty.common.gesture.ChartCrosshairConfig
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.common.tooltip.TooltipPosition
import com.himanshoe.charty.common.util.toChartLabel
import com.himanshoe.charty.line.data.LineData

private const val DEFAULT_SELECTION_COLUMN_ARGB = 0x142962FF
private const val MIN_DOWNSAMPLE_THRESHOLD = 3

/**
 * Configuration for [com.himanshoe.charty.line.LineChart],
 * [com.himanshoe.charty.line.AreaChart], [com.himanshoe.charty.line.MultilineChart], and
 * [com.himanshoe.charty.line.StackedAreaChart] appearance and behaviour.
 * Not all properties apply to every chart type — see individual property docs for details.
 *
 * @property lineWidth Stroke width of the line in pixels.
 * @property showPoints Whether to draw circular markers at each data point.
 * @property pointRadius Radius of point markers in pixels. Only used when [showPoints] is `true`.
 * @property pointAlpha Opacity of point markers in the range `[0, 1]`.
 * @property strokeCap Cap style applied to both line ends and each segment.
 * @property smoothCurve When `true`, draws a cubic-bezier smooth curve instead of straight
 *   segments between data points. Kept for compatibility; prefer [interpolation]. When
 *   [interpolation] is [LineInterpolation.LINEAR] and this is `true`, the line is drawn smooth.
 *   Honoured by all four charts: [com.himanshoe.charty.line.LineChart],
 *   [com.himanshoe.charty.line.AreaChart], [com.himanshoe.charty.line.MultilineChart] and
 *   [com.himanshoe.charty.line.StackedAreaChart].
 * @property interpolation How points are connected: [LineInterpolation.LINEAR] straight segments,
 *   [LineInterpolation.SMOOTH] a cubic curve, or [LineInterpolation.STEP] horizontal-then-vertical
 *   steps. Takes precedence over [smoothCurve] unless it is [LineInterpolation.LINEAR]. Honoured by
 *   all four charts: [com.himanshoe.charty.line.LineChart], [com.himanshoe.charty.line.AreaChart],
 *   [com.himanshoe.charty.line.MultilineChart] and [com.himanshoe.charty.line.StackedAreaChart].
 *   The area-filled charts fill under the interpolated outline, so a stepped fill follows its steps.
 * @property negativeValuesDrawMode Controls how bars below zero are rendered.
 * @property animation Entry animation played when the chart first appears.
 * @property animateValueChanges When `true`, the line tweens from its previous points to the new ones
 *   whenever the data changes (using [animation]); when `false` (default) new data appears instantly.
 *   Has no effect if [animation] is [Animation.Disabled].
 * @property referenceLine Optional horizontal or vertical reference line drawn across the chart,
 *   over the series. Honoured by all four charts: [com.himanshoe.charty.line.LineChart],
 *   [com.himanshoe.charty.line.AreaChart], [com.himanshoe.charty.line.MultilineChart] and
 *   [com.himanshoe.charty.line.StackedAreaChart].
 * @property referenceBand Optional shaded value region drawn behind the data (see [ReferenceBandConfig]).
 * @property markers Persistent markers pinned to specific points, always drawn regardless of touch
 *   (see [PersistentMarker]). Empty (the default) draws none. A marker's `dataIndex` picks an x
 *   position, counting back from the newest point when negative, so `dataIndex = -1` labels the
 *   latest value. Where that x holds several values the marker anchors on the topmost one:
 *   in [com.himanshoe.charty.line.MultilineChart] the highest series at that x, and in
 *   [com.himanshoe.charty.line.StackedAreaChart] the top of the stack, that is the group's total.
 *   A marker with no label of its own shows the value it is anchored on.
 * @property tooltipConfig Appearance of the tooltip bubble shown on tap. Only used when
 *   [crosshairConfig] is `null`; the crosshair has its own label config.
 * @property tooltipPosition Preferred placement of the tap tooltip relative to the tapped point.
 * @property tooltipFormatter Converts a [LineData] point into the string shown in the tooltip, and
 *   in the crosshair label. The multi-series charts have no [LineData] of their own, so they project
 *   onto one before calling this: [com.himanshoe.charty.line.MultilineChart] passes the group label
 *   qualified with the one-based series number (`"Jan Line 2"`) for a tap, and `"L1"`, `"L2"`, … for
 *   each entry of the crosshair's combined label; [com.himanshoe.charty.line.StackedAreaChart]
 *   passes the group label with the tapped band's own value, and the group's total for the crosshair.
 * @property crosshairConfig When non-null, enables a draggable [ChartCrosshairConfig] that
 *   tracks the user's finger and snaps to the nearest data point. When set, it replaces the
 *   standard tap-to-tooltip interaction.
 * @property highlightSelectedColumn When `true`, a translucent vertical band is drawn behind the
 *   selected point's column while a tap selection is active. Toggle it off to disable the highlight.
 * @property selectionColumnColor Fill of the selection-column highlight as a [ChartyColor] (solid or
 *   gradient). Use a semi-transparent color — the alpha channel controls the opacity. Only used when
 *   [highlightSelectedColumn] is `true`.
 * @property selectionColumnWidth Width of the highlight band in pixels. When `null` (default) the
 *   per-point column width is used. Only used when [highlightSelectedColumn] is `true`.
 * @property legendLabels Series labels shown in the legend below the chart. Pass an empty list
 *   (the default) to hide the legend. For [com.himanshoe.charty.line.MultilineChart] and
 *   [com.himanshoe.charty.line.StackedAreaChart], each entry maps to a series by index.
 * @property legendTextStyle Text style applied to legend labels.
 * @property showGradientFill When `true`, draws a semi-transparent gradient fill under each
 *   series line in [com.himanshoe.charty.line.MultilineChart]. Has no effect on other chart types.
 * @property gradientFillAlpha Opacity of the gradient fill in the range `[0, 1]`. Only used when
 *   [showGradientFill] is `true`.
 * @property fillAlpha Opacity of the filled area under the line in the range `[0, 1]`. Used by
 *   [com.himanshoe.charty.line.AreaChart] for its area fill. Has no effect on other chart types.
 * @property downsampleThreshold When set, the visible series is reduced to at most this many points
 *   with the shape-preserving LTTB algorithm before drawing, keeping large series (tens of thousands
 *   of points) at interactive frame rates. `null` (the default) draws every point. Must be `>= 3`.
 *   Applies to [com.himanshoe.charty.line.LineChart], [com.himanshoe.charty.line.AreaChart],
 *   [com.himanshoe.charty.line.MultilineChart], and [com.himanshoe.charty.line.StackedAreaChart].
 *   For the multi-series charts, selection is driven by the per-x sum of all series so every series
 *   keeps the same x-indices and stays aligned.
 * @property visibleWindow When set, only the most recent this-many points are shown — a rolling
 *   window. As new points are appended to `data` the window advances to the latest, ideal for
 *   live/streaming data. `null` (the default) shows the whole series and changes nothing. Must be
 *   `>= 2`. Ignored when an interactive viewport (zoom/pan) is configured, which takes precedence.
 */
@Stable
data class LineChartConfig(
    val lineWidth: Float = 3f,
    val showPoints: Boolean = true,
    val pointRadius: Float = 6f,
    val pointAlpha: Float = 1f,
    val strokeCap: StrokeCap = StrokeCap.Round,
    val smoothCurve: Boolean = false,
    val interpolation: LineInterpolation = LineInterpolation.LINEAR,
    val negativeValuesDrawMode: NegativeValuesDrawMode = NegativeValuesDrawMode.BELOW_AXIS,
    val animation: Animation = Animation.Default,
    val animateValueChanges: Boolean = false,
    val referenceLine: ReferenceLineConfig? = null,
    val referenceBand: ReferenceBandConfig? = null,
    val markers: List<PersistentMarker> = emptyList(),
    val tooltipConfig: TooltipConfig = TooltipConfig(),
    val tooltipPosition: TooltipPosition = TooltipPosition.AUTO,
    val tooltipFormatter: (LineData) -> String = { lineData ->
        "${lineData.label}: ${lineData.value.toChartLabel()}"
    },
    val crosshairConfig: ChartCrosshairConfig? = null,
    val highlightSelectedColumn: Boolean = false,
    val selectionColumnColor: ChartyColor = ChartyColor.Solid(Color(DEFAULT_SELECTION_COLUMN_ARGB)),
    val selectionColumnWidth: Float? = null,
    val legendLabels: List<String> = emptyList(),
    val legendTextStyle: TextStyle = TextStyle(fontSize = 12.sp, color = Color.Unspecified),
    val showGradientFill: Boolean = false,
    val gradientFillAlpha: Float = 0.3f,
    val fillAlpha: Float = 0.3f,
    val downsampleThreshold: Int? = null,
    val visibleWindow: Int? = null,
) {
    init {
        requireValidVisibleWindow(visibleWindow)
        require(lineWidth > 0) { "Line width must be greater than 0" }
        require(pointRadius > 0) { "Point radius must be greater than 0" }
        require(pointAlpha in 0f..1f) { "Point alpha must be between 0 and 1" }
        require(gradientFillAlpha in 0f..1f) { "gradientFillAlpha must be between 0 and 1" }
        require(fillAlpha in 0f..1f) { "fillAlpha must be between 0 and 1" }
        require(downsampleThreshold == null || downsampleThreshold >= MIN_DOWNSAMPLE_THRESHOLD) {
            "downsampleThreshold must be null or >= $MIN_DOWNSAMPLE_THRESHOLD"
        }
    }
}
