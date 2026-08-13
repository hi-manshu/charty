package com.himanshoe.charty.bar.config

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.PersistentMarker
import com.himanshoe.charty.common.config.requireValidVisibleWindow
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.common.tooltip.TooltipPosition

/**
 * Represents a single segment within a mosaic bar that was clicked
 *
 * @property barGroup The entire bar group that contains this segment
 * @property segmentIndex The index of the clicked segment within the bar
 * @property segmentValue The value of the clicked segment
 * @property segmentPercentage The percentage this segment represents of the total
 */
@Immutable
data class MosaicBarSegment(
    val barGroup: BarGroup,
    val segmentIndex: Int,
    val segmentValue: Float,
    val segmentPercentage: Float,
)

/**
 * Configuration for [com.himanshoe.charty.bar.MosaicBarChart]
 *
 * @property barWidthFraction Fraction of available space that each bar occupies (0.0f - 1.0f)
 * @property animation Animation configuration (Disabled or Enabled with duration)
 * @property animateValueChanges When `true`, segment values tween from their previous values to the
 *   new ones whenever the data changes (using [animation]), so the 100% split glides to its new
 *   proportions; every intermediate frame is still a valid normalization. When `false` (default) new
 *   data appears instantly. Has no effect if [animation] is [Animation.Disabled].
 * @property markers Persistent markers pinned to specific bars, drawn at all times regardless of
 *   touch (see [PersistentMarker]). Every bar fills the full plot height, so a marker is anchored at
 *   the top-centre of its bar and its default label is the bar's raw total.
 *   `PersistentMarker(dataIndex = -1)` is the idiomatic way to label the latest value — the rightmost
 *   bar. Empty (the default) draws none.
 * @property tooltipConfig Configuration for tooltip appearance when a segment is clicked.
 *   `null`, the default, takes it from the ambient
 *   [ChartyTheme][com.himanshoe.charty.common.theme.ChartyTheme].
 * @property tooltipPosition Preferred position for tooltips (ABOVE, BELOW, or AUTO)
 * @property tooltipFormatter Converts a data point into the string shown in its tooltip.
 * @property visibleWindow Rolling "show last N" window; `null` (default) shows every point and
 *   changes nothing. As data is appended the window advances to the latest. Must be `>= 2`.
 */
@Stable
data class MosaicBarChartConfig(
    val barWidthFraction: Float = 0.9f,
    val animation: Animation = Animation.Default,
    val animateValueChanges: Boolean = false,
    val markers: List<PersistentMarker> = emptyList(),
    val tooltipConfig: TooltipConfig? = null,
    val tooltipPosition: TooltipPosition = TooltipPosition.AUTO,
    val tooltipFormatter: (MosaicBarSegment) -> String = { segment ->
        "${segment.barGroup.label} [${segment.segmentIndex}]: ${segment.segmentPercentage.toInt()}%"
    },
    val visibleWindow: Int? = null,
) {
    init {
        requireValidVisibleWindow(visibleWindow)
        require(barWidthFraction in 0f..1f) { "Bar width fraction must be between 0 and 1" }
    }
}
