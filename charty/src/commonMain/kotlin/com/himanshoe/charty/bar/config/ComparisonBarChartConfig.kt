package com.himanshoe.charty.bar.config

import androidx.compose.runtime.Stable
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.CornerRadius
import com.himanshoe.charty.common.config.ReferenceLineConfig
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.common.tooltip.TooltipPosition

/**
 * Represents a single bar in a comparison chart that was clicked
 *
 * @property barGroup The bar group that contains this bar
 * @property barIndex The index of the clicked bar within the group
 * @property barValue The value of the clicked bar
 */
@Stable
data class ComparisonBarSegment(
    val barGroup: BarGroup,
    val barIndex: Int,
    val barValue: Float,
)

/**
 * Configuration for Comparison Bar Chart (formerly Grouped Bar Chart) appearance and behavior
 *
 * @property negativeValuesDrawMode How to draw negative values (BELOW_AXIS or FROM_MIN_VALUE)
 * @property cornerRadius Corner radius for bar corners (None, Small, Medium, Large, ExtraLarge, or Custom)
 * @property animation Animation configuration (Disabled or Enabled with duration)
 * @property referenceLine Optional reference line configuration
 * @property tooltipConfig Configuration for tooltip appearance when a bar is clicked
 * @property tooltipPosition Preferred position for tooltips (ABOVE, BELOW, or AUTO)
 * @property tooltipFormatter Converts a data point into the string shown in its tooltip.
 */
@Stable
data class ComparisonBarChartConfig(
    val negativeValuesDrawMode: NegativeValuesDrawMode = NegativeValuesDrawMode.BELOW_AXIS,
    val cornerRadius: CornerRadius = CornerRadius.Medium,
    val animation: Animation = Animation.Default,
    val referenceLine: ReferenceLineConfig? = null,
    val tooltipConfig: TooltipConfig = TooltipConfig(),
    val tooltipPosition: TooltipPosition = TooltipPosition.AUTO,
    val tooltipFormatter: (ComparisonBarSegment) -> String = { segment ->
        "${segment.barGroup.label} [${segment.barIndex}]: ${segment.barValue}"
    },
)
