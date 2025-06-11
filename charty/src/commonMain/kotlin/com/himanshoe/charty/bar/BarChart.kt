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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMaxOfOrNull
import com.himanshoe.charty.bar.config.BarChartColorConfig
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.config.BarTooltip
import com.himanshoe.charty.bar.model.BarData
// import com.himanshoe.charty.bar.modifier.drawAxisLineForVerticalChart // To be removed
// import com.himanshoe.charty.bar.modifier.drawRangeLinesForVerticalChart // To be removed
import com.himanshoe.charty.common.ChartColor
import com.himanshoe.charty.common.LabelConfig
import com.himanshoe.charty.common.TargetConfig
import com.himanshoe.charty.common.modifiers.CommonDrawAxisLines
import com.himanshoe.charty.common.modifiers.CommonDrawHorizontalGridLines
import com.himanshoe.charty.common.modifiers.CommonDrawXAxisLabels
import com.himanshoe.charty.common.modifiers.CommonDrawYAxisLabels
import com.himanshoe.charty.common.asSolidChartColor
import com.himanshoe.charty.common.drawTargetLineIfNeeded
import com.himanshoe.charty.common.getDrawingPath
import com.himanshoe.charty.common.getTetStyle
import com.himanshoe.charty.common.getXLabelTextCharCount
import com.himanshoe.charty.common.utils.calculateChartPaddings
import com.himanshoe.charty.common.utils.calculateValueRange
import com.himanshoe.charty.common.utils.isClickInsideRect
import com.himanshoe.charty.common.utils.padListToMinimumCount
import kotlin.math.absoluteValue
import androidx.compose.ui.graphics.PathEffect // Required for CommonDrawHorizontalGridLines
import androidx.compose.runtime.mutableStateListOf

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
    val allValuesAreZero = remember(barData) { barData.all { it.yValue == 0f } } // Kept for other logic like padding

    val (minValue, maxValue) = remember(barData, barChartConfig.drawNegativeValueChart) {
        calculateValueRange(
            data = barData,
            yValueSelector = { it.yValue },
            handleAllZeroAsSpecialMax = true,
            defaultMinIfAllPositive = 0f,
            defaultMaxIfAllNegative = 0f
        )
    }

    val hasNegativeValues = remember(barData) { barData.fastAny { it.yValue < 0 } } // Kept for other logic
    val displayData = remember(barData, barChartConfig.minimumBarCount) {
        padListToMinimumCount(
            originalList = barData,
            minimumCount = barChartConfig.minimumBarCount,
            defaultItemFactory = { BarData(0F, " ", Color.Unspecified.asSolidChartColor()) }
        )
    }
    val canDrawNegativeChart = hasNegativeValues && barChartConfig.drawNegativeValueChart
    val textMeasurer = rememberTextMeasurer()
    // val bottomLabelPadding = if (labelConfig.showXLabel && !hasNegativeValues) 8.dp else 0.dp // Kept for reference, was removed
    // val leftPadding = if (labelConfig.showYLabel && !allValuesAreZero) 24.dp else 0.dp // Kept for reference, was removed
    // val topPadding = if (barTooltip != null) 24.dp else 0.dp // Removed
    // val bottomPadding = if (canDrawNegativeChart) 24.dp else bottomLabelPadding // Removed

    val chartPaddings = calculateChartPaddings(
        labelConfig = labelConfig,
        yAxisLabelWidth = 24.dp, // Standard Y axis label width
        xAxisLabelHeight = 8.dp, // Standard X axis label height (when X axis is at bottom)
        xAxisLabelHeightWhenNegative = 24.dp, // X axis label height (when X axis is centered)
        hasNegativeValuesForXAxis = canDrawNegativeChart, // In BarChart, canDrawNegativeChart means X axis is centered
        canDrawNegativeChart = canDrawNegativeChart,
        allYValuesAreZero = allValuesAreZero,
        hasTopFixedPadding = barTooltip != null,
        topFixedPaddingValue = 24.dp // Standard tooltip padding
    )

    var clickedOffset by mutableStateOf<Offset?>(null)
    var clickedBarIndex by mutableIntStateOf(-1)

    BarChartCanvasScaffold(
        modifier = modifier.padding( // Padding modifier
            start = chartPaddings.start,
            top = chartPaddings.top,
            bottom = chartPaddings.bottom,
            end = chartPaddings.end
        ),
        showAxisLines = barChartConfig.showAxisLines,
        showRangeLines = barChartConfig.showGridLines,
        axisLineColor = barChartColorConfig.axisLineColor, // Axis line color
        rangeLineColor = barChartColorConfig.gridLineColor, // Range line color
        canDrawNegativeChart = canDrawNegativeChart, // Can draw negative chart
        labelConfig = labelConfig, // Label config
        onClick = { offset -> // On click
            clickedOffset = offset
        },
        data = { displayData }, // Data
    ) { canvasHeight, gap, barWidth -> // Content

        target?.let { // Target line
            // Ensure target is within the calculated min/max range, unless maxValue is the special -1F
            if (maxValue != -1f) { // Check if maxValue is the special case for all zeros
                require(it in minValue..maxValue) { "Target value $it should be between $minValue and $maxValue" }
            } else if (it != 0f) { // If all values are zero (max is -1F), target must also be 0
                require(it == 0f) { "Target value $it must be 0 when all chart values are 0." }
            }

            val targetLineY = if (hasNegativeValues) canvasHeight / 2 else canvasHeight
            // Handle division by zero if maxValue is 0 or the special -1F.
            // If maxValue is -1F (all zeros), target must be 0, so targetLineYPosition should be where 0 is.
            val effectiveMaxValueForTarget = if (maxValue == -1f) 0f else maxValue
            val targetRatio = if (effectiveMaxValueForTarget == 0f) 0f else (it / effectiveMaxValueForTarget)

            val targetLineYPosition = targetLineY - (targetRatio * targetLineY)

            drawTargetLineIfNeeded( // Draw target line if needed
                canvasWidth = size.width, // Canvas width
                targetConfig = targetConfig,
                yPoint = targetLineYPosition
            )
        }

        displayData.fastForEachIndexed { index, barDataEntry -> // Use a different name for barData in the loop
            // Handle division by zero for height calculation if maxValue is 0 or the special -1F
            val effectiveMaxValueForHeight = if (maxValue == -1f) 0f else maxValue // if all zero, treat max as 0 for height
            val height = if (effectiveMaxValueForHeight == 0f) 0f else (barDataEntry.yValue / effectiveMaxValueForHeight) * canvasHeight
            val maxHeight = if (effectiveMaxValueForHeight == 0f) 0f else (effectiveMaxValueForHeight / effectiveMaxValueForHeight) * canvasHeight

            val yAxis = canvasHeight / 2 // yAxis position
            val (topLeftY, backgroundTopLeftY) = getBarAndBackgroundBarTopLeft( // Get top left Y and background top left Y
                canDrawNegativeChart = canDrawNegativeChart, // Can draw negative chart
                barData = barDataEntry, // Bar data
                yAxis = yAxis, // yAxis position
                height = height, // Height
                canvasHeight = canvasHeight, // Canvas height
                maxHeight = maxHeight, // Max height
            )
            val color = getBarColor( // Get bar color
                barData = barDataEntry, // Bar data
                barChartColorConfig = barChartColorConfig, // Bar chart color config
            )

            val (individualBarTopLeft, individualBarRectSize) = getBarTopLeftAndRectSize( // Get bar top left and rect size
                index = index, // Index
                barWidth = barWidth, // Bar width
                gap = gap, // Gap
                clickedBarIndex = clickedBarIndex, // Clicked bar index
                barData = barDataEntry, // Bar data
                topLeftY = topLeftY, // Top left Y
                height = height, // Height
                canDrawNegativeChart = canDrawNegativeChart // Can draw negative chart
            )
            clickedOffset?.let { offset -> // Clicked offset
                if (isClickInsideRect(offset, individualBarTopLeft, individualBarRectSize)) { // Is click inside bar
                    clickedBarIndex = index // Set clicked bar index
                    onBarClick(index, barDataEntry) // On bar click
                    clickedOffset = null // Reset clicked offset
                    clickedBarIndex = -1 // Reset clicked bar index
                }
            }
            val textCharCount = labelConfig.getXLabelTextCharCount( // Get X label text char count
                xValue = barDataEntry.xValue, // X value
                displayDataCount = displayData.count() // Display data count
            )

            val textSizeFactor = if (displayData.count() <= 13) 4 else 2 // Text size factor
            val textLayoutResult = textMeasurer.measure( // Measure text
                text = barDataEntry.xValue.toString().take(textCharCount), // Text
                style = labelConfig.getTetStyle(fontSize = (barWidth / textSizeFactor).toSp()), // Style
                overflow = TextOverflow.Clip, // Overflow
                maxLines = 1, // Max lines
            )

            val (textOffsetY, calculatedCornerRadius) = getTextYOffsetAndCornerRadius( // Get text Y offset and corner radius
                barData = barDataEntry, // Bar data
                individualBarTopLeft = individualBarTopLeft,
                textLayoutResult = textLayoutResult,
                individualBarRectSize = individualBarRectSize,
                barChartConfig = barChartConfig,
                barWidth = barWidth
            )
            val cornerRadius = getCornerRadius(barChartConfig, calculatedCornerRadius)

            if (!allValuesAreZero && barDataEntry.yValue != 0F) { // If not all values are zero and bar data yValue is not 0F
                backgroundColorBar( // Draw background color bar
                    barData = barDataEntry, // Bar data
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
                topLeftCornerRadius = if (barDataEntry.yValue >= 0) cornerRadius else CornerRadius.Zero, // Top left corner radius
                topRightCornerRadius = if (barDataEntry.yValue >= 0) cornerRadius else CornerRadius.Zero, // Top right corner radius
                bottomLeftCornerRadius = if (barDataEntry.yValue < 0) cornerRadius else CornerRadius.Zero, // Bottom left corner radius
                bottomRightCornerRadius = if (barDataEntry.yValue < 0) cornerRadius else CornerRadius.Zero // Bottom right corner radius
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
                // X-Label drawing logic is now moved to CommonDrawXAxisLabels modifier in BarChartCanvasScaffold
                if (!allValuesAreZero && barTooltip != null) {
                    drawTooltip(
                        textMeasurer = textMeasurer,
                        barData = barDataEntry, // Bar data
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
        BarTooltip.BarTop -> if (barData.yValue >= 0) { // If bar data yValue is greater than or equal to 0
            individualBarTopLeft.y - tooltipTextLayoutResult.size.height - gap // Tooltip Y offset
        } else { // Else
            individualBarTopLeft.y + individualBarRectSize.height + gap // Tooltip Y offset
        }

        BarTooltip.GraphTop -> if (barData.yValue >= 0) { // If bar data yValue is greater than or equal to 0
            backgroundTopLeftY - tooltipTextLayoutResult.size.height - gap // Tooltip Y offset
        } else { // Else
            backgroundTopLeftY + individualBarRectSize.height + gap // Tooltip Y offset
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
    val textOffsetY = if (barData.yValue < 0) { // If bar data yValue is less than 0
        individualBarTopLeft.y - textLayoutResult.size.height - 5 // Text Y offset
    } else { // Else
        individualBarTopLeft.y + individualBarRectSize.height + 5 // Text Y offset
    }
    val cornerRadius = if (barChartConfig.showCurvedBar) { // If show curved bar
        CornerRadius( // Corner radius
            x = barWidth / 2, // X
            y = barWidth / 2 // Y
        )
    } else { // Else
        CornerRadius.Zero // Corner radius zero
    }
    return Pair(textOffsetY, cornerRadius) // Return text Y offset and corner radius
}

private fun getBarTopLeftAndRectSize( // Get bar top left and rect size
    index: Int, // Index
    barWidth: Float, // Bar width
    gap: Float, // Gap
    clickedBarIndex: Int, // Clicked bar index
    barData: BarData, // Bar data
    topLeftY: Float, // Top left Y
    height: Float, // Height
    canDrawNegativeChart: Boolean // Can draw negative chart
): Pair<Offset, Size> { // Return offset and size
    val isClickedBar = clickedBarIndex == index // Is clicked bar
    val heightAdjustment = // Height adjustment
        if (isClickedBar) height.absoluteValue * 0.02F / (if (canDrawNegativeChart) 2 else 1) else 0f // If is clicked bar then height absolute value * 0.02F / (if can draw negative chart then 2 else 1) else 0f

    val xOffset = index * (barWidth + gap) // X offset
    val individualBarTopLeft = Offset( // Individual bar top left
        x = xOffset, // X
        y = if (barData.yValue < 0) { // If bar data yValue is less than 0
            topLeftY // Top left Y
        } else { // Else
            topLeftY - heightAdjustment // Top left Y - height adjustment
        }
    )

    val individualBarRectSize = Size( // Individual bar rect size
        width = barWidth, // Width
        height = if (isClickedBar) { // If is clicked bar
            height.absoluteValue * 1.02F / (if (canDrawNegativeChart) 2 else 1) // Height absolute value * 1.02F / (if can draw negative chart then 2 else 1)
        } else { // Else
            height.absoluteValue / (if (canDrawNegativeChart) 2 else 1) // Height absolute value / (if can draw negative chart then 2 else 1)
        }
    )
    return Pair(individualBarTopLeft, individualBarRectSize) // Return individual bar top left and individual bar rect size
}

internal fun getBarAndBackgroundBarTopLeft( // Get bar and background bar top left
    canDrawNegativeChart: Boolean, // Can draw negative chart
    barData: BarData, // Bar data
    yAxis: Float, // yAxis position
    height: Float, // Height
    canvasHeight: Float, // Canvas height
    maxHeight: Float, // Max height
): Pair<Float, Float> { // Return float and float
    val topLeftY = if (canDrawNegativeChart) { // If can draw negative chart
        if (barData.yValue < 0) yAxis else yAxis - height / 2 // If bar data yValue is less than 0 then yAxis else yAxis - height / 2
    } else { // Else
        canvasHeight - height // Canvas height - height
    }
    val backgroundTopLeftY = if (canDrawNegativeChart) { // If can draw negative chart
        if (barData.yValue < 0) yAxis else yAxis - maxHeight / 2 // If bar data yValue is less than 0 then yAxis else yAxis - maxHeight / 2
    } else { // Else
        canvasHeight - maxHeight // Canvas height - maxHeight
    }
    return Pair(topLeftY, backgroundTopLeftY) // Return top left Y and background top left Y
}

private fun DrawScope.backgroundColorBar( // Draw background color bar
    barData: BarData, // Bar data
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
// internal fun getDisplayData has been removed and replaced by padListToMinimumCount

// internal fun isClickInsideBar has been moved to common.utils.GeometryUtils

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
    val barDataList = data() // Renamed to avoid conflict with barData in Canvas scope
    val textMeasurer = rememberTextMeasurer()
    // maxValue and minValue are now calculated in BarChartContent and passed to CommonDrawYAxisLabels
    // However, CommonDrawYAxisLabels (called by this Scaffold) needs them.
    // This means BarChartContent's minValue and maxValue need to be accessible here.
    // For now, we re-calculate them here, but this is redundant if we pass them down from BarChartContent.
    // This highlights a potential need to either pass min/max to Scaffold or have Scaffold calculate them once.
    val (minValue, maxValue) = remember(barDataList, canDrawNegativeChart) { // Added canDrawNegativeChart to key
        calculateValueRange(
            data = barDataList,
            yValueSelector = { it.yValue },
            handleAllZeroAsSpecialMax = true, // Bar chart specific behavior
            defaultMinIfAllPositive = 0f,
            defaultMaxIfAllNegative = 0f
        )
    }

    val xPositions = remember { mutableStateListOf<Float>() }
    val labelTexts = remember { mutableStateListOf<String>() }
    var localCanvasWidth by remember { mutableStateOf(0f) }
    var localCanvasHeight by remember { mutableStateOf(0f) }
    var calculatedXAxisYPosition by remember { mutableStateOf(0f) }
    var calculatedBarWidth by remember { mutableStateOf(0f) }


    val helperModifier = Modifier.drawWithCache {
        localCanvasWidth = size.width
        localCanvasHeight = size.height
        calculatedXAxisYPosition = if (canDrawNegativeChart) localCanvasHeight / 2 else localCanvasHeight

        if (barDataList.isNotEmpty()) {
            val count = barDataList.count()
            val gap = localCanvasWidth / (count * 10) // Approximation, consider if this is identical to canvas scope's gap
            calculatedBarWidth = (localCanvasWidth - gap * (count - 1)) / count

            xPositions.clear()
            labelTexts.clear()
            barDataList.forEachIndexed { index, data ->
                xPositions.add(index * (calculatedBarWidth + gap) + calculatedBarWidth / 2)
                labelTexts.add(data.xValue.toString())
            }
        }
        onDrawBehind {} // No drawing, just calculations
    }

    Canvas(
        modifier = modifier
            .then(helperModifier) // Calculate positions and dimensions first
            .then(
                if (showAxisLines) {
                    Modifier.CommonDrawAxisLines(
                        axisColor = axisLineColor,
                        strokeWidth = 2f, // from old drawAxisLineForVerticalChart
                        centerHorizontallyIfNegative = canDrawNegativeChart
                    )
                } else Modifier
            )
            .then(
                if (showRangeLines) {
                    Modifier.CommonDrawHorizontalGridLines(
                        gridLineColor = rangeLineColor,
                        strokeWidth = 1f, // from old drawRangeLinesForVerticalChart
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f), // from old
                        steps = 3, // Original rangeLineCount was 3 (3 above, 3 below center)
                        centerHorizontallyIfNegative = canDrawNegativeChart
                    )
                } else Modifier
            )
            .then(
                if (labelConfig.showXLabel && barDataList.isNotEmpty()) {
                    Modifier.CommonDrawXAxisLabels(
                        labelConfig = labelConfig,
                        textMeasurer = textMeasurer,
                        xPositions = xPositions,
                        labelTexts = labelTexts,
                        xAxisYPosition = calculatedXAxisYPosition,
                        dataCount = barDataList.count(),
                        fontSizeSelector = { canvasWidth, _, dataCountValue ->
                            // Approximate barWidth for font size calculation
                            // This is a bit of a hack as calculatedBarWidth is from helperModifier's scope
                            // but fontSizeSelector is called from CommonDrawXAxisLabels' scope.
                            // For simplicity, we use the `calculatedBarWidth` captured in BarChartCanvasScaffold's scope.
                            // A more robust way might involve passing barWidth directly if font size is critical.
                            val currentBarWidth = calculatedBarWidth
                            if (dataCountValue > 0 && currentBarWidth > 0) {
                                (currentBarWidth / (if (dataCountValue <= 13) 4 else 2)).sp
                            } else 12.sp
                        },
                        labelFormatter = { text, _ -> text }, // Bar chart x-labels were not specially formatted beyond char count
                        textCharCountProvider = { text, count ->
                            labelConfig.getXLabelTextCharCount(xValue = text, displayDataCount = count)
                        }
                    )
                } else Modifier
            )
            .then(
                if (labelConfig.showYLabel) {
                Modifier.CommonDrawYAxisLabels(
                    labelConfig = labelConfig,
                    textMeasurer = textMeasurer,
                    minValue = minValue,
                    maxValue = maxValue,
                    axisColor = labelConfig.textColor,
                    steps = 4,
                    dataCount = barDataList.count(),
                    fontSizeSelector = { canvasWidth, _, dataCountValue ->
                        if (dataCountValue > 0) (canvasWidth / dataCountValue / 10).sp else 12.sp
                    },
                    labelFormatter = { value -> value.toString().take(4) }
                )
            } else Modifier
            )
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures { onClick(it) } }
    ) {
        // content lambda params (canvasHeight, gap, barWidth) are now calculated in the main Canvas scope
        // These are specific to the bar drawing part, not the axis/labels which are now modifiers
        val currentCanvasHeight = size.height
        val currentGap = if (barDataList.isNotEmpty()) size.width / (barDataList.count() * 10) else 0f
        val currentBarWidth = if (barDataList.isNotEmpty()) (size.width - currentGap * (barDataList.count() - 1)) / barDataList.count() else 0f
        content(currentCanvasHeight, currentGap, currentBarWidth)
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
