package com.himanshoe.charty.point.config

import com.himanshoe.charty.common.config.Animation

/**
 * Configuration for Point Chart appearance and behavior
 *
 * @param pointRadius Radius of each point in pixels
 * @param pointAlpha Alpha (transparency) value for points (0.0f - 1.0f)
 * @param showLabels Whether to show data labels on points
 * @param animation Animation configuration (Disabled or Enabled with duration)
 */
data class PointChartConfig(
    val pointRadius: Float = 8f,
    val pointAlpha: Float = 1f,
    val showLabels: Boolean = false,
    val animation: Animation = Animation.Default
) {
    init {
        require(pointRadius > 0) { "Point radius must be greater than 0" }
        require(pointAlpha in 0f..1f) { "Point alpha must be between 0 and 1" }
    }
}

