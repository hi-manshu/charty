package com.himanshoe.charty.bar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.rememberTextMeasurer
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.internal.bar.barchart.calculateBarBaselineY
import com.himanshoe.charty.bar.internal.bar.barchart.createBarAxisConfig
import com.himanshoe.charty.bar.internal.bar.barchart.createBarChartModifier
import com.himanshoe.charty.bar.internal.bar.barchart.drawBarReferenceLineIfNeeded
import com.himanshoe.charty.bar.internal.bar.barchart.drawBarTooltipIfNeeded
import com.himanshoe.charty.bar.internal.bar.barchart.drawBars
import com.himanshoe.charty.bar.internal.bar.barchart.rememberBarValueRange
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.ChartyColors
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.accessibility.generateBarChartDescription
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.data.getLabels
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.rememberChartDescription
import com.himanshoe.charty.common.rememberWindowedData
import com.himanshoe.charty.common.syncInteractionDataSizes
import com.himanshoe.charty.common.tooltip.rememberTooltipManager
import com.himanshoe.charty.common.updateInteractionBounds

/**
 * A composable function that displays a bar chart.
 *
 * A bar chart is a graphical representation of data that uses rectangular bars to show comparisons between categories.
 * This composable allows for customization of the bar chart's appearance and behavior.
 *
 * @param data A lambda function that returns a list of [BarData] to be displayed in the chart.
 * @param modifier The modifier to be applied to the chart.
 * @param color The color or color scheme for the bars, defined by a [ChartyColor].
 * @param barConfig The configuration for the bars, such as width, corner radius, and animation, defined by a [BarChartConfig].
 * @param scaffoldConfig The configuration for the chart's scaffold, including axes and labels, defined by a [ChartScaffoldConfig].
 * @param onBarClick A lambda function to be invoked when a bar is clicked, providing the corresponding [BarData].
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 *
 * BarChart(
 *     data = {
 *         listOf(
 *             BarData("Jan", 100f),
 *             BarData("Feb", 150f),
 *             BarData("Mar", 120f)
 *         )
 *     },
 *     color = ChartyColor.Solid(ChartyColors.Blue),
 *     barConfig = BarChartConfig(
 *         barWidthFraction = 0.6f,
 *         roundedTopCorners = true,
 *         topCornerRadius = CornerRadius.Large,
 *         animation = Animation.Enabled()
 *     )
 * )
 */
@Composable
fun BarChart(
    data: () -> List<BarData>,
    modifier: Modifier = Modifier,
    color: ChartyColor = ChartyColor.Solid(ChartyColors.Blue),
    barConfig: BarChartConfig = BarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    onBarClick: ((BarData) -> Unit)? = null,
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
) {
    val fullDataList = remember(data) { data() }
    require(fullDataList.isNotEmpty()) { "Bar chart data cannot be empty" }

    val dataList = rememberWindowedData(fullDataList, interactionConfig.viewPortState)

    val (minValue, maxValue) = rememberBarValueRange(dataList, barConfig.negativeValuesDrawMode)
    val isBelowAxisMode = barConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS
    val animationProgress = rememberChartAnimation(barConfig.animation)
    val tooltipManager = rememberTooltipManager<Rect, BarData>()
    val textMeasurer = rememberTextMeasurer()

    val chartDescription = rememberChartDescription(fullDataList, interactionConfig.accessibilityDescription) {
        generateBarChartDescription(it, minValue, maxValue)
    }

    syncInteractionDataSizes(
        interactionConfig.viewPortState,
        interactionConfig.brushSelectionState,
        fullDataList.size,
        dataList.size,
    )

    val clickModifier = createBarChartModifier(
        modifier = modifier,
        onBarClick = onBarClick,
        dataList = dataList,
        barConfig = barConfig,
        barBounds = tooltipManager.bounds,
        onTooltipUpdate = tooltipManager::updateTooltip,
    )

    val chartModifier = buildInteractionModifier(
        base = clickModifier,
        interactionConfig = interactionConfig,
        dataList = dataList,
    )

    ChartScaffold(
        modifier = chartModifier,
        xLabels = dataList.getLabels(),
        yAxisConfig = createBarAxisConfig(minValue, maxValue, isBelowAxisMode),
        config = scaffoldConfig,
        leftLabelRotation = scaffoldConfig.leftLabelRotation,
        contentDescription = chartDescription,
    ) { chartContext ->
        updateInteractionBounds(interactionConfig, chartContext)

        tooltipManager.clearBounds()
        val baselineY = calculateBarBaselineY(minValue, isBelowAxisMode, chartContext)

        drawBars(
            dataList = dataList,
            chartContext = chartContext,
            barConfig = barConfig,
            baselineY = baselineY,
            animationProgress = animationProgress.value,
            color = color,
            onBarClick = onBarClick,
            barBounds = tooltipManager.bounds,
        )

        drawBarReferenceLineIfNeeded(barConfig, chartContext, textMeasurer)
        drawBarTooltipIfNeeded(tooltipManager.tooltipState, barConfig, textMeasurer, chartContext)

        drawInteractionOverlays(interactionConfig, chartContext, dataList.size, textMeasurer)
    }
}
