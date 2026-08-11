package com.himanshoe.charty.combo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.combo.config.ComboChartConfig
import com.himanshoe.charty.combo.data.ComboChartData
import com.himanshoe.charty.combo.ext.getLabels
import com.himanshoe.charty.combo.internal.ComboChartConstants
import com.himanshoe.charty.combo.internal.calculateLinePointPositions
import com.himanshoe.charty.combo.internal.comboChartClickHandler
import com.himanshoe.charty.combo.internal.comboLineRange
import com.himanshoe.charty.combo.internal.comboPrimaryRange
import com.himanshoe.charty.combo.internal.drawComboBars
import com.himanshoe.charty.combo.internal.drawComboLine
import com.himanshoe.charty.combo.internal.toSecondaryAxisConfig
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.accessibility.generateComboChartDescription
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.draw.drawReferenceBandIfNeeded
import com.himanshoe.charty.common.draw.drawReferenceLine
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.gesture.ChartCrosshair
import com.himanshoe.charty.common.gesture.ChartCrosshairHost
import com.himanshoe.charty.common.gesture.CrosshairManager
import com.himanshoe.charty.common.gesture.CrosshairState
import com.himanshoe.charty.common.gesture.chartCrosshairHandler
import com.himanshoe.charty.common.gesture.rememberChartCrosshair
import com.himanshoe.charty.common.rememberChartDescription
import com.himanshoe.charty.common.rememberWindowedData
import com.himanshoe.charty.common.syncInteractionDataSizes
import com.himanshoe.charty.common.theme.ChartyThemeDefaults
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.tooltip.drawTooltip
import com.himanshoe.charty.common.updateInteractionBounds
import com.himanshoe.charty.line.internal.line.drawLineChartCrosshair

private data class ComboDrawParams(
    val dataList: List<ComboChartData>,
    val chartContext: com.himanshoe.charty.common.ChartContext,
    val comboConfig: com.himanshoe.charty.combo.config.ComboChartConfig,
    val barColor: ChartyColor,
    val lineColor: ChartyColor,
    val minValue: Float,
    val lineRange: Pair<Float, Float>?,
    val isBelowAxisMode: Boolean,
    val animationProgress: Float,
    val onDataClick: ((ComboChartData) -> Unit)?,
    val dataBounds: MutableList<Pair<androidx.compose.ui.geometry.Rect, ComboChartData>>,
    val crosshairBounds: MutableList<Pair<Offset, ComboChartData>>?,
    val crosshairManager: CrosshairManager<ComboChartData>?,
    val crosshairState: CrosshairState?,
    val tooltipState: com.himanshoe.charty.common.tooltip.TooltipState?,
    val textMeasurer: androidx.compose.ui.text.TextMeasurer,
    val interactionConfig: com.himanshoe.charty.common.config.ChartInteractionConfig,
    val drawCrosshairLabel: Boolean,
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
 * @param emptyContent Optional custom placeholder shown when the data is empty; when
 *   `null` (default) a built-in "No data" state is used.
 * @param barColor The color or color scheme for the bars, defined by a [ChartyColor].
 * @param lineColor The color or color scheme for the line and its points, defined by a [ChartyColor].
 * @param comboConfig The configuration for the combo chart's appearance and behavior, defined by a
 *   [ComboChartConfig].
 * @param scaffoldConfig The configuration for the chart's scaffold, including axes and labels,
 *   defined by a [ChartScaffoldConfig].
 * @param onDataClick A lambda function invoked when a data point (bar or line point) is clicked,
 *   providing the corresponding [ComboChartData].
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 * @param crosshair The draggable crosshair: `null` (default) off, or a [ChartCrosshair] to enable a
 *   guide line that snaps to the nearest point, with a built-in or custom label drawn over it.
 */
@Composable
fun ComboChart(
    data: () -> List<ComboChartData>,
    modifier: Modifier = Modifier,
    emptyContent: (@Composable () -> Unit)? = null,
    barColor: ChartyColor = ChartyColor.Solid(Color(ComboChartConstants.DEFAULT_BAR_COLOR)),
    lineColor: ChartyColor = ChartyColor.Solid(Color(ComboChartConstants.DEFAULT_LINE_COLOR)),
    comboConfig: ComboChartConfig = ComboChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartyThemeDefaults.scaffoldConfig(),
    onDataClick: ((ComboChartData) -> Unit)? = null,
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
    crosshair: ChartCrosshair<ComboChartData>? = null,
) {
    val fullDataList = remember(data) { data() }
    if (fullDataList.isEmpty()) {
        ChartEmptyState(modifier = modifier, content = emptyContent)
        return
    }
    val effectiveComboConfig = crosshair?.let { comboConfig.copy(crosshairConfig = it.config) } ?: comboConfig
    val activeCrosshair = crosshair ?: comboConfig.crosshairConfig?.let { ChartCrosshair<ComboChartData>(config = it) }

    val dataList = rememberWindowedData(fullDataList = fullDataList, viewPortState = interactionConfig.viewPortState)

    val (minValue, maxValue) =
        remember(dataList, comboConfig.negativeValuesDrawMode, comboConfig.secondaryAxisForLine) {
            comboPrimaryRange(
                dataList = dataList,
                negativeValuesDrawMode = comboConfig.negativeValuesDrawMode,
                secondaryAxisForLine = comboConfig.secondaryAxisForLine,
            )
        }

    val secondaryLineRange =
        remember(dataList, comboConfig.secondaryAxisForLine) {
            comboLineRange(dataList = dataList, secondaryAxisForLine = comboConfig.secondaryAxisForLine)
        }

    val isBelowAxisMode = comboConfig.negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS
    val animationProgress = rememberChartAnimation(comboConfig.animation)
    var tooltipState by remember { mutableStateOf<TooltipState?>(null) }
    val dataBounds = remember { mutableListOf<Pair<Rect, ComboChartData>>() }
    val crosshairBounds = remember { mutableListOf<Pair<Offset, ComboChartData>>() }
    val (crosshairManager, animatedCrosshairState) =
        rememberChartCrosshair<ComboChartData>(effectiveComboConfig.crosshairConfig != null)
    val textMeasurer = rememberTextMeasurer()

    val chartDescription =
        rememberChartDescription(fullDataList, interactionConfig.accessibilityDescription) {
            generateComboChartDescription(it)
        }

    syncInteractionDataSizes(
        viewPortState = interactionConfig.viewPortState,
        brushSelectionState = interactionConfig.brushSelectionState,
        fullDataSize = fullDataList.size,
        dataSize = dataList.size,
    )

    val clickModifier =
        buildComboModifier(
            crosshairManager = crosshairManager,
            comboConfig = effectiveComboConfig,
            dataList = dataList,
            crosshairBounds = crosshairBounds,
            dataBounds = dataBounds,
            onDataClick = onDataClick,
            onTooltipStateChange = { state, _ -> tooltipState = state },
        )

    val chartModifier =
        buildInteractionModifier(
            base = modifier.then(clickModifier),
            interactionConfig = interactionConfig,
            dataList = dataList,
        )

    Box(modifier = chartModifier) {
        ChartScaffold(
            modifier = Modifier.fillMaxSize(),
            xLabels = dataList.getLabels(),
            yAxisConfig =
                AxisConfig(
                    minValue = minValue,
                    maxValue = maxValue,
                    steps = 6,
                    drawAxisAtZero = isBelowAxisMode,
                ),
            config = scaffoldConfig,
            contentDescription = chartDescription,
            secondaryYAxisConfig = secondaryLineRange.toSecondaryAxisConfig(),
        ) { chartContext ->
            updateInteractionBounds(interactionConfig = interactionConfig, chartContext = chartContext)
            dataBounds.clear()
            drawComboContent(
                ComboDrawParams(
                    dataList = dataList,
                    chartContext = chartContext,
                    comboConfig = effectiveComboConfig,
                    barColor = barColor,
                    lineColor = lineColor,
                    minValue = minValue,
                    lineRange = secondaryLineRange,
                    isBelowAxisMode = isBelowAxisMode,
                    animationProgress = animationProgress.value,
                    onDataClick = onDataClick,
                    dataBounds = dataBounds,
                    crosshairBounds =
                        crosshairBounds.takeIf { crosshairManager != null },
                    crosshairManager = crosshairManager,
                    crosshairState = animatedCrosshairState?.resolve(),
                    tooltipState = tooltipState,
                    textMeasurer = textMeasurer,
                    interactionConfig = interactionConfig,
                    drawCrosshairLabel = false,
                ),
            )
        }

        ComboChartOverlays(
            crosshairManager = crosshairManager,
            animatedCrosshairState = animatedCrosshairState?.resolve(),
            crosshair = activeCrosshair,
        )
    }
}

@Composable
private fun BoxScope.ComboChartOverlays(
    crosshairManager: CrosshairManager<ComboChartData>?,
    animatedCrosshairState: CrosshairState?,
    crosshair: ChartCrosshair<ComboChartData>?,
) {
    if (crosshair != null) {
        ChartCrosshairHost(
            crosshair = crosshair,
            item = crosshairManager?.selectedItem,
            state = animatedCrosshairState,
            modifier = Modifier.matchParentSize(),
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawComboContent(p: ComboDrawParams) {
    val baselineY =
        if (p.minValue < 0f && p.isBelowAxisMode) {
            p.chartContext.convertValueToYPosition(0f)
        } else {
            p.chartContext.bottom
        }
    drawReferenceBandIfNeeded(
        referenceBandConfig = p.comboConfig.referenceBand,
        chartContext = p.chartContext,
        orientation = ChartOrientation.VERTICAL,
        textMeasurer = p.textMeasurer,
    )
    drawComboBars(
        dataList = p.dataList,
        chartContext = p.chartContext,
        comboConfig = p.comboConfig,
        barColor = p.barColor,
        baselineY = baselineY,
        animationProgress = p.animationProgress,
        isBelowAxisMode = p.isBelowAxisMode,
        dataBounds =
            p.dataBounds.takeIf { p.onDataClick != null },
    )
    val pointPositions = p.chartContext.calculateLinePointPositions(dataList = p.dataList, lineRange = p.lineRange)
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
        dataBounds =
            p.dataBounds.takeIf { p.onDataClick != null },
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
    drawInteractionOverlays(
        interactionConfig = p.interactionConfig,
        chartContext = p.chartContext,
        totalItems = p.dataList.size,
        textMeasurer = p.textMeasurer,
    )
    p.crosshairState?.let { state ->
        p.comboConfig.crosshairConfig?.let { config ->
            drawLineChartCrosshair(
                state,
                config,
                p.chartContext,
                p.textMeasurer,
                p.lineColor,
                drawLabel = p.drawCrosshairLabel,
            )
        }
    }
}

private fun buildComboModifier(
    crosshairManager: CrosshairManager<ComboChartData>?,
    comboConfig: ComboChartConfig,
    dataList: List<ComboChartData>,
    crosshairBounds: MutableList<Pair<Offset, ComboChartData>>,
    dataBounds: MutableList<Pair<Rect, ComboChartData>>,
    onDataClick: ((ComboChartData) -> Unit)?,
    onTooltipStateChange: (TooltipState?, ComboChartData?) -> Unit,
): Modifier =
    when {
        crosshairManager != null ->
            Modifier.chartCrosshairHandler(
                dataList = dataList,
                pointBounds = crosshairBounds,
                onCrosshairUpdate = crosshairManager::update,
                labelFormatter = { data -> "${data.label}: ${data.lineValue}" },
                dismissOnRelease = comboConfig.crosshairConfig?.dismissOnRelease ?: true,
            )
        onDataClick != null ->
            Modifier.comboChartClickHandler(
                dataList = dataList,
                comboConfig = comboConfig,
                dataBounds = dataBounds,
                onDataClick = onDataClick,
                onTooltipStateChange = onTooltipStateChange,
            )
        else -> Modifier
    }
