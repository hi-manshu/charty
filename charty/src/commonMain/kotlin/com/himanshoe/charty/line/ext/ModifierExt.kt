package com.himanshoe.charty.line.ext

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.common.LabelConfig
import com.himanshoe.charty.common.getXLabelTextCharCount
import com.himanshoe.charty.common.modifiers.CommonDrawAxisLines
import com.himanshoe.charty.common.modifiers.CommonDrawHorizontalGridLines
import com.himanshoe.charty.common.modifiers.CommonDrawXAxisLabels
import com.himanshoe.charty.common.modifiers.CommonDrawYAxisLabels
import com.himanshoe.charty.line.config.LineChartColorConfig
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.model.LineData

/**
 * Extension function to draw axes and grid lines on a line chart.
 *
 * @receiver Modifier The modifier to which this function is applied.
 * @param data A list of LineData objects representing the data points for the chart.
 * @param colorConfig The color configuration for the chart.
 * @param chartConfig The configuration for the chart, including line widths and path effects.
 * @param textMeasurer The TextMeasurer used to measure and draw text.
 * @param labelConfig The configuration for the labels, including whether to show labels on the axes.
 * @param minValue The minimum value on the y-axis.
 * @param yRange The range of values on the y-axis.
 * @return A Modifier with the axes and grid lines drawn.
 */
internal fun Modifier.drawAxesAndGridLines(
    data: List<LineData>,
    colorConfig: LineChartColorConfig,
    chartConfig: LineChartConfig,
    textMeasurer: TextMeasurer,
    labelConfig: LabelConfig,
    minValue: Float,
    yRange: Float,
): Modifier {
    val xPositions = mutableListOf<Float>()
    val labelTexts = mutableListOf<String>()
    var localCanvasWidth = 0f
    var localCanvasHeight = 0f

    // Modifier to capture canvas size and calculate xPositions
    val layoutHelperModifier = Modifier.drawWithCache {
        localCanvasWidth = size.width
        localCanvasHeight = size.height
        val xStep = if (data.size > 1) localCanvasWidth / (data.size - 1) else 0f
        if (labelConfig.showXLabel) {
            data.forEachIndexed { index, _ ->
                xPositions.add(index * xStep)
                // xValue is expected to be a String or convertible to String for labels
                labelTexts.add(data[index].xValue.toString())
            }
        }
        onDrawBehind {} // No drawing here, just capturing size and calculating positions
    }

    return this
        .then(layoutHelperModifier) // Apply helper first to get canvas dimensions
        .CommonDrawAxisLines(
            axisColor = colorConfig.axisColor,
            strokeWidth = chartConfig.axisConfig.axisLineWidth,
            centerHorizontallyIfNegative = false
        )
        .CommonDrawHorizontalGridLines(
            gridLineColor = colorConfig.gridLineColor,
            strokeWidth = chartConfig.gridConfig.gridLineWidth,
            pathEffect = chartConfig.gridConfig.gridLinePathEffect,
            steps = 4, // Line chart has 4 divisions for Y axis grid
            centerHorizontallyIfNegative = false
        )
        .CommonDrawXAxisLabels(
            labelConfig = labelConfig,
            textMeasurer = textMeasurer,
            xPositions = xPositions,
            labelTexts = labelTexts,
            xAxisYPosition = localCanvasHeight, // Pass captured canvasHeight
            dataCount = data.size,
            fontSizeSelector = { canvasWidth, _, dataCount ->
                val factor = if (dataCount <= 13) 70 else 90
                (canvasWidth / factor).sp
            },
            labelFormatter = { text, _ -> text }, // No specific formatting, original text
            textCharCountProvider = { text, count ->
                // Here, 'text' is xValue.toString()
                labelConfig.getXLabelTextCharCount(xValue = text, displayDataCount = count)
            }
        )
        .CommonDrawYAxisLabels(
            labelConfig = labelConfig,
            textMeasurer = textMeasurer,
            minValue = minValue,
            maxValue = minValue + yRange,
            axisColor = labelConfig.textColor,
            steps = 4,
            dataCount = data.size,
            fontSizeSelector = { canvasWidth, _, _ -> (canvasWidth / 70).sp },
            labelFormatter = { value -> value.toString() }
        )
}
