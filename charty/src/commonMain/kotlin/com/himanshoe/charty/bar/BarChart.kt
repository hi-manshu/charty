package com.himanshoe.charty.bar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMaxOfOrNull
import com.himanshoe.charty.bar.config.BarChartColorConfig
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.config.BarTooltip
import com.himanshoe.charty.bar.model.BarData
import com.himanshoe.charty.bar.modifier.drawAxisLineForVerticalChart
import com.himanshoe.charty.bar.modifier.drawRangeLinesForVerticalChart
import com.himanshoe.charty.bar.modifier.drawYAxisLabel
import com.himanshoe.charty.common.ChartColor
import com.himanshoe.charty.common.LabelConfig
import com.himanshoe.charty.common.TargetConfig
import com.himanshoe.charty.common.asSolidChartColor
import com.himanshoe.charty.common.drawTargetLineIfNeeded
import com.himanshoe.charty.common.getDrawingPath
import com.himanshoe.charty.common.getTetStyle
import com.himanshoe.charty.common.getXLabelTextCharCount
import kotlin.math.absoluteValue

/**
 * A composable function that displays a bar chart.
 *
 * @param data A lambda function that returns a list of `BarData` representing the data points for the bar chart.
 * @param modifier A `Modifier` for customizing the layout or drawing behavior of the chart.
 * @param target An optional target value to be displayed on the chart.
 * @param targetConfig A `TargetConfig` object for configuring the appearance of the target line.
 * @param barChartConfig A `BarChartConfig` object for configuring the chart's appearance and behavior.
 * @param labelConfig A `LabelConfig` object for configuring the labels on the chart.
 * @param barChartColorConfig A `BarChartColorConfig` object for configuring the colors of the bars, axis lines, and grid lines.
 * @param onBarClick A lambda function to handle click events on the bars. It receives the index of the clicked bar and the corresponding `BarData` as parameters.
 */
@Composable
fun BarChart(
    data: () -> List<BarData>,
    modifier: Modifier = Modifier,
    target: Float? = null,
    targetConfig: TargetConfig = TargetConfig.default(),
    barChartConfig: BarChartConfig = BarChartConfig.default(),
    labelConfig: LabelConfig = LabelConfig.default(),
    barTooltip: BarTooltip? = null,
    barChartColorConfig: BarChartColorConfig = BarChartColorConfig.default(),
    onBarClick: (Int, BarData) -> Unit = { _, _ -> },
) {
    BarChartContent(
        data = data,
        modifier = modifier,
        target = target,
        targetConfig = targetConfig,
        barChartConfig = barChartConfig,
        labelConfig = labelConfig,
        barTooltip = barTooltip,
        barChartColorConfig = barChartColorConfig,
        onBarClick = onBarClick
    )
}

@Composable
private fun BarChartContent(
    data: () -> List<BarData>,
    modifier: Modifier = Modifier,
    barTooltip: BarTooltip? = null,
    target: Float? = null,
    targetConfig: TargetConfig = TargetConfig.default(),
    barChartConfig: BarChartConfig = BarChartConfig.default(),
    labelConfig: LabelConfig = LabelConfig.default(),
    barChartColorConfig: BarChartColorConfig = BarChartColorConfig.default(),
    onBarClick: (Int, BarData) -> Unit = { _, _ -> },
) {
    val barData = data()
    val allValuesAreZero = remember(barData) { barData.all { it.yValue == 0f } }
    val maxValue = remember(barData) {
        if (allValuesAreZero) 100f else barData.fastMaxOfOrNull { it.yValue.absoluteValue } ?: 0f
    }
    val minValue = remember(barData) { barData.minOfOrNull { it.yValue.absoluteValue } ?: 0f }
    val hasNegativeValues = remember(barData) { barData.fastAny { it.yValue < 0 } }
    val displayData = remember(barData) { getDisplayData(barData, barChartConfig.minimumBarCount) }
    val canDrawNegativeChart = hasNegativeValues && barChartConfig.drawNegativeValueChart
    val textMeasurer = rememberTextMeasurer()
    val bottomLabelPadding = if (labelConfig.showXLabel && !hasNegativeValues) 8.dp else 0.dp
    val leftPadding = if (labelConfig.showYLabel && !allValuesAreZero) 24.dp else 0.dp
    val topPadding = if (barTooltip != null) 24.dp else 0.dp
    val bottomPadding = if (canDrawNegativeChart) 24.dp else bottomLabelPadding
    var clickedOffset by mutableStateOf<Offset?>(null)
    var clickedBarIndex by mutableIntStateOf(-1)

    BarChartCanvasScaffold(
        modifier = modifier.padding(
            bottom = bottomPadding,
            start = leftPadding,
            top = topPadding,
        ),
        showAxisLines = barChartConfig.showAxisLines,
        showRangeLines = barChartConfig.showGridLines,
        axisLineColor = barChartColorConfig.axisLineColor,
        rangeLineColor = barChartColorConfig.gridLineColor,
        canDrawNegativeChart = canDrawNegativeChart,
        labelConfig = labelConfig,
        onClick = { offset ->
            clickedOffset = offset
        },
        data = { displayData },
    ) { canvasHeight, gap, barWidth ->

        target?.let {
            require(it in minValue..maxValue) { "Target value should be between $minValue and $maxValue" }
            val targetLineY = if (hasNegativeValues) canvasHeight / 2 else canvasHeight
            val targetLineYPosition = targetLineY - (it / maxValue) * targetLineY
            drawTargetLineIfNeeded(
                canvasWidth = size.width,
                targetConfig = targetConfig,
                yPoint = targetLineYPosition
            )
        }

        displayData.fastForEachIndexed { index, barData ->
            val height = barData.yValue / maxValue * canvasHeight
            val maxHeight = maxValue / maxValue * canvasHeight
            val yAxis = canvasHeight / 2
            val (topLeftY, backgroundTopLeftY) = getBarAndBackgroundBarTopLeft(
                canDrawNegativeChart = canDrawNegativeChart,
                barData = barData,
                yAxis = yAxis,
                height = height,
                canvasHeight = canvasHeight,
                maxHeight = maxHeight,
            )
            val color = getBarColor(
                barData = barData,
                barChartColorConfig = barChartColorConfig,
            )

            val (individualBarTopLeft, individualBarRectSize) = getBarTopLeftAndRectSize(
                index = index,
                barWidth = barWidth,
                gap = gap,
                clickedBarIndex = clickedBarIndex,
                barData = barData,
                topLeftY = topLeftY,
                height = height,
                canDrawNegativeChart = canDrawNegativeChart
            )
            clickedOffset?.let { offset ->
                if (isClickInsideBar(offset, individualBarTopLeft, individualBarRectSize)) {
                    clickedBarIndex = index
                    onBarClick(index, barData)
                }
            }
            val textCharCount = labelConfig.getXLabelTextCharCount(
                xValue = barData.xValue,
                displayDataCount = displayData.count()
            )

            val textSizeFactor = if (displayData.count() <= 13) 4 else 2
            val textLayoutResult = textMeasurer.measure(
                text = barData.xValue.toString().take(textCharCount),
                style = labelConfig.getTetStyle(fontSize = (barWidth / textSizeFactor).toSp()),
                overflow = TextOverflow.Clip,
                maxLines = 1,
            )

            val (textOffsetY, calculatedCornerRadius) = getTextYOffsetAndCornerRadius(
                barData = barData,
                individualBarTopLeft = individualBarTopLeft,
                textLayoutResult = textLayoutResult,
                individualBarRectSize = individualBarRectSize,
                barChartConfig = barChartConfig,
                barWidth = barWidth
            )
            val cornerRadius = getCornerRadius(barChartConfig, calculatedCornerRadius)

            if (!allValuesAreZero && barData.yValue != 0F) {
                backgroundColorBar(
                    barData = barData,
                    index = index,
                    barBackgroundColor = barChartColorConfig.barBackgroundColor,
                    barWidth = barWidth,
                    gap = gap,
                    backgroundTopLeftY = backgroundTopLeftY,
                    canDrawNegativeChart = canDrawNegativeChart,
                    maxHeight = maxHeight,
                    cornerRadius = cornerRadius,
                )
            }
            getDrawingPath(
                barTopLeft = individualBarTopLeft,
                barRectSize = individualBarRectSize,
                topLeftCornerRadius = if (barData.yValue >= 0) cornerRadius else CornerRadius.Zero,
                topRightCornerRadius = if (barData.yValue >= 0) cornerRadius else CornerRadius.Zero,
                bottomLeftCornerRadius = if (barData.yValue < 0) cornerRadius else CornerRadius.Zero,
                bottomRightCornerRadius = if (barData.yValue < 0) cornerRadius else CornerRadius.Zero
            ).let { path ->
                val brush = Brush.linearGradient(
                    colors = color,
                    start = Offset(individualBarTopLeft.x, individualBarTopLeft.y),
                    end = Offset(
                        x = individualBarTopLeft.x + individualBarRectSize.width,
                        y = individualBarTopLeft.y + individualBarRectSize.height
                    )
                )
                drawPath(path = path, brush = brush)
                if (labelConfig.showXLabel) {
                    require(
                        barData.xValue.toString().isNotEmpty()
                    ) { "X value should not be empty" }
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(
                            x = individualBarTopLeft.x + barWidth / 2 - textLayoutResult.size.width / 2,
                            y = textOffsetY,
                        ),
                    )
                }
                if (!allValuesAreZero && barTooltip != null) {
                    drawTooltip(
                        textMeasurer = textMeasurer,
                        barData = barData,
                        labelConfig = labelConfig,
                        barWidth = barWidth,
                        textSizeFactor = textSizeFactor,
                        barTooltip = barTooltip,
                        individualBarTopLeft = individualBarTopLeft,
                        gap = gap,
                        individualBarRectSize = individualBarRectSize,
                        backgroundTopLeftY = backgroundTopLeftY
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawTooltip(
    textMeasurer: TextMeasurer,
    barData: BarData,
    labelConfig: LabelConfig,
    barWidth: Float,
    textSizeFactor: Int,
    barTooltip: BarTooltip,
    individualBarTopLeft: Offset,
    individualBarRectSize: Size,
    gap: Float,
    backgroundTopLeftY: Float
) {
    val tooltipTextLayoutResult = textMeasurer.measure(
        text = barData.yValue.toInt().toString(),
        style = labelConfig.getTetStyle(fontSize = (barWidth / textSizeFactor).toSp()),
        overflow = TextOverflow.Clip,
        maxLines = 1,
    )
    val tooltipYOffset = when (barTooltip) {
        BarTooltip.BarTop -> if (barData.yValue >= 0) {
            individualBarTopLeft.y - tooltipTextLayoutResult.size.height - gap
        } else {
            individualBarTopLeft.y + individualBarRectSize.height + gap
        }

        BarTooltip.GraphTop -> if (barData.yValue >= 0) {
            backgroundTopLeftY - tooltipTextLayoutResult.size.height - gap
        } else {
            backgroundTopLeftY + individualBarRectSize.height + gap
        }
    }
    drawText(
        textLayoutResult = tooltipTextLayoutResult,
        topLeft = Offset(
            x = individualBarTopLeft.x + barWidth / 2 - tooltipTextLayoutResult.size.width / 2,
            y = tooltipYOffset
        ),
    )
}

private fun getCornerRadius(
    barChartConfig: BarChartConfig,
    cornerRadius: CornerRadius
) = barChartConfig.cornerRadius ?: cornerRadius

internal fun getTextYOffsetAndCornerRadius(
    barData: BarData,
    individualBarTopLeft: Offset,
    textLayoutResult: TextLayoutResult,
    individualBarRectSize: Size,
    barChartConfig: BarChartConfig,
    barWidth: Float
): Pair<Float, CornerRadius> {
    val textOffsetY = if (barData.yValue < 0) {
        individualBarTopLeft.y - textLayoutResult.size.height - 5
    } else {
        individualBarTopLeft.y + individualBarRectSize.height + 5
    }
    val cornerRadius = if (barChartConfig.showCurvedBar) {
        CornerRadius(
            x = barWidth / 2,
            y = barWidth / 2
        )
    } else {
        CornerRadius.Zero
    }
    return Pair(textOffsetY, cornerRadius)
}

private fun getBarTopLeftAndRectSize(
    index: Int,
    barWidth: Float,
    gap: Float,
    clickedBarIndex: Int,
    barData: BarData,
    topLeftY: Float,
    height: Float,
    canDrawNegativeChart: Boolean
): Pair<Offset, Size> {
    val isClickedBar = clickedBarIndex == index
    val heightAdjustment =
        if (isClickedBar) height.absoluteValue * 0.02F / (if (canDrawNegativeChart) 2 else 1) else 0f

    val xOffset = index * (barWidth + gap)
    val individualBarTopLeft = Offset(
        x = xOffset,
        y = if (barData.yValue < 0) {
            topLeftY
        } else {
            topLeftY - heightAdjustment
        }
    )

    val individualBarRectSize = Size(
        width = barWidth,
        height = if (isClickedBar) {
            height.absoluteValue * 1.02F / (if (canDrawNegativeChart) 2 else 1)
        } else {
            height.absoluteValue / (if (canDrawNegativeChart) 2 else 1)
        }
    )
    return Pair(individualBarTopLeft, individualBarRectSize)
}

internal fun getBarAndBackgroundBarTopLeft(
    canDrawNegativeChart: Boolean,
    barData: BarData,
    yAxis: Float,
    height: Float,
    canvasHeight: Float,
    maxHeight: Float,
): Pair<Float, Float> {
    val topLeftY = if (canDrawNegativeChart) {
        if (barData.yValue < 0) yAxis else yAxis - height / 2
    } else {
        canvasHeight - height
    }
    val backgroundTopLeftY = if (canDrawNegativeChart) {
        if (barData.yValue < 0) yAxis else yAxis - maxHeight / 2
    } else {
        canvasHeight - maxHeight
    }
    return Pair(topLeftY, backgroundTopLeftY)
}

private fun DrawScope.backgroundColorBar(
    barData: BarData,
    index: Int,
    barWidth: Float,
    barBackgroundColor: ChartColor,
    gap: Float,
    backgroundTopLeftY: Float,
    canDrawNegativeChart: Boolean,
    maxHeight: Float,
    cornerRadius: CornerRadius,
) {
    val color = if (barData.barBackgroundColor.value.fastAny { it == Color.Unspecified }) {
        barBackgroundColor.value
    } else {
        barData.barBackgroundColor.value
    }
    drawRoundRect(
        brush = Brush.linearGradient(color),
        topLeft = Offset(x = index * (barWidth + gap), y = backgroundTopLeftY),
        size = Size(
            width = barWidth,
            height = if (canDrawNegativeChart) maxHeight.absoluteValue / 2 else maxHeight.absoluteValue,
        ),
        cornerRadius = cornerRadius,
    )
}

internal fun getDisplayData(
    data: List<BarData>,
    minimumBarCount: Int,
): List<BarData> = if (data.size < minimumBarCount) {
    List(minimumBarCount - data.size) {
        BarData(
            0F, " ", Color.Unspecified.asSolidChartColor()
        )
    } + data
} else {
    data
}

internal fun isClickInsideBar(
    clickOffset: Offset,
    rectTopLeft: Offset,
    rectSize: Size,
) = (
        clickOffset.x in rectTopLeft.x..(rectTopLeft.x + rectSize.width) &&
                clickOffset.y in rectTopLeft.y..(rectTopLeft.y + rectSize.height)
        )

/**
 * A composable function that provides a scaffold for drawing a bar chart canvas.
 *
 * @param modifier The modifier to be applied to the canvas.
 * @param showAxisLines A boolean indicating whether to show axis lines.
 * @param showRangeLines A boolean indicating whether to show range lines.
 * @param canDrawNegativeChart A boolean indicating whether the chart can draw negative values.
 * @param axisLineColor The color of the axis lines.
 * @param rangeLineColor The color of the range lines.
 * @param onClick A lambda function to handle click events on the canvas.
 * @param content A lambda function that defines the content to be drawn on the canvas.
 */
@Composable
internal fun BarChartCanvasScaffold(
    modifier: Modifier = Modifier,
    showAxisLines: Boolean = false,
    showRangeLines: Boolean = false,
    canDrawNegativeChart: Boolean = false,
    axisLineColor: ChartColor = Color.Black.asSolidChartColor(),
    labelConfig: LabelConfig = LabelConfig.default(),
    rangeLineColor: ChartColor = Color.Gray.asSolidChartColor(),
    data: () -> List<BarData> = { emptyList() },
    onClick: (Offset) -> Unit = {},
    content: DrawScope.(Float, Float, Float) -> Unit = { _, _, _ -> },
) {
    val barData = data()
    val textMeasurer = rememberTextMeasurer()
    val maxValue = barData.fastMaxOfOrNull { it.yValue } ?: 0f
    val minValue = if (canDrawNegativeChart) barData.minOfOrNull { it.yValue } ?: 0f else 0f
    val step = (maxValue - minValue) / 4

    Canvas(
        modifier = modifier.then(
            if (showAxisLines) {
                Modifier.drawAxisLineForVerticalChart(
                    hasNegativeValues = canDrawNegativeChart,
                    axisLineColor = axisLineColor,
                )
            } else {
                Modifier
            },
        ).then(
            if (labelConfig.showYLabel) {
                Modifier.drawYAxisLabel(
                    minValue = minValue,
                    step = step,
                    maxValue = maxValue,
                    labelConfig = labelConfig,
                    textMeasurer = textMeasurer,
                    count = barData.count()
                )
            } else {
                Modifier
            },
        ).then(
            if (showRangeLines) {
                Modifier.drawRangeLinesForVerticalChart(
                    hasNegativeValues = canDrawNegativeChart,
                    rangeLineColor = rangeLineColor,
                )
            } else {
                Modifier
            },
        )
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures { onClick(it) } },
    ) {
        val (canvasWidth, canvasHeight) = size
        val gap = canvasWidth / (barData.count() * 10)
        val barWidth = (canvasWidth - gap * (barData.count() - 1)) / barData.count()

        content(
            canvasHeight,
            gap,
            barWidth
        )
    }
}

internal fun Modifier.drawYAxisLineForHorizontalChart(
    hasNegativeValues: Boolean,
    allNegativeValues: Boolean,
    allPositiveValues: Boolean,
    axisLineColor: ChartColor,
): Modifier = this.drawWithCache {
    val canvasWidth = size.width
    val canvasHeight = size.height
    val xAxis =
        if (hasNegativeValues && !allNegativeValues && !allPositiveValues) canvasWidth / 2 else 0f

    onDrawBehind {
        drawLine(
            brush = Brush.linearGradient(axisLineColor.value),
            start = Offset(xAxis, 0f),
            end = Offset(xAxis, canvasHeight),
            strokeWidth = 2f,
        )
    }
}

internal fun Modifier.drawRangeLineForHorizontalChart(
    allNegativeValues: Boolean,
    allPositiveValues: Boolean,
    axisLineColor: ChartColor,
): Modifier = this.drawWithCache {
    val canvasWidth = size.width
    val canvasHeight = size.height
    val dashLength = 10f
    val dashGap = 10f

    onDrawBehind {
        val dashCount = 5
        val dashSpacing = canvasWidth / (dashCount + 1)
        val drawDashedLine: (Float) -> Unit = { x ->
            var currentY = 0f
            while (currentY < canvasHeight) {
                drawLine(
                    brush = Brush.linearGradient(axisLineColor.value),
                    start = Offset(x, currentY),
                    end = Offset(x, currentY + dashLength),
                    strokeWidth = 2f,
                )
                currentY += dashLength + dashGap
            }
        }

        if (allNegativeValues || allPositiveValues) {
            val centerX = canvasWidth / 2
            drawDashedLine(centerX - 2 * dashSpacing)
            drawDashedLine(centerX - dashSpacing)
            drawDashedLine(centerX)
            drawDashedLine(centerX + dashSpacing)
            drawDashedLine(centerX + 2 * dashSpacing)
        } else {
            val centerX = canvasWidth / 2
            drawDashedLine(centerX - dashSpacing)
            drawDashedLine(centerX - 2 * dashSpacing)
            drawDashedLine(centerX)
            drawDashedLine(centerX + dashSpacing)
            drawDashedLine(centerX + 2 * dashSpacing)
        }
    }
}
