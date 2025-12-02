package com.himanshoe.charty.bar

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.bar.config.ComparisonBarChartConfig
import com.himanshoe.charty.bar.config.ComparisonBarSegment
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.bar.ext.calculateMaxValue
import com.himanshoe.charty.bar.ext.calculateMinValue
import com.himanshoe.charty.bar.ext.getAllValues
import com.himanshoe.charty.bar.ext.getLabels
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.draw.drawReferenceLine
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.tooltip.drawTooltip

private const val DEFAULT_AXIS_STEPS = 6
private const val BAR_WIDTH_FRACTION = 0.8f
private const val GROUP_PADDING_FRACTION = 0.1f

/**
 * Comparison Bar Chart - Display multiple bars per category for comparison
 *
 * A comparison bar chart displays multiple data series side-by-side for each category.
 * Perfect for comparing sub-categories or multiple metrics within each main category.
 * Formerly known as Grouped Bar Chart.
 *
 * Usage:
 * ```kotlin
 * ComparisonBarChart(
 *     data = {
 *         listOf(
 *             BarGroup("Q1", listOf(45f, 52f)),
 *             BarGroup("Q2", listOf(58f, 63f)),
 *             BarGroup("Q3", listOf(72f, 68f))
 *         )
 *     },
 *     colors = ChartyColor.Gradient(
 *         listOf(Color(0xFFE91E63), Color(0xFF2196F3))
 *     )
 * )
 * ```
 *
 * @param data Lambda returning list of bar groups, each containing multiple values. Each BarGroup should specify its own colors via BarGroup.colors property
 * @param modifier Modifier for the chart
 * @param comparisonConfig Configuration for comparison chart behavior (e.g., negative values draw mode)
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
 * @param onBarClick Optional callback when a bar segment is clicked
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun ComparisonBarChart(
    data: () -> List<BarGroup>,
    modifier: Modifier = Modifier,
    comparisonConfig: ComparisonBarChartConfig = ComparisonBarChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    onBarClick: ((ComparisonBarSegment) -> Unit)? = null,
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Comparison bar chart data cannot be empty" }

    val (minValue, maxValue) = rememberComparisonChartValues(dataList)
    val isBelowAxisMode = comparisonConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS
    var tooltipState by remember { mutableStateOf<TooltipState?>(null) }
    val barBounds = remember { mutableListOf<Pair<Rect, ComparisonBarSegment>>() }
    val textMeasurer = rememberTextMeasurer()

    val chartModifier = createComparisonChartModifier(
        modifier = modifier,
        onBarClick = onBarClick,
        dataList = dataList,
        comparisonConfig = comparisonConfig,
        barBounds = barBounds,
        onTooltipUpdate = { tooltipState = it },
    )

    ChartScaffold(
        modifier = chartModifier,
        xLabels = dataList.getLabels(),
        yAxisConfig = createAxisConfig(minValue, maxValue, isBelowAxisMode),
        config = scaffoldConfig,
    ) { chartContext ->
        barBounds.clear()
        val baselineY = calculateBaselineY(minValue, isBelowAxisMode, chartContext)

        drawComparisonBars(
            dataList = dataList,
            chartContext = chartContext,
            comparisonConfig = comparisonConfig,
            baselineY = baselineY,
            onBarClick = onBarClick,
            barBounds = barBounds,
        )

        drawReferenceLineIfNeeded(comparisonConfig, chartContext, textMeasurer)
        drawTooltipIfNeeded(tooltipState, comparisonConfig, textMeasurer, chartContext)
    }
}

@Composable
private fun rememberComparisonChartValues(
    dataList: List<BarGroup>,
): Pair<Float, Float> {
    return remember(dataList) {
        val allValues = dataList.getAllValues()
        val calculatedMin = calculateMinValue(allValues)
        val calculatedMax = calculateMaxValue(allValues)
        val finalMin = if (calculatedMin >= 0f) 0f else calculatedMin

        finalMin to calculatedMax
    }
}

@Composable
private fun createComparisonChartModifier(
    onBarClick: ((ComparisonBarSegment) -> Unit)?,
    dataList: List<BarGroup>,
    comparisonConfig: ComparisonBarChartConfig,
    barBounds: List<Pair<Rect, ComparisonBarSegment>>,
    onTooltipUpdate: (TooltipState?) -> Unit,
    modifier: Modifier = Modifier,
): Modifier {
    return if (onBarClick != null) {
        modifier.pointerInput(dataList, comparisonConfig, onBarClick) {
            detectTapGestures { offset ->
                handleComparisonBarClick(offset, barBounds, onBarClick, comparisonConfig, onTooltipUpdate)
            }
        }
    } else {
        modifier
    }
}

private fun handleComparisonBarClick(
    offset: androidx.compose.ui.geometry.Offset,
    barBounds: List<Pair<Rect, ComparisonBarSegment>>,
    onBarClick: (ComparisonBarSegment) -> Unit,
    comparisonConfig: ComparisonBarChartConfig,
    onTooltipUpdate: (TooltipState?) -> Unit,
) {
    val clickedBar = barBounds.find { (rect, _) -> rect.contains(offset) }

    clickedBar?.let { (rect, segment) ->
        onBarClick.invoke(segment)
        onTooltipUpdate(
            TooltipState(
                content = comparisonConfig.tooltipFormatter(segment),
                x = rect.left,
                y = rect.top,
                barWidth = rect.width,
                position = comparisonConfig.tooltipPosition,
            ),
        )
    } ?: onTooltipUpdate(null)
}

private fun createAxisConfig(
    minValue: Float,
    maxValue: Float,
    isBelowAxisMode: Boolean,
): AxisConfig {
    return AxisConfig(
        minValue = minValue,
        maxValue = maxValue,
        steps = DEFAULT_AXIS_STEPS,
        drawAxisAtZero = isBelowAxisMode,
    )
}

private fun calculateBaselineY(
    minValue: Float,
    isBelowAxisMode: Boolean,
    chartContext: com.himanshoe.charty.common.ChartContext,
): Float {
    return if (minValue < 0f && isBelowAxisMode) {
        chartContext.convertValueToYPosition(0f)
    } else {
        chartContext.bottom
    }
}

private fun DrawScope.drawComparisonBars(
    dataList: List<BarGroup>,
    chartContext: com.himanshoe.charty.common.ChartContext,
    comparisonConfig: ComparisonBarChartConfig,
    baselineY: Float,
    onBarClick: ((ComparisonBarSegment) -> Unit)?,
    barBounds: MutableList<Pair<Rect, ComparisonBarSegment>>,
) {
    dataList.fastForEachIndexed { groupIndex, group ->
        val groupWidth = chartContext.width / dataList.size
        val barWidth = groupWidth / group.values.size * BAR_WIDTH_FRACTION

        group.values.fastForEachIndexed { barIndex, value ->
            val barX = calculateBarX(
                chartContext = chartContext,
                groupWidth = groupWidth,
                groupIndex = groupIndex,
                barWidth = barWidth,
                barIndex = barIndex,
            )

            val barValueY = chartContext.convertValueToYPosition(value)
            val isNegative = value < 0f

            val (barTop, barHeight) = calculateComparisonBarDimensions(
                value = value,
                baselineY = baselineY,
                barValueY = barValueY,
            )

            if (onBarClick != null) {
                barBounds.add(
                    Rect(
                        left = barX,
                        top = barTop,
                        right = barX + barWidth,
                        bottom = barTop + barHeight,
                    ) to ComparisonBarSegment(
                        barGroup = group,
                        barIndex = barIndex,
                        barValue = value,
                    ),
                )
            }
            val barChartyColor = getBarColor(group, barIndex)
            val barBrush = createBarBrush(barChartyColor, barTop, barHeight)
            drawRoundedBar(
                brush = barBrush,
                x = barX,
                y = barTop,
                width = barWidth,
                height = barHeight,
                isNegative = isNegative,
                isBelowAxisMode = comparisonConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS,
                cornerRadius = comparisonConfig.cornerRadius.value,
            )
        }
    }
}

private fun calculateBarX(
    chartContext: com.himanshoe.charty.common.ChartContext,
    groupWidth: Float,
    groupIndex: Int,
    barWidth: Float,
    barIndex: Int,
): Float {
    return chartContext.left +
        groupWidth * groupIndex +
        barWidth * barIndex +
        groupWidth * GROUP_PADDING_FRACTION
}

private fun calculateComparisonBarDimensions(
    value: Float,
    baselineY: Float,
    barValueY: Float,
): Pair<Float, Float> {
    val isNegative = value < 0f

    return if (isNegative) {
        val barTop = baselineY
        val barHeight = barValueY - baselineY
        barTop to barHeight
    } else {
        val barHeight = baselineY - barValueY
        val barTop = baselineY - barHeight
        barTop to barHeight
    }
}

private fun getBarColor(
    group: BarGroup,
    barIndex: Int,
): ChartyColor {
    require(group.colors != null) {
        "ComparisonBarChart requires each BarGroup to specify colors. Please set the 'colors' property in BarGroup."
    }
    require(barIndex < group.colors.size) {
        "BarGroup '${group.label}' has ${group.values.size} values but only " +
            "${group.colors.size} colors. Please provide a color for each value."
    }
    return group.colors[barIndex]
}

private fun createBarBrush(
    barChartyColor: ChartyColor,
    barTop: Float,
    barHeight: Float,
): Brush {
    return when (barChartyColor) {
        is ChartyColor.Solid -> Brush.verticalGradient(
            colors = listOf(barChartyColor.color, barChartyColor.color),
            startY = barTop,
            endY = barTop + barHeight,
        )

        is ChartyColor.Gradient -> Brush.verticalGradient(
            colors = barChartyColor.colors,
            startY = barTop,
            endY = barTop + barHeight,
        )
    }
}

private fun DrawScope.drawReferenceLineIfNeeded(
    comparisonConfig: ComparisonBarChartConfig,
    chartContext: com.himanshoe.charty.common.ChartContext,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
) {
    comparisonConfig.referenceLine?.let { referenceLineConfig ->
        drawReferenceLine(
            chartContext = chartContext,
            orientation = ChartOrientation.VERTICAL,
            config = referenceLineConfig,
            textMeasurer = textMeasurer,
        )
    }
}

private fun DrawScope.drawTooltipIfNeeded(
    tooltipState: TooltipState?,
    comparisonConfig: ComparisonBarChartConfig,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    chartContext: com.himanshoe.charty.common.ChartContext,
) {
    tooltipState?.let { state ->
        drawTooltip(
            tooltipState = state,
            config = comparisonConfig.tooltipConfig,
            textMeasurer = textMeasurer,
            chartWidth = chartContext.right,
            chartTop = chartContext.top,
            chartBottom = chartContext.bottom,
        )
    }
}

/**
 * Helper function to draw a comparison bar with rounded corners and gradient support
 */
private fun DrawScope.drawRoundedBar(
    brush: Brush,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    isNegative: Boolean,
    isBelowAxisMode: Boolean,
    cornerRadius: Float,
) {
    val path = Path().apply {
        if (isNegative && isBelowAxisMode) {
            addRoundRect(
                RoundRect(
                    left = x,
                    top = y,
                    right = x + width,
                    bottom = y + height,
                    topLeftCornerRadius = CornerRadius.Zero,
                    topRightCornerRadius = CornerRadius.Zero,
                    bottomLeftCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    bottomRightCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                ),
            )
        } else {
            addRoundRect(
                RoundRect(
                    left = x,
                    top = y,
                    right = x + width,
                    bottom = y + height,
                    topLeftCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    topRightCornerRadius = CornerRadius(cornerRadius, cornerRadius),
                    bottomLeftCornerRadius = CornerRadius.Zero,
                    bottomRightCornerRadius = CornerRadius.Zero,
                ),
            )
        }
    }
    drawPath(path, brush)
}
