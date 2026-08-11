package com.himanshoe.charty.bar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.internal.bar.horizontal.HorizontalBarDrawParams
import com.himanshoe.charty.bar.internal.bar.horizontal.calculateHorizontalBaselineX
import com.himanshoe.charty.bar.internal.bar.horizontal.createHorizontalAxisConfig
import com.himanshoe.charty.bar.internal.bar.horizontal.drawHorizontalBars
import com.himanshoe.charty.bar.internal.bar.horizontal.drawHorizontalReferenceLineIfNeeded
import com.himanshoe.charty.bar.internal.bar.horizontal.drawHorizontalTooltipIfNeeded
import com.himanshoe.charty.bar.internal.bar.horizontal.rememberHorizontalAnimation
import com.himanshoe.charty.bar.internal.bar.horizontal.rememberHorizontalValueRange
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.accessibility.generateBarChartDescription
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.dragTooltipActive
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.gesture.createRectangularTooltipState
import com.himanshoe.charty.common.gesture.rectangularChartScrubHandler
import com.himanshoe.charty.common.rememberChartDescription
import com.himanshoe.charty.common.rememberWindowedData
import com.himanshoe.charty.common.syncInteractionDataSizes
import com.himanshoe.charty.common.theme.ChartyThemeDefaults
import com.himanshoe.charty.common.tooltip.ChartTooltipOverlay
import com.himanshoe.charty.common.tooltip.rememberTooltipManager
import com.himanshoe.charty.common.updateInteractionBounds

/**
 * A composable function that displays a horizontal bar chart.
 *
 * A horizontal bar chart presents categorical data with horizontal rectangular bars, where the
 * length of each bar is proportional to the value it represents. This type of chart is particularly
 * useful for comparing categories with long labels or when there are many categories to display.
 *
 * @param data A lambda function that returns a list of [BarData] to be displayed in the chart.
 * @param modifier The modifier to be applied to the chart.
 * @param color The color or color scheme for the bars, defined by a [ChartyColor].
 * @param barConfig The configuration for the bars, such as width and corner radius, defined by a
 *   [BarChartConfig].
 * @param scaffoldConfig The configuration for the chart's scaffold, including axes and labels,
 *   defined by a [ChartScaffoldConfig].
 * @param onBarClick A lambda function invoked when a bar is clicked, providing the corresponding
 *   [BarData].
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 * @param tooltipContent An optional composable slot for rendering a custom tooltip layout. When
 *   provided, it replaces the default canvas tooltip and is invoked with the tapped [BarData].
 *
 * Example usage:
 * ```kotlin
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
 * ```
 */
@Composable
fun HorizontalBarChart(
    data: () -> List<BarData>,
    modifier: Modifier = Modifier,
    color: ChartyColor = ChartyThemeDefaults.primaryColor(),
    barConfig: BarChartConfig = BarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartyThemeDefaults.scaffoldConfig(),
    onBarClick: ((BarData) -> Unit)? = null,
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
    tooltipContent: (@Composable (BarData) -> Unit)? = null,
) {
    val fullDataList = remember(data) { data() }
    require(fullDataList.isNotEmpty()) { "Horizontal bar chart data cannot be empty" }

    val dataList = rememberWindowedData(fullDataList, interactionConfig.viewPortState)

    val (minValue, maxValue) = rememberHorizontalValueRange(dataList, barConfig.negativeValuesDrawMode)
    val isBelowAxisMode = barConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS
    val drawAxisAtZero = minValue < 0f && maxValue > 0f && isBelowAxisMode

    val animationProgress = rememberHorizontalAnimation(barConfig.animation)
    val tooltipManager = rememberTooltipManager<Rect, BarData>()
    val textMeasurer = rememberTextMeasurer()

    val chartDescription =
        rememberChartDescription(fullDataList, interactionConfig.accessibilityDescription) {
            generateBarChartDescription(it, minValue, maxValue)
        }

    syncInteractionDataSizes(
        interactionConfig.viewPortState,
        interactionConfig.brushSelectionState,
        fullDataList.size,
        dataList.size,
    )

    val scrubModifier =
        if (interactionConfig.dragTooltipActive) {
            modifier.rectangularChartScrubHandler(
                dataList = dataList,
                bounds = tooltipManager.bounds,
                onTooltipStateChange = tooltipManager::updateTooltip,
                createTooltipContent = { barData, rect ->
                    createRectangularTooltipState(
                        content = barConfig.tooltipFormatter(barData),
                        rect = rect,
                        position = barConfig.tooltipPosition,
                    )
                },
            )
        } else {
            modifier
        }

    val chartModifier =
        buildInteractionModifier(
            base = scrubModifier,
            interactionConfig = interactionConfig,
            dataList = dataList,
        )

    Box(modifier = chartModifier) {
        ChartScaffold(
            modifier = Modifier.fillMaxSize(),
            xLabels = dataList.fastMap { it.label },
            yAxisConfig = createHorizontalAxisConfig(minValue, maxValue, drawAxisAtZero),
            config = scaffoldConfig,
            orientation = ChartOrientation.HORIZONTAL,
            contentDescription = chartDescription,
        ) { chartContext ->
            updateInteractionBounds(interactionConfig, chartContext)

            tooltipManager.clearBounds()
            val baselineX = calculateHorizontalBaselineX(drawAxisAtZero, minValue, maxValue, chartContext)

            drawHorizontalBars(
                HorizontalBarDrawParams(
                    dataList = dataList,
                    chartContext = chartContext,
                    barConfig = barConfig,
                    baselineX = baselineX,
                    animationProgress = animationProgress.value,
                    color = color,
                    isBelowAxisMode = isBelowAxisMode,
                    minValue = minValue,
                    maxValue = maxValue,
                    onBarClick = onBarClick,
                    onBarBoundCalculated = { bounds -> tooltipManager.bounds.add(bounds) },
                    textMeasurer = textMeasurer,
                    recordBounds = onBarClick != null || interactionConfig.dragTooltipActive,
                ),
            )

            drawHorizontalReferenceLineIfNeeded(barConfig, chartContext, textMeasurer)
            if (tooltipContent == null) {
                drawHorizontalTooltipIfNeeded(tooltipManager.tooltipState, barConfig, textMeasurer, chartContext)
            }

            drawInteractionOverlays(interactionConfig, chartContext, dataList.size, textMeasurer)
        }

        if (tooltipContent != null) {
            ChartTooltipOverlay(
                item = tooltipManager.selectedItem,
                anchor = tooltipManager.tooltipState,
                config = barConfig.tooltipConfig,
                modifier = Modifier.matchParentSize(),
                content = tooltipContent,
            )
        }
    }
}
