package com.himanshoe.charty.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.himanshoe.charty.common.accessibility.ChartAccessibility
import com.himanshoe.charty.common.accessibility.ChartDataPointSemantics
import com.himanshoe.charty.common.axis.AXIS_LABEL_MARGIN
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.axis.DrawAxisAndLabels
import com.himanshoe.charty.common.axis.measureAxisGutter
import com.himanshoe.charty.common.axis.measureMaxLabelWidth
import com.himanshoe.charty.common.axis.measureTrailingLabelOverhang
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.theme.ChartyThemeDefaults

private const val LEFT_PADDING_WITHOUT_LABELS = 20f
private const val RIGHT_PADDING = 20f
private const val TOP_PADDING = 20f
private const val BOTTOM_PADDING_WITH_LABELS = 50f
private const val BOTTOM_PADDING_WITHOUT_LABELS = 20f
private val STREAMING_OVERLAY_PADDING = 12.dp

/**
 * A composable that provides a basic structure for creating charts.
 * It includes support for axes, labels, and a content area where the chart data can be drawn.
 *
 * @param modifier The modifier to be applied to the chart scaffold.
 * @param xLabels A list of labels for the x-axis.
 * @param yAxisConfig The configuration for the y-axis, including min and max values.
 * @param config The general configuration for the chart scaffold, such as whether to show labels.
 * @param orientation The orientation of the chart, either [ChartOrientation.VERTICAL] or [ChartOrientation.HORIZONTAL].
 * @param accessibility Screen-reader description(s) for the chart: the whole-chart summary plus
 *   optional per-point descriptions for point-by-point traversal (see [ChartAccessibility]).
 * @param secondaryYAxisConfig Optional second value axis rendered on the right edge with its own
 *   scale, for dual-axis charts. When non-null, the right gutter widens and a right axis is drawn;
 *   plot a series against it with [ChartContext.convertValueToYPosition] and this config's range.
 * @param streaming When non-null, the chart is in rolling-window streaming mode: category positions
 *   (both the plotted series and the x-axis labels) are driven by [ChartStreamingRender.layout] and
 *   the plot is clipped to its bounds so points slide in and out at the edges, while
 *   [ChartStreamingRender.overlay] floats over the bottom centre of the plot, clear of the axis
 *   label gutter so it never covers a label. `null` (the default) draws statically.
 * @param content A lambda function that provides a [DrawScope] and [ChartContext] for drawing the chart content.
 */
@Composable
fun ChartScaffold(
    modifier: Modifier = Modifier,
    xLabels: List<String> = emptyList(),
    yAxisConfig: AxisConfig = AxisConfig(),
    config: ChartScaffoldConfig = ChartyThemeDefaults.scaffoldConfig(),
    orientation: ChartOrientation = ChartOrientation.VERTICAL,
    accessibility: ChartAccessibility = ChartAccessibility(),
    secondaryYAxisConfig: AxisConfig? = null,
    streaming: ChartStreamingRender? = null,
    content: DrawScope.(ChartContext) -> Unit,
) {
    val streamingLayout = streaming?.layout
    val description = accessibility.contentDescription
    val accessibilityModifier =
        if (description != null) {
            Modifier.semantics { this.contentDescription = description }
        } else {
            Modifier
        }
    val textMeasurer = rememberTextMeasurer()
    val leftPadding =
        remember(yAxisConfig, xLabels, config.showLabels, config.labelTextStyle, orientation) {
            measureLeftGutter(
                orientation = orientation,
                xLabels = xLabels,
                yAxisConfig = yAxisConfig,
                config = config,
                textMeasurer = textMeasurer,
            )
        }
    val rightPadding =
        remember(secondaryYAxisConfig, yAxisConfig, config.showLabels, config.labelTextStyle, orientation) {
            measureRightGutter(
                orientation = orientation,
                secondaryYAxisConfig = secondaryYAxisConfig,
                yAxisConfig = yAxisConfig,
                config = config,
                textMeasurer = textMeasurer,
            )
        }

    Box(modifier = modifier.then(accessibilityModifier)) {
        DrawAxisAndLabels(
            xLabels = xLabels,
            yAxisConfig = yAxisConfig,
            config = config,
            orientation = orientation,
            leftLabelRotation = config.leftLabelRotation,
            leftPadding = leftPadding,
            rightPadding = rightPadding,
            secondaryYAxisConfig = secondaryYAxisConfig,
            streamingLayout = streamingLayout,
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
                    streaming = streamingLayout,
                )

            if (streamingLayout == null) {
                content(chartContext)
            } else {
                clipToPlot(chartContext = chartContext, orientation = orientation) { content(chartContext) }
            }
        }

        ChartDataPointSemantics(
            descriptions = accessibility.dataPointDescriptions,
            orientation = orientation,
            modifier = Modifier.matchParentSize(),
        )

        streaming?.overlay?.let { overlay ->
            val axisGutter =
                with(LocalDensity.current) {
                    if (config.showLabels && xLabels.isNotEmpty()) {
                        BOTTOM_PADDING_WITH_LABELS.toDp()
                    } else {
                        BOTTOM_PADDING_WITHOUT_LABELS.toDp()
                    }
                }
            Box(
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = axisGutter + STREAMING_OVERLAY_PADDING),
            ) {
                overlay()
            }
        }
    }
}

/**
 * Clips [block] to the plot along the category axis so a streaming window's outgoing and incoming
 * items are hidden past the edges: horizontally for [ChartOrientation.VERTICAL] charts (which slide
 * left) and vertically for [ChartOrientation.HORIZONTAL] ones (which slide up). The perpendicular
 * axis is left unclipped so point markers and labels near the bounds are never cut.
 */
private fun DrawScope.clipToPlot(
    chartContext: ChartContext,
    orientation: ChartOrientation,
    block: DrawScope.() -> Unit,
) {
    if (orientation == ChartOrientation.HORIZONTAL) {
        clipRect(left = 0f, top = chartContext.top, right = size.width, bottom = chartContext.bottom) { block() }
    } else {
        clipRect(left = chartContext.left, top = 0f, right = chartContext.right, bottom = size.height) { block() }
    }
}

/**
 * The gutter on the left of the plot.
 *
 * A horizontal chart carries its categories there, so it is measured against those names; every other
 * orientation carries the value axis, so it is measured against the widest tick label.
 */
private fun measureLeftGutter(
    orientation: ChartOrientation,
    xLabels: List<String>,
    yAxisConfig: AxisConfig,
    config: ChartScaffoldConfig,
    textMeasurer: TextMeasurer,
): Float =
    if (orientation == ChartOrientation.HORIZONTAL) {
        if (config.showLabels) {
            (
                measureMaxLabelWidth(
                    labels = xLabels,
                    textMeasurer = textMeasurer,
                    labelStyle = config.labelTextStyle,
                ) + AXIS_LABEL_MARGIN * 2f
            ).coerceAtLeast(LEFT_PADDING_WITHOUT_LABELS)
        } else {
            LEFT_PADDING_WITHOUT_LABELS
        }
    } else {
        measureAxisGutter(
            axisConfig = yAxisConfig,
            textMeasurer = textMeasurer,
            labelStyle = config.labelTextStyle,
            showLabels = config.showLabels,
        )
    }

/**
 * The gutter on the right of the plot: a second value axis if there is one, otherwise room for the
 * overhang of the last label on a horizontal chart, otherwise a plain margin.
 */
private fun measureRightGutter(
    orientation: ChartOrientation,
    secondaryYAxisConfig: AxisConfig?,
    yAxisConfig: AxisConfig,
    config: ChartScaffoldConfig,
    textMeasurer: TextMeasurer,
): Float =
    when {
        secondaryYAxisConfig != null ->
            measureAxisGutter(
                axisConfig = secondaryYAxisConfig,
                textMeasurer = textMeasurer,
                labelStyle = config.labelTextStyle,
                showLabels = config.showLabels,
            )

        orientation == ChartOrientation.HORIZONTAL ->
            measureTrailingLabelOverhang(
                axisConfig = yAxisConfig,
                textMeasurer = textMeasurer,
                labelStyle = config.labelTextStyle,
                showLabels = config.showLabels,
            )

        else -> RIGHT_PADDING
    }
