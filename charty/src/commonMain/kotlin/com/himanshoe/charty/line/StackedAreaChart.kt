package com.himanshoe.charty.line

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastFlatMap
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.ChartLegend
import com.himanshoe.charty.common.ChartOrientation
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.accessibility.generateLineGroupChartDescription
import com.himanshoe.charty.common.animation.isAnimated
import com.himanshoe.charty.common.animation.rememberChartAnimationState
import com.himanshoe.charty.common.animation.toFloatSpec
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.draw.drawReferenceBand
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
import com.himanshoe.charty.common.util.calculateMaxValue
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineGroup
import com.himanshoe.charty.line.data.StackedAreaPoint
import com.himanshoe.charty.line.ext.getLabels
import com.himanshoe.charty.line.internal.line.drawLineChartCrosshair
import com.himanshoe.charty.line.internal.stackedarea.StackedAreaChartConstants
import com.himanshoe.charty.line.internal.stackedarea.StackedAreaSeriesParams
import com.himanshoe.charty.line.internal.stackedarea.calculateCumulativePositions
import com.himanshoe.charty.line.internal.stackedarea.calculateLowerPositions
import com.himanshoe.charty.line.internal.stackedarea.drawStackedAreaSeries
import com.himanshoe.charty.line.internal.stackedarea.stackedAreaChartClickHandler

private data class StackedAreaDrawParams(
    val dataList: List<LineGroup>,
    val chartContext: ChartContext,
    val colorList: List<Color>,
    val lineConfig: LineChartConfig,
    val fillAlpha: Float,
    val animationProgress: Float,
    val onAreaClick: ((StackedAreaPoint) -> Unit)?,
    val areaSegmentBounds: MutableList<Triple<Rect, Path, StackedAreaPoint>>,
    val crosshairBounds: MutableList<Pair<Offset, LineGroup>>?,
    val crosshairManager: CrosshairManager<LineGroup>?,
    val crosshairState: CrosshairState?,
    val tooltipState: TooltipState?,
    val textMeasurer: TextMeasurer,
    val interactionConfig: ChartInteractionConfig,
    val drawCrosshairLabel: Boolean,
)

private fun calculateStackedCumulativeValues(dataList: List<LineGroup>): List<Float> =
    dataList.fastFlatMap { group ->
        val cumulative = mutableListOf<Float>()
        var sum = 0f
        group.values.fastForEach { value ->
            sum += value
            cumulative.add(sum)
        }
        cumulative
    }

/**
 * A composable function that displays a stacked area chart.
 *
 * Example:
 * ```kotlin
 * StackedAreaChart(
 *     data = {
 *         listOf(
 *             LineGroup(label = "Jan", values = listOf(10f, 20f, 5f)),
 *             LineGroup(label = "Feb", values = listOf(15f, 18f, 8f)),
 *         )
 *     },
 * )
 * ```
 *
 * @param data A lambda function that returns a list of [LineGroup].
 * @param modifier The modifier to be applied to the chart.
 * @param emptyContent Optional custom placeholder shown when the data is empty; when
 *   `null` (default) a built-in "No data" state is used.
 * @param colors The color or color scheme for the stacked areas.
 * @param lineConfig The configuration for the lines' appearance and behavior.
 * @param scaffoldConfig The configuration for the chart's scaffold.
 * @param fillAlpha The alpha transparency for the filled areas, ranging from 0.0f to 1.0f.
 * @param onAreaClick A lambda function invoked when an area is clicked.
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 * @param crosshair The draggable crosshair: `null` (default) off, or a [ChartCrosshair] to enable a
 *   guide line that snaps to the nearest point, with a built-in or custom label drawn over it.
 */
@Composable
fun StackedAreaChart(
    data: () -> List<LineGroup>,
    modifier: Modifier = Modifier,
    emptyContent: (@Composable () -> Unit)? = null,
    colors: ChartyColor =
        ChartyColor.Gradient(
            listOf(
                Color(StackedAreaChartConstants.DEFAULT_COLOR_1),
                Color(StackedAreaChartConstants.DEFAULT_COLOR_2),
                Color(StackedAreaChartConstants.DEFAULT_COLOR_3),
            ),
        ),
    lineConfig: LineChartConfig = LineChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartyThemeDefaults.scaffoldConfig(),
    fillAlpha: Float = 0.7f,
    onAreaClick: ((StackedAreaPoint) -> Unit)? = null,
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
    crosshair: ChartCrosshair<LineGroup>? = null,
) {
    val fullDataList = remember(data) { data() }
    if (fullDataList.isEmpty()) {
        ChartEmptyState(modifier = modifier, content = emptyContent)
        return
    }
    val effectiveLineConfig = crosshair?.let { lineConfig.copy(crosshairConfig = it.config) } ?: lineConfig
    val activeCrosshair = crosshair ?: lineConfig.crosshairConfig?.let { ChartCrosshair<LineGroup>(config = it) }
    require(fillAlpha in 0f..1f) { "Fill alpha must be between 0 and 1" }

    val dataList = rememberWindowedData(fullDataList = fullDataList, viewPortState = interactionConfig.viewPortState)

    val (maxValue, colorList) =
        remember(dataList, colors) {
            calculateMaxValue(calculateStackedCumulativeValues(dataList)) to colors.value
        }

    val animationProgress = rememberChartAnimationState(lineConfig.animation)
    var tooltipState by remember { mutableStateOf<TooltipState?>(null) }
    val areaSegmentBounds = remember { mutableListOf<Triple<Rect, Path, StackedAreaPoint>>() }
    val crosshairBounds = remember { mutableListOf<Pair<Offset, LineGroup>>() }
    val (crosshairManager, animatedCrosshairState) =
        rememberChartCrosshair<LineGroup>(effectiveLineConfig.crosshairConfig != null)
    val textMeasurer = rememberTextMeasurer()

    LaunchedEffect(lineConfig.animation) {
        if (lineConfig.animation.isAnimated) {
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = lineConfig.animation.toFloatSpec(),
            )
        }
    }

    val chartDescription =
        rememberChartDescription(fullDataList, interactionConfig.accessibilityDescription) {
            generateLineGroupChartDescription(data = it, chartTypeName = "stacked area")
        }

    syncInteractionDataSizes(
        viewPortState = interactionConfig.viewPortState,
        brushSelectionState = interactionConfig.brushSelectionState,
        fullDataSize = fullDataList.size,
        dataSize = dataList.size,
    )

    val clickModifier =
        buildStackedAreaModifier(
            crosshairManager = crosshairManager,
            lineConfig = effectiveLineConfig,
            dataList = dataList,
            crosshairBounds = crosshairBounds,
            areaSegmentBounds = areaSegmentBounds,
            onAreaClick = onAreaClick,
            onTooltipStateChange = { state, _ -> tooltipState = state },
        )

    Column(modifier = modifier) {
        val chartModifier =
            buildInteractionModifier(
                base = Modifier.fillMaxSize().then(clickModifier),
                interactionConfig = interactionConfig,
                dataList = dataList,
            )

        Box(modifier = Modifier.weight(1f)) {
            ChartScaffold(
                modifier = chartModifier,
                xLabels = dataList.getLabels(),
                yAxisConfig = AxisConfig(minValue = 0f, maxValue = maxValue, steps = 6, drawAxisAtZero = false),
                config = scaffoldConfig,
                contentDescription = chartDescription,
            ) { chartContext ->
                updateInteractionBounds(interactionConfig = interactionConfig, chartContext = chartContext)
                areaSegmentBounds.clear()
                drawStackedAreaContent(
                    StackedAreaDrawParams(
                        dataList = dataList,
                        chartContext = chartContext,
                        colorList = colorList,
                        lineConfig = effectiveLineConfig,
                        fillAlpha = fillAlpha,
                        animationProgress = animationProgress.value,
                        onAreaClick = onAreaClick,
                        areaSegmentBounds = areaSegmentBounds,
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

            StackedAreaChartOverlays(
                crosshairManager = crosshairManager,
                animatedCrosshairState = animatedCrosshairState?.resolve(),
                crosshair = activeCrosshair,
            )
        }
        if (lineConfig.legendLabels.isNotEmpty()) {
            ChartLegend(
                labels = lineConfig.legendLabels,
                colors = colorList,
                textStyle = lineConfig.legendTextStyle,
            )
        }
    }
}

@Composable
private fun BoxScope.StackedAreaChartOverlays(
    crosshairManager: CrosshairManager<LineGroup>?,
    animatedCrosshairState: CrosshairState?,
    crosshair: ChartCrosshair<LineGroup>?,
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

private fun DrawScope.drawStackedAreaContent(params: StackedAreaDrawParams) {
    val dataList = params.dataList
    val chartContext = params.chartContext
    val colorList = params.colorList
    val lineConfig = params.lineConfig
    val fillAlpha = params.fillAlpha
    val animationProgress = params.animationProgress
    val onAreaClick = params.onAreaClick
    val areaSegmentBounds = params.areaSegmentBounds
    val tooltipState = params.tooltipState
    val textMeasurer = params.textMeasurer
    val interactionConfig = params.interactionConfig
    val baselineY = chartContext.bottom
    val startX = chartContext.left
    val seriesCount = dataList.getOrNull(0)?.values?.size ?: 0

    lineConfig.referenceBand?.let { band ->
        drawReferenceBand(
            chartContext = chartContext,
            orientation = ChartOrientation.VERTICAL,
            config = band,
            textMeasurer = textMeasurer,
        )
    }

    params.crosshairBounds?.let { bounds ->
        bounds.clear()
        if (seriesCount > 0) {
            chartContext.calculateCumulativePositions(dataList, seriesCount - 1).fastForEachIndexed { idx, pos ->
                dataList.getOrNull(idx)?.let { bounds.add(pos to it) }
            }
        }
    }

    for (seriesIndex in seriesCount - 1 downTo 0) {
        val seriesColor = colorList[seriesIndex % colorList.size]
        val cumulativePositions = chartContext.calculateCumulativePositions(dataList, seriesIndex)
        val lowerPositions = chartContext.calculateLowerPositions(dataList, seriesIndex, baselineY)

        drawStackedAreaSeries(
            StackedAreaSeriesParams(
                seriesIndex = seriesIndex,
                seriesColor = seriesColor,
                cumulativePositions = cumulativePositions,
                lowerPositions = lowerPositions,
                startX = startX,
                baselineY = baselineY,
                lineConfig = lineConfig,
                fillAlpha = fillAlpha,
                animationProgress = animationProgress,
                dataList = dataList,
                onSegmentBoundsCalculated =
                    if (onAreaClick != null) {
                        { bounds -> areaSegmentBounds.add(bounds) }
                    } else {
                        null
                    },
            ),
        )
    }

    if (params.crosshairManager == null) {
        tooltipState?.let { state ->
            drawTooltip(
                tooltipState = state,
                config = lineConfig.tooltipConfig,
                textMeasurer = textMeasurer,
                chartWidth = chartContext.right,
                chartTop = chartContext.top,
                chartBottom = chartContext.bottom,
            )
        }
    }
    drawInteractionOverlays(
        interactionConfig = interactionConfig,
        chartContext = chartContext,
        totalItems = dataList.size,
        textMeasurer = textMeasurer,
    )

    params.crosshairState?.let { state ->
        lineConfig.crosshairConfig?.let { config ->
            val dotColor = ChartyColor.Solid(colorList.firstOrNull() ?: Color.Transparent)
            drawLineChartCrosshair(
                state,
                config,
                chartContext,
                textMeasurer,
                dotColor,
                drawLabel = params.drawCrosshairLabel,
            )
        }
    }
}

private fun buildStackedAreaModifier(
    crosshairManager: CrosshairManager<LineGroup>?,
    lineConfig: LineChartConfig,
    dataList: List<LineGroup>,
    crosshairBounds: MutableList<Pair<Offset, LineGroup>>,
    areaSegmentBounds: MutableList<Triple<Rect, Path, StackedAreaPoint>>,
    onAreaClick: ((StackedAreaPoint) -> Unit)?,
    onTooltipStateChange: (TooltipState?, StackedAreaPoint?) -> Unit,
): Modifier =
    when {
        crosshairManager != null ->
            Modifier.chartCrosshairHandler(
                dataList = dataList,
                pointBounds = crosshairBounds,
                onCrosshairUpdate = crosshairManager::update,
                labelFormatter = { group -> "${group.label}: ${group.values.sum()}" },
                dismissOnRelease = lineConfig.crosshairConfig?.dismissOnRelease ?: true,
            )
        onAreaClick != null ->
            Modifier.stackedAreaChartClickHandler(
                dataList = dataList,
                lineConfig = lineConfig,
                areaSegmentBounds = areaSegmentBounds,
                onAreaClick = onAreaClick,
                onTooltipStateChange = onTooltipStateChange,
            )
        else -> Modifier
    }
