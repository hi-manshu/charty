package com.himanshoe.charty.bar.config

import com.himanshoe.charty.common.config.CornerRadius
import com.himanshoe.charty.common.config.ReferenceLineConfig

/**
 * Configuration for Comparison Bar Chart (formerly Grouped Bar Chart) appearance and behavior
 *
 * @param negativeValuesDrawMode How to draw negative values (BELOW_AXIS or FROM_MIN_VALUE)
 * @param cornerRadius Corner radius for bar corners (None, Small, Medium, Large, ExtraLarge, or Custom)
 */
data class ComparisonBarChartConfig(
    val negativeValuesDrawMode: NegativeValuesDrawMode = NegativeValuesDrawMode.BELOW_AXIS,
    val cornerRadius: CornerRadius = CornerRadius.Medium,
    val referenceLine: ReferenceLineConfig? = null
)
