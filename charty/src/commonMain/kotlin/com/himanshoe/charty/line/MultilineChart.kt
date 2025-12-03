package com.himanshoe.charty.line

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.rememberTextMeasurer
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.ChartyColors
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.tooltip.drawTooltip
import com.himanshoe.charty.common.util.calculateMaxValue
import com.himanshoe.charty.common.util.calculateMinValue
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineGroup
import com.himanshoe.charty.line.data.MultilinePoint
import com.himanshoe.charty.line.ext.getAllValues
import com.himanshoe.charty.line.ext.getLabels
import com.himanshoe.charty.line.internal.multiline.drawLineSeries
import com.himanshoe.charty.line.internal.multiline.multilineChartClickHandler

/**
 * A composable function that displays a multiline chart.
 *
 * A multiline chart shows multiple data series as separate lines on the same chart, making it ideal for comparing trends over time.
 * Each line starts smoothly from the (0,0) axis intersection point.
 *
 * @param data A lambda function that returns a list of [LineGroup], where each group contains values for all series at a specific x-position.
 * @param modifier The modifier to be applied to the chart.
 * @param colors The color or color scheme for the lines. A gradient is recommended to distinguish between multiple lines.
 * @param lineConfig The configuration for the lines' appearance and behavior, defined by a [LineChartConfig].
 * @param scaffoldConfig The configuration for the chart's scaffold, including axes and labels, defined by a [ChartScaffoldConfig].
 * @param onPointClick A lambda function to be invoked when a point on any line is clicked, providing the corresponding [MultilinePoint].
 *
 * MultilineChart(
 *     data = {
 *         listOf(
 *             LineGroup("Mon", listOf(20f, 35f, 15f)),
 *             LineGroup("Tue", listOf(45f, 28f, 38f)),
 *             LineGroup("Wed", listOf(30f, 52f, 25f)),
 *             LineGroup("Thu", listOf(70f, 40f, 55f))
 *         )
 *     },
 *     colors = ChartyColors.DefaultMultiline,
 *     lineConfig = LineChartConfig(
 *         lineWidth = 3f,
 *         showPoints = true,
 *         pointRadius = 6f,
 *         smoothCurve = true
 *     )
 * )
 */
@Composable
fun MultilineChart(
    data: () -> List<LineGroup>,
    modifier: Modifier = Modifier,
    colors: ChartyColor = ChartyColors.DefaultMultiline,
    lineConfig: LineChartConfig = LineChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    onPointClick: ((MultilinePoint) -> Unit)? = null,
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Multiline chart data cannot be empty" }

    val (minValue, maxValue, colorList) =
        remember(dataList, colors, lineConfig.negativeValuesDrawMode) {
            val allValues = dataList.getAllValues()
            Triple(
                calculateMinValue(allValues),
                calculateMaxValue(allValues),
                colors.value,
            )
        }

    val isBelowAxisMode =
        lineConfig.negativeValuesDrawMode == com.himanshoe.charty.bar.config.NegativeValuesDrawMode.BELOW_AXIS

    val animationProgress =
        remember {
            Animatable(if (lineConfig.animation is Animation.Enabled) 0f else 1f)
        }
    var tooltipState by remember { mutableStateOf<TooltipState?>(null) }
    val pointBounds = remember { mutableListOf<Pair<Offset, MultilinePoint>>() }
    val textMeasurer = rememberTextMeasurer()
    LaunchedEffect(lineConfig.animation) {
        if (lineConfig.animation is Animation.Enabled) {
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = lineConfig.animation.duration),
            )
        }
    }

    ChartScaffold(
        modifier = modifier.then(
            if (onPointClick != null) {
                Modifier.multilineChartClickHandler(
                    dataList = dataList,
                    lineConfig = lineConfig,
                    pointBounds = pointBounds,
                    onPointClick = onPointClick,
                    onTooltipStateChange = { tooltipState = it },
                )
            } else {
                Modifier
            },
        ),
        xLabels = dataList.getLabels(),
        yAxisConfig =
            AxisConfig(
                minValue = minValue,
                maxValue = maxValue,
                steps = 6,
                drawAxisAtZero = isBelowAxisMode,
            ),
        config = scaffoldConfig,
    ) { chartContext ->
        pointBounds.clear()
        val seriesCount = dataList.firstOrNull()?.values?.size ?: 0

        for (seriesIndex in 0 until seriesCount) {
            drawLineSeries(
                seriesIndex = seriesIndex,
                dataList = dataList,
                chartContext = chartContext,
                lineConfig = lineConfig,
                colorList = colorList,
                animationProgress = animationProgress.value,
                pointBounds = if (onPointClick != null) pointBounds else null,
            )
        }

        tooltipState?.let { state ->
            drawTooltip(
                tooltipState = state,
                config = lineConfig.tooltipConfig,
                textMeasurer = textMeasurer,
                chartWidth = chartContext.right,
                chartTop = chartContext.top,
                chartBottom = chartContext.bottom,
            )
        }
    }
}
