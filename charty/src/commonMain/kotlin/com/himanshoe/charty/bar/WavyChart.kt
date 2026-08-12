package com.himanshoe.charty.bar

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.bar.config.WavyChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.internal.bar.barAccessibility
import com.himanshoe.charty.bar.internal.bar.rememberAnimatedBarValues
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.animation.rememberAnimatedRange
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.draw.drawPersistentMarkers
import com.himanshoe.charty.common.draw.formatMarkerValue
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.gesture.ChartCrosshair
import com.himanshoe.charty.common.gesture.ChartCrosshairHost
import com.himanshoe.charty.common.gesture.CrosshairManager
import com.himanshoe.charty.common.gesture.CrosshairState
import com.himanshoe.charty.common.gesture.chartCrosshairHandler
import com.himanshoe.charty.common.gesture.rememberChartCrosshair
import com.himanshoe.charty.common.rememberWindowedData
import com.himanshoe.charty.common.syncInteractionDataSizes
import com.himanshoe.charty.common.theme.ChartyThemeDefaults
import com.himanshoe.charty.common.updateInteractionBounds
import com.himanshoe.charty.line.internal.line.drawLineChartCrosshair
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

private const val WAVY_CHART_PHASE_TARGET_MULTIPLIER = 2f
private const val MIN_WAVE_SEGMENTS = 4
private const val MIN_BAR_WIDTH_FRACTION = 0.1f

/** Precomputed wave rendering parameters, passed as a bundle to avoid long parameter lists. */
private data class WaveDrawContext(
    val barSpacing: Float,
    val baselineY: Float,
    val waveAmplitude: Float,
    val segments: Int,
    val basePhase: Float,
    val strokeWidthPx: Float,
    val wavyConfig: WavyChartConfig,
    val color: ChartyColor,
)

/**
 * Wavy Chart - Bar-like chart with animated sine wave lines for each bar.
 *
 * Example:
 * ```kotlin
 * WavyChart(
 *     data = {
 *         listOf(
 *             BarData(label = "Mon", value = 20f),
 *             BarData(label = "Tue", value = 35f),
 *             BarData(label = "Wed", value = 28f),
 *         )
 *     },
 *     color = ChartyColor.Solid(ChartyColors.Blue),
 * )
 * ```
 *
 * @param data Lambda returning list of bar data to display.
 * @param modifier Modifier for the chart.
 * @param emptyContent Optional custom placeholder shown when the data is empty; when
 *   `null` (default) a built-in "No data" state is used.
 * @param color Color for the wave lines.
 * @param wavyConfig Configuration for wave appearance and animation.
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels.

 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 * @param crosshair The draggable crosshair: `null` (default) off, or a [ChartCrosshair] to enable a
 *   guide line that snaps to the nearest bar, with a built-in or custom label drawn over it.
 */
@Composable
fun WavyChart(
    data: () -> List<BarData>,
    modifier: Modifier = Modifier,
    emptyContent: (@Composable () -> Unit)? = null,
    color: ChartyColor = ChartyThemeDefaults.primaryColor(),
    wavyConfig: WavyChartConfig = WavyChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartyThemeDefaults.scaffoldConfig(),
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
    crosshair: ChartCrosshair<BarData>? = null,
) {
    val fullDataList by remember(data) { derivedStateOf { data() } }
    if (fullDataList.isEmpty()) {
        ChartEmptyState(modifier = modifier, content = emptyContent)
        return
    }
    val crosshairConfig = crosshair?.config

    val visible =
        rememberWindowedData(
            fullDataList = fullDataList,
            viewPortState = interactionConfig.viewPortState,
            visibleWindow = wavyConfig.visibleWindow,
            animation = Animation.Default,
        )
    val dataList = visible.data

    val textMeasurer = rememberTextMeasurer()
    val crosshairBounds = remember { mutableListOf<Pair<Offset, BarData>>() }
    val (crosshairManager, animatedCrosshairState) =
        rememberChartCrosshair<BarData>(
            enabled = crosshairConfig != null,
            viewPortState = interactionConfig.viewPortState,
        )

    syncInteractionDataSizes(
        viewPortState = interactionConfig.viewPortState,
        brushSelectionState = interactionConfig.brushSelectionState,
        fullDataSize = fullDataList.size,
        dataSize = dataList.size,
    )

    val (rawMinValue, rawMaxValue) =
        remember(dataList) {
            val values = dataList.fastMap { it.value }
            val rawMin = values.minOrNull() ?: 0f
            val rawMax = values.maxOrNull() ?: 0f
            val minVal = min(rawMin, 0f)
            val maxVal = max(rawMax, rawMin.coerceAtLeast(0f))
            minVal to maxVal
        }
    val (minValue, maxValue) =
        rememberAnimatedRange(
            minValue = rawMinValue,
            maxValue = rawMaxValue,
            animation = Animation.Default,
            active = visible.streaming != null,
        )

    val displayList =
        rememberAnimatedBarValues(
            dataList = dataList,
            animation = Animation.Default,
            enabled = wavyConfig.animateValueChanges,
        )

    val basePhase = rememberWavyBasePhase(wavyConfig)
    val strokeWidthPx = wavyConfig.strokeWidthDp.dp.value

    val gestureBase =
        if (crosshairManager != null) {
            Modifier.chartCrosshairHandler(
                dataList = dataList,
                pointBounds = crosshairBounds,
                onCrosshairUpdate = crosshairManager::update,
                labelFormatter = { bar -> "${bar.label}: ${bar.value}" },
                dismissOnRelease = crosshairConfig?.dismissOnRelease ?: true,
            )
        } else {
            Modifier
        }
    val chartModifier =
        buildInteractionModifier(
            base = modifier.then(gestureBase),
            interactionConfig = interactionConfig,
            dataList = dataList,
        )

    Box(modifier = chartModifier) {
        ChartScaffold(
            accessibility =
                barAccessibility(
                    description = interactionConfig.accessibilityDescription,
                    labels = dataList.fastMap { it.label },
                    values = dataList.fastMap { it.value },
                    fallbackDescription = "Wavy chart, ${fullDataList.size} data points.",
                ),
            streamingLayout = visible.streaming,
            modifier = Modifier.fillMaxSize(),
            xLabels = dataList.fastMap { it.label },
            yAxisConfig =
                AxisConfig(
                    minValue = minValue,
                    maxValue = maxValue,
                    steps = 5,
                    drawAxisAtZero = minValue < 0f,
                ),
            config = scaffoldConfig,
        ) { chartContext ->
            updateInteractionBounds(interactionConfig = interactionConfig, chartContext = chartContext)

            val barCount = dataList.size
            if (barCount == 0) {
                return@ChartScaffold
            }

            populateWavyCrosshairBounds(
                chartContext = chartContext,
                dataList = dataList,
                crosshairManager = crosshairManager,
                crosshairBounds = crosshairBounds,
            )

            drawWavyBars(
                dataList = displayList,
                chartContext = chartContext,
                wavyConfig = wavyConfig,
                color = color,
                minValue = minValue,
                basePhase = basePhase,
                strokeWidthPx = strokeWidthPx,
                textMeasurer = textMeasurer,
            )

            drawInteractionOverlays(
                interactionConfig = interactionConfig,
                chartContext = chartContext,
                totalItems = dataList.size,
                textMeasurer = textMeasurer,
            )

            animatedCrosshairState?.resolve()?.let { state ->
                crosshairConfig?.let { cfg ->
                    drawLineChartCrosshair(
                        state,
                        cfg,
                        chartContext,
                        textMeasurer,
                        color,
                        drawLabel = false,
                    )
                }
            }
        }

        WavyChartOverlays(
            crosshairManager = crosshairManager,
            animatedCrosshairState = animatedCrosshairState?.resolve(),
            crosshair = crosshair,
        )
    }
}

@Composable
private fun BoxScope.WavyChartOverlays(
    crosshairManager: CrosshairManager<BarData>?,
    animatedCrosshairState: CrosshairState?,
    crosshair: ChartCrosshair<BarData>?,
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

@Composable
private fun rememberWavyBasePhase(wavyConfig: WavyChartConfig): Float {
    val infinite = rememberInfiniteTransition(label = "wavy-chart")
    val basePhase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = (WAVY_CHART_PHASE_TARGET_MULTIPLIER * PI).toFloat(),
        animationSpec =
            infiniteRepeatable(
                animation =
                    tween(
                        durationMillis = wavyConfig.animationDurationMillis,
                        easing = wavyConfig.animationEasing,
                    ),
                repeatMode = RepeatMode.Restart,
            ),
        label = "wave-phase",
    )
    return basePhase
}

private fun wavyPointPositions(
    chartContext: ChartContext,
    values: List<Float>,
): List<Offset> {
    val barSpacing = chartContext.width / (values.size * WAVY_CHART_PHASE_TARGET_MULTIPLIER)
    return List(values.size) { index ->
        Offset(
            x = chartContext.left + barSpacing * (1 + index * 2),
            y = chartContext.convertValueToYPosition(values[index]),
        )
    }
}

private fun populateWavyCrosshairBounds(
    chartContext: ChartContext,
    dataList: List<BarData>,
    crosshairManager: CrosshairManager<BarData>?,
    crosshairBounds: MutableList<Pair<Offset, BarData>>,
) {
    crosshairBounds.clear()
    if (crosshairManager == null) {
        return
    }
    val barCount = dataList.size
    if (barCount == 0) {
        return
    }
    val positions = wavyPointPositions(chartContext = chartContext, values = dataList.fastMap { it.value })
    dataList.fastForEachIndexed { index, barData ->
        crosshairBounds.add(positions[index] to barData)
    }
}

private fun DrawScope.drawWavyBars(
    dataList: List<BarData>,
    chartContext: ChartContext,
    wavyConfig: WavyChartConfig,
    color: ChartyColor,
    minValue: Float,
    basePhase: Float,
    strokeWidthPx: Float,
    textMeasurer: TextMeasurer,
) {
    val barCount = dataList.size
    val barSpacing = chartContext.width / (barCount * WAVY_CHART_PHASE_TARGET_MULTIPLIER)
    val barWidth = barSpacing * wavyConfig.barWidthFraction.coerceIn(MIN_BAR_WIDTH_FRACTION, 1f)
    val waveCtx =
        WaveDrawContext(
            barSpacing = barSpacing,
            baselineY =
                if (minValue < 0f) {
                    chartContext.convertValueToYPosition(0f)
                } else {
                    chartContext.bottom
                },
            waveAmplitude = barWidth * wavyConfig.waveAmplitudeFractionOfBarWidth,
            segments = wavyConfig.waveSegments.coerceAtLeast(MIN_WAVE_SEGMENTS),
            basePhase = basePhase,
            strokeWidthPx = strokeWidthPx,
            wavyConfig = wavyConfig,
            color = color,
        )
    dataList.fastForEachIndexed { index, barData ->
        drawSingleWave(index = index, barData = barData, chartContext = chartContext, waveCtx = waveCtx)
    }
    val values = dataList.fastMap { it.value }
    drawPersistentMarkers(
        chartContext = chartContext,
        markers = wavyConfig.markers,
        pointPositions = wavyPointPositions(chartContext = chartContext, values = values),
        valueLabelFor = { index -> formatMarkerValue(values[index]) },
        textMeasurer = textMeasurer,
    )
}

private fun DrawScope.drawSingleWave(
    index: Int,
    barData: BarData,
    chartContext: ChartContext,
    waveCtx: WaveDrawContext,
) {
    val barSpacing = waveCtx.barSpacing
    val baselineY = waveCtx.baselineY
    val waveAmplitude = waveCtx.waveAmplitude
    val segments = waveCtx.segments
    val basePhase = waveCtx.basePhase
    val wavyConfig = waveCtx.wavyConfig
    val strokeWidthPx = waveCtx.strokeWidthPx
    val xCenter = chartContext.left + barSpacing * (1 + index * 2)
    val valueTop = chartContext.convertValueToYPosition(barData.value)

    val top: Float
    val bottom: Float

    if (barData.value >= 0f) {
        top = valueTop
        bottom = baselineY
    } else {
        top = baselineY
        bottom = valueTop
    }

    val barHeight = bottom - top
    if (barHeight == 0f) {
        return
    }

    val path = Path()
    val dy = barHeight / segments

    val phase = basePhase + index * wavyConfig.phaseOffsetPerBar

    var y = top
    for (i in 0..segments) {
        val progress = i / segments.toFloat()
        val angle = (progress * 2f * PI + phase).toFloat()
        val dx = sin(angle) * waveAmplitude
        val point = Offset(xCenter + dx, y)
        if (i == 0) {
            path.moveTo(point.x, point.y)
        } else {
            path.lineTo(point.x, point.y)
        }
        y += dy
    }
    val brush = Brush.verticalGradient(barData.color?.value ?: waveCtx.color.value)
    drawPath(
        path = path,
        brush = brush,
        style = Stroke(width = strokeWidthPx),
    )
}
