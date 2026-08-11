package com.himanshoe.charty.combo.config

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.StrokeCap
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.combo.data.ComboChartData
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.CornerRadius
import com.himanshoe.charty.common.config.ReferenceBandConfig
import com.himanshoe.charty.common.config.ReferenceLineConfig
import com.himanshoe.charty.common.gesture.ChartCrosshairConfig
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.common.tooltip.TooltipPosition

/**
 * Configuration for Combo Chart appearance and behavior
 *
 * @property barWidthFraction Fraction of available space that each bar occupies (0.0f - 1.0f)
 * @property barCornerRadius Corner radius for bar corners (None, Small, Medium, Large, ExtraLarge, or Custom)
 * @property lineWidth Width of the line stroke in pixels
 * @property showPoints Whether to show circular markers at data points on the line
 * @property pointRadius Radius of point markers in pixels (if showPoints is true)
 * @property pointAlpha Alpha (transparency) value for points (0.0f - 1.0f)
 * @property strokeCap The style of line endings (Butt, Round, or Square)
 * @property smoothCurve Whether to draw smooth curves instead of straight lines
 * @property negativeValuesDrawMode How to draw negative values (BELOW_AXIS or FROM_MIN_VALUE)
 * @property animation Animation configuration (Disabled or Enabled with duration)
 * @property referenceLine Optional reference line configuration to draw a shared target/avg line across the combo chart
 * @property referenceBand Optional shaded value region drawn behind the combo data (see [ReferenceBandConfig])
 * @property secondaryAxisForLine When `true`, the line is plotted against its own secondary Y axis on
 *   the right (scaled to the line values), while the bars keep the left axis (scaled to the bar
 *   values). Useful when bar and line series have very different magnitudes.
 * @property tooltipConfig Configuration for tooltip appearance when a data point is clicked
 * @property tooltipPosition Preferred position for tooltips (ABOVE, BELOW, or AUTO)
 * @property tooltipFormatter Converts a data point into the string shown in its tooltip.
 * @property crosshairConfig When non-null, enables a draggable crosshair snapping to line points.
 *   Enabling this replaces tap-to-tooltip interaction.
 */
@Stable
data class ComboChartConfig(
    val barWidthFraction: Float = 0.6f,
    val barCornerRadius: CornerRadius = CornerRadius.Medium,
    val lineWidth: Float = 3f,
    val showPoints: Boolean = true,
    val pointRadius: Float = 6f,
    val pointAlpha: Float = 1f,
    val strokeCap: StrokeCap = StrokeCap.Round,
    val smoothCurve: Boolean = false,
    val negativeValuesDrawMode: NegativeValuesDrawMode = NegativeValuesDrawMode.BELOW_AXIS,
    val animation: Animation = Animation.Default,
    val referenceLine: ReferenceLineConfig? = null,
    val referenceBand: ReferenceBandConfig? = null,
    val secondaryAxisForLine: Boolean = false,
    val tooltipConfig: TooltipConfig = TooltipConfig(),
    val tooltipPosition: TooltipPosition = TooltipPosition.AUTO,
    val tooltipFormatter: (ComboChartData) -> String = { data ->
        "${data.label}: Bar=${data.barValue}, Line=${data.lineValue}"
    },
    val crosshairConfig: ChartCrosshairConfig? = null,
) {
    init {
        require(barWidthFraction in 0f..1f) { "Bar width fraction must be between 0 and 1" }
        require(lineWidth > 0) { "Line width must be greater than 0" }
        require(pointRadius > 0) { "Point radius must be greater than 0" }
        require(pointAlpha in 0f..1f) { "Point alpha must be between 0 and 1" }
    }
}
