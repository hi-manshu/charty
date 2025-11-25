package com.himanshoe.charty.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.axis.DrawAxisAndLabels
import com.himanshoe.charty.common.axis.LabelRotation
import com.himanshoe.charty.common.config.ChartScaffoldConfig

/**
 * ChartScaffold - Clean and simple chart scaffold with axis and labels.
 *
 * All positioning is handled internally via ChartContext.
 * Supports both vertical and horizontal orientations.
 *
 * @param modifier Modifier for the chart
 * @param xLabels Labels for the X-axis
 * @param yAxisConfig Configuration for Y-axis values and range
 * @param config Scaffold configuration for styling
 * @param orientation Chart orientation (VERTICAL or HORIZONTAL)
 * @param leftLabelRotation Rotation for left-side labels (Y-axis labels in VERTICAL mode, category labels in HORIZONTAL mode). Default is LabelRotation.Straight. Use LabelRotation.Angle45Negative for -45-degree rotation.
 * @param content Drawing lambda that receives ChartContext for positioning
 */
@Composable
fun ChartScaffold(
    modifier: Modifier = Modifier,
    xLabels: List<String> = emptyList(),
    yAxisConfig: AxisConfig = AxisConfig(),
    config: ChartScaffoldConfig = ChartScaffoldConfig(),
    orientation: ChartOrientation = ChartOrientation.VERTICAL,
    leftLabelRotation: LabelRotation = LabelRotation.Straight,
    content: DrawScope.(ChartContext) -> Unit,
) {
    Box(modifier = modifier) {
        DrawAxisAndLabels(
            xLabels = xLabels,
            yAxisConfig = yAxisConfig,
            config = config,
            orientation = orientation,
            leftLabelRotation = leftLabelRotation,
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val leftPadding = if (config.showLabels) 60f else 20f
            val rightPadding = 20f
            val topPadding = 20f
            val bottomPadding = if (config.showLabels && xLabels.isNotEmpty()) 50f else 20f

            val chartContext =
                ChartContext(
                    left = leftPadding,
                    top = topPadding,
                    right = size.width - rightPadding,
                    bottom = size.height - bottomPadding,
                    width = size.width - leftPadding - rightPadding,
                    height = size.height - topPadding - bottomPadding,
                    minValue = yAxisConfig.minValue,
                    maxValue = yAxisConfig.maxValue,
                )

            content(chartContext)
        }
    }
}
