package com.himanshoe.charty.radar.config

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

private const val DEFAULT_STAGGER_DELAY = 0.15f

/**
 * Configuration for Multiple Radar Chart with enhanced flexibility
 *
 * This config extends the basic RadarChartConfig to support multiple dataset scenarios
 * with additional features like legends, staggered animations, and dataset-specific styling.
 *
 * @property radarConfig Base radar chart configuration
 * @property showLegend Whether to show a legend for datasets
 * @property legendPosition Position of the legend (TOP, BOTTOM, LEFT, RIGHT)
 * @property legendTextStyle TextStyle for legend labels - allows full customization of text appearance
 * @property staggerAnimation Animate datasets with a stagger effect
 * @property staggerDelay Delay multiplier between dataset animations (0.0 to 0.5)
 * @property datasetLineWidth Optional custom line width per dataset (null uses radarConfig.dataLineWidth)
 * @property datasetPointRadius Optional custom point radius per dataset (null uses radarConfig.dataPointRadius)
 * @property showPointInnerCircle Show inner white circle on data points for better visibility
 */
@Stable
data class MultipleRadarChartConfig(
    val radarConfig: RadarChartConfig = RadarChartConfig(),
    val showLegend: Boolean = false,
    val legendPosition: LegendPosition = LegendPosition.TOP,
    val legendTextStyle: TextStyle = TextStyle(fontSize = 12.sp),
    val staggerAnimation: Boolean = true,
    val staggerDelay: Float = DEFAULT_STAGGER_DELAY,
    val datasetLineWidth: Float? = null,
    val datasetPointRadius: Float? = null,
    val showPointInnerCircle: Boolean = true,
) {
    init {
        require(staggerDelay in 0f..0.5f) { "Stagger delay must be between 0 and 0.5" }
        datasetLineWidth?.let { require(it > 0f) { "Dataset line width must be positive" } }
        datasetPointRadius?.let { require(it >= 0f) { "Dataset point radius must be non-negative" } }
    }
}

/**
 * Where the legend sits relative to the chart.
 *
 * The four edges lay the entries out along that edge; the four corners stack them in place, which
 * keeps a long legend from stealing the chart's width.
 */
enum class LegendPosition {
    /** Centred above the chart, entries in a row. */
    TOP,

    /** Centred below the chart, entries in a row. */
    BOTTOM,

    /** Down the left side, entries in a column. */
    LEFT,

    /** Down the right side, entries in a column. */
    RIGHT,

    /** Stacked in the top-left corner, over the plot. */
    TOP_LEFT,

    /** Stacked in the top-right corner, over the plot. */
    TOP_RIGHT,

    /** Stacked in the bottom-left corner, over the plot. */
    BOTTOM_LEFT,

    /** Stacked in the bottom-right corner, over the plot. */
    BOTTOM_RIGHT,
}
