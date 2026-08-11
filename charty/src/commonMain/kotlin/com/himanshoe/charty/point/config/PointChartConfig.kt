package com.himanshoe.charty.point.config

import androidx.compose.runtime.Stable
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.ReferenceBandConfig
import com.himanshoe.charty.common.config.ReferenceLineConfig
import com.himanshoe.charty.common.gesture.ChartCrosshairConfig
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.common.tooltip.TooltipPosition
import com.himanshoe.charty.point.data.PointData

/**
 * Configuration for Point Chart appearance and behavior
 *
 * @property pointRadius Radius of each point in pixels
 * @property pointAlpha Alpha (transparency) value for points (0.0f - 1.0f)
 * @property showLabels Whether to show data labels on points
 * @property negativeValuesDrawMode How to draw negative values (BELOW_AXIS or FROM_MIN_VALUE)
 * @property animation Animation configuration (Disabled or Enabled with duration)
 * @property referenceLine Optional reference line configuration for reusable target/avg line support
 * @property referenceBand Optional shaded value region drawn behind the points (see [ReferenceBandConfig])
 * @property tooltipConfig Configuration for tooltip appearance when a point is clicked
 * @property tooltipPosition Preferred position for tooltips (ABOVE, BELOW, or AUTO)
 * @property tooltipFormatter Function to format tooltip content from PointData
 * @property crosshairConfig When non-null, enables a draggable crosshair that tracks the user's finger
 *   and snaps to the nearest point. When set, it replaces the standard tap-to-tooltip interaction.
 *   Applies to [com.himanshoe.charty.point.PointChart] and [com.himanshoe.charty.point.BubbleChart].
 */
@Stable
data class PointChartConfig(
    val pointRadius: Float = 8f,
    val pointAlpha: Float = 1f,
    val showLabels: Boolean = false,
    val negativeValuesDrawMode: NegativeValuesDrawMode = NegativeValuesDrawMode.BELOW_AXIS,
    val animation: Animation = Animation.Default,
    val referenceLine: ReferenceLineConfig? = null,
    val referenceBand: ReferenceBandConfig? = null,
    val tooltipConfig: TooltipConfig = TooltipConfig(),
    val tooltipPosition: TooltipPosition = TooltipPosition.AUTO,
    val tooltipFormatter: (PointData) -> String = { pointData ->
        "${pointData.label}: ${pointData.value}"
    },
    val crosshairConfig: ChartCrosshairConfig? = null,
) {
    init {
        require(pointRadius > 0) { "Point radius must be greater than 0" }
        require(pointAlpha in 0f..1f) { "Point alpha must be between 0 and 1" }
    }
}
