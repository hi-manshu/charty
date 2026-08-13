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
 * Represents a single segment within a stacked horizontal bar that was clicked.
 *
 * @property barGroup The entire bar group that contains this segment.
 * @property segmentIndex The index of the clicked segment within the bar.
 * @property segmentValue The value of the clicked segment.
 */
@Immutable
data class StackedHorizontalBarSegment(
    val barGroup: BarGroup,
    val segmentIndex: Int,
    val segmentValue: Float,
)

/**
 * Configuration for Stacked Horizontal Bar Chart appearance and behavior.
 *
 * @property barWidthFraction Fraction of the available row height that each bar occupies (0.0f – 1.0f).
 * @property barSpacing Spacing between bars in pixels. Must be non-negative.
 * @property rightCornerRadius Corner radius applied to the right (trailing) end of each stacked bar.
 * @property animation Animation configuration ([Animation.Disabled] or [Animation.Enabled] with duration).
 * @property animateValueChanges When `true`, every segment tweens from its previous value to the new
 *   one whenever the data changes (using [animation]), so a bar glides to its new composition as a
 *   unit; when `false` (default) new data appears instantly. Has no effect if [animation] is
 *   [Animation.Disabled].
 * @property referenceLine Optional configuration for a vertical reference line (e.g., a target or average line).
 * @property markers Persistent markers pinned to specific rows, drawn at all times regardless of
 *   touch (see [PersistentMarker]). A marker is anchored at the centre of its row's value end, level
 *   with the stacked total, which is also its default label. `PersistentMarker(dataIndex = -1)` is
 *   the idiomatic way to label the latest value — the bottom row. Empty (the default) draws none.
 * @property tooltipConfig Configuration for tooltip appearance when a segment is clicked.
 * @property tooltipPosition Preferred position for tooltips ([TooltipPosition.ABOVE], [TooltipPosition.BELOW], or [TooltipPosition.AUTO]).
 * @property tooltipFormatter Lambda that formats the tooltip label for a clicked [StackedHorizontalBarSegment].
 * @property visibleWindow Rolling "show last N" window; `null` (default) shows every point and
 *   changes nothing. As data is appended the window advances to the latest. Must be `>= 2`.
 */
@Stable
data class StackedHorizontalBarChartConfig(
    val barWidthFraction: Float = 0.6f,
    val barSpacing: Float = 0f,
    val rightCornerRadius: CornerRadius = CornerRadius.Medium,
    val animation: Animation = Animation.Default,
    val animateValueChanges: Boolean = false,
    val referenceLine: ReferenceLineConfig? = null,
    val markers: List<PersistentMarker> = emptyList(),
    val tooltipConfig: TooltipConfig = TooltipConfig(),
    val tooltipPosition: TooltipPosition = TooltipPosition.AUTO,
    val tooltipFormatter: (StackedHorizontalBarSegment) -> String = { segment ->
        "${segment.barGroup.label} [${segment.segmentIndex}]: ${segment.segmentValue.toChartLabel()}"
    },
    val visibleWindow: Int? = null,
) {
    init {
        requireValidVisibleWindow(visibleWindow)
        require(barWidthFraction in 0f..1f) { "Bar width fraction must be between 0 and 1" }
        require(barSpacing >= 0) { "Bar spacing must be non-negative" }
    }
}
