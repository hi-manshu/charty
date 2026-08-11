package com.himanshoe.charty.line.config

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.ReferenceBandConfig
import com.himanshoe.charty.common.config.ReferenceLineConfig
import com.himanshoe.charty.common.gesture.ChartCrosshairConfig
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.common.tooltip.TooltipPosition
import com.himanshoe.charty.line.data.LineData

/**
 * Configuration for [com.himanshoe.charty.line.LineChart],
 * [com.himanshoe.charty.line.AreaChart], [com.himanshoe.charty.line.MultilineChart], and
 * [com.himanshoe.charty.line.StackedAreaChart] appearance and behaviour.
 * Not all properties apply to every chart type — see individual property docs for details.
 *
 * @property lineWidth Stroke width of the line in pixels.
 * @property showPoints Whether to draw circular markers at each data point.
 * @property pointRadius Radius of point markers in pixels. Only used when [showPoints] is `true`.
 * @property pointAlpha Opacity of point markers in the range `[0, 1]`.
 * @property strokeCap Cap style applied to both line ends and each segment.
 * @property smoothCurve When `true`, draws a cubic-bezier smooth curve instead of straight
 *   segments between data points.
 * @property negativeValuesDrawMode Controls how bars below zero are rendered.
 * @property animation Entry animation played when the chart first appears.
 * @property referenceLine Optional horizontal or vertical reference line drawn across the chart.
 * @property referenceBand Optional shaded value region drawn behind the data (see [ReferenceBandConfig]).
 * @property tooltipConfig Appearance of the tooltip bubble shown on tap. Only used when
 *   [crosshairConfig] is `null`; the crosshair has its own label config.
 * @property tooltipPosition Preferred placement of the tap tooltip relative to the tapped point.
 * @property tooltipFormatter Converts a [LineData] point into the string shown in the tooltip.
 * @property crosshairConfig When non-null, enables a draggable [ChartCrosshairConfig] that
 *   tracks the user's finger and snaps to the nearest data point. When set, it replaces the
 *   standard tap-to-tooltip interaction.
 * @property legendLabels Series labels shown in the legend below the chart. Pass an empty list
 *   (the default) to hide the legend. For [com.himanshoe.charty.line.MultilineChart] and
 *   [com.himanshoe.charty.line.StackedAreaChart], each entry maps to a series by index.
 * @property legendTextStyle Text style applied to legend labels.
 * @property showGradientFill When `true`, draws a semi-transparent gradient fill under each
 *   series line in [com.himanshoe.charty.line.MultilineChart]. Has no effect on other chart types.
 * @property gradientFillAlpha Opacity of the gradient fill in the range `[0, 1]`. Only used when
 *   [showGradientFill] is `true`.
 * @property fillAlpha Opacity of the filled area under the line in the range `[0, 1]`. Used by
 *   [com.himanshoe.charty.line.AreaChart] for its area fill. Has no effect on other chart types.
 */
@Stable
data class LineChartConfig(
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
    val tooltipConfig: TooltipConfig = TooltipConfig(),
    val tooltipPosition: TooltipPosition = TooltipPosition.AUTO,
    val tooltipFormatter: (LineData) -> String = { lineData ->
        "${lineData.label}: ${lineData.value}"
    },
    val crosshairConfig: ChartCrosshairConfig? = null,
    val legendLabels: List<String> = emptyList(),
    val legendTextStyle: TextStyle = TextStyle(fontSize = 12.sp, color = Color.Unspecified),
    val showGradientFill: Boolean = false,
    val gradientFillAlpha: Float = 0.3f,
    val fillAlpha: Float = 0.3f,
) {
    init {
        require(lineWidth > 0) { "Line width must be greater than 0" }
        require(pointRadius > 0) { "Point radius must be greater than 0" }
        require(pointAlpha in 0f..1f) { "Point alpha must be between 0 and 1" }
        require(gradientFillAlpha in 0f..1f) { "gradientFillAlpha must be between 0 and 1" }
        require(fillAlpha in 0f..1f) { "fillAlpha must be between 0 and 1" }
    }
}
