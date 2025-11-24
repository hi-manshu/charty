package com.himanshoe.charty.bar.config

import androidx.compose.ui.graphics.Color
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.CornerRadius

/**
 * Configuration for [com.himanshoe.charty.bar.WaterfallChart]
 */
data class WaterfallChartConfig(
    val barWidthFraction: Float = 0.6f,
    val cornerRadius: CornerRadius = CornerRadius.Medium,
    val positiveColor: ChartyColor = ChartyColor.Solid(Color.Yellow),
    val negativeColor: ChartyColor = ChartyColor.Solid(Color(0xFFD64C66)),
    val animation: Animation = Animation.Default
) {
    init {
        require(barWidthFraction in 0f..1f) { "Bar width fraction must be between 0 and 1" }
    }
}

