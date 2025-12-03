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
import com.himanshoe.charty.bar.config.BubbleBarChartConfig
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.internal.bar.bubblebar.BubbleBarDrawParams
import com.himanshoe.charty.bar.internal.bar.bubblebar.calculateBaselineY
import com.himanshoe.charty.bar.internal.bar.bubblebar.createAxisConfig
import com.himanshoe.charty.bar.internal.bar.bubblebar.createBubbleChartModifier
import com.himanshoe.charty.bar.internal.bar.bubblebar.drawBubbleBars
import com.himanshoe.charty.bar.internal.bar.bubblebar.drawReferenceLineIfNeeded
import com.himanshoe.charty.bar.internal.bar.bubblebar.drawTooltipIfNeeded
import com.himanshoe.charty.bar.internal.bar.bubblebar.rememberValueRange
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.ChartyColors
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.data.getLabels
import com.himanshoe.charty.common.tooltip.TooltipState

/**
 * A composable function that displays a bubble bar chart.
 *
 * A bubble bar chart represents data as stacked bubbles in vertical columns.
 * The number and size of the bubbles are proportional to the value they represent, offering a visually distinct way to compare categorical data.
 *
 * @param data A lambda function that returns a list of [BarData] to be displayed in the chart.
 * @param modifier The modifier to be applied to the chart.
 * @param color The color or color scheme for the bubbles, defined by a [ChartyColor].
 * @param bubbleConfig The configuration for the bubbles, such as radius, spacing, and animation, defined by a [BubbleBarChartConfig].
 * @param scaffoldConfig The configuration for the chart's scaffold, including axes and labels, defined by a [ChartScaffoldConfig].
 * @param onBarClick A lambda function to be invoked when a bar (a column of bubbles) is clicked, providing the corresponding [BarData].
 *
 * BubbleBarChart(
 *     data = {
 *         listOf(
 *             BarData("Jan", 100f),
 *             BarData("Feb", 150f),
 *             BarData("Mar", 120f)
 *         )
 *     },
 *     color = ChartyColor.Solid(Color(0xFF2196F3)),
 *     bubbleConfig = BubbleBarChartConfig(
 *         barWidthFraction = 0.6f,
 *         bubbleRadius = 8.dp,
 *         bubbleSpacing = 4.dp,
 *         animation = Animation.Enabled()
 *     )
 * )
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun BubbleBarChart(
    data: () -> List<BarData>,
    modifier: Modifier = Modifier,
    color: ChartyColor = ChartyColor.Solid(ChartyColors.Blue),
    bubbleConfig: BubbleBarChartConfig = BubbleBarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    onBarClick: ((BarData) -> Unit)? = null,
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Bubble bar chart data cannot be empty" }

    val (minValue, maxValue) = rememberValueRange(dataList, bubbleConfig.negativeValuesDrawMode)
    val isBelowAxisMode = bubbleConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS

    val animationProgress = rememberChartAnimation(bubbleConfig.animation)
    var tooltipState by remember { mutableStateOf<TooltipState?>(null) }
    val barBounds = remember { mutableListOf<Pair<Rect, BarData>>() }
    val textMeasurer = rememberTextMeasurer()

    val chartModifier = createBubbleChartModifier(
        modifier = modifier,
        onBarClick = onBarClick,
        dataList = dataList,
        bubbleConfig = bubbleConfig,
        barBounds = barBounds,
        onTooltipUpdate = { tooltipState = it },
    )

    ChartScaffold(
        modifier = chartModifier,
        xLabels = dataList.getLabels(),
        yAxisConfig = createAxisConfig(minValue, maxValue, isBelowAxisMode),
        config = scaffoldConfig,
        leftLabelRotation = scaffoldConfig.leftLabelRotation,
    ) { chartContext ->
        barBounds.clear()
        val baselineY = calculateBaselineY(minValue, isBelowAxisMode, chartContext)

        val drawParams = BubbleBarDrawParams(
            dataList = dataList,
            chartContext = chartContext,
            bubbleConfig = bubbleConfig,
            baselineY = baselineY,
            animationProgress = animationProgress.value,
            color = color,
            onBarClick = onBarClick,
            barBounds = barBounds,
            textMeasurer = textMeasurer,
        )

        drawBubbleBars(drawParams)
        drawReferenceLineIfNeeded(drawParams)
        drawTooltipIfNeeded(drawParams, tooltipState)
    }
}

