package com.himanshoe.charty.bar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.rememberTextMeasurer
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.internal.bar.barchart.BarDrawParams
import com.himanshoe.charty.bar.internal.bar.barchart.calculateBarBaselineY
import com.himanshoe.charty.bar.internal.bar.barchart.createBarAxisConfig
import com.himanshoe.charty.bar.internal.bar.barchart.createBarChartModifier
import com.himanshoe.charty.bar.internal.bar.barchart.drawBarReferenceLineIfNeeded
import com.himanshoe.charty.bar.internal.bar.barchart.drawBarTooltipIfNeeded
import com.himanshoe.charty.bar.internal.bar.barchart.drawBars
import com.himanshoe.charty.bar.internal.bar.barchart.rememberBarValueRange
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.ChartyColors
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.accessibility.generateBarChartDescription
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.data.getLabels
import com.himanshoe.charty.common.dragTooltipActive
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.rememberChartDescription
import com.himanshoe.charty.common.rememberWindowedData
import com.himanshoe.charty.common.syncInteractionDataSizes
import com.himanshoe.charty.common.tooltip.ChartTooltipOverlay
import com.himanshoe.charty.common.tooltip.rememberTooltipManager
import com.himanshoe.charty.common.updateInteractionBounds

/**
 * A composable function that displays a bar chart.
 *
 * A bar chart uses rectangular bars to compare values across categories.
 * Supports positive and negative values, click interactions, tooltips,
 * data labels, and animated entry.
 *
 * Usage:
 * ```kotlin
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
 *         cornerRadius = CornerRadius.Large,
 *         showDataLabels = true,
 *         animation = Animation.Default,
 *     )
 * )
 * ```
 *
 * @param data A lambda returning the list of [BarData] points to display.
 * @param modifier Modifier applied to the chart.
 * @param color Color or gradient for the bars, defined by a [ChartyColor].
 * @param barConfig Appearance and behaviour configuration — width fraction, corner radius,
 *   animation, negative-value mode, data labels, tooltip, and reference line.
 * @param scaffoldConfig Styling for the chart scaffold — axes, grid, and labels.
 * @param onBarClick Called with the [BarData] of the tapped bar. Pass `null` to disable.
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 * @param tooltipContent An optional composable slot for rendering a custom tooltip layout. When
 *   provided, it replaces the default canvas tooltip and is invoked with the tapped [BarData].
 *   When `null`, the built-in tooltip configured via [barConfig] is drawn instead.
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
    tooltipContent: (@Composable (BarData) -> Unit)? = null,
) {
    val fullDataList = remember(data) { data() }
    require(fullDataList.isNotEmpty()) { "Bar chart data cannot be empty" }

    val dataList = rememberWindowedData(fullDataList, interactionConfig.viewPortState)

    val (minValue, maxValue) = rememberBarValueRange(dataList, barConfig.negativeValuesDrawMode)
    val isBelowAxisMode = barConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS
    val animationProgress = rememberChartAnimation(barConfig.animation)
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

    val clickModifier =
        createBarChartModifier(
            modifier = modifier,
            onBarClick = onBarClick,
            dataList = dataList,
            barConfig = barConfig,
            barBounds = tooltipManager.bounds,
            onTooltipUpdate = tooltipManager::updateTooltip,
            enableScrub = interactionConfig.dragTooltipActive,
        )

    val chartModifier =
        buildInteractionModifier(
            base = clickModifier,
            interactionConfig = interactionConfig,
            dataList = dataList,
        )

    Box(modifier = chartModifier) {
        ChartScaffold(
            modifier = Modifier.fillMaxSize(),
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
                BarDrawParams(
                    dataList = dataList,
                    chartContext = chartContext,
                    barConfig = barConfig,
                    baselineY = baselineY,
                    animationProgress = animationProgress.value,
                    color = color,
                    barBounds = tooltipManager.bounds,
                    textMeasurer = textMeasurer,
                    recordBounds = onBarClick != null || interactionConfig.dragTooltipActive,
                ),
            )

            drawBarReferenceLineIfNeeded(barConfig, chartContext, textMeasurer)
            if (tooltipContent == null) {
                drawBarTooltipIfNeeded(tooltipManager.tooltipState, barConfig, textMeasurer, chartContext)
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
