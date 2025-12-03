package com.himanshoe.charty.combo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.combo.config.ComboChartConfig
import com.himanshoe.charty.combo.data.ComboChartData
import com.himanshoe.charty.combo.ext.getAllValues
import com.himanshoe.charty.combo.ext.getLabels
import com.himanshoe.charty.combo.internal.ComboChartConstants
import com.himanshoe.charty.combo.internal.calculateLinePointPositions
import com.himanshoe.charty.combo.internal.comboChartClickHandler
import com.himanshoe.charty.combo.internal.drawComboBars
import com.himanshoe.charty.combo.internal.drawComboLine
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.draw.drawReferenceLine
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.tooltip.drawTooltip
import kotlin.math.max
import kotlin.math.min

/**
 * A composable function that displays a combo chart, combining a bar chart and a line chart.
 *
 * A combo chart is useful for visualizing two different data series on the same chart, allowing for easy comparison of trends and magnitudes.
 *
 * @param data A lambda function that returns a list of [ComboChartData], each containing a bar value and a line value.
 * @param modifier The modifier to be applied to the chart.
 * @param barColor The color or color scheme for the bars, defined by a [ChartyColor].
 * @param lineColor The color or color scheme for the line and its points, defined by a [ChartyColor].
 * @param comboConfig The configuration for the combo chart's appearance and behavior, defined by a [ComboChartConfig].
 * @param scaffoldConfig The configuration for the chart's scaffold, including axes and labels, defined by a [ChartScaffoldConfig].
 * @param onDataClick A lambda function to be invoked when a data point (either a bar or a line point) is clicked, providing the corresponding [ComboChartData].
 *
 * @sample
 * ComboChart(
 *     data = {
 *         listOf(
 *             ComboChartData("Jan", barValue = 100f, lineValue = 80f),
 *             ComboChartData("Feb", barValue = 150f, lineValue = 120f),
 *             ComboChartData("Mar", barValue = 120f, lineValue = 140f),
 *             ComboChartData("Apr", barValue = 180f, lineValue = 160f)
 *         )
 *     },
 *     barColor = ChartyColor.Solid(Color(0xFF2196F3)),
 *     lineColor = ChartyColor.Solid(Color(0xFFF44336)),
 *     comboConfig = ComboChartConfig(
 *         barWidthFraction = 0.6f,
 *         lineWidth = 3f,
 *         showPoints = true,
 *         animation = Animation.Enabled()
 *     )
 * )
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun ComboChart(
    data: () -> List<ComboChartData>,
    modifier: Modifier = Modifier,
    barColor: ChartyColor = ChartyColor.Solid(Color(ComboChartConstants.DEFAULT_BAR_COLOR)),
    lineColor: ChartyColor = ChartyColor.Solid(Color(ComboChartConstants.DEFAULT_LINE_COLOR)),
    comboConfig: ComboChartConfig = ComboChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    onDataClick: ((ComboChartData) -> Unit)? = null,
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Combo chart data cannot be empty" }

    val (minValue, maxValue) =
        remember(dataList, comboConfig.negativeValuesDrawMode) {
            val allValues = dataList.getAllValues()
            val calculatedMin = allValues.minOrNull() ?: 0f
            val calculatedMax = allValues.maxOrNull() ?: 0f
            val minVal = if (comboConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS) {
                min(calculatedMin, 0f)
            } else {
                calculatedMin
            }

            val maxVal = max(calculatedMax, if (minVal < 0f) 0f else calculatedMin)
            minVal to maxVal
        }

    val isBelowAxisMode = comboConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS
    val animationProgress = rememberChartAnimation(comboConfig.animation)
    var tooltipState by remember { mutableStateOf<TooltipState?>(null) }
    val dataBounds = remember { mutableListOf<Pair<Rect, ComboChartData>>() }
    val textMeasurer = rememberTextMeasurer()


    ChartScaffold(
        modifier = modifier.then(
            if (onDataClick != null) {
                Modifier.comboChartClickHandler(
                    dataList = dataList,
                    comboConfig = comboConfig,
                    dataBounds = dataBounds,
                    onDataClick = onDataClick,
                    onTooltipStateChange = { tooltipState = it },
                )
            } else {
                Modifier
            },
        ),
        xLabels = dataList.getLabels(),
        yAxisConfig = AxisConfig(
            minValue = minValue,
            maxValue = maxValue,
            steps = 6,
            drawAxisAtZero = isBelowAxisMode,
        ),
        config = scaffoldConfig,
    ) { chartContext ->
        dataBounds.clear()

        val baselineY = if (minValue < 0f && isBelowAxisMode) {
            chartContext.convertValueToYPosition(0f)
        } else {
            chartContext.bottom
        }

        drawComboBars(
            dataList = dataList,
            chartContext = chartContext,
            comboConfig = comboConfig,
            barColor = barColor,
            baselineY = baselineY,
            animationProgress = animationProgress.value,
            isBelowAxisMode = isBelowAxisMode,
            dataBounds = if (onDataClick != null) dataBounds else null,
        )

        val pointPositions = chartContext.calculateLinePointPositions(dataList)

        drawComboLine(
            pointPositions = pointPositions,
            lineColor = lineColor,
            comboConfig = comboConfig,
            animationProgress = animationProgress.value,
            dataList = dataList,
            dataBounds = if (onDataClick != null) dataBounds else null,
        )
        comboConfig.referenceLine?.let { referenceLineConfig ->
            drawReferenceLine(
                chartContext = chartContext,
                orientation = ChartOrientation.VERTICAL,
                config = referenceLineConfig,
                textMeasurer = textMeasurer,
            )
        }
        tooltipState?.let { state ->
            drawTooltip(
                tooltipState = state,
                config = comboConfig.tooltipConfig,
                textMeasurer = textMeasurer,
                chartWidth = chartContext.right,
                chartTop = chartContext.top,
                chartBottom = chartContext.bottom,
            )
        }
    }
}

