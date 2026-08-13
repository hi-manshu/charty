package com.himanshoe.charty.diverging.config

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

private const val DEFAULT_BAR_WIDTH_FRACTION = 0.62f
private const val DEFAULT_CENTER_GAP_FRACTION = 0.02f
private const val MAX_CENTER_GAP_FRACTION = 0.5f
private const val DEFAULT_VALUE_LABEL_SP = 10

/**
 * How the two halves of a diverging chart are scaled against each other.
 */
enum class DivergingSideScaling {
    /**
     * Both halves share one scale, taken from the largest magnitude across **both** series, so a bar
     * of a given length means the same number whichever side it is on. This is the honest default:
     * the two halves stay directly comparable, which is the whole point of putting them back to back.
     */
    SHARED,

    /**
     * Each half is scaled to its own maximum, so both sides always reach the plot edge. It makes the
     * *shape* of a small series readable next to a much larger one, at the cost of comparability:
     * equal bar lengths no longer mean equal values. Because a single value axis can no longer
     * describe both halves, the chart hides its value-axis tick labels in this mode — turn on
     * [DivergingBarChartConfig.showValueLabels] so each bar states its own number.
     */
    INDEPENDENT,
}

/**
 * Appearance and behaviour of a [com.himanshoe.charty.diverging.DivergingBarChart].
 *
 * @property barWidthFraction Fraction of a category row's height that the bars occupy (0f–1f).
 * @property cornerRadius Corner radius applied to the outer end of each bar.
 * @property sideScaling Whether both halves share one scale or each is scaled to its own maximum;
 *   see [DivergingSideScaling].
 * @property centerGapFraction Fraction of the plot width left empty at the centre axis, splitting the
 *   two halves apart (0f–0.5f). `0f` makes the halves meet on the axis line.
 * @property animation The entry animation; bars grow outwards from the centre axis.
 * @property showValueLabels Whether each bar states its own magnitude just beyond its outer end.
 * @property valueFormatter Formats a magnitude for its value label and its tooltip.
 * @property valueLabelStyle Text style for the value labels.
 * @property tooltipConfig Appearance of the built-in canvas tooltip.
 * @property tooltipPosition Preferred placement of the tooltip relative to the tapped bar.
 * @property visibleWindow Rolling "show last N rows" window; `null` (default) shows every row.
 *   Must be `>= 2`.
 */
@Stable
data class DivergingBarChartConfig(
    val barWidthFraction: Float = DEFAULT_BAR_WIDTH_FRACTION,
    val cornerRadius: CornerRadius = CornerRadius.Small,
    val sideScaling: DivergingSideScaling = DivergingSideScaling.SHARED,
    val centerGapFraction: Float = DEFAULT_CENTER_GAP_FRACTION,
    val animation: Animation = Animation.Default,
    val showValueLabels: Boolean = false,
    val valueFormatter: (Float) -> String = { value -> value.toChartLabel() },
    val valueLabelStyle: TextStyle =
        TextStyle(
            fontSize = DEFAULT_VALUE_LABEL_SP.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.DarkGray,
        ),
    val tooltipConfig: TooltipConfig? = null,
    val tooltipPosition: TooltipPosition = TooltipPosition.AUTO,
    val visibleWindow: Int? = null,
) {
    init {
        requireValidVisibleWindow(visibleWindow)
        require(barWidthFraction in 0f..1f) { "barWidthFraction must be between 0 and 1" }
        require(centerGapFraction in 0f..MAX_CENTER_GAP_FRACTION) {
            "centerGapFraction must be between 0 and $MAX_CENTER_GAP_FRACTION"
        }
    }
}
