package com.himanshoe.charty.common.axis

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import com.himanshoe.charty.common.ChartOrientation
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
 */
@OptIn(ExperimentalTextApi::class)
@Composable
internal fun DrawAxisAndLabels(
    xLabels: List<String>,
    yAxisConfig: AxisConfig,
    config: ChartScaffoldConfig,
    orientation: ChartOrientation,
    leftLabelRotation: LabelRotation,
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = config.labelTextStyle

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
                )

            ChartOrientation.HORIZONTAL ->
                drawHorizontalChartAxes(
                    xLabels = xLabels,
                    yAxisConfig = yAxisConfig,
                    config = config,
                    textMeasurer = textMeasurer,
                    labelStyle = labelStyle,
                    leftLabelRotation = leftLabelRotation,
                )
        }
    }
}
