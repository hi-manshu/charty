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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastFlatMap
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.bar.config.BarChartColorConfig
import com.himanshoe.charty.bar.config.StackBarConfig
import com.himanshoe.charty.bar.model.StackBarData
import com.himanshoe.charty.bar.modifier.drawAxisLineForVerticalChart
import com.himanshoe.charty.common.LabelConfig
import com.himanshoe.charty.common.TargetConfig
import com.himanshoe.charty.common.asSolidChartColor
import com.himanshoe.charty.common.drawTargetLineIfNeeded
import com.himanshoe.charty.common.getDrawingPath
import com.himanshoe.charty.common.getTetStyle
import com.himanshoe.charty.common.modifiers.CommonDrawAxisLines
import com.himanshoe.charty.common.modifiers.CommonDrawHorizontalGridLines
import com.himanshoe.charty.common.modifiers.CommonDrawXAxisLabels
import com.himanshoe.charty.common.modifiers.CommonDrawYAxisLabels
import com.himanshoe.charty.common.utils.isClickInsideRect
import com.himanshoe.charty.common.utils.padListToMinimumCount
import kotlin.math.absoluteValue
import androidx.compose.runtime.mutableStateListOf
// import com.himanshoe.charty.bar.modifier.drawAxisLineForVerticalChart // Will be removed by replacing its usage

/**
 * A composable function that displays a line stacked bar chart.
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
fun LineStackedBarChart(
    data: () -> List<StackBarData>,
    modifier: Modifier = Modifier,
    target: Float? = null,
    targetConfig: TargetConfig = TargetConfig.default(),
    stackBarConfig: StackBarConfig = StackBarConfig.default(),
    barChartColorConfig: BarChartColorConfig = BarChartColorConfig.default(),
    labelConfig: LabelConfig = LabelConfig.default(),
    onBarClick: (Int, StackBarData) -> Unit = { _, _ -> },
) {
    LineStackBarChartContent(
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
private fun LineStackBarChartContent(
    data: () -> List<StackBarData>,
    stackBarConfig: StackBarConfig,
    labelConfig: LabelConfig,
    modifier: Modifier,
    barChartColorConfig: BarChartColorConfig,
    target: Float?,
    targetConfig: TargetConfig,
    onBarClick: (Int, StackBarData) -> Unit
) {
    val lineData = data()
    val displayData = remember(lineData, stackBarConfig.minimumBarCount) {
        padListToMinimumCount(
            originalList = lineData,
            minimumCount = stackBarConfig.minimumBarCount,
            defaultItemFactory = { StackBarData("", emptyList(), emptyList()) }
        )
    }
    val maxValue =
        remember(displayData) { displayData.maxOfOrNull { it.values.sum().absoluteValue } ?: 0f }
    val hasNegativeValues = remember(displayData) { // Changed key from lineData to displayData for consistency
        displayData.fastFlatMap { it.values }.fastAny { it < 0 }
    }
    val textMeasurer = rememberTextMeasurer()
    val bottomPadding = if (labelConfig.showXLabel && !hasNegativeValues) 8.dp else 0.dp // This logic might be superseded by calculateChartPaddings if we use it here too
    val leftPadding = if (labelConfig.showYLabel) 24.dp else 0.dp // This logic might be superseded

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
        textMeasurer = textMeasurer,
        showGridLines = stackBarConfig.showGridLines,
        showAxisLines = stackBarConfig.showAxisLines,
        onBarClick = { clickedOffset = it }
    ) { canvasWidth, canvasHeight ->
        val gap = canvasWidth / (displayData.size * 10)
        val barWidth = (canvasWidth - gap * (displayData.size - 1)) / displayData.size / 3

        target?.let {
            drawTargetLineIfNeeded(
                canvasWidth = canvasWidth,
                targetConfig = targetConfig,
                yPoint = (canvasHeight - (it / maxValue) * canvasHeight)
            )
        }

        displayData.fastForEachIndexed { index, stackBarData ->
            var accumulatedHeight = 0f
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
                            x = individualBarTopLeft.x + individualBarRectSize.width,
                            y = individualBarTopLeft.y + expandedHeight
                        )
                    )
                    drawPath(path = path, brush = brush)
                }
                accumulatedHeight += height
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
        x = if (index == 0) barWidth + gap else index * (barWidth * 3 + gap) + barWidth,
        y = topLeftY
    )

    val individualBarRectSize = Size(
        width = barWidth,
        height = height
    )
    val cornerRadius =
        if (stackBarConfig.showCurvedBar && valueIndex == stackBarData.values.lastIndex) {
            CornerRadius(barWidth / 2, barWidth / 2)
        } else {
            CornerRadius.Zero
        }
    return Triple(individualBarTopLeft, individualBarRectSize, cornerRadius)
}

@Composable
internal fun StackBarChartScaffold(
    maxValue: Float,
    bottomPadding: Dp,
    leftPadding: Dp,
    hasNegativeValues: Boolean,
    showAxisLines: Boolean,
    showGridLines: Boolean,
    textMeasurer: TextMeasurer,
    modifier: Modifier = Modifier,
    displayData: List<StackBarData> = emptyList(),
    barChartColorConfig: BarChartColorConfig = BarChartColorConfig.default(),
    labelConfig: LabelConfig = LabelConfig.default(),
    onBarClick: (Offset) -> Unit = {},
    content: DrawScope.(Float, Float) -> Unit,
) {
    val dataCount = displayData.count()

    val xPositions = remember { mutableStateListOf<Float>() }
    val labelTexts = remember { mutableStateListOf<String>() }
    var localCanvasWidth by remember { mutableStateOf(0f) }
    var localCanvasHeight by remember { mutableStateOf(0f) }
    var calculatedXAxisYPosition by remember { mutableStateOf(0f) }

    val helperModifier = Modifier.drawWithCache {
        localCanvasWidth = size.width
        localCanvasHeight = size.height
        calculatedXAxisYPosition = if (hasNegativeValues) localCanvasHeight / 2 else localCanvasHeight

        if (displayData.isNotEmpty()) {
            val count = displayData.count()
            val barWidthForXLabels = localCanvasWidth * 9 / (count * 10) // Approximation from old scaffold
            val gapForXLabels = localCanvasWidth / (count * 10) // Approximation from old scaffold

            xPositions.clear()
            labelTexts.clear()
            displayData.forEachIndexed { index, data ->
                val itemCenterOffset = (index * (barWidthForXLabels + gapForXLabels)) + (barWidthForXLabels / 2)
                xPositions.add(itemCenterOffset)
                labelTexts.add(data.label)
            }
        }
        onDrawBehind {}
    }

    Canvas(
        modifier = modifier
            .padding(bottom = bottomPadding, start = leftPadding) // Existing padding
            .fillMaxSize()
            .then(helperModifier)
            .then(
                if (showAxisLines) {
                    Modifier.CommonDrawAxisLines(
                        axisColor = barChartColorConfig.axisLineColor,
                        strokeWidth = 1.dp.toPx(), // Consistent with other charts
                        centerHorizontallyIfNegative = hasNegativeValues
                    )
                } else Modifier
            )
            .then(
                if (showGridLines) {
                    Modifier.CommonDrawHorizontalGridLines(
                        gridLineColor = barChartColorConfig.gridLineColor,
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = null,
                        steps = 4,
                        centerHorizontallyIfNegative = hasNegativeValues
                    )
                } else Modifier
            )
            .then(
                if (labelConfig.showYLabel) { // only add modifier if labels are to be shown
                    Modifier.CommonDrawYAxisLabels(
                        labelConfig = labelConfig,
                        textMeasurer = textMeasurer,
                        minValue = if (hasNegativeValues) -maxValue else 0f,
                        maxValue = maxValue,
                        axisColor = labelConfig.textColor,
                        steps = 4,
                        dataCount = dataCount,
                        fontSizeSelector = { cw, _, dc -> if (dc > 0) (cw / dc / 10).sp else 12.sp },
                        labelFormatter = { value -> value.toInt().toString().take(4) } // To Int then take 4
                    )
                } else Modifier
            )
            .then(
                if (labelConfig.showXLabel && dataCount > 0) { // only add modifier if labels are to be shown
                    Modifier.CommonDrawXAxisLabels(
                        labelConfig = labelConfig,
                        textMeasurer = textMeasurer,
                        xPositions = xPositions,
                        labelTexts = labelTexts,
                        xAxisYPosition = calculatedXAxisYPosition,
                        dataCount = dataCount,
                        fontSizeSelector = { cw, _, dc ->
                            if (dc > 0) {
                                val barWidthApprox = cw * 9 / (dc * 10)
                                (barWidthApprox / 4).sp
                            } else 12.sp
                        },
                        labelFormatter = { text, _ -> text },
                        textCharCountProvider = null // Original did not truncate based on getXLabelTextCharCount
                    )
                } else Modifier
            )
            .pointerInput(Unit) { detectTapGestures { onBarClick(it) } }
    ) {
        content(size.width, size.height)
    }
}
// Removed internal fun getDisplayData as it's replaced by padListToMinimumCount
