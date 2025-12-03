package com.himanshoe.charty.bar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.himanshoe.charty.common.tooltip.TooltipState

/**
 * Comparison Bar Chart - Display multiple bars per category for comparison
 *
 * A comparison bar chart displays multiple data series side-by-side for each category.
 * Perfect for comparing sub-categories or multiple metrics within each main category.
 * Formerly known as Grouped Bar Chart.
 *
 * Usage:
 * ```kotlin
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
 * ```
 *
 * @param data Lambda returning list of bar groups, each containing multiple values. Each BarGroup should specify its own colors via BarGroup.colors property
 * @param modifier Modifier for the chart
 * @param comparisonConfig Configuration for comparison chart behavior (e.g., negative values draw mode)
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
 * @param onBarClick Optional callback when a bar segment is clicked
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
    var tooltipState by remember { mutableStateOf<TooltipState?>(null) }
    val barBounds = remember { mutableListOf<Pair<Rect, ComparisonBarSegment>>() }
    val textMeasurer = rememberTextMeasurer()

    val chartModifier = createComparisonChartModifier(
        modifier = modifier,
        onBarClick = onBarClick,
        dataList = dataList,
        comparisonConfig = comparisonConfig,
        barBounds = barBounds,
        onTooltipUpdate = { tooltipState = it },
    )

    ChartScaffold(
        modifier = chartModifier,
        xLabels = dataList.getLabels(),
        yAxisConfig = createComparisonAxisConfig(minValue, maxValue, isBelowAxisMode),
        config = scaffoldConfig,
    ) { chartContext ->
        barBounds.clear()
        val baselineY = calculateComparisonBaselineY(minValue, isBelowAxisMode, chartContext)

        drawComparisonBars(
            ComparisonBarDrawParams(
                dataList = dataList,
                chartContext = chartContext,
                comparisonConfig = comparisonConfig,
                baselineY = baselineY,
                onBarClick = onBarClick,
                barBounds = barBounds,
            )
        )

        drawComparisonReferenceLineIfNeeded(comparisonConfig, chartContext, textMeasurer)
        drawComparisonTooltipIfNeeded(tooltipState, comparisonConfig, textMeasurer, chartContext)
    }
}

