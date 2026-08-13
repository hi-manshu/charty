package com.himanshoe.charty.bar.config

import androidx.compose.runtime.Stable
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.PersistentMarker
import com.himanshoe.charty.common.config.ReferenceLineConfig
import com.himanshoe.charty.common.config.requireValidVisibleWindow
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.common.tooltip.TooltipPosition
import com.himanshoe.charty.common.util.toChartLabel

/**
 * Configuration for Bubble Bar Chart appearance and behavior
 *
 * @property barWidthFraction Fraction of available space that each bar column occupies (0.0f - 1.0f)
 * @property bubbleRadius Radius of each bubble in pixels
 * @property bubbleSpacing Spacing between bubbles in pixels
 * @property negativeValuesDrawMode How to draw negative values (BELOW_AXIS or FROM_MIN_VALUE)
 * @property animation Animation configuration (Disabled or Enabled with duration)
 * @property animateValueChanges When `true`, bubble columns tween from their previous values to the
 *   new ones whenever the data changes (using [animation]); when `false` (default) new data appears
 *   instantly. Has no effect if [animation] is [Animation.Disabled].
 * @property referenceLine Optional reference line configuration (target/average line)
 * @property markers Persistent markers pinned to specific columns, drawn at all times regardless of
 *   touch (see [PersistentMarker]). A marker is anchored at the top-centre of its column.
 *   `PersistentMarker(dataIndex = -1)` is the idiomatic way to label the latest value — the
 *   rightmost column. Empty (the default) draws none.
 * @property tooltipConfig Configuration for tooltip appearance when a bar is clicked.
 *   `null`, the default, takes it from the ambient
 *   [ChartyTheme][com.himanshoe.charty.common.theme.ChartyTheme].
 * @property tooltipPosition Preferred position for tooltips (ABOVE, BELOW, or AUTO)
 * @property tooltipFormatter Converts a data point into the string shown in its tooltip.
 * @property visibleWindow Rolling "show last N" window; `null` (default) shows every point and
 *   changes nothing. As data is appended the window advances to the latest. Must be `>= 2`.
 */
@Stable
data class BubbleBarChartConfig(
    val barWidthFraction: Float = 0.2f,
    val bubbleRadius: Float = 100f,
    val bubbleSpacing: Float = 8f,
    val negativeValuesDrawMode: NegativeValuesDrawMode = NegativeValuesDrawMode.BELOW_AXIS,
    val animation: Animation = Animation.Default,
    val animateValueChanges: Boolean = false,
    val referenceLine: ReferenceLineConfig? = null,
    val markers: List<PersistentMarker> = emptyList(),
    val tooltipConfig: TooltipConfig? = null,
    val tooltipPosition: TooltipPosition = TooltipPosition.AUTO,
    val tooltipFormatter: (BarData) -> String = { barData ->
        "${barData.label}: ${barData.value.toChartLabel()}"
    },
    val visibleWindow: Int? = null,
) {
    init {
        requireValidVisibleWindow(visibleWindow)
        require(barWidthFraction in 0f..1f) { "Bar width fraction must be between 0 and 1" }
        require(bubbleRadius > 0) { "Bubble radius must be positive" }
        require(bubbleSpacing >= 0) { "Bubble spacing must be non-negative" }
    }
}
