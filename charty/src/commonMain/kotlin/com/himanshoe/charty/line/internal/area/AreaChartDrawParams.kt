package com.himanshoe.charty.line.internal.area

import androidx.compose.ui.geometry.Offset
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineData

/**
 * Parameters for drawing area chart.
 *
 * @property dataList List of line data points
 * @property pointPositions Calculated positions for each point
 * @property baselineY Y-coordinate of the baseline (bottom of filled area)
 * @property config Line chart configuration
 * @property color Color configuration for the area
 * @property fillAlpha Transparency of the filled area
 * @property animationProgress Current animation progress (0.0 to 1.0)
 * @property chartContext Chart drawing context
 * @property onBarBoundCalculated Callback for bound calculation
 */
internal data class AreaChartDrawParams(
    val dataList: List<LineData>,
    val pointPositions: List<Offset>,
    val baselineY: Float,
    val config: LineChartConfig,
    val color: ChartyColor,
    val fillAlpha: Float,
    val animationProgress: Float,
    val chartContext: ChartContext,
    val onBarBoundCalculated: (Pair<Offset, LineData>) -> Unit,
)

