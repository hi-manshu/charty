package com.himanshoe.charty.bar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import com.himanshoe.charty.bar.config.LollipopBarChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.internal.bar.lollipop.createAxisConfig
import com.himanshoe.charty.bar.internal.bar.lollipop.createLollipopChartModifier
import com.himanshoe.charty.bar.internal.bar.lollipop.drawLollipops
import com.himanshoe.charty.bar.internal.bar.lollipop.drawTooltipHighlightIfNeeded
import com.himanshoe.charty.bar.internal.bar.lollipop.drawTooltipIfNeeded
import com.himanshoe.charty.bar.internal.bar.lollipop.rememberLollipopAnimation
import com.himanshoe.charty.bar.internal.bar.lollipop.rememberLollipopValueRange
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.data.getLabels
import com.himanshoe.charty.common.tooltip.rememberTooltipManager

private const val DEFAULT_COLOR_HEX = 0xFF2196F3

/**
 * A composable function that displays a lollipop bar chart.
 *
 * A lollipop bar chart is a variation of a traditional bar chart that uses a vertical line (stem) and a circular head to represent each value.
 * This design provides a visually lighter and more modern way to compare categories.
 *
 * @param data A lambda function that returns a list of [BarData] to be displayed in the chart.
 * @param modifier The modifier to be applied to the chart.
 * @param colors The color or color scheme for the stems and circles, defined by a [ChartyColor].
 * @param config The configuration for the lollipop chart's appearance, such as stem thickness and circle radius, defined by a [LollipopBarChartConfig].
 * @param scaffoldConfig The configuration for the chart's scaffold, including axes and labels, defined by a [ChartScaffoldConfig].
 * @param onBarClick A lambda function to be invoked when a lollipop is clicked, providing the corresponding [BarData].
 *
 * @sample
 * LollipopBarChart(
 *     data = {
 *         listOf(
 *             BarData("Category A", 100f),
 *             BarData("Category B", 150f),
 *             BarData("Category C", 120f)
 *         )
 *     },
 *     colors = ChartyColor.Solid(Color(0xFF2196F3)),
 *     config = LollipopBarChartConfig(
 *         stemThickness = 4.dp,
 *         circleRadius = 8.dp
 *     )
 * )
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun LollipopBarChart(
    data: () -> List<BarData>,
    modifier: Modifier = Modifier,
    colors: ChartyColor = ChartyColor.Solid(Color(DEFAULT_COLOR_HEX)),
    config: LollipopBarChartConfig = LollipopBarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    onBarClick: ((BarData) -> Unit)? = null,
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Lollipop bar chart data cannot be empty" }

    val (minValue, maxValue) = rememberLollipopValueRange(dataList)
    val animationProgress = rememberLollipopAnimation(config.animation)
    val tooltipManager = rememberTooltipManager<Offset, BarData>()
    val textMeasurer = rememberTextMeasurer()

    val chartModifier = createLollipopChartModifier(
        modifier = modifier,
        onBarClick = onBarClick,
        dataList = dataList,
        config = config,
        lollipopBounds = tooltipManager.bounds,
        onTooltipUpdate = tooltipManager::updateTooltip,
    )

    ChartScaffold(
        modifier = chartModifier,
        xLabels = dataList.getLabels(),
        yAxisConfig = createAxisConfig(minValue, maxValue),
        config = scaffoldConfig,
    ) { chartContext ->
        tooltipManager.clearBounds()

        drawLollipops(
            dataList = dataList,
            chartContext = chartContext,
            config = config,
            animationProgress = animationProgress.value,
            colors = colors,
            onBarClick = onBarClick,
            lollipopBounds = tooltipManager.bounds,
        )

        drawTooltipHighlightIfNeeded(tooltipManager.tooltipState, config, chartContext)
        drawTooltipIfNeeded(tooltipManager.tooltipState, config, textMeasurer, chartContext)
    }
}
