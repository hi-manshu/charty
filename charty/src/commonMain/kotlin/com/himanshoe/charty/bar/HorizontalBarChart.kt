package com.himanshoe.charty.bar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.internal.bar.horizontal.HorizontalBarDrawParams
import com.himanshoe.charty.bar.internal.bar.horizontal.calculateHorizontalAxisOffset
import com.himanshoe.charty.bar.internal.bar.horizontal.calculateHorizontalBaselineX
import com.himanshoe.charty.bar.internal.bar.horizontal.createHorizontalAxisConfig
import com.himanshoe.charty.bar.internal.bar.horizontal.drawHorizontalBars
import com.himanshoe.charty.bar.internal.bar.horizontal.drawHorizontalReferenceLineIfNeeded
import com.himanshoe.charty.bar.internal.bar.horizontal.drawHorizontalTooltipIfNeeded
import com.himanshoe.charty.bar.internal.bar.horizontal.rememberHorizontalAnimation
import com.himanshoe.charty.bar.internal.bar.horizontal.rememberHorizontalValueRange
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.ChartyColors
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.tooltip.rememberTooltipManager

/**
 * A composable function that displays a horizontal bar chart.
 *
 * A horizontal bar chart presents categorical data with horizontal rectangular bars, where the length of each bar is proportional to the value it represents.
 * This type of chart is particularly useful for comparing categories with long labels or when there are many categories to display.
 *
 * @param data A lambda function that returns a list of [BarData] to be displayed in the chart.
 * @param modifier The modifier to be applied to the chart.
 * @param color The color or color scheme for the bars, defined by a [ChartyColor].
 * @param barConfig The configuration for the bars, such as width and corner radius, defined by a [BarChartConfig].
 * @param scaffoldConfig The configuration for the chart's scaffold, including axes and labels, defined by a [ChartScaffoldConfig].
 * @param onBarClick A lambda function to be invoked when a bar is clicked, providing the corresponding [BarData].
 *
 * @sample
 * HorizontalBarChart(
 *     data = {
 *         listOf(
 *             BarData("Category A", 100f),
 *             BarData("Category B", 150f),
 *             BarData("Category C", 120f)
 *         )
 *     },
 *     color = ChartyColor.Solid(Color(0xFF2196F3)),
 *     barConfig = BarChartConfig(
 *         barWidthFraction = 0.6f,
 *         cornerRadius = CornerRadius.Large
 *     )
 * )
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun HorizontalBarChart(
    data: () -> List<BarData>,
    modifier: Modifier = Modifier,
    color: ChartyColor = ChartyColor.Solid(ChartyColors.Blue),
    barConfig: BarChartConfig = BarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    onBarClick: ((BarData) -> Unit)? = null,
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Horizontal bar chart data cannot be empty" }

    val (minValue, maxValue) = rememberHorizontalValueRange(dataList, barConfig.negativeValuesDrawMode)
    val isBelowAxisMode = barConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS
    val drawAxisAtZero = minValue < 0f && maxValue > 0f && isBelowAxisMode

    val animationProgress = rememberHorizontalAnimation(barConfig.animation)
    val tooltipManager = rememberTooltipManager<Rect, BarData>()
    val textMeasurer = rememberTextMeasurer()

    ChartScaffold(
        modifier = modifier,
        xLabels = dataList.map { it.label },
        yAxisConfig = createHorizontalAxisConfig(minValue, maxValue, drawAxisAtZero),
        config = scaffoldConfig,
        orientation = ChartOrientation.HORIZONTAL,
    ) { chartContext ->
        tooltipManager.clearBounds()
        val axisOffset = calculateHorizontalAxisOffset(scaffoldConfig)
        val baselineX = calculateHorizontalBaselineX(drawAxisAtZero, minValue, maxValue, chartContext, axisOffset)

        drawHorizontalBars(
            HorizontalBarDrawParams(
                dataList = dataList,
                chartContext = chartContext,
                barConfig = barConfig,
                baselineX = baselineX,
                axisOffset = axisOffset,
                animationProgress = animationProgress.value,
                color = color,
                isBelowAxisMode = isBelowAxisMode,
                minValue = minValue,
                maxValue = maxValue,
                onBarClick = onBarClick,
                onBarBoundCalculated = { bounds -> tooltipManager.bounds.add(bounds) },
            ),
        )

        drawHorizontalReferenceLineIfNeeded(barConfig, chartContext, textMeasurer)
        drawHorizontalTooltipIfNeeded(tooltipManager.tooltipState, barConfig, textMeasurer, chartContext)
    }
}
