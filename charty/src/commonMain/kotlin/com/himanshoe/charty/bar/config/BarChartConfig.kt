package com.himanshoe.charty.bar.config

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.CornerRadius
import com.himanshoe.charty.common.config.PersistentMarker
import com.himanshoe.charty.common.config.ReferenceBandConfig
import com.himanshoe.charty.common.config.ReferenceLineConfig
import com.himanshoe.charty.common.config.requireValidVisibleWindow
import com.himanshoe.charty.common.gesture.ChartCrosshairConfig
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.common.tooltip.TooltipPosition
import com.himanshoe.charty.common.util.toChartLabel

/**
 * Defines how negative values should be drawn in bar charts
 */
enum class NegativeValuesDrawMode {
    /**
     * Negative bars extend below the zero axis line (axis centered)
     * Positive bars extend above the zero axis line
     * Best for visualizing profit/loss, gains/losses, etc.
     */
    BELOW_AXIS,

    /**
     * All bars drawn from the minimum value upward
     * The axis starts at the lowest value instead of zero
     * Best for showing relative differences when all values should appear above baseline
     */
    FROM_MIN_VALUE,
}

/**
 * Configuration for Bar Chart appearance and behavior
 *
 * @property barWidthFraction Fraction of available space that each bar occupies (0.0f - 1.0f)
 * @property barSpacing Spacing between bars in pixels
 * @property cornerRadius Corner radius for bar corners (None, Small, Medium, Large, ExtraLarge, or Custom)
 * @property negativeValuesDrawMode How to draw negative values (BELOW_AXIS or FROM_MIN_VALUE)
 * @property animation Animation configuration (Disabled or Enabled with duration)
 * @property animateValueChanges When `true`, bar heights tween from their previous values to the new
 *   ones whenever the data changes (using [animation]); when `false` (default) new data appears
 *   instantly. Has no effect if [animation] is [Animation.Disabled].
 * @property referenceLine Optional reference line configuration (target/average line), drawn over the
 *   bars. It marks a value on the value axis, so it is horizontal on
 *   [com.himanshoe.charty.bar.BarChart] and vertical on [com.himanshoe.charty.bar.HorizontalBarChart]
 *   and [com.himanshoe.charty.bar.SpanChart], whose values run left to right.
 * @property referenceBand Optional shaded value region drawn behind the bars (see
 *   [ReferenceBandConfig]). Like [referenceLine] it follows the chart's value axis, so it is a
 *   horizontal stripe on [com.himanshoe.charty.bar.BarChart] and a vertical one on
 *   [com.himanshoe.charty.bar.HorizontalBarChart] and [com.himanshoe.charty.bar.SpanChart].
 * @property markers Persistent markers pinned to specific bars, drawn at all times regardless of
 *   touch (see [PersistentMarker]). A marker is anchored at the top-centre of its bar in
 *   [com.himanshoe.charty.bar.BarChart], and at the centre of its bar's value end (the end away from
 *   the axis) in [com.himanshoe.charty.bar.HorizontalBarChart] and
 *   [com.himanshoe.charty.bar.SpanChart], where a span is marked at its end value.
 *   `PersistentMarker(dataIndex = -1)` is the idiomatic way to label the latest value: the rightmost
 *   bar for the vertical chart, the bottom bar for the horizontal ones. Empty (the default) draws none.
 * @property tooltipConfig Configuration for tooltip appearance when a bar is clicked.
 *   `null`, the default, takes it from the ambient
 *   [ChartyTheme][com.himanshoe.charty.common.theme.ChartyTheme].
 * @property tooltipPosition Preferred position for tooltips (ABOVE, BELOW, or AUTO)
 * @property tooltipFormatter Converts a data point into the string shown in its tooltip. Honoured by
 *   [com.himanshoe.charty.bar.BarChart] and [com.himanshoe.charty.bar.HorizontalBarChart], which both
 *   plot [BarData]. [com.himanshoe.charty.bar.SpanChart] cannot honour it and ignores it: a span is a
 *   [com.himanshoe.charty.bar.data.SpanData] with a `startValue` and an `endValue` rather than the
 *   single `value` this formatter receives, so there is no faithful way to hand one to a
 *   `(BarData) -> String`. Span tooltips always read `label: startValue - endValue`.
 * @property crosshairConfig When non-null, enables a draggable [ChartCrosshairConfig] that tracks
 *   the user's finger and snaps to the nearest bar — by x on [com.himanshoe.charty.bar.BarChart]
 *   and by y on [com.himanshoe.charty.bar.HorizontalBarChart]. Usually set for you by passing a
 *   `crosshair` to the chart rather than configured here directly.
 * @property showDataLabels Whether to show value labels above each bar
 * @property dataLabelFormatter Formats the bar value for the data label text
 * @property dataLabelStyle Text style for data labels
 * @property visibleWindow Rolling "show last N" window; `null` (default) shows every point and
 *   changes nothing. As data is appended the window advances to the latest. Must be `>= 2`.
 */
@Stable
data class BarChartConfig(
    val barWidthFraction: Float = 0.6f,
    val barSpacing: Float = 0f,
    val cornerRadius: CornerRadius = CornerRadius.Medium,
    val negativeValuesDrawMode: NegativeValuesDrawMode = NegativeValuesDrawMode.BELOW_AXIS,
    val animation: Animation = Animation.Default,
    val animateValueChanges: Boolean = false,
    val referenceLine: ReferenceLineConfig? = null,
    val referenceBand: ReferenceBandConfig? = null,
    val markers: List<PersistentMarker> = emptyList(),
    val tooltipConfig: TooltipConfig? = null,
    val tooltipPosition: TooltipPosition = TooltipPosition.AUTO,
    val tooltipFormatter: (BarData) -> String = { barData ->
        "${barData.label}: ${barData.value.toChartLabel()}"
    },
    val crosshairConfig: ChartCrosshairConfig? = null,
    val showDataLabels: Boolean = false,
    val dataLabelFormatter: (BarData) -> String = { barData -> barData.value.toChartLabel() },
    val dataLabelStyle: TextStyle =
        TextStyle(
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.DarkGray,
        ),
    val visibleWindow: Int? = null,
) {
    init {
        requireValidVisibleWindow(visibleWindow)
        require(barWidthFraction in 0f..1f) { "Bar width fraction must be between 0 and 1" }
        require(barSpacing >= 0) { "Bar spacing must be non-negative" }
    }
}
