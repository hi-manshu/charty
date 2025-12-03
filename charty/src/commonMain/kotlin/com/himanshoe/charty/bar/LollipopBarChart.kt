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
 * Lollipop Bar Chart - vertical line with a circular head for each value.
 *
 * This chart is similar to a traditional bar chart but uses a thin "stem" and a
 * configurable circle at the value position, making it visually lighter for
 * category comparisons.
 *
 * Configuration options allow customizing stem thickness, circle radius and circle
 * color, along with standard bar width fraction and animation.
 *
 * @param data Lambda returning list of bar data to display
 * @param modifier Modifier for the chart
 * @param colors Color configuration for stems and circles
 * @param config Configuration for lollipop chart appearance
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
 * @param onBarClick Optional callback when a lollipop is clicked
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
