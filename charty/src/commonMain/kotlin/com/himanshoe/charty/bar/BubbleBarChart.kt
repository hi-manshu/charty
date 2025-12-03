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
 * Bubble Bar Chart - Display data as stacked bubbles in vertical columns
 *
 * A bubble bar chart presents categorical data with circles/bubbles stacked vertically.
 * The number and size of bubbles is proportional to the value they represent.
 * Ideal for creating visually appealing comparisons between discrete categories.
 *
 * Usage:
 * ```kotlin
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
 * ```
 *
 * @param data Lambda returning list of bar data to display
 * @param modifier Modifier for the chart
 * @param color Color configuration - Solid for uniform bubbles, Gradient for gradient effect
 * @param bubbleConfig Configuration for bubble appearance
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels (includes leftLabelRotation)
 * @param onBarClick Optional callback when a bar is clicked
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

