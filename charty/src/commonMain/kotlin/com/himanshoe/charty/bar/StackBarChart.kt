package com.himanshoe.charty.bar

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
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastFlatMap
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMaxOfOrNull
import com.himanshoe.charty.bar.config.BarChartColorConfig
import com.himanshoe.charty.bar.config.StackBarConfig
import com.himanshoe.charty.bar.model.StackBarData
import com.himanshoe.charty.common.LabelConfig
import com.himanshoe.charty.common.TargetConfig
import com.himanshoe.charty.common.getDrawingPath
import com.himanshoe.charty.common.utils.isClickInsideRect
import com.himanshoe.charty.common.utils.padListToMinimumCount
import kotlin.math.absoluteValue

/**
 * A composable function that displays a stacked bar chart.
 *
 * @param data A lambda function that returns a list of `StackBarData` representing the data to be displayed.
 * @param modifier A `Modifier` for this composable.
 * @param target An optional target value to be displayed as a line on the chart.
 * @param targetConfig Configuration for the target line.
 * @param stackBarConfig Configuration for the stacked bar chart.
 * @param barChartColorConfig Configuration for the colors used in the bar chart.
 * @param labelConfig Configuration for the labels displayed on the chart.
 * @param onBarClick A lambda function to be invoked when a bar is clicked, with the index and data of the clicked bar.
 */
@Composable
fun StackedBarChart(
    data: () -> List<StackBarData>,
    modifier: Modifier = Modifier,
    target: Float? = null,
    targetConfig: TargetConfig = TargetConfig.default(),
    stackBarConfig: StackBarConfig = StackBarConfig.default(),
    barChartColorConfig: BarChartColorConfig = BarChartColorConfig.default(),
    labelConfig: LabelConfig = LabelConfig.default(),
    onBarClick: (Int, StackBarData) -> Unit = { _, _ -> },
) {
    StackBarChartContent(
        data = data,
        stackBarConfig = stackBarConfig,
        labelConfig = labelConfig,
        modifier = modifier,
        barChartColorConfig = barChartColorConfig,
        target = target,
        targetConfig = targetConfig,
        onBarClick = onBarClick
    )
}

@Composable
private fun StackBarChartContent(
    data: () -> List<StackBarData>,
    stackBarConfig: StackBarConfig,
    target: Float?,
    targetConfig: TargetConfig,
    modifier: Modifier = Modifier,
    barChartColorConfig: BarChartColorConfig = BarChartColorConfig.default(),
    labelConfig: LabelConfig = LabelConfig.default(),
    onBarClick: (Int, StackBarData) -> Unit = { _, _ -> },
) {
    val stackBarData = data()
    val displayData = remember(stackBarData, stackBarConfig.minimumBarCount) {
        padListToMinimumCount(
            originalList = stackBarData,
            minimumCount = stackBarConfig.minimumBarCount,
            defaultItemFactory = { StackBarData("", emptyList(), emptyList()) }
        )
    }
    val maxValue =
        remember(displayData) { displayData.fastMaxOfOrNull { it.values.sum().absoluteValue } ?: 0f }
    val hasNegativeValues = remember(stackBarData) { displayData.fastFlatMap { it.values }.fastAny { it < 0 } }
    val textMeasurer = rememberTextMeasurer()
    val bottomPadding = if (labelConfig.showXLabel && !hasNegativeValues) 8.dp else 0.dp
    val leftPadding = if (labelConfig.showYLabel) 24.dp else 0.dp

    var clickedOffset by mutableStateOf(Offset.Zero)
    var clickedBarIndex by mutableIntStateOf(-1)

    StackBarChartScaffold(
        maxValue = maxValue,
        bottomPadding = bottomPadding,
        leftPadding = leftPadding,
        hasNegativeValues = hasNegativeValues,
        modifier = modifier,
        displayData = displayData,
        barChartColorConfig = barChartColorConfig,
        labelConfig = labelConfig,
        showAxisLines = stackBarConfig.showAxisLines,
        showGridLines = stackBarConfig.showGridLines,
        textMeasurer = textMeasurer,
        onBarClick = { clickedOffset = it }
    ) { canvasWidth, canvasHeight ->
        val gap = canvasWidth / (displayData.size * 10)
        val barWidth = (canvasWidth - gap * (displayData.size - 1)) / displayData.size

        target?.let {
            require(it in 0f..maxValue) { "Target value should be between 0 and $maxValue" }
            val targetLineY = canvasHeight - (it / maxValue) * canvasHeight
            val brush = Brush.linearGradient(targetConfig.targetLineBarColors.value)
            drawLine(
                brush = brush,
                start = Offset(0f, targetLineY),
                end = Offset(size.width, targetLineY),
                strokeWidth = targetConfig.targetStrokeWidth,
                pathEffect = targetConfig.pathEffect
            )
        }

        displayData.fastForEachIndexed { index, stackBarData ->
            var accumulatedHeight = 0f
            // Draw background bar
            val color = barChartColorConfig.barBackgroundColor.value
            drawRect(
                brush = Brush.linearGradient(color),
                topLeft = Offset((index + 1) * (barWidth + gap) - barWidth, 0f),
                size = Size(barWidth, canvasHeight)
            )
            stackBarData.values.fastForEachIndexed { valueIndex, value ->
                val height = value.absoluteValue / maxValue * canvasHeight
                val expandedHeight = if (clickedBarIndex == index) (height * 1.05f) else height
                val topLeftY = canvasHeight - accumulatedHeight - expandedHeight
                val color = stackBarData.colors[valueIndex].value

                val (individualBarTopLeft, individualBarRectSize, cornerRadius) = getBarTopLeftSizeAndRadius(
                    index = index,
                    barWidth = barWidth,
                    gap = gap,
                    topLeftY = topLeftY,
                    height = height,
                    stackBarConfig = stackBarConfig,
                    valueIndex = valueIndex,
                    stackBarData = stackBarData
                )

                if (isClickInsideRect(clickedOffset, individualBarTopLeft, individualBarRectSize)) {
                    clickedBarIndex = index
                    onBarClick(index, stackBarData)
                }

                getDrawingPath(
                    individualBarTopLeft = individualBarTopLeft,
                    individualBarRectSize = individualBarRectSize.copy(height = expandedHeight),
                    cornerRadius = cornerRadius
                ).let { path ->
                    val brush = Brush.linearGradient(
                        colors = color,
                        start = Offset(individualBarTopLeft.x, individualBarTopLeft.y),
                        end = Offset(
                            individualBarTopLeft.x + individualBarRectSize.width,
                            individualBarTopLeft.y + expandedHeight
                        )
                    )
                    drawPath(path = path, brush = brush)
                    accumulatedHeight += height
                }
            }
        }
    }
}

private fun getBarTopLeftSizeAndRadius(
    index: Int,
    barWidth: Float,
    gap: Float,
    topLeftY: Float,
    height: Float,
    stackBarConfig: StackBarConfig,
    valueIndex: Int,
    stackBarData: StackBarData
): Triple<Offset, Size, CornerRadius> {
    val individualBarTopLeft = Offset(
        x = (index + 1) * (barWidth + gap) - barWidth,
        y = topLeftY
    )
    val individualBarRectSize = Size(width = barWidth, height = height)
    val cornerRadius =
        if (stackBarConfig.showCurvedBar && valueIndex == stackBarData.values.lastIndex) {
            CornerRadius(barWidth / 2, barWidth / 2)
        } else {
            CornerRadius.Zero
        }
    return Triple(individualBarTopLeft, individualBarRectSize, cornerRadius)
}

// internal fun getDisplayData has been removed and replaced by common.utils.padListToMinimumCount
