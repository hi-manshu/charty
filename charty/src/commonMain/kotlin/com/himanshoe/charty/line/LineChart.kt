package com.himanshoe.charty.line

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.himanshoe.charty.common.LabelConfig
import com.himanshoe.charty.common.TargetConfig
import com.himanshoe.charty.common.drawTargetLineIfNeeded
import com.himanshoe.charty.common.utils.calculateChartPaddings
import com.himanshoe.charty.common.utils.calculateValueRange
import com.himanshoe.charty.line.config.LineChartColorConfig
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.ext.drawAxesAndGridLines
import com.himanshoe.charty.line.ext.drawIndicatorLine
import com.himanshoe.charty.line.ext.drawLineChartTooltip
import com.himanshoe.charty.line.ext.drawLineCurve
import com.himanshoe.charty.line.ext.drawValueOnLine
import com.himanshoe.charty.line.model.LineData

/**
 * A composable function that renders a line chart.
 *
 * @param data A lambda function that returns a list of `LineData` representing the data points for the line chart.
 * @param modifier A optional `Modifier` for customizing the layout or drawing behavior of the LineChart.
 * @param smoothLineCurve A boolean indicating whether the line should be drawn with smooth curves.
 * @param showFilledArea A boolean indicating whether the area under the line should be filled.
 * @param showLineStroke A boolean indicating whether the line should be stroked.
 * @param colorConfig A `LineChartColorConfig` object for configuring the colors of the line and filled area.
 * @param labelConfig A `LabelConfig` object for configuring the labels on the chart.
 * @param chartConfig A `LineChartConfig` object for configuring the chart's appearance and behavior.
 *
 * @throws IllegalArgumentException if both `showFilledArea` and `showLineStroke` are false.
 */
@Composable
fun LineChart(
    data: () -> List<LineData>,
    modifier: Modifier = Modifier,
    smoothLineCurve: Boolean = true,
    showFilledArea: Boolean = false,
    showLineStroke: Boolean = true,
    showOnClickBar: Boolean = true,
    colorConfig: LineChartColorConfig = LineChartColorConfig.default(),
    labelConfig: LabelConfig = LabelConfig.default(),
    target: Float? = null,
    targetConfig: TargetConfig = TargetConfig.default(),
    chartConfig: LineChartConfig = LineChartConfig(),
    onClick: (LineData) -> Unit = {}
) {
    require(showFilledArea || showLineStroke) {
        "Both showFilledArea and showLineStroke cannot be false at the same time"
    }
    LineChartContent(
        data = data,
        modifier = modifier,
        smoothLineCurve = smoothLineCurve,
        showFilledArea = showFilledArea,
        showOnClickBar = showOnClickBar,
        showLineStroke = showLineStroke,
        colorConfig = colorConfig,
        labelConfig = labelConfig,
        chartConfig = chartConfig,
        target = target,
        targetConfig = targetConfig,
        onClick = onClick,
        onValueChange = {},
    )
}

@Composable
fun LineChart(
    data: () -> List<LineData>,
    modifier: Modifier = Modifier,
    smoothLineCurve: Boolean = true,
    showFilledArea: Boolean = false,
    showLineStroke: Boolean = true,
    colorConfig: LineChartColorConfig = LineChartColorConfig.default(),
    labelConfig: LabelConfig = LabelConfig.default(),
    target: Float? = null,
    targetConfig: TargetConfig = TargetConfig.default(),
    chartConfig: LineChartConfig = LineChartConfig(),
    onValueChange: (LineData) -> Unit = {},
) {
    require(showFilledArea || showLineStroke) {
        "Both showFilledArea and showLineStroke cannot be false at the same time"
    }
    LineChartContent(
        data = data,
        modifier = modifier,
        colorConfig = colorConfig,
        showLineStroke = showLineStroke,
        showFilledArea = showFilledArea,
        labelConfig = labelConfig,
        chartConfig = chartConfig,
        smoothLineCurve = smoothLineCurve,
        target = target,
        onClick = { },
        onValueChange = onValueChange,
        targetConfig = targetConfig,
        showOnClickBar = false,
    )
}

@Composable
private fun LineChartContent(
    data: () -> List<LineData>,
    modifier: Modifier = Modifier,
    smoothLineCurve: Boolean = true,
    showFilledArea: Boolean = false,
    showOnClickBar: Boolean = true,
    showLineStroke: Boolean = true,
    colorConfig: LineChartColorConfig = LineChartColorConfig.default(),
    labelConfig: LabelConfig = LabelConfig.default(),
    chartConfig: LineChartConfig = LineChartConfig(),
    target: Float? = null,
    targetConfig: TargetConfig = TargetConfig.default(),
    onClick: (LineData) -> Unit = {},
    onValueChange: (LineData) -> Unit = {}
) {
    val lineData = data()
    val textMeasurer = rememberTextMeasurer()

    val (minValue, maxValue) = remember(lineData) {
        calculateValueRange(
            data = lineData,
            yValueSelector = { it.yValue },
            // Line chart uses actual min/max, no special handling for all zero / all positive/negative
            handleAllZeroAsSpecialMax = false,
            defaultMinIfAllPositive = null,
            defaultMaxIfAllNegative = null
        )
    }
    val yRange = maxValue - minValue

    // val bottomPadding = if (labelConfig.showXLabel) 24.dp else 0.dp // Removed
    // val leftPadding = if (labelConfig.showYLabel) 24.dp else 0.dp // Removed

    val chartPaddings = calculateChartPaddings(
        labelConfig = labelConfig,
        yAxisLabelWidth = 24.dp,
        xAxisLabelHeight = 24.dp, // Line chart uses a consistent 24.dp for X labels when shown
        xAxisLabelHeightWhenNegative = 24.dp, // Not applicable to LineChart's current design
        hasNegativeValuesForXAxis = false, // LineChart X-axis is not centered
        canDrawNegativeChart = false, // LineChart does not have a centered X-axis mode like BarChart
        allYValuesAreZero = lineData.all { it.yValue == 0f }, // To allow Y label hiding if all zero
        hasTopFixedPadding = false, // LineChart does not have fixed top padding like BarChart tooltip
        topFixedPaddingValue = 0.dp
    )

    var clickedIndex by remember { mutableStateOf(-1) }
    var clickedOffset by remember { mutableStateOf(Offset.Zero) }
    val dragPosition = remember { mutableStateOf<Offset?>(null) }
    val interactionTooltipConfig = chartConfig.interactionTooltipConfig
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = chartPaddings.start,
                top = chartPaddings.top,
                bottom = chartPaddings.bottom,
                end = chartPaddings.end
            )
            .pointerInput(Unit) {
                if (showOnClickBar) {
                    detectTapGestures { offset ->
                        val xStep = size.width / (lineData.size - 1)
                        val tappedIndex =
                            ((offset.x + xStep / 2) / xStep).toInt().coerceIn(0, lineData.size - 1)
                        val tappedData = lineData[tappedIndex]
                        clickedIndex = tappedIndex
                        clickedOffset = offset
                        onClick(tappedData)
                    }
                }
                if (interactionTooltipConfig.isLongPressDragEnabled) {
                    detectDragGesturesAfterLongPress(
                        onDrag = { change, _ ->
                            dragPosition.value = change.position
                        },
                        onDragEnd = {
                            dragPosition.value = null
                        }
                    )
                }
            }
            .drawAxesAndGridLines(
                data = lineData,
                colorConfig = colorConfig,
                chartConfig = chartConfig,
                textMeasurer = textMeasurer,
                labelConfig = labelConfig,
                minValue = minValue,
                yRange = yRange
            )
    ) {
        val (canvasWidth, canvasHeight) = size
        val xStep = canvasWidth / (lineData.size - 1)
        target?.let {
            drawTargetLineIfNeeded(
                canvasWidth = canvasWidth,
                targetConfig = targetConfig,
                yPoint = canvasHeight - (target - minValue) * canvasHeight / yRange
            )
        }

        drawLineCurve(
            data = { lineData },
            canvasHeight = canvasHeight,
            xStep = xStep,
            minValue = minValue,
            maxValue = maxValue,
            chartConfig = chartConfig,
            showLineStroke = showLineStroke,
            showFilledArea = showFilledArea,
            lineColor = colorConfig.lineColor,
            fillColor = colorConfig.lineFillColor,
            smoothLineCurve = smoothLineCurve
        )
        if (chartConfig.lineConfig.showValueOnLine) {
            drawValueOnLine(
                lineData = lineData,
                xStep = xStep,
                minValue = minValue,
                yRange = yRange,
                canvasHeight = canvasHeight,
                textMeasurer = textMeasurer,
                valueTextColor = chartConfig.lineConfig.valueTextColor,
                valueTextStyle = chartConfig.lineConfig.valueTextStyle
            )
        }

        if (showOnClickBar) {
            if (clickedIndex != -1) {
                val barX = when (clickedIndex) {
                    0 -> 0f
                    lineData.size - 1 -> size.width - xStep / 2
                    else -> clickedIndex * xStep - xStep / 2
                }
                drawRoundRect(
                    brush = Brush.linearGradient(colorConfig.selectionBarColor.value),
                    topLeft = Offset(barX, 0f),
                    size = Size(width = xStep, height = canvasHeight),
                    cornerRadius = CornerRadius(15F)
                )
            }
        } else if (chartConfig.interactionTooltipConfig.isLongPressDragEnabled) {
            dragPosition.value?.let { position ->
                val xTooltipPosition = when {
                    position.x < leftPadding.toPx() -> leftPadding.toPx()
                    position.x > size.width -> size.width
                    else -> position.x
                }
                drawIndicatorLine(
                    xTooltipPosition = xTooltipPosition,
                    canvasHeight = canvasHeight,
                    interactionTooltipConfig = chartConfig.interactionTooltipConfig
                )
                drawLineChartTooltip(
                    lineData = lineData,
                    xTooltipPosition = xTooltipPosition,
                    xStep = xStep,
                    minValue = minValue,
                    yRange = yRange,
                    canvasHeight = canvasHeight,
                    textMeasurer = textMeasurer,
                    onValueChange = onValueChange,
                    interactionTooltipConfig = chartConfig.interactionTooltipConfig
                )
            }
        }
    }
}
