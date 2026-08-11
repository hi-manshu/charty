package com.himanshoe.charty.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.axis.DrawAxisAndLabels
import com.himanshoe.charty.common.axis.LabelRotation
import com.himanshoe.charty.common.axis.measureAxisGutter
import com.himanshoe.charty.common.config.ChartScaffoldConfig

private const val HORIZONTAL_LEFT_PADDING_WITH_LABELS = 100f
private const val LEFT_PADDING_WITHOUT_LABELS = 20f
private const val RIGHT_PADDING = 20f
private const val TOP_PADDING = 20f
private const val BOTTOM_PADDING_WITH_LABELS = 50f
private const val BOTTOM_PADDING_WITHOUT_LABELS = 20f

/**
 * A composable that provides a basic structure for creating charts.
 * It includes support for axes, labels, and a content area where the chart data can be drawn.
 *
 * @param modifier The modifier to be applied to the chart scaffold.
 * @param xLabels A list of labels for the x-axis.
 * @param yAxisConfig The configuration for the y-axis, including min and max values.
 * @param config The general configuration for the chart scaffold, such as whether to show labels.
 * @param orientation The orientation of the chart, either [ChartOrientation.VERTICAL] or [ChartOrientation.HORIZONTAL].
 * @param leftLabelRotation The rotation for the labels on the left axis.
 * @param contentDescription An accessibility description read by screen readers. When provided,
 *   it is attached to the chart's root composable via [Modifier.semantics]. Generate one
 *   automatically with helpers such as `generateLineChartDescription`.
 * @param secondaryYAxisConfig Optional second value axis rendered on the right edge with its own
 *   scale, for dual-axis charts. When non-null, the right gutter widens and a right axis is drawn;
 *   plot a series against it with [ChartContext.convertValueToYPosition] and this config's range.
 * @param content A lambda function that provides a [DrawScope] and [ChartContext] for drawing the chart content.
 */
@Composable
fun ChartScaffold(
    modifier: Modifier = Modifier,
    xLabels: List<String> = emptyList(),
    yAxisConfig: AxisConfig = AxisConfig(),
    config: ChartScaffoldConfig = ChartScaffoldConfig(),
    orientation: ChartOrientation = ChartOrientation.VERTICAL,
    leftLabelRotation: LabelRotation = LabelRotation.Straight,
    contentDescription: String? = null,
    secondaryYAxisConfig: AxisConfig? = null,
    content: DrawScope.(ChartContext) -> Unit,
) {
    val accessibilityModifier =
        if (contentDescription != null) {
            Modifier.semantics { this.contentDescription = contentDescription }
        } else {
            Modifier
        }
    val textMeasurer = rememberTextMeasurer()
    val leftPadding =
        remember(yAxisConfig, config.showLabels, config.labelTextStyle, orientation) {
            when {
                orientation == ChartOrientation.HORIZONTAL ->
                    if (config.showLabels) {
                        HORIZONTAL_LEFT_PADDING_WITH_LABELS
                    } else {
                        LEFT_PADDING_WITHOUT_LABELS
                    }

                else ->
                    measureAxisGutter(
                        axisConfig = yAxisConfig,
                        textMeasurer = textMeasurer,
                        labelStyle = config.labelTextStyle,
                        showLabels = config.showLabels,
                    )
            }
        }
    val rightPadding =
        remember(secondaryYAxisConfig, config.showLabels, config.labelTextStyle) {
            secondaryYAxisConfig?.let {
                measureAxisGutter(
                    axisConfig = it,
                    textMeasurer = textMeasurer,
                    labelStyle = config.labelTextStyle,
                    showLabels = config.showLabels,
                )
            } ?: RIGHT_PADDING
        }

    Box(modifier = modifier.then(accessibilityModifier)) {
        DrawAxisAndLabels(
            xLabels = xLabels,
            yAxisConfig = yAxisConfig,
            config = config,
            orientation = orientation,
            leftLabelRotation = leftLabelRotation,
            leftPadding = leftPadding,
            rightPadding = rightPadding,
            secondaryYAxisConfig = secondaryYAxisConfig,
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val bottomPadding =
                if (config.showLabels && xLabels.isNotEmpty()) {
                    BOTTOM_PADDING_WITH_LABELS
                } else {
                    BOTTOM_PADDING_WITHOUT_LABELS
                }

            val chartContext =
                ChartContext(
                    left = leftPadding,
                    top = TOP_PADDING,
                    right = size.width - rightPadding,
                    bottom = size.height - bottomPadding,
                    minValue = yAxisConfig.minValue,
                    maxValue = yAxisConfig.maxValue,
                )

            content(chartContext)
        }
    }
}
