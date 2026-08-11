package com.himanshoe.charty.candlestick

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.candlestick.config.CandlestickChartConfig
import com.himanshoe.charty.candlestick.data.CandleData
import com.himanshoe.charty.candlestick.ext.calculateMaxValue
import com.himanshoe.charty.candlestick.ext.calculateMinValue
import com.himanshoe.charty.candlestick.ext.getLabels
import com.himanshoe.charty.candlestick.internal.CandlestickChartConstants
import com.himanshoe.charty.candlestick.internal.CandlestickDrawParams
import com.himanshoe.charty.candlestick.internal.calculateOptimizedLabels
import com.himanshoe.charty.candlestick.internal.drawCandlestick
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.accessibility.generateCandlestickChartDescription
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.rememberWindowedData
import com.himanshoe.charty.common.syncInteractionDataSizes
import com.himanshoe.charty.common.theme.ChartyThemeDefaults
import com.himanshoe.charty.common.updateInteractionBounds

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
 */
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
) {
    val fullDataList = remember(data) { data() }
    if (fullDataList.isEmpty()) {
        ChartEmptyState(modifier = modifier, content = emptyContent)
        return
    }

    val dataList = rememberWindowedData(fullDataList = fullDataList, viewPortState = interactionConfig.viewPortState)

    val (minValue, maxValue) =
        remember(dataList) {
            calculateMinValue(dataList) to calculateMaxValue(dataList)
        }
    val xLabels =
        remember(dataList) {
            calculateOptimizedLabels(dataList.getLabels())
        }

    val animationProgress = rememberChartAnimation(candlestickConfig.animation)
    val textMeasurer = rememberTextMeasurer()

    syncInteractionDataSizes(
        viewPortState = interactionConfig.viewPortState,
        brushSelectionState = interactionConfig.brushSelectionState,
        fullDataSize = fullDataList.size,
        dataSize = dataList.size,
    )

    val chartModifier =
        buildInteractionModifier(
            base = modifier,
            interactionConfig = interactionConfig,
            dataList = dataList,
        )

    ChartScaffold(
        modifier = chartModifier,
        xLabels = xLabels,
        yAxisConfig =
            AxisConfig(
                minValue = minValue,
                maxValue = maxValue,
                steps = 6,
                drawAxisAtZero = false,
            ),
        config = scaffoldConfig,
        contentDescription =
            interactionConfig.accessibilityDescription
                ?: generateCandlestickChartDescription(fullDataList),
    ) { chartContext ->
        updateInteractionBounds(interactionConfig = interactionConfig, chartContext = chartContext)

        dataList.fastForEachIndexed { index, candle ->
            drawCandleBar(
                index = index,
                candle = candle,
                dataList = dataList,
                chartContext = chartContext,
                candlestickConfig = candlestickConfig,
                bullishColor = bullishColor,
                bearishColor = bearishColor,
                animationProgress = animationProgress.value,
            )
        }

        drawInteractionOverlays(
            interactionConfig = interactionConfig,
            chartContext = chartContext,
            totalItems = dataList.size,
            textMeasurer = textMeasurer,
        )
    }
}

private fun DrawScope.drawCandleBar(
    index: Int,
    candle: CandleData,
    dataList: List<CandleData>,
    chartContext: ChartContext,
    candlestickConfig: CandlestickChartConfig,
    bullishColor: ChartyColor,
    bearishColor: ChartyColor,
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
    val candleColor =
        if (isBullish) {
            bullishColor.value
        } else {
            bearishColor.value
        }

    val bodyTop = minOf(openY, closeY)
    val bodyBottom = maxOf(openY, closeY)
    val bodyHeight = bodyBottom - bodyTop
    val actualBodyHeight = maxOf(bodyHeight, candlestickConfig.minCandleBodyHeight)
    val actualBodyTop =
        if (bodyHeight < candlestickConfig.minCandleBodyHeight) {
            (openY + closeY - candlestickConfig.minCandleBodyHeight) / CandlestickChartConstants.TWO
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
            brush = Brush.verticalGradient(candleColor),
            centerX = candleX + candleWidth / CandlestickChartConstants.TWO,
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
