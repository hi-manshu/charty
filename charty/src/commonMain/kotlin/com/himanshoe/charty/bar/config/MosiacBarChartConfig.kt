package com.himanshoe.charty.bar.config

import com.himanshoe.charty.common.config.Animation

/**
 * Configuration for [com.himanshoe.charty.bar.MosiacBarChart]
 */
data class MosiacBarChartConfig(
    val barWidthFraction: Float = 0.9f,
    val animation: Animation = Animation.Default,
) {
    init {
        require(barWidthFraction in 0f..1f) { "Bar width fraction must be between 0 and 1" }
    }
}
