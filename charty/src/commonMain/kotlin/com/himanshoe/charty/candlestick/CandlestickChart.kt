package com.himanshoe.charty.candlestick

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMapIndexed
import com.himanshoe.charty.candlestick.config.CandlestickChartConfig
import com.himanshoe.charty.candlestick.data.CandleData
import com.himanshoe.charty.candlestick.ext.calculateMaxValue
import com.himanshoe.charty.candlestick.ext.calculateMinValue
import com.himanshoe.charty.candlestick.ext.getLabels
import com.himanshoe.charty.candlestick.internal.CandlestickChartConstants
import com.himanshoe.charty.candlestick.internal.CandlestickDrawParams
import com.himanshoe.charty.candlestick.internal.calculateOptimizedLabels
import com.himanshoe.charty.candlestick.internal.candlestickTooltipHandler
import com.himanshoe.charty.candlestick.internal.drawCandlestick
import com.himanshoe.charty.candlestick.internal.populateCandleBounds
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.accessibility.ChartAccessibility
import com.himanshoe.charty.common.accessibility.buildDataPointDescriptions
import com.himanshoe.charty.common.accessibility.generateCandlestickChartDescription
import com.himanshoe.charty.common.animation.rememberAnimatedValues
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.constants.ChartConstants
import com.himanshoe.charty.common.draw.drawPersistentMarkers
import com.himanshoe.charty.common.draw.formatMarkerValue
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.rememberCartesianChartState
import com.himanshoe.charty.common.streamingRender
import com.himanshoe.charty.common.theme.ChartyThemeDefaults
import com.himanshoe.charty.common.theme.orThemeDefault
import com.himanshoe.charty.common.tooltip.ChartTooltip
import com.himanshoe.charty.common.tooltip.ChartTooltipHost
import com.himanshoe.charty.common.tooltip.NoneTooltip
import com.himanshoe.charty.common.tooltip.drawTooltip
import com.himanshoe.charty.common.tooltip.isCanvas
import com.himanshoe.charty.common.tooltip.rememberTooltipManager
import com.himanshoe.charty.common.updateInteractionBounds

private const val CANDLE_PRICE_COUNT = 4

/**
 * A composable function that displays a candlestick chart.
 *
 * Example:
 * ```kotlin
 * CandlestickChart(
 *     data = {
 *         listOf(
 *             CandleData("Mon", open = 100f, high = 110f, low = 95f, close = 105f),
 *             CandleData("Tue", open = 105f, high = 112f, low = 102f, close = 103f),
 *         )
 *     },
 * )
 * ```
 *
 * @param data A lambda function that returns a list of [CandleData] to be displayed in the chart.
 * @param modifier The modifier to be applied to the chart.
 * @param emptyContent Optional custom placeholder shown when the data is empty; when
 *   `null` (default) a built-in "No data" state is used.
 * @param bullishColor The color for bullish candles.
 * @param bearishColor The color for bearish candles.
 * @param candlestickConfig The configuration for the candlestick's appearance.
 * @param scaffoldConfig The configuration for the chart's scaffold.
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 * @param tooltip How a tapped candle is presented: the built-in canvas bubble (the default), a custom
 *   Compose overlay via [ChartTooltip.compose], or [ChartTooltip.none] to disable it. A tap resolves
 *   to the whole candle — its column width, spanning high to low, so body and wicks share one hit
 *   area — and shows every price that candle encodes, not one of the four:
 *   [CandlestickChartConfig.tooltipFormatter] reads `Mon  O 100 H 110 L 95 C 105` by default.
 */
@Suppress("LongParameterList") // Public API surface; params get bundled in the next API pass.
@Composable
fun CandlestickChart(
    data: () -> List<CandleData>,
    modifier: Modifier = Modifier,
    emptyContent: (@Composable () -> Unit)? = null,
    bullishColor: ChartyColor = ChartyColor.Solid(Color(CandlestickChartConstants.DEFAULT_BULLISH_COLOR)),
    bearishColor: ChartyColor = ChartyColor.Solid(Color(CandlestickChartConstants.DEFAULT_BEARISH_COLOR)),
    candlestickConfig: CandlestickChartConfig = CandlestickChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartyThemeDefaults.scaffoldConfig(),
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
    tooltip: ChartTooltip<CandleData> = ChartTooltip.canvas(),
) {
    val fullDataList by remember(data) { derivedStateOf { data() } }
    if (fullDataList.isEmpty()) {
        ChartEmptyState(modifier = modifier, content = emptyContent)
        return
    }

    val chartState =
        rememberCartesianChartState(
            fullData = fullDataList,
            interactionConfig = interactionConfig,
            animation = candlestickConfig.animation,
            visibleWindow = candlestickConfig.visibleWindow,
            displayData = {
                rememberAnimatedCandleData(
                    dataList = it,
                    animation = candlestickConfig.animation,
                    enabled = candlestickConfig.animateValueChanges,
                )
            },
        ) { windowed, _ ->
            remember(windowed) {
                calculateMinValue(windowed) to calculateMaxValue(windowed)
            }
        }
    val dataList = chartState.data
    val displayList = chartState.displayData
    val minValue = chartState.minValue
    val maxValue = chartState.maxValue
    val xLabels =
        remember(dataList) {
            calculateOptimizedLabels(dataList.getLabels())
        }

    val animationProgress = chartState.animationProgress
    val textMeasurer = rememberTextMeasurer()
    val resolvedTooltipConfig = candlestickConfig.tooltipConfig.orThemeDefault()
    val tooltipManager = rememberTooltipManager<Rect, CandleData>(dataKey = dataList)
    val tapEnabled = tooltip !is NoneTooltip

    val chartModifier =
        buildInteractionModifier(
            base =
                modifier.candlestickTooltipHandler(
                    dataList = dataList,
                    candlestickConfig = candlestickConfig,
                    candleBounds = tooltipManager.bounds,
                    onTooltipUpdate = tooltipManager::updateTooltip,
                    enabled = tapEnabled,
                ),
            interactionConfig = interactionConfig,
            dataList = dataList,
        )

    Box(modifier = chartModifier) {
        ChartScaffold(
            accessibility =
                ChartAccessibility(
                    contentDescription =
                        interactionConfig.accessibilityDescription
                            ?: generateCandlestickChartDescription(fullDataList),
                    dataPointDescriptions =
                        buildDataPointDescriptions(
                            labels = dataList.fastMap { it.label },
                            values = dataList.fastMap { it.close },
                        ),
                ),
            streaming = interactionConfig.streamingRender(chartState.streaming),
            modifier = Modifier.fillMaxSize(),
            xLabels = xLabels,
            yAxisConfig =
                AxisConfig(
                    minValue = minValue,
                    maxValue = maxValue,
                    steps = ChartConstants.DEFAULT_AXIS_STEPS,
                    drawAxisAtZero = false,
                ),
            config = scaffoldConfig,
        ) { chartContext ->
            updateInteractionBounds(interactionConfig = interactionConfig, chartContext = chartContext)

            populateCandleBounds(
                chartContext = chartContext,
                dataList = dataList,
                enabled = tapEnabled,
                candleWidthFraction = candlestickConfig.candleWidthFraction,
                candleBounds = tooltipManager.bounds,
            )

            val bullishBrush = Brush.verticalGradient(bullishColor.value)
            val bearishBrush = Brush.verticalGradient(bearishColor.value)
            displayList.fastForEachIndexed { index, candle ->
                drawCandleBar(
                    index = index,
                    candle = candle,
                    dataList = displayList,
                    chartContext = chartContext,
                    candlestickConfig = candlestickConfig,
                    bullishBrush = bullishBrush,
                    bearishBrush = bearishBrush,
                    animationProgress = animationProgress.value,
                )
            }

            if (candlestickConfig.markers.isNotEmpty()) {
                drawPersistentMarkers(
                    chartContext = chartContext,
                    markers = candlestickConfig.markers,
                    pointPositions =
                        candleMarkerPositions(
                            chartContext = chartContext,
                            dataList = displayList,
                            candleWidthFraction = candlestickConfig.candleWidthFraction,
                        ),
                    valueLabelFor = { index -> formatMarkerValue(displayList[index].close) },
                    textMeasurer = textMeasurer,
                )
            }

            if (tooltip.isCanvas()) {
                tooltipManager.tooltipState?.let { state ->
                    drawTooltip(
                        tooltipState = state,
                        config = resolvedTooltipConfig,
                        textMeasurer = textMeasurer,
                        chartWidth = chartContext.right,
                        chartTop = chartContext.top,
                        chartBottom = chartContext.bottom,
                    )
                }
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

/**
 * Returns [dataList] with every candle's open, high, low, and close tweened toward their targets
 * whenever the data changes. All four prices share one progress, so a candle never renders an
 * inconsistent body or wick mid-transition. When [enabled] is `false` or [animation] is disabled the
 * list is returned unchanged.
 */
@Composable
private fun rememberAnimatedCandleData(
    dataList: List<CandleData>,
    animation: Animation,
    enabled: Boolean,
): List<CandleData> {
    val flattened =
        remember(dataList) {
            dataList.flatMap { listOf(it.open, it.high, it.low, it.close) }
        }
    val animatedValues =
        rememberAnimatedValues(
            targetValues = flattened,
            animation = animation,
            enabled = enabled,
        )
    return remember(dataList, animatedValues) {
        if (animatedValues.size != flattened.size) {
            dataList
        } else {
            dataList.fastMapIndexed { index, candle ->
                candle.copy(
                    open = animatedValues[index * CANDLE_PRICE_COUNT],
                    high = animatedValues[index * CANDLE_PRICE_COUNT + 1],
                    low = animatedValues[index * CANDLE_PRICE_COUNT + 2],
                    close = animatedValues[index * CANDLE_PRICE_COUNT + 3],
                )
            }
        }
    }
}

/**
 * Pixel positions of every drawn candle's close price, centred on the candle and ordered the same as
 * [dataList], so a marker index of `-1` lands on the rightmost candle.
 */
private fun candleMarkerPositions(
    chartContext: ChartContext,
    dataList: List<CandleData>,
    candleWidthFraction: Float,
): List<Offset> {
    val candleWidth = chartContext.calculateBarWidth(dataList.size, candleWidthFraction)
    return List(dataList.size) { index ->
        Offset(
            x =
                chartContext.calculateBarLeftPosition(index, dataList.size, candleWidthFraction) +
                    candleWidth / 2f,
            y = chartContext.convertValueToYPosition(dataList[index].close),
        )
    }
}

private fun DrawScope.drawCandleBar(
    index: Int,
    candle: CandleData,
    dataList: List<CandleData>,
    chartContext: ChartContext,
    candlestickConfig: CandlestickChartConfig,
    bullishBrush: Brush,
    bearishBrush: Brush,
    animationProgress: Float,
) {
    val candleX =
        chartContext.calculateBarLeftPosition(
            index,
            dataList.size,
            candlestickConfig.candleWidthFraction,
        )
    val candleWidth =
        chartContext.calculateBarWidth(
            dataList.size,
            candlestickConfig.candleWidthFraction,
        )
    val openY = chartContext.convertValueToYPosition(candle.open)
    val highY = chartContext.convertValueToYPosition(candle.high)
    val lowY = chartContext.convertValueToYPosition(candle.low)
    val closeY = chartContext.convertValueToYPosition(candle.close)
    val isBullish = candle.isBullish
    val candleBrush =
        if (isBullish) {
            bullishBrush
        } else {
            bearishBrush
        }

    val bodyTop = minOf(openY, closeY)
    val bodyBottom = maxOf(openY, closeY)
    val bodyHeight = bodyBottom - bodyTop
    val actualBodyHeight = maxOf(bodyHeight, candlestickConfig.minCandleBodyHeight)
    val actualBodyTop =
        if (bodyHeight < candlestickConfig.minCandleBodyHeight) {
            (openY + closeY - candlestickConfig.minCandleBodyHeight) / 2f
        } else {
            bodyTop
        }

    val animatedBodyTop =
        chartContext.bottom -
            (chartContext.bottom - actualBodyTop) * animationProgress
    val animatedBodyHeight = actualBodyHeight * animationProgress
    val animatedHighY =
        chartContext.bottom -
            (chartContext.bottom - highY) * animationProgress
    val animatedLowY =
        chartContext.bottom -
            (chartContext.bottom - lowY) * animationProgress

    drawCandlestick(
        CandlestickDrawParams(
            brush = candleBrush,
            centerX = candleX + candleWidth / 2f,
            bodyTop = animatedBodyTop,
            bodyHeight = animatedBodyHeight,
            bodyWidth = candleWidth,
            highY = animatedHighY,
            lowY = animatedLowY,
            wickWidth = candleWidth * candlestickConfig.wickWidthFraction,
            showWicks = candlestickConfig.showWicks,
            cornerRadius = candlestickConfig.cornerRadius.value,
        ),
    )
}
