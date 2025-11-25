package com.himanshoe.charty.point.config

import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.ReferenceLineConfig
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.common.tooltip.TooltipPosition
import com.himanshoe.charty.point.data.PointData

/**
 * Configuration for Point Chart appearance and behavior
 *
 * @param pointRadius Radius of each point in pixels
 * @param pointAlpha Alpha (transparency) value for points (0.0f - 1.0f)
 * @param showLabels Whether to show data labels on points
 * @param negativeValuesDrawMode How to draw negative values (BELOW_AXIS or FROM_MIN_VALUE)
 * @param animation Animation configuration (Disabled or Enabled with duration)
 * @param referenceLine Optional reference line configuration for reusable target/avg line support
 * @param tooltipConfig Configuration for tooltip appearance when a point is clicked
 * @param tooltipPosition Preferred position for tooltips (ABOVE, BELOW, or AUTO)
 * @param tooltipFormatter Function to format tooltip content from PointData
 */
data class PointChartConfig(
    val pointRadius: Float = 8f,
    val pointAlpha: Float = 1f,
    val showLabels: Boolean = false,
    val negativeValuesDrawMode: NegativeValuesDrawMode = NegativeValuesDrawMode.BELOW_AXIS,
    val animation: Animation = Animation.Default,
    val referenceLine: ReferenceLineConfig? = null,
    val tooltipConfig: TooltipConfig = TooltipConfig(),
    val tooltipPosition: TooltipPosition = TooltipPosition.AUTO,
    val tooltipFormatter: (PointData) -> String = { pointData ->
        "${pointData.label}: ${pointData.value}"
    },
) {
    init {
        require(pointRadius > 0) { "Point radius must be greater than 0" }
        require(pointAlpha in 0f..1f) { "Point alpha must be between 0 and 1" }
    }
}
