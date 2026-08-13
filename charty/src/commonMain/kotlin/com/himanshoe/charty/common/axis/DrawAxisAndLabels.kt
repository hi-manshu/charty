package com.himanshoe.charty.common.axis

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.rememberTextMeasurer
import com.himanshoe.charty.color.withChartyColor
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.StreamingLayout
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.ext.drawHorizontalChartAxes
import com.himanshoe.charty.common.ext.drawVerticalChartAxes

/**
 * An internal composable function responsible for drawing the axis lines, grid lines, and labels for a chart.
 *
 * This function handles both vertical and horizontal chart orientations, delegating the drawing logic
 * to the appropriate extension function based on the specified [orientation].
 *
 * @param xLabels A list of strings for the x-axis labels.
 * @param yAxisConfig The configuration for the y-axis.
 * @param config The general configuration for the chart scaffold.
 * @param orientation The orientation of the chart, either [ChartOrientation.VERTICAL] or [ChartOrientation.HORIZONTAL].
 * @param leftLabelRotation The rotation for the labels on the left axis.
 * @param streamingLayout When non-null, category-axis labels are positioned by this sliding layout
 *   (and clipped to the plot) so they move in lockstep with the streaming series.
 */
@Composable
internal fun DrawAxisAndLabels(
    xLabels: List<String>,
    yAxisConfig: AxisConfig,
    config: ChartScaffoldConfig,
    orientation: ChartOrientation,
    leftLabelRotation: LabelRotation,
    leftPadding: Float,
    rightPadding: Float,
    secondaryYAxisConfig: AxisConfig? = null,
    streamingLayout: StreamingLayout? = null,
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = config.labelTextStyle.withChartyColor(config.labelTextColor)

    Canvas(modifier = Modifier.fillMaxSize()) {
        when (orientation) {
            ChartOrientation.VERTICAL ->
                drawVerticalChartAxes(
                    xLabels = xLabels,
                    yAxisConfig = yAxisConfig,
                    config = config,
                    textMeasurer = textMeasurer,
                    labelStyle = labelStyle,
                    leftLabelRotation = leftLabelRotation,
                    leftPadding = leftPadding,
                    rightPadding = rightPadding,
                    secondaryYAxisConfig = secondaryYAxisConfig,
                    streamingLayout = streamingLayout,
                )

            ChartOrientation.HORIZONTAL ->
                drawHorizontalChartAxes(
                    xLabels = xLabels,
                    yAxisConfig = yAxisConfig,
                    config = config,
                    textMeasurer = textMeasurer,
                    labelStyle = labelStyle,
                    leftLabelRotation = leftLabelRotation,
                    streamingLayout = streamingLayout,
                )
        }
    }
}
