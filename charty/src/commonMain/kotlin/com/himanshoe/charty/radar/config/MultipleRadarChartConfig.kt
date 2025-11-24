package com.himanshoe.charty.radar.config

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

private const val DEFAULT_STAGGER_DELAY = 0.15f

/**
 * Configuration for Multiple Radar Chart with enhanced flexibility
 *
 * This config extends the basic RadarChartConfig to support multiple dataset scenarios
 * with additional features like legends, staggered animations, and dataset-specific styling.
 *
 * @param radarConfig Base radar chart configuration
 * @param showLegend Whether to show a legend for datasets
 * @param legendPosition Position of the legend (TOP, BOTTOM, LEFT, RIGHT)
 * @param legendTextStyle TextStyle for legend labels - allows full customization of text appearance
 * @param allowDatasetToggle Allow clicking datasets to toggle visibility
 * @param highlightOnHover Highlight dataset when hovering/clicking
 * @param staggerAnimation Animate datasets with a stagger effect
 * @param staggerDelay Delay multiplier between dataset animations (0.0 to 0.5)
 * @param datasetLineWidth Optional custom line width per dataset (null uses radarConfig.dataLineWidth)
 * @param datasetPointRadius Optional custom point radius per dataset (null uses radarConfig.dataPointRadius)
 * @param showPointInnerCircle Show inner white circle on data points for better visibility
 * @param blendMode Blend mode for overlapping areas (NORMAL, ADDITIVE, MULTIPLY)
 * @param maxDataSets Maximum number of datasets to display (0 = unlimited)
 */
data class MultipleRadarChartConfig(
    val radarConfig: RadarChartConfig = RadarChartConfig(),
    val showLegend: Boolean = false,
    val legendPosition: LegendPosition = LegendPosition.TOP,
    val legendTextStyle: TextStyle = TextStyle(fontSize = 12.sp),
    val allowDatasetToggle: Boolean = false,
    val highlightOnHover: Boolean = false,
    val staggerAnimation: Boolean = true,
    val staggerDelay: Float = DEFAULT_STAGGER_DELAY,
    val datasetLineWidth: Float? = null,
    val datasetPointRadius: Float? = null,
    val showPointInnerCircle: Boolean = true,
    val blendMode: BlendMode = BlendMode.NORMAL,
    val maxDataSets: Int = 0
) {
    init {
        require(staggerDelay in 0f..0.5f) { "Stagger delay must be between 0 and 0.5" }
        require(maxDataSets >= 0) { "Max datasets must be non-negative" }
        datasetLineWidth?.let { require(it > 0f) { "Dataset line width must be positive" } }
        datasetPointRadius?.let { require(it >= 0f) { "Dataset point radius must be non-negative" } }
    }
}

/**
 * Position for chart legend
 */
enum class LegendPosition {
    TOP,
    BOTTOM,
    LEFT,
    RIGHT,
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT
}

/**
 * Blend mode for overlapping dataset areas
 */
enum class BlendMode {
    /** Normal blending with alpha transparency */
    NORMAL,

    /** Additive blending - colors add together (brighter overlaps) */
    ADDITIVE,

    /** Multiply blending - colors multiply (darker overlaps) */
    MULTIPLY
}

