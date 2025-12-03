package com.himanshoe.charty.bar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import com.himanshoe.charty.bar.config.ComparisonBarChartConfig
import com.himanshoe.charty.bar.config.ComparisonBarSegment
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.bar.ext.getLabels
import com.himanshoe.charty.bar.internal.bar.comparison.ComparisonBarDrawParams
import com.himanshoe.charty.bar.internal.bar.comparison.calculateComparisonBaselineY
import com.himanshoe.charty.bar.internal.bar.comparison.createComparisonAxisConfig
import com.himanshoe.charty.bar.internal.bar.comparison.createComparisonChartModifier
import com.himanshoe.charty.bar.internal.bar.comparison.drawComparisonBars
import com.himanshoe.charty.bar.internal.bar.comparison.drawComparisonReferenceLineIfNeeded
import com.himanshoe.charty.bar.internal.bar.comparison.drawComparisonTooltipIfNeeded
import com.himanshoe.charty.bar.internal.bar.comparison.rememberComparisonChartValues
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.tooltip.rememberTooltipManager

/**
 * A composable function that displays a comparison bar chart.
 *
 * A comparison bar chart, also known as a grouped bar chart, displays multiple data series side-by-side for each category.
 * It is ideal for comparing sub-categories or multiple metrics within each main category.
 *
 * @param data A lambda function that returns a list of [BarGroup], where each group contains multiple values to be compared.
 * @param modifier The modifier to be applied to the chart.
 * @param comparisonConfig The configuration for the comparison bar chart, such as the mode for drawing negative values, defined by a [ComparisonBarChartConfig].
 * @param scaffoldConfig The configuration for the chart's scaffold, including axes and labels, defined by a [ChartScaffoldConfig].
 * @param onBarClick A lambda function to be invoked when a bar segment is clicked, providing the corresponding [ComparisonBarSegment].
 *
 * @sample
 * ComparisonBarChart(
 *     data = {
 *         listOf(
 *             BarGroup("Q1", listOf(45f, 52f)),
 *             BarGroup("Q2", listOf(58f, 63f)),
 *             BarGroup("Q3", listOf(72f, 68f))
 *         )
 *     },
 *     colors = ChartyColor.Gradient(
 *         listOf(Color(0xFFE91E63), Color(0xFF2196F3))
 *     )
 * )
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun ComparisonBarChart(
    data: () -> List<BarGroup>,
    modifier: Modifier = Modifier,
    comparisonConfig: ComparisonBarChartConfig = ComparisonBarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    onBarClick: ((ComparisonBarSegment) -> Unit)? = null,
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Comparison bar chart data cannot be empty" }

    val (minValue, maxValue) = rememberComparisonChartValues(dataList)
    val isBelowAxisMode = comparisonConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS
    val tooltipManager = rememberTooltipManager<Rect, ComparisonBarSegment>()
    val textMeasurer = rememberTextMeasurer()

    val chartModifier = createComparisonChartModifier(
        modifier = modifier,
        onBarClick = onBarClick,
        dataList = dataList,
        comparisonConfig = comparisonConfig,
        barBounds = tooltipManager.bounds,
        onTooltipUpdate = tooltipManager::updateTooltip,
    )

    ChartScaffold(
        modifier = chartModifier,
        xLabels = dataList.getLabels(),
        yAxisConfig = createComparisonAxisConfig(minValue, maxValue, isBelowAxisMode),
        config = scaffoldConfig,
    ) { chartContext ->
        tooltipManager.clearBounds()
        val baselineY = calculateComparisonBaselineY(minValue, isBelowAxisMode, chartContext)

        drawComparisonBars(
            ComparisonBarDrawParams(
                dataList = dataList,
                chartContext = chartContext,
                comparisonConfig = comparisonConfig,
                baselineY = baselineY,
                onBarClick = onBarClick,
                barBounds = tooltipManager.bounds,
            ),
        )

        drawComparisonReferenceLineIfNeeded(comparisonConfig, chartContext, textMeasurer)
        drawComparisonTooltipIfNeeded(tooltipManager.tooltipState, comparisonConfig, textMeasurer, chartContext)
    }
}
