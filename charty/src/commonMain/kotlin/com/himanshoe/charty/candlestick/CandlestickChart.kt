@file:Suppress(
    "LongMethod",
    "LongParameterList",
    "FunctionNaming",
    "CyclomaticComplexMethod",
    "WildcardImport",
    "MagicNumber",
    "MaxLineLength",
    "ReturnCount",
    "UnusedImports",
)

package com.himanshoe.charty.candlestick

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.candlestick.config.CandlestickChartConfig
import com.himanshoe.charty.candlestick.data.CandleData
import com.himanshoe.charty.candlestick.ext.calculateMaxValue
import com.himanshoe.charty.candlestick.ext.calculateMinValue
import com.himanshoe.charty.candlestick.ext.getLabels
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.ChartScaffoldConfig

/**
 * Candlestick Chart - Display financial data as candlesticks
 *
 * A candlestick chart is commonly used in financial markets to show the open, high, low,
 * and close (OHLC) values for each time period. Each candlestick consists of a body
 * (representing the range between open and close) and wicks/shadows (showing the high
 * and low extremes).
 *
 * - **Bullish candles** (close > open): Typically shown in green/blue, indicating price increase
 * - **Bearish candles** (close < open): Typically shown in red/pink, indicating price decrease
 *
 * Usage:
 * ```kotlin
 * CandlestickChart(
 *     data = {
 *         listOf(
 *             CandleData(
 *                 label = "09:00",
 *                 open = 100f,
 *                 high = 110f,
 *                 low = 95f,
 *                 close = 105f
 *             ),
 *             CandleData(
 *                 label = "10:00",
 *                 open = 105f,
 *                 high = 115f,
 *                 low = 100f,
 *                 close = 112f
 *             )
 *         )
 *     },
 *     bullishColor = ChartyColor.Solid(Color(0xFF4CAF50)), // Green for up
 *     bearishColor = ChartyColor.Solid(Color(0xFFF44336)), // Red for down
 *     candlestickConfig = CandlestickChartConfig(
 *         candleWidthFraction = 0.7f,
 *         wickWidthFraction = 0.1f,
 *         showWicks = true,
 *         animation = Animation.Enabled(duration = 800)
 *     )
 * )
 * ```
 *
 * @param data Lambda returning list of candle data to display
 * @param modifier Modifier for the chart
 * @param bullishColor Color for bullish candles (close >= open)
 * @param bearishColor Color for bearish candles (close < open)
 * @param candlestickConfig Configuration for candlestick appearance
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
 */
@Composable
fun CandlestickChart(
    data: () -> List<CandleData>,
    modifier: Modifier = Modifier,
    bullishColor: ChartyColor = ChartyColor.Solid(Color(0xFF4CAF50)),
    bearishColor: ChartyColor = ChartyColor.Solid(Color(0xFFF44336)),
    candlestickConfig: CandlestickChartConfig = CandlestickChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Candlestick chart data cannot be empty" }
    val (minValue, maxValue) = remember(dataList) {
        calculateMinValue(dataList) to calculateMaxValue(dataList)
    }
    val xLabels = remember(dataList) {
        val allLabels = dataList.getLabels()
        if (allLabels.size > 10) {
            val indices =
                (0 until 5).map { i ->
                    (i * (allLabels.size - 1)) / 4
                }
            allLabels.mapIndexed { index, label ->
                if (index in indices) label else ""
            }
        } else {
            allLabels
        }
    }

    val animationProgress = remember {
        Animatable(if (candlestickConfig.animation is Animation.Enabled) 0f else 1f)
    }
    LaunchedEffect(candlestickConfig.animation) {
        if (candlestickConfig.animation is Animation.Enabled) {
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = candlestickConfig.animation.duration),
            )
        }
    }

    ChartScaffold(
        modifier = modifier,
        xLabels = xLabels,
        yAxisConfig =
            AxisConfig(
                minValue = minValue,
                maxValue = maxValue,
                steps = 6,
                drawAxisAtZero = false,
            ),
        config = scaffoldConfig,
    ) { chartContext ->
        dataList.fastForEachIndexed { index, candle ->
            val candleX = chartContext.calculateBarLeftPosition(
                index,
                dataList.size,
                candlestickConfig.candleWidthFraction,
            )
            val candleWidth = chartContext.calculateBarWidth(
                dataList.size,
                candlestickConfig.candleWidthFraction,
            )
            val openY = chartContext.convertValueToYPosition(candle.open)
            val highY = chartContext.convertValueToYPosition(candle.high)
            val lowY = chartContext.convertValueToYPosition(candle.low)
            val closeY = chartContext.convertValueToYPosition(candle.close)
            val isBullish = candle.isBullish
            val candleColor = if (isBullish) {
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
                    (openY + closeY - candlestickConfig.minCandleBodyHeight) / 2
                } else {
                    bodyTop
                }

            val animatedBodyTop = chartContext.bottom -
                (chartContext.bottom - actualBodyTop) * animationProgress.value
            val animatedBodyHeight = actualBodyHeight * animationProgress.value
            val animatedHighY = chartContext.bottom -
                (chartContext.bottom - highY) * animationProgress.value
            val animatedLowY = chartContext.bottom -
                (chartContext.bottom - lowY) * animationProgress.value

            // Draw the candlestick
            drawCandlestick(
                brush = Brush.verticalGradient(candleColor),
                centerX = candleX + candleWidth / 2,
                bodyTop = animatedBodyTop,
                bodyHeight = animatedBodyHeight,
                bodyWidth = candleWidth,
                highY = animatedHighY,
                lowY = animatedLowY,
                wickWidth = candleWidth * candlestickConfig.wickWidthFraction,
                showWicks = candlestickConfig.showWicks,
                cornerRadius = candlestickConfig.cornerRadius.value,
            )
        }
    }
}

/**
 * Helper function to draw a single candlestick with optional rounded corners
 */
private fun DrawScope.drawCandlestick(
    brush: Brush,
    centerX: Float,
    bodyTop: Float,
    bodyHeight: Float,
    bodyWidth: Float,
    highY: Float,
    lowY: Float,
    wickWidth: Float,
    showWicks: Boolean,
    cornerRadius: Float,
) {
    val bodyLeft = centerX - bodyWidth / 2
    val bodyBottom = bodyTop + bodyHeight

    if (showWicks) {
        if (highY < bodyTop) {
            drawLine(
                brush = brush,
                start = Offset(centerX, highY),
                end = Offset(centerX, bodyTop),
                strokeWidth = wickWidth,
            )
        }

        if (lowY > bodyBottom) {
            drawLine(
                brush = brush,
                start = Offset(centerX, bodyBottom),
                end = Offset(centerX, lowY),
                strokeWidth = wickWidth,
            )
        }
    }

    if (cornerRadius > 0f) {
        drawRoundRect(
            brush = brush,
            topLeft = Offset(bodyLeft, bodyTop),
            size = Size(bodyWidth, bodyHeight),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
        )
    } else {
        drawRect(
            brush = brush,
            topLeft = Offset(bodyLeft, bodyTop),
            size = Size(bodyWidth, bodyHeight),
        )
    }
}
