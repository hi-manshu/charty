package com.himanshoe.charty.bar

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMaxOfOrNull
import com.himanshoe.charty.bar.config.BarChartColorConfig
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.model.BarData
import com.himanshoe.charty.common.LabelConfig
import com.himanshoe.charty.common.TargetConfig
import com.himanshoe.charty.common.asSolidChartColor
import com.himanshoe.charty.common.drawTargetLineIfNeeded
import com.himanshoe.charty.common.getDrawingPath
import com.himanshoe.charty.common.getTetStyle
import com.himanshoe.charty.common.getXLabelTextCharCount
import com.himanshoe.charty.common.utils.isClickInsideRect
import com.himanshoe.charty.common.utils.padListToMinimumCount
import kotlin.math.absoluteValue
import androidx.compose.ui.graphics.Color // Needed for defaultItemFactory in padListToMinimumCount

/**
 * A composable function that displays a line bar chart.
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
fun LineBarChart(
    data: () -> List<BarData>,
    modifier: Modifier = Modifier,
    target: Float? = null,
    targetConfig: TargetConfig = TargetConfig.default(),
    barChartConfig: BarChartConfig = BarChartConfig.default(),
    labelConfig: LabelConfig = LabelConfig.default(),
    barChartColorConfig: BarChartColorConfig = BarChartColorConfig.default(),
    onBarClick: (Int, BarData) -> Unit = { _, _ -> },
) {
    LineBarChartContent(
        data = data,
        modifier = modifier,
        barChartConfig = barChartConfig,
        labelConfig = labelConfig,
        targetConfig = targetConfig,
        target = target,
        barChartColorConfig = barChartColorConfig,
        onBarClick = onBarClick
    )
}

@Composable
private fun LineBarChartContent(
    data: () -> List<BarData>,
    modifier: Modifier = Modifier,
    target: Float? = null,
    targetConfig: TargetConfig = TargetConfig.default(),
    barChartConfig: BarChartConfig = BarChartConfig.default(),
    labelConfig: LabelConfig = LabelConfig.default(),
    barChartColorConfig: BarChartColorConfig = BarChartColorConfig.default(),
    onBarClick: (Int, BarData) -> Unit = { _, _ -> },
) {
    val listData = data()
    val minValue = remember(listData) { listData.minOfOrNull { it.yValue.absoluteValue } ?: 0f }
    val maxValue = remember(listData) { listData.fastMaxOfOrNull { it.yValue.absoluteValue } ?: 0f }
    val hasNegativeValues = remember(listData) { listData.fastAny { it.yValue < 0 } }
    val displayData = remember(listData, barChartConfig.minimumBarCount) {
        padListToMinimumCount(
            originalList = listData,
            minimumCount = barChartConfig.minimumBarCount,
            defaultItemFactory = { BarData(0F, " ", Color.Unspecified.asSolidChartColor()) }
        )
    }
    val canDrawNegativeChart = hasNegativeValues && barChartConfig.drawNegativeValueChart
    val textMeasurer = rememberTextMeasurer()
    val bottomPadding = if (labelConfig.showXLabel && !hasNegativeValues) 8.dp else 0.dp
    val leftPadding = if (labelConfig.showYLabel) 24.dp else 0.dp

    var clickedOffset by mutableStateOf(Offset.Zero)
    var clickedBarIndex by mutableIntStateOf(-1)

    BarChartCanvasScaffold(
        modifier = modifier.padding(bottom = bottomPadding, start = leftPadding),
        showAxisLines = barChartConfig.showAxisLines,
        showRangeLines = barChartConfig.showGridLines,
        axisLineColor = barChartColorConfig.axisLineColor,
        rangeLineColor = barChartColorConfig.gridLineColor,
        canDrawNegativeChart = canDrawNegativeChart,
        onClick = { clickedOffset = it },
        data = data
    ) { canvasHeight, _, barWidth ->
        target?.let {
            require(it in minValue..maxValue) { "Target value should be between $minValue and $maxValue" }
            val targetLineY = if (hasNegativeValues) canvasHeight / 2 else canvasHeight
            val targetLineYPosition = targetLineY - (it / maxValue) * targetLineY
            drawTargetLineIfNeeded(
                canvasWidth = size.width,
                targetConfig = targetConfig,
                yPoint = targetLineYPosition,
            )
        }

        displayData.fastForEachIndexed { index, barData ->
            val color = getBarColor(
                barData = barData,
                barChartColorConfig = barChartColorConfig,
            )
            val height = barData.yValue / maxValue * canvasHeight
            val maxHeight = maxValue / maxValue * canvasHeight
            val yAxis = canvasHeight / 2
            val (topLeftY, backgroundTopLeftY) = getBarAndBackgroundBarTopLeft(
                canDrawNegativeChart = canDrawNegativeChart,
                barData = barData,
                yAxis = yAxis,
                height = height,
                canvasHeight = canvasHeight,
                maxHeight = maxHeight
            )
            val (singleBarTopLeft, singleBarRectSize) = getBarTopLeftAndRectSize(
                index = index,
                barWidth = barWidth,
                barData = barData,
                topLeftY = topLeftY,
                clickedBarIndex = clickedBarIndex,
                height = height,
                canDrawNegativeChart = canDrawNegativeChart
            )

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
                individualBarTopLeft = singleBarTopLeft,
                textLayoutResult = textLayoutResult,
                individualBarRectSize = singleBarRectSize,
                barChartConfig = barChartConfig,
                barWidth = barWidth
            )
            val cornerRadius = getCornerRadius(barChartConfig, calculatedCornerRadius)

            if (isClickInsideRect(clickedOffset, singleBarTopLeft, singleBarRectSize)) {
                clickedBarIndex = index
                onBarClick(index, barData)
            }

            if (barData.yValue != 0F) {
                backgroundColorBar(
                    barData = barData,
                    index = index,
                    barWidth = barWidth,
                    backgroundTopLeftY = backgroundTopLeftY,
                    canDrawNegativeChart = canDrawNegativeChart,
                    maxHeight = maxHeight,
                    cornerRadius = cornerRadius,
                )
            }
            getDrawingPath(
                barTopLeft = singleBarTopLeft,
                barRectSize = singleBarRectSize,
                topLeftCornerRadius = if (barData.yValue >= 0) cornerRadius else CornerRadius.Zero,
                topRightCornerRadius = if (barData.yValue >= 0) cornerRadius else CornerRadius.Zero,
                bottomLeftCornerRadius = if (barData.yValue < 0) cornerRadius else CornerRadius.Zero,
                bottomRightCornerRadius = if (barData.yValue < 0) cornerRadius else CornerRadius.Zero
            ).let {
                val textOffset = Offset(
                    x = singleBarTopLeft.x + singleBarRectSize.width.div(2) - textLayoutResult.size.width.div(
                        2
                    ),
                    y = textOffsetY,
                )
                drawPath(path = it, brush = Brush.linearGradient(color))
                if (labelConfig.showXLabel) {
                    drawText(
                        textLayoutResult = textLayoutResult,
                        brush = Brush.linearGradient(labelConfig.textColor.value),
                        topLeft = textOffset,
                    )
                }
            }
        }
    }
}

private fun getCornerRadius(
    barChartConfig: BarChartConfig,
    cornerRadius: CornerRadius
) = barChartConfig.cornerRadius ?: cornerRadius

private fun getBarTopLeftAndRectSize(
    index: Int,
    barWidth: Float,
    barData: BarData,
    topLeftY: Float,
    clickedBarIndex: Int,
    height: Float,
    canDrawNegativeChart: Boolean,
): Pair<Offset, Size> {
    val individualBarTopLeft = Offset(
        x = index * barWidth + (barWidth - barWidth / 3) / 2,
        y = if (barData.yValue < 0) {
            topLeftY
        } else {
            topLeftY - if (clickedBarIndex == index) {
                height.absoluteValue * 0.02F / (if (canDrawNegativeChart) 2 else 1)
            } else {
                0f
            }
        }
    )

    val individualBarRectSize = Size(
        width = barWidth / 3,
        height = if (clickedBarIndex == index) {
            height.absoluteValue * 1.02F / (if (canDrawNegativeChart) 2 else 1)
        } else {
            height.absoluteValue / (if (canDrawNegativeChart) 2 else 1)
        }
    )
    return Pair(individualBarTopLeft, individualBarRectSize)
}

private fun DrawScope.backgroundColorBar(
    barData: BarData,
    index: Int,
    barWidth: Float,
    backgroundTopLeftY: Float,
    canDrawNegativeChart: Boolean,
    maxHeight: Float,
    cornerRadius: CornerRadius,
) {
    drawRoundRect(
        brush = Brush.linearGradient(barData.barBackgroundColor.value),
        topLeft = Offset(
            x = index * barWidth + (barWidth - barWidth / 3) / 2,
            y = backgroundTopLeftY
        ),
        size = Size(
            width = barWidth / 3,
            height = if (canDrawNegativeChart) maxHeight.absoluteValue / 2 else maxHeight.absoluteValue
        ),
        cornerRadius = cornerRadius,
    )
}
