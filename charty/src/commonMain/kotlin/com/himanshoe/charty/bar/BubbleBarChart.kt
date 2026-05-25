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
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.data.getLabels
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.rememberWindowedData
import com.himanshoe.charty.common.syncInteractionDataSizes
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.updateInteractionBounds

/**
 * A composable function that displays a bubble bar chart.
 *
 * @param data A lambda function that returns a list of [BarData] to be displayed in the chart.
 * @param modifier The modifier to be applied to the chart.
 * @param color The color or color scheme for the bubbles.
 * @param bubbleConfig The configuration for the bubbles.
 * @param scaffoldConfig The configuration for the chart's scaffold.
 * @param onBarClick A lambda function invoked when a bar is clicked.
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
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
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
) {
    val fullDataList = remember(data) { data() }
    require(fullDataList.isNotEmpty()) { "Bubble bar chart data cannot be empty" }

    val dataList = rememberWindowedData(fullDataList, interactionConfig.viewPortState)

    val (minValue, maxValue) = rememberValueRange(dataList, bubbleConfig.negativeValuesDrawMode)
    val isBelowAxisMode = bubbleConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS

    val animationProgress = rememberChartAnimation(bubbleConfig.animation)
    var tooltipState by remember { mutableStateOf<TooltipState?>(null) }
    val barBounds = remember { mutableListOf<Pair<Rect, BarData>>() }
    val textMeasurer = rememberTextMeasurer()

    syncInteractionDataSizes(
        viewPortState = interactionConfig.viewPortState,
        brushSelectionState = interactionConfig.brushSelectionState,
        fullDataSize = fullDataList.size,
        dataSize = dataList.size,
    )

    val clickModifier = createBubbleChartModifier(
        modifier = modifier,
        onBarClick = onBarClick,
        dataList = dataList,
        bubbleConfig = bubbleConfig,
        barBounds = barBounds,
        onTooltipUpdate = { tooltipState = it },
    )

    val chartModifier = buildInteractionModifier(
        base = clickModifier,
        interactionConfig = interactionConfig,
        dataList = dataList,
    )

    ChartScaffold(
        modifier = chartModifier,
        xLabels = dataList.getLabels(),
        yAxisConfig = createAxisConfig(minValue, maxValue, isBelowAxisMode),
        config = scaffoldConfig,
        leftLabelRotation = scaffoldConfig.leftLabelRotation,
        contentDescription = interactionConfig.accessibilityDescription
            ?: "Bubble bar chart, ${fullDataList.size} data points.",
    ) { chartContext ->
        updateInteractionBounds(interactionConfig, chartContext)

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

        drawInteractionOverlays(interactionConfig, chartContext, dataList.size, textMeasurer)
    }
}
