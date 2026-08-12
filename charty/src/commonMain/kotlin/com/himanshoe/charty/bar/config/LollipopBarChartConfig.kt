package com.himanshoe.charty.bar.config

import androidx.compose.runtime.Stable
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.PersistentMarker
import com.himanshoe.charty.common.config.requireValidVisibleWindow
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.common.tooltip.TooltipPosition

/**
 * Configuration for [com.himanshoe.charty.bar.LollipopBarChart]
 *
 * @property barWidthFraction Fraction of horizontal space each lollipop stem occupies (0f..1f)
 * @property stemThickness Thickness of the vertical stem in pixels
 * @property circleRadius Radius of the lollipop circle in pixels
 * @property circleStrokeWidth Optional stroke width for drawing the circle as a ring; 0f for filled
 * @property circleColor Optional override color for the circle head. If null, falls back to the bar color.
 * @property animation Animation configuration for growing stems from baseline
 * @property animateValueChanges When `true`, lollipop heads tween from their previous values to the
 *   new ones whenever the data changes (using [animation]); when `false` (default) new data appears
 *   instantly. Has no effect if [animation] is [Animation.Disabled].
 * @property markers Persistent markers pinned to specific lollipops, drawn at all times regardless of
 *   touch (see [PersistentMarker]). A marker is anchored at the centre of its lollipop head, the top
 *   of the stem. `PersistentMarker(dataIndex = -1)` is the idiomatic way to label the latest value —
 *   the rightmost lollipop. Empty (the default) draws none.
 * @property tooltipConfig Configuration for tooltip appearance when a bar is clicked
 * @property tooltipPosition Preferred position for tooltips (ABOVE, BELOW, or AUTO)
 * @property tooltipFormatter Converts a data point into the string shown in its tooltip.
 * @property visibleWindow Rolling "show last N" window; `null` (default) shows every point and
 *   changes nothing. As data is appended the window advances to the latest. Must be `>= 2`.
 */
@Stable
data class LollipopBarChartConfig(
    val barWidthFraction: Float = 0.2f,
    val stemThickness: Float = 6f,
    val circleRadius: Float = 14f,
    val circleStrokeWidth: Float = 0f,
    val circleColor: ChartyColor? = null,
    val animation: Animation = Animation.Enabled(),
    val animateValueChanges: Boolean = false,
    val markers: List<PersistentMarker> = emptyList(),
    val tooltipConfig: TooltipConfig = TooltipConfig(),
    val tooltipPosition: TooltipPosition = TooltipPosition.AUTO,
    val tooltipFormatter: (BarData) -> String = { barData ->
        "${barData.label}: ${barData.value}"
    },
    val visibleWindow: Int? = null,
) {
    init {
        requireValidVisibleWindow(visibleWindow)
        require(barWidthFraction in 0f..1f) { "Bar width fraction must be between 0 and 1" }
        require(stemThickness > 0f) { "Stem thickness must be positive" }
        require(circleRadius > 0f) { "Circle radius must be positive" }
        require(circleStrokeWidth >= 0f) { "Circle stroke width cannot be negative" }
    }
}
