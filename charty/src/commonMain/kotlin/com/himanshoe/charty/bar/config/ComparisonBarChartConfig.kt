package com.himanshoe.charty.bar.config

import androidx.compose.runtime.Stable
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.CornerRadius
import com.himanshoe.charty.common.config.NegativeValuesDrawMode
import com.himanshoe.charty.common.config.PersistentMarker
import com.himanshoe.charty.common.config.ReferenceLineConfig
import com.himanshoe.charty.common.config.requireValidVisibleWindow
import com.himanshoe.charty.common.gesture.ChartCrosshairConfig
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.common.tooltip.TooltipPosition
import com.himanshoe.charty.common.util.toChartLabel

/**
 * Represents a single bar in a comparison chart that was clicked
 *
 * @property barGroup The bar group that contains this bar
 * @property barIndex The index of the clicked bar within the group
 * @property barValue The value of the clicked bar
 * @property visibleWindow Rolling "show last N" window; `null` (default) shows every group and
 *   changes nothing. As data is appended the window advances to the latest. Must be `>= 2`.
 */
@Stable
data class ComparisonBarSegment(
    val barGroup: BarGroup,
    val barIndex: Int,
    val barValue: Float,
)

/**
 * Configuration for Comparison Bar Chart (formerly Grouped Bar Chart) appearance and behavior
 *
 * @property negativeValuesDrawMode How to draw negative values (BELOW_AXIS or FROM_MIN_VALUE)
 * @property cornerRadius Corner radius for bar corners (None, Small, Medium, Large, ExtraLarge, or Custom)
 * @property animation Animation configuration (Disabled or Enabled with duration)
 * @property animateValueChanges When `true`, every series in every group tweens from its previous
 *   value to the new one whenever the data changes (using [animation]); when `false` (default) new
 *   data appears instantly. Has no effect if [animation] is [Animation.Disabled].
 * @property referenceLine Optional reference line configuration
 * @property markers Persistent markers pinned to specific groups, drawn at all times regardless of
 *   touch (see [PersistentMarker]). A marker is anchored at the top-centre of its group, level with
 *   the group's tallest bar. `PersistentMarker(dataIndex = -1)` is the idiomatic way to label the
 *   latest value — the rightmost group. Empty (the default) draws none.
 * @property tooltipConfig Configuration for tooltip appearance when a bar is clicked.
 *   `null`, the default, takes it from the ambient
 *   [ChartyTheme][com.himanshoe.charty.common.theme.ChartyTheme].
 * @property tooltipPosition Preferred position for tooltips (ABOVE, BELOW, or AUTO)
 * @property tooltipFormatter Converts a data point into the string shown in its tooltip.
 * @property crosshairConfig When non-null, enables a draggable [ChartCrosshairConfig] that tracks the
 *   user's finger and snaps by x to the nearest group, resting on the top of that group's tallest
 *   bar. Usually set for you by passing a `crosshair` to the chart rather than configured here
 *   directly.
 * @property visibleWindow Rolling "show last N" window; `null` (default) shows every group and
 *   changes nothing. As data is appended the window advances to the latest. Must be `>= 2`.
 */
@Stable
data class ComparisonBarChartConfig(
    val negativeValuesDrawMode: NegativeValuesDrawMode = NegativeValuesDrawMode.BELOW_AXIS,
    val cornerRadius: CornerRadius = CornerRadius.Medium,
    val animation: Animation = Animation.Default,
    val animateValueChanges: Boolean = false,
    val referenceLine: ReferenceLineConfig? = null,
    val markers: List<PersistentMarker> = emptyList(),
    val tooltipConfig: TooltipConfig? = null,
    val tooltipPosition: TooltipPosition = TooltipPosition.AUTO,
    val tooltipFormatter: (ComparisonBarSegment) -> String = { segment ->
        "${segment.barGroup.label} [${segment.barIndex}]: ${segment.barValue.toChartLabel()}"
    },
    val crosshairConfig: ChartCrosshairConfig? = null,
    val visibleWindow: Int? = null,
) {
    init {
        requireValidVisibleWindow(visibleWindow)
    }
}
