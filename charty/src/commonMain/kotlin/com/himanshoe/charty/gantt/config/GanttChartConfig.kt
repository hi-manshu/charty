package com.himanshoe.charty.gantt.config

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.CornerRadius
import com.himanshoe.charty.common.config.requireValidVisibleWindow
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.common.tooltip.TooltipPosition
import com.himanshoe.charty.common.util.toChartLabel

private const val DEFAULT_BAR_HEIGHT_FRACTION = 0.55f
private const val DEFAULT_INCOMPLETE_ALPHA = 0.35f
private const val DEFAULT_SEGMENT_LABEL_SP = 10

/**
 * Appearance and behaviour of a [com.himanshoe.charty.gantt.GanttChart].
 *
 * @property barHeightFraction Fraction of a row's height that its segments occupy (0f–1f).
 * @property cornerRadius Corner radius applied to each segment.
 * @property animation The entry animation; segments grow from their start value towards their end.
 * @property incompleteAlpha Opacity of the unfinished part of a segment that carries a
 *   [com.himanshoe.charty.gantt.data.GanttSegment.progress] (0f–1f). Segments without a progress
 *   value are always drawn solid.
 * @property showSegmentLabels Whether a segment's own label is drawn inside it when it fits.
 * @property segmentLabelStyle Text style for the in-segment labels.
 * @property valueFormatter Formats an axis value for the value-axis ticks and the tooltip. Override
 *   it to render the units the values actually carry — dates, durations, distances.
 * @property axisSteps Number of divisions on the value axis.
 * @property tooltipConfig Appearance of the built-in canvas tooltip.
 * @property tooltipPosition Preferred placement of the tooltip relative to the tapped segment.
 * @property visibleWindow Rolling "show last N rows" window; `null` (default) shows every row.
 *   Must be `>= 2`.
 */
@Stable
data class GanttChartConfig(
    val barHeightFraction: Float = DEFAULT_BAR_HEIGHT_FRACTION,
    val cornerRadius: CornerRadius = CornerRadius.Small,
    val animation: Animation = Animation.Default,
    val incompleteAlpha: Float = DEFAULT_INCOMPLETE_ALPHA,
    val showSegmentLabels: Boolean = false,
    val segmentLabelStyle: TextStyle =
        TextStyle(
            fontSize = DEFAULT_SEGMENT_LABEL_SP.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
        ),
    val valueFormatter: (Float) -> String = { value -> value.toChartLabel() },
    val axisSteps: Int = 5,
    val tooltipConfig: TooltipConfig? = null,
    val tooltipPosition: TooltipPosition = TooltipPosition.AUTO,
    val visibleWindow: Int? = null,
) {
    init {
        requireValidVisibleWindow(visibleWindow)
        require(barHeightFraction in 0f..1f) { "barHeightFraction must be between 0 and 1" }
        require(incompleteAlpha in 0f..1f) { "incompleteAlpha must be between 0 and 1" }
        require(axisSteps > 0) { "axisSteps must be positive" }
    }
}
