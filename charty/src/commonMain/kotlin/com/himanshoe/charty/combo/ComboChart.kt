package com.himanshoe.charty.combo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
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
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.draw.drawReferenceLine
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.gesture.CrosshairManager
import com.himanshoe.charty.common.gesture.chartCrosshairHandler
import com.himanshoe.charty.common.gesture.rememberCrosshairManager
import com.himanshoe.charty.common.rememberChartDescription
import com.himanshoe.charty.common.rememberWindowedData
import com.himanshoe.charty.common.syncInteractionDataSizes
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.tooltip.drawTooltip
import com.himanshoe.charty.common.updateInteractionBounds
import com.himanshoe.charty.line.internal.line.drawLineChartCrosshair
import kotlin.math.max
import kotlin.math.min

private data class ComboDrawParams(
    val dataList: List<ComboChartData>,
    val chartContext: com.himanshoe.charty.common.ChartContext,
    val comboConfig: com.himanshoe.charty.combo.config.ComboChartConfig,
    val barColor: ChartyColor,
    val lineColor: ChartyColor,
    val minValue: Float,
    val isBelowAxisMode: Boolean,
    val animationProgress: Float,
    val onDataClick: ((ComboChartData) -> Unit)?,
    val dataBounds: MutableList<Pair<androidx.compose.ui.geometry.Rect, ComboChartData>>,
    val crosshairBounds: MutableList<Pair<Offset, ComboChartData>>?,
    val crosshairManager: CrosshairManager?,
    val tooltipState: com.himanshoe.charty.common.tooltip.TooltipState?,
    val textMeasurer: androidx.compose.ui.text.TextMeasurer,
    val interactionConfig: com.himanshoe.charty.common.config.ChartInteractionConfig,
)

/**
 * A composable function that displays a combo chart, combining a bar chart and a line chart.
 *
 * A combo chart is useful for visualizing two different data series on the same chart, allowing for
 * easy comparison of trends and magnitudes. Both the bar values and line values share a unified
 * y-axis, so the data ranges should be comparable.
 *
 * @param data A lambda function that returns a list of [ComboChartData], each containing a bar
 *   value and a line value.
 * @param modifier The modifier to be applied to the chart.
 * @param barColor The color or color scheme for the bars, defined by a [ChartyColor].
 * @param lineColor The color or color scheme for the line and its points, defined by a [ChartyColor].
 * @param comboConfig The configuration for the combo chart's appearance and behavior, defined by a
 *   [ComboChartConfig].
 * @param scaffoldConfig The configuration for the chart's scaffold, including axes and labels,
 *   defined by a [ChartScaffoldConfig].
 * @param onDataClick A lambda function invoked when a data point (bar or line point) is clicked,
 *   providing the corresponding [ComboChartData].
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 *
 * Example usage:
 * ```kotlin
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
 * ```
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
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
) {
    val fullDataList = remember(data) { data() }
    require(fullDataList.isNotEmpty()) { "Combo chart data cannot be empty" }

    val dataList = rememberWindowedData(fullDataList, interactionConfig.viewPortState)

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
    val crosshairBounds = remember { mutableListOf<Pair<Offset, ComboChartData>>() }
    val crosshairManager = if (comboConfig.crosshairConfig != null) rememberCrosshairManager() else null
    val textMeasurer = rememberTextMeasurer()

    val chartDescription = rememberChartDescription(fullDataList, interactionConfig.accessibilityDescription) {
        "Combo chart, ${it.size} data points."
    }

    syncInteractionDataSizes(
        interactionConfig.viewPortState,
        interactionConfig.brushSelectionState,
        fullDataList.size,
        dataList.size,
    )

    val clickModifier = buildComboModifier(
        crosshairManager = crosshairManager,
        comboConfig = comboConfig,
        dataList = dataList,
        crosshairBounds = crosshairBounds,
        dataBounds = dataBounds,
        onDataClick = onDataClick,
        onTooltipStateChange = { tooltipState = it },
    )

    val chartModifier = buildInteractionModifier(
        base = modifier.then(clickModifier),
        interactionConfig = interactionConfig,
        dataList = dataList,
    )

    ChartScaffold(
        modifier = chartModifier,
        xLabels = dataList.getLabels(),
        yAxisConfig = AxisConfig(
            minValue = minValue,
            maxValue = maxValue,
            steps = 6,
            drawAxisAtZero = isBelowAxisMode,
        ),
        config = scaffoldConfig,
        contentDescription = chartDescription,
    ) { chartContext ->
        updateInteractionBounds(interactionConfig, chartContext)
        dataBounds.clear()
        drawComboContent(
            ComboDrawParams(
                dataList = dataList,
                chartContext = chartContext,
                comboConfig = comboConfig,
                barColor = barColor,
                lineColor = lineColor,
                minValue = minValue,
                isBelowAxisMode = isBelowAxisMode,
                animationProgress = animationProgress.value,
                onDataClick = onDataClick,
                dataBounds = dataBounds,
                crosshairBounds = if (crosshairManager != null) crosshairBounds else null,
                crosshairManager = crosshairManager,
                tooltipState = tooltipState,
                textMeasurer = textMeasurer,
                interactionConfig = interactionConfig,
            )
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawComboContent(p: ComboDrawParams) {
    val baselineY = if (p.minValue < 0f && p.isBelowAxisMode) {
        p.chartContext.convertValueToYPosition(0f)
    } else {
        p.chartContext.bottom
    }
    drawComboBars(
        dataList = p.dataList,
        chartContext = p.chartContext,
        comboConfig = p.comboConfig,
        barColor = p.barColor,
        baselineY = baselineY,
        animationProgress = p.animationProgress,
        isBelowAxisMode = p.isBelowAxisMode,
        dataBounds = if (p.onDataClick != null) p.dataBounds else null,
    )
    val pointPositions = p.chartContext.calculateLinePointPositions(p.dataList)
    p.crosshairBounds?.let { bounds ->
        bounds.clear()
        pointPositions.fastForEachIndexed { index, pos ->
            p.dataList.getOrNull(index)?.let { bounds.add(pos to it) }
        }
    }
    drawComboLine(
        pointPositions = pointPositions,
        lineColor = p.lineColor,
        comboConfig = p.comboConfig,
        animationProgress = p.animationProgress,
        dataList = p.dataList,
        dataBounds = if (p.onDataClick != null) p.dataBounds else null,
    )
    p.comboConfig.referenceLine?.let { referenceLineConfig ->
        drawReferenceLine(
            chartContext = p.chartContext,
            orientation = ChartOrientation.VERTICAL,
            config = referenceLineConfig,
            textMeasurer = p.textMeasurer,
        )
    }
    if (p.crosshairManager == null) {
        p.tooltipState?.let { state ->
            drawTooltip(
                tooltipState = state,
                config = p.comboConfig.tooltipConfig,
                textMeasurer = p.textMeasurer,
                chartWidth = p.chartContext.right,
                chartTop = p.chartContext.top,
                chartBottom = p.chartContext.bottom,
            )
        }
    }
    drawInteractionOverlays(p.interactionConfig, p.chartContext, p.dataList.size, p.textMeasurer)
    p.crosshairManager?.state?.let { state ->
        p.comboConfig.crosshairConfig?.let { config ->
            drawLineChartCrosshair(state, config, p.chartContext, p.textMeasurer, p.lineColor)
        }
    }
}

private fun buildComboModifier(
    crosshairManager: CrosshairManager?,
    comboConfig: ComboChartConfig,
    dataList: List<ComboChartData>,
    crosshairBounds: MutableList<Pair<Offset, ComboChartData>>,
    dataBounds: MutableList<Pair<Rect, ComboChartData>>,
    onDataClick: ((ComboChartData) -> Unit)?,
    onTooltipStateChange: (TooltipState?) -> Unit,
): Modifier = when {
    crosshairManager != null -> Modifier.chartCrosshairHandler(
        dataList = dataList,
        pointBounds = crosshairBounds,
        onCrosshairUpdate = crosshairManager::update,
        labelFormatter = { data -> "${data.label}: ${data.lineValue}" },
        dismissOnRelease = comboConfig.crosshairConfig?.dismissOnRelease ?: true,
    )
    onDataClick != null -> Modifier.comboChartClickHandler(
        dataList = dataList,
        comboConfig = comboConfig,
        dataBounds = dataBounds,
        onDataClick = onDataClick,
        onTooltipStateChange = onTooltipStateChange,
    )
    else -> Modifier
}
