package com.himanshoe.charty.bar.config

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.CornerRadius
import com.himanshoe.charty.common.config.PersistentMarker
import com.himanshoe.charty.common.config.requireValidVisibleWindow
import com.himanshoe.charty.common.gesture.ChartCrosshairConfig
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.common.tooltip.TooltipPosition
import com.himanshoe.charty.common.util.toChartLabel

private const val DEFAULT_NEGATIVE_COLOR = 0xFFD64C66

/**
 * Configuration for [com.himanshoe.charty.bar.WaterfallChart]
 *
 * @property barWidthFraction Fraction of available space that each bar occupies (0.0f - 1.0f)
 * @property cornerRadius Corner radius for bar corners
 * @property positiveColor Color for positive value bars
 * @property negativeColor Color for negative value bars
 * @property animation Animation configuration (Disabled or Enabled with duration)
 * @property animateValueChanges When `true`, each step's contribution tweens from its previous value
 *   to the new one whenever the data changes (using [animation]); the running totals are recomputed
 *   from the tweened steps, so every frame shows a consistent cumulative sequence. When `false`
 *   (default) new data appears instantly. Has no effect if [animation] is [Animation.Disabled].
 * @property markers Persistent markers pinned to specific steps, drawn at all times regardless of
 *   touch (see [PersistentMarker]). A marker is anchored at the top-centre of its floating bar and
 *   labelled with the total at that top edge — the higher of the running totals either side of the
 *   step.
 *   `PersistentMarker(dataIndex = -1)` is the idiomatic way to label the latest value — the rightmost
 *   bar. Empty (the default) draws none.
 * @property tooltipConfig Configuration for tooltip appearance when a bar is clicked
 * @property tooltipPosition Preferred position for tooltips (ABOVE, BELOW, or AUTO)
 * @property tooltipFormatter Converts a data point into the string shown in its tooltip.
 * @property crosshairConfig When non-null, enables a draggable [ChartCrosshairConfig] that tracks the
 *   user's finger and snaps by x to the nearest step, resting on the top edge of its floating bar.
 *   Usually set for you by passing a `crosshair` to the chart rather than configured here directly.
 * @property visibleWindow Rolling "show last N" window; `null` (default) shows every point and
 *   changes nothing. As data is appended the window advances to the latest. Must be `>= 2`.
 */
@Stable
data class WaterfallChartConfig(
    val barWidthFraction: Float = 0.6f,
    val cornerRadius: CornerRadius = CornerRadius.Medium,
    val positiveColor: ChartyColor = ChartyColor.Solid(Color.Yellow),
    val negativeColor: ChartyColor = ChartyColor.Solid(Color(DEFAULT_NEGATIVE_COLOR)),
    val animation: Animation = Animation.Default,
    val animateValueChanges: Boolean = false,
    val markers: List<PersistentMarker> = emptyList(),
    val tooltipConfig: TooltipConfig = TooltipConfig(),
    val tooltipPosition: TooltipPosition = TooltipPosition.AUTO,
    val tooltipFormatter: (BarData) -> String = { barData ->
        "${barData.label}: ${barData.value.toChartLabel()}"
    },
    val crosshairConfig: ChartCrosshairConfig? = null,
    val visibleWindow: Int? = null,
) {
    init {
        requireValidVisibleWindow(visibleWindow)
        require(barWidthFraction in 0f..1f) { "Bar width fraction must be between 0 and 1" }
    }
}
