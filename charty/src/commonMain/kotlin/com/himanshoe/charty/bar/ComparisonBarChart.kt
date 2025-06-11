package com.himanshoe.charty.bar

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastFlatMap
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.bar.config.ComparisonBarChartConfig
import com.himanshoe.charty.bar.model.ComparisonBarData
// import com.himanshoe.charty.bar.modifier.drawAxesAndGridLines // To be removed
import com.himanshoe.charty.common.LabelConfig
import com.himanshoe.charty.common.asSolidChartColor
import com.himanshoe.charty.common.getDrawingPath
import com.himanshoe.charty.common.getTetStyle
import com.himanshoe.charty.common.getXLabelTextCharCount
import com.himanshoe.charty.common.modifiers.CommonDrawAxisLines
import com.himanshoe.charty.common.modifiers.CommonDrawHorizontalGridLines
import com.himanshoe.charty.common.modifiers.CommonDrawXAxisLabels
import com.himanshoe.charty.common.modifiers.CommonDrawYAxisLabels
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.draw.drawWithCache

/**
 * A composable function that displays a comparison bar chart.
 *
 * @param data A lambda function that returns a list of `ComparisonBarData` representing the data points for the comparison bar chart.
 * @param modifier A `Modifier` for customizing the layout or drawing behavior of the chart.
 * @param labelConfig A `LabelConfig` object for configuring the labels on the chart.
 * @param comparisonBarChartConfig A `ComparisonBarChartConfig` object for configuring the chart's appearance and behavior.
 * @param onGroupClicked A lambda function to handle click events on the bars. It receives the index of the clicked bar as a parameter.
 */
@Composable
fun ComparisonBarChart(
    data: () -> List<ComparisonBarData>,
    modifier: Modifier = Modifier,
    labelConfig: LabelConfig = LabelConfig.default(),
    comparisonBarChartConfig: ComparisonBarChartConfig = ComparisonBarChartConfig.default(),
    onGroupClicked: (Int) -> Unit = {}
) {
    ComparisonBarContent(
        labelConfig = labelConfig,
        modifier = modifier,
        data = data,
        comparisonBarChartConfig = comparisonBarChartConfig,
        onGroupClicked = onGroupClicked
    )
}

@Composable
private fun ComparisonBarContent(
    data: () -> List<ComparisonBarData>,
    labelConfig: LabelConfig,
    comparisonBarChartConfig: ComparisonBarChartConfig,
    modifier: Modifier = Modifier,
    onGroupClicked: (Int) -> Unit = {}
) {
    var selectedCategory by remember { mutableStateOf(-1) }
    val textMeasurer = rememberTextMeasurer()
    val bottomPadding = if (labelConfig.showXLabel) 24.dp else 0.dp
    val leftPadding = if (labelConfig.showYLabel) 24.dp else 0.dp

    val dataList = data()
    val maxValue = remember(dataList) { dataList.fastFlatMap { it.bars }.maxOfOrNull { it } ?: 0f }
    val groupCount = dataList.size
    val maxBarsCount = remember(dataList) { dataList.maxOfOrNull { it.bars.size } ?: 0 }

    val xPositions = remember { mutableStateListOf<Float>() }
    val labelTexts = remember { mutableStateListOf<String>() }
    var localCanvasWidth by remember { mutableStateOf(0f) }
    var localCanvasHeight by remember { mutableStateOf(0f) }
    var xAxisYPositionState by remember { mutableStateOf(0f) }

    val helperModifier = Modifier.drawWithCache {
        localCanvasWidth = size.width
        localCanvasHeight = size.height
        xAxisYPositionState = localCanvasHeight // X-axis at the bottom for ComparisonBarChart

        if (groupCount > 0) {
            val groupWidth = localCanvasWidth / groupCount
            xPositions.clear()
            labelTexts.clear()
            dataList.forEachIndexed { index, group ->
                xPositions.add(index * groupWidth + groupWidth / 2)
                labelTexts.add(group.label)
            }
        }
        onDrawBehind {}
    }

    Canvas(
        modifier = modifier
            .padding(bottom = bottomPadding, start = leftPadding)
            .fillMaxSize()
            .then(helperModifier)
            .then(
                if (comparisonBarChartConfig.showAxisLines) {
                    Modifier.CommonDrawAxisLines(
                        axisColor = comparisonBarChartConfig.axisLineColor ?: Color.Black.asSolidChartColor(),
                        strokeWidth = 1.dp.toPx(), // Old: 2f, but 1.dp.toPx() is more common
                        centerHorizontallyIfNegative = false
                    )
                } else Modifier
            )
            .then(
                if (comparisonBarChartConfig.showGridLines) {
                    Modifier.CommonDrawHorizontalGridLines(
                        gridLineColor = comparisonBarChartConfig.gridLineColor ?: Color.LightGray.asSolidChartColor(),
                        strokeWidth = 1.dp.toPx(), // Old: 1f
                        pathEffect = null, // Old had no pathEffect for grid lines
                        steps = 4, // Old had 4 steps
                        centerHorizontallyIfNegative = false
                    )
                } else Modifier
            )
            .then(
                Modifier.CommonDrawYAxisLabels(
                    labelConfig = labelConfig,
                    textMeasurer = textMeasurer,
                    minValue = 0f, // Comparison chart likely starts Y at 0
                    maxValue = maxValue,
                    axisColor = labelConfig.textColor, // Standard practice
                    steps = 4, // Matches old grid
                    dataCount = groupCount, // dataCount for Y-axis labels might not be directly relevant here as in bar/line
                    fontSizeSelector = { _, _, _ -> 12.sp }, // Matches old Y-label font size
                    labelFormatter = { value -> value.toInt().toString() } // Format to Int as it's usually counts/values
                )
            )
            .then(
                if (labelConfig.showXLabel && groupCount > 0) {
                    Modifier.CommonDrawXAxisLabels(
                        labelConfig = labelConfig,
                        textMeasurer = textMeasurer,
                        xPositions = xPositions,
                        labelTexts = labelTexts,
                        xAxisYPosition = xAxisYPositionState,
                        dataCount = groupCount,
                        fontSizeSelector = { cw, _, gc ->
                            if (gc > 0) (cw / (gc * (if (gc <= 5) 20 else 40))).sp else 12.sp
                        },
                        labelFormatter = { text, _ -> text },
                        textCharCountProvider = { text, count ->
                            labelConfig.getXLabelTextCharCount(xValue = text, displayDataCount = count)
                        }
                    )
                } else Modifier
            )
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val groupWidth = localCanvasWidth / groupCount
                    val clickedGroupIndex = (offset.x / groupWidth).toInt()
                    if (clickedGroupIndex in dataList.indices) {
                        selectedCategory = clickedGroupIndex
                        onGroupClicked(clickedGroupIndex)
                    }
                }
            }
    ) {
        // Bars are drawn in the main Canvas DrawScope
        val currentCanvasWidth = size.width // Use size.width from this scope
        val currentCanvasHeight = size.height // Use size.height from this scope
        val groupWidth = if (groupCount > 0) currentCanvasWidth / groupCount else 0f
        val barWidth = if (maxBarsCount > 0) groupWidth / (maxBarsCount * 2) else 0f

        val cornerRadius = if (comparisonBarChartConfig.showCurvedBar) {
            CornerRadius(
                x = barWidth / 2,
                y = barWidth / 2
            )
        } else {
            CornerRadius.Zero
        }

        dataList.fastForEachIndexed { groupIndex, group ->
            val groupStartX = groupIndex * groupWidth
            if (groupIndex == selectedCategory) {
                drawBar(
                    cornerRadius = cornerRadius,
                    offset = Offset(groupStartX, 0f),
                    size = Size(width = groupWidth, height = canvasHeight),
                    brush = SolidColor(Color.Gray.copy(alpha = 0.1F))
                )
            }
            group.bars.fastForEachIndexed { barIndex, barValue ->
                val barHeight = (barValue / maxValue) * canvasHeight

                val barX = groupStartX +
                        (groupWidth - (group.bars.size * (barWidth + barWidth / 4))) / 2 +
                        barIndex * (barWidth + barWidth / 4)

                val brush = Brush.linearGradient(
                    colors = group.colors[barIndex].value,
                    start = Offset(
                        x = barX,
                        y = canvasHeight - barHeight
                    ),
                    end = Offset(
                        x = barX + barWidth,
                        y = canvasHeight
                    )
                )

                drawBar(
                    brush = brush,
                    size = Size(width = barWidth, height = barHeight),
                    cornerRadius = cornerRadius,
                    offset = Offset(barX, canvasHeight - barHeight)
                )
            }

            for (barIndex in group.bars.size until maxBarsCount) {
                val totalBarWidth = barWidth + barWidth / 4
                val groupOffset = (groupWidth - (group.bars.size * totalBarWidth)) / 2
                val barX = groupStartX + groupOffset + barIndex * totalBarWidth
                drawBar(
                    size = Size(width = barWidth, height = 0F),
                    brush = SolidColor(Color.Transparent),
                    cornerRadius = cornerRadius,
                    offset = Offset(barX, canvasHeight)
                )
            }

            // X-Label drawing logic is now handled by CommonDrawXAxisLabels modifier
        }
    }
}

private fun DrawScope.drawBar(
    size: Size,
    brush: Brush,
    cornerRadius: CornerRadius,
    offset: Offset
) {
    getDrawingPath(
        individualBarTopLeft = offset,
        individualBarRectSize = size,
        cornerRadius = cornerRadius
    ).let { path ->
        drawPath(path = path, brush = brush)
    }
}
