package com.himanshoe.charty.bar.config

import androidx.compose.ui.graphics.Color
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.CornerRadius
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.common.tooltip.TooltipPosition

/**
 * Configuration for [com.himanshoe.charty.bar.WaterfallChart]
 *
 * @param barWidthFraction Fraction of available space that each bar occupies (0.0f - 1.0f)
 * @param cornerRadius Corner radius for bar corners
 * @param positiveColor Color for positive value bars
 * @param negativeColor Color for negative value bars
 * @param animation Animation configuration (Disabled or Enabled with duration)
 * @param tooltipConfig Configuration for tooltip appearance when a bar is clicked
 * @param tooltipPosition Preferred position for tooltips (ABOVE, BELOW, or AUTO)
 */
data class WaterfallChartConfig(
    val barWidthFraction: Float = 0.6f,
    val cornerRadius: CornerRadius = CornerRadius.Medium,
    val positiveColor: ChartyColor = ChartyColor.Solid(Color.Yellow),
    val negativeColor: ChartyColor = ChartyColor.Solid(Color(0xFFD64C66)),
    val animation: Animation = Animation.Default,
    val tooltipConfig: TooltipConfig = TooltipConfig(),
    val tooltipPosition: TooltipPosition = TooltipPosition.AUTO,
    val tooltipFormatter: (BarData) -> String = { barData ->
        "${barData.label}: ${barData.value}"
    },
) {
    init {
        require(barWidthFraction in 0f..1f) { "Bar width fraction must be between 0 and 1" }
    }
}
