package com.himanshoe.charty.bar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.data.getLabels
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.rememberWindowedData
import com.himanshoe.charty.common.syncInteractionDataSizes
import com.himanshoe.charty.common.theme.ChartyThemeDefaults
import com.himanshoe.charty.common.tooltip.ChartTooltip
import com.himanshoe.charty.common.tooltip.ChartTooltipHost
import com.himanshoe.charty.common.tooltip.isCanvas
import com.himanshoe.charty.common.tooltip.rememberTooltipManager
import com.himanshoe.charty.common.updateInteractionBounds

private const val DEFAULT_COLOR_HEX = 0xFF2196F3

/**
 * A composable function that displays a lollipop bar chart.
 *
 * Example:
 * ```kotlin
 * LollipopBarChart(
 *     data = {
 *         listOf(
 *             BarData(label = "Jan", value = 40f),
 *             BarData(label = "Feb", value = 65f),
 *             BarData(label = "Mar", value = 50f),
 *         )
 *     },
 *     colors = ChartyColor.Solid(ChartyColors.Blue),
 * )
 * ```
 *
 * @param data A lambda function that returns a list of [BarData] to be displayed in the chart.
 * @param modifier The modifier to be applied to the chart.
 * @param emptyContent Optional custom placeholder shown when the data is empty; when
 *   `null` (default) a built-in "No data" state is used.
 * @param colors The color or color scheme for the stems and circles.
 * @param config The configuration for the lollipop chart's appearance.
 * @param scaffoldConfig The configuration for the chart's scaffold.
 * @param onBarClick A lambda function invoked when a lollipop is clicked.
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 * @param tooltip How the tap tooltip is shown: ChartTooltip.canvas() (built-in bubble),
 *   ChartTooltip.compose { } (your Composable), or ChartTooltip.none().
 */
@Composable
fun LollipopBarChart(
    data: () -> List<BarData>,
    modifier: Modifier = Modifier,
    emptyContent: (@Composable () -> Unit)? = null,
    colors: ChartyColor = ChartyColor.Solid(Color(DEFAULT_COLOR_HEX)),
    config: LollipopBarChartConfig = LollipopBarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartyThemeDefaults.scaffoldConfig(),
    onBarClick: ((BarData) -> Unit)? = null,
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
    tooltip: ChartTooltip<BarData> = ChartTooltip.canvas(),
) {
    val fullDataList = remember(data) { data() }
    if (fullDataList.isEmpty()) {
        ChartEmptyState(modifier = modifier, content = emptyContent)
        return
    }

    val dataList = rememberWindowedData(fullDataList = fullDataList, viewPortState = interactionConfig.viewPortState)

    val (minValue, maxValue) = rememberLollipopValueRange(dataList)
    val animationProgress = rememberLollipopAnimation(config.animation)
    val tooltipManager = rememberTooltipManager<Offset, BarData>()
    val textMeasurer = rememberTextMeasurer()

    syncInteractionDataSizes(
        viewPortState = interactionConfig.viewPortState,
        brushSelectionState = interactionConfig.brushSelectionState,
        fullDataSize = fullDataList.size,
        dataSize = dataList.size,
    )

    val clickModifier =
        createLollipopChartModifier(
            modifier = modifier,
            onBarClick = onBarClick,
            dataList = dataList,
            config = config,
            lollipopBounds = tooltipManager.bounds,
            onTooltipUpdate = tooltipManager::updateTooltip,
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
            yAxisConfig = createAxisConfig(minValue, maxValue),
            config = scaffoldConfig,
            contentDescription =
                interactionConfig.accessibilityDescription
                    ?: "Lollipop chart, ${fullDataList.size} data points.",
        ) { chartContext ->
            updateInteractionBounds(interactionConfig = interactionConfig, chartContext = chartContext)

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
            if (tooltip.isCanvas()) {
                drawTooltipIfNeeded(tooltipManager.tooltipState, config, textMeasurer, chartContext)
            }

            drawInteractionOverlays(
                interactionConfig = interactionConfig,
                chartContext = chartContext,
                totalItems = dataList.size,
                textMeasurer = textMeasurer,
            )
        }

        ChartTooltipHost(
            tooltip = tooltip,
            item = tooltipManager.selectedItem,
            anchor = tooltipManager.tooltipState,
            modifier = Modifier.matchParentSize(),
        )
    }
}
