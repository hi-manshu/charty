package com.himanshoe.charty.bar.config

import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.ReferenceLineConfig
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.common.tooltip.TooltipPosition

/**
 * Configuration for Bubble Bar Chart appearance and behavior
 *
 * @param barWidthFraction Fraction of available space that each bar column occupies (0.0f - 1.0f)
 * @param bubbleRadius Radius of each bubble in pixels
 * @param bubbleSpacing Spacing between bubbles in pixels
 * @param negativeValuesDrawMode How to draw negative values (BELOW_AXIS or FROM_MIN_VALUE)
 * @param animation Animation configuration (Disabled or Enabled with duration)
 * @param referenceLine Optional reference line configuration (target/average line)
 * @param tooltipConfig Configuration for tooltip appearance when a bar is clicked
 * @param tooltipPosition Preferred position for tooltips (ABOVE, BELOW, or AUTO)
 */
data class BubbleBarChartConfig(
    val barWidthFraction: Float = 0.2f,
    val bubbleRadius: Float = 100f,
    val bubbleSpacing: Float = 8f,
    val negativeValuesDrawMode: NegativeValuesDrawMode = NegativeValuesDrawMode.BELOW_AXIS,
    val animation: Animation = Animation.Default,
    val referenceLine: ReferenceLineConfig? = null,
    val tooltipConfig: TooltipConfig = TooltipConfig(),
    val tooltipPosition: TooltipPosition = TooltipPosition.AUTO,
    val tooltipFormatter: (BarData) -> String = { barData ->
        "${barData.label}: ${barData.value}"
    },
) {
    init {
        require(barWidthFraction in 0f..1f) { "Bar width fraction must be between 0 and 1" }
        require(bubbleRadius > 0) { "Bubble radius must be positive" }
        require(bubbleSpacing >= 0) { "Bubble spacing must be non-negative" }
    }
}

