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

private const val LEFT_PADDING_WITH_LABELS = 60f
private const val LEFT_PADDING_WITHOUT_LABELS = 20f
private const val RIGHT_PADDING = 20f
private const val TOP_PADDING = 20f
private const val BOTTOM_PADDING_WITH_LABELS = 50f
private const val BOTTOM_PADDING_WITHOUT_LABELS = 20f

/**
 * ChartScaffold - Provides a scaffold for charts with axis and labels.
 *
 * @param modifier Modifier for the chart
 * @param xLabels Labels for the X-axis
 * @param yAxisConfig Configuration for Y-axis values and range
 * @param config Scaffold configuration for styling
 * @param orientation Chart orientation
 * @param leftLabelRotation Rotation for left-side labels
 * @param content Drawing lambda that receives ChartContext
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
            val leftPadding = if (config.showLabels) {
                LEFT_PADDING_WITH_LABELS
            } else {
                LEFT_PADDING_WITHOUT_LABELS
            }
            val bottomPadding = if (config.showLabels && xLabels.isNotEmpty()) {
                BOTTOM_PADDING_WITH_LABELS
            } else {
                BOTTOM_PADDING_WITHOUT_LABELS
            }

            val chartContext = ChartContext(
                left = leftPadding,
                top = TOP_PADDING,
                right = size.width - RIGHT_PADDING,
                bottom = size.height - bottomPadding,
                width = size.width - leftPadding - RIGHT_PADDING,
                height = size.height - TOP_PADDING - bottomPadding,
                minValue = yAxisConfig.minValue,
                maxValue = yAxisConfig.maxValue,
            )

            content(chartContext)
        }
    }
}
