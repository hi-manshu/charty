package com.himanshoe.charty.bar.config

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.CornerRadius
import com.himanshoe.charty.common.config.PersistentMarker
import com.himanshoe.charty.common.config.ReferenceLineConfig
import com.himanshoe.charty.common.config.requireValidVisibleWindow
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.common.tooltip.TooltipPosition
import com.himanshoe.charty.common.util.toChartLabel

/**
 * Identifies a single bar inside a grouped horizontal bar chart that was clicked.
 *
 * @property barGroup The group (row) that contains the tapped bar.
 * @property barIndex Zero-based index of the bar within its group.
 * @property barValue The value of the tapped bar.
 */
@Immutable
data class GroupedHorizontalBarEntry(
    val barGroup: BarGroup,
    val barIndex: Int,
    val barValue: Float,
)

/**
 * Configuration for [com.himanshoe.charty.bar.GroupedHorizontalBarChart].
 *
 * @property barWidthFraction Fraction of each row's height occupied by all bars collectively (0.0f – 1.0f).
 *   Remaining space is used as top/bottom padding around the bar group within the row.
 * @property barSpacing Gap in pixels between individual bars within the same group.
 * @property cornerRadius Corner radius for the trailing (right) end of each bar.
 * @property negativeValuesDrawMode How to render negative values:
 *   [NegativeValuesDrawMode.BELOW_AXIS] draws them to the left of the zero axis line;
 *   [NegativeValuesDrawMode.FROM_MIN_VALUE] aligns all bars from the minimum value.
 * @property animation Animation configuration ([Animation.Disabled] or [Animation.Enabled]).
 * @property animateValueChanges When `true`, every bar in every group tweens from its previous value
 *   to the new one whenever the data changes (using [animation]); when `false` (default) new data
 *   appears instantly. Has no effect if [animation] is [Animation.Disabled].
 * @property referenceLine Optional vertical reference line (e.g. a target or average value).
 * @property markers Persistent markers pinned to specific rows, drawn at all times regardless of
 *   touch (see [PersistentMarker]). A marker is anchored at the centre of its row, level with the
 *   value end of the row's longest bar. `PersistentMarker(dataIndex = -1)` is the idiomatic way to
 *   label the latest value — the bottom row. Empty (the default) draws none.
 * @property tooltipConfig Tooltip appearance configuration.
 * @property tooltipPosition Preferred tooltip placement ([TooltipPosition.AUTO] by default).
 * @property tooltipFormatter Lambda that formats the tooltip text for a tapped [GroupedHorizontalBarEntry].
 * @property visibleWindow Rolling "show last N" window; `null` (default) shows every point and
 *   changes nothing. As data is appended the window advances to the latest. Must be `>= 2`.
 */
@Stable
data class GroupedHorizontalBarChartConfig(
    val barWidthFraction: Float = 0.8f,
    val barSpacing: Float = 4f,
    val cornerRadius: CornerRadius = CornerRadius.Medium,
    val negativeValuesDrawMode: NegativeValuesDrawMode = NegativeValuesDrawMode.BELOW_AXIS,
    val animation: Animation = Animation.Default,
    val animateValueChanges: Boolean = false,
    val referenceLine: ReferenceLineConfig? = null,
    val markers: List<PersistentMarker> = emptyList(),
    val tooltipConfig: TooltipConfig? = null,
    val tooltipPosition: TooltipPosition = TooltipPosition.AUTO,
    val tooltipFormatter: (GroupedHorizontalBarEntry) -> String = { entry ->
        "${entry.barGroup.label}[${entry.barIndex}]: ${entry.barValue.toChartLabel()}"
    },
    val visibleWindow: Int? = null,
) {
    init {
        requireValidVisibleWindow(visibleWindow)
        require(barWidthFraction in 0f..1f) { "Bar width fraction must be between 0 and 1" }
        require(barSpacing >= 0f) { "Bar spacing must be non-negative" }
    }
}
