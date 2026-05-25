package com.himanshoe.charty.bar

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import com.himanshoe.charty.bar.config.WavyChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.ChartyColors
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.rememberWindowedData
import com.himanshoe.charty.common.syncInteractionDataSizes
import com.himanshoe.charty.common.updateInteractionBounds
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
 * @param data Lambda returning list of bar data to display.
 * @param modifier Modifier for the chart.
 * @param color Color for the wave lines.
 * @param wavyConfig Configuration for wave appearance and animation.
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels.
 * @param interactionConfig Bundles viewport, brush-selection, annotation, and accessibility options.
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun WavyChart(
    data: () -> List<BarData>,
    modifier: Modifier = Modifier,
    color: ChartyColor = ChartyColor.Solid(ChartyColors.Blue),
    wavyConfig: WavyChartConfig = WavyChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    interactionConfig: ChartInteractionConfig = ChartInteractionConfig(),
) {
    val fullDataList = remember(data) { data() }
    require(fullDataList.isNotEmpty()) { "Wavy chart data cannot be empty" }

    val dataList = rememberWindowedData(fullDataList, interactionConfig.viewPortState)

    val textMeasurer = rememberTextMeasurer()

    syncInteractionDataSizes(
        viewPortState = interactionConfig.viewPortState,
        brushSelectionState = interactionConfig.brushSelectionState,
        fullDataSize = fullDataList.size,
        dataSize = dataList.size,
    )

    val (minValue, maxValue) = remember(dataList) {
        val values = dataList.fastMap { it.value }
        val rawMin = values.minOrNull() ?: 0f
        val rawMax = values.maxOrNull() ?: 0f
        val minVal = min(rawMin, 0f)
        val maxVal = max(rawMax, if (minVal < 0f) 0f else rawMin)
        minVal to maxVal
    }

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

    val strokeWidthPx = wavyConfig.strokeWidthDp.dp.value

    val chartModifier = buildInteractionModifier(
        base = modifier,
        interactionConfig = interactionConfig,
        dataList = dataList,
    )

    ChartScaffold(
        modifier = chartModifier,
        xLabels = dataList.fastMap { it.label },
        yAxisConfig = AxisConfig(minValue = minValue, maxValue = maxValue, steps = 5, drawAxisAtZero = minValue < 0f),
        config = scaffoldConfig,
        contentDescription = interactionConfig.accessibilityDescription
            ?: "Wavy chart, ${fullDataList.size} data points.",
    ) { chartContext ->
        updateInteractionBounds(interactionConfig, chartContext)

        val barCount = dataList.size
        if (barCount == 0) return@ChartScaffold

        drawWavyBars(
            dataList = dataList,
            chartContext = chartContext,
            wavyConfig = wavyConfig,
            color = color,
            minValue = minValue,
            basePhase = basePhase,
            strokeWidthPx = strokeWidthPx,
        )

        drawInteractionOverlays(interactionConfig, chartContext, dataList.size, textMeasurer)
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
) {
    val barCount = dataList.size
    val barSpacing = chartContext.width / (barCount * WAVY_CHART_PHASE_TARGET_MULTIPLIER)
    val barWidth = barSpacing * wavyConfig.barWidthFraction.coerceIn(MIN_BAR_WIDTH_FRACTION, 1f)
    val waveCtx = WaveDrawContext(
        barSpacing = barSpacing,
        baselineY = if (minValue < 0f) chartContext.convertValueToYPosition(0f) else chartContext.bottom,
        waveAmplitude = barWidth * wavyConfig.waveAmplitudeFractionOfBarWidth,
        segments = wavyConfig.waveSegments.coerceAtLeast(MIN_WAVE_SEGMENTS),
        basePhase = basePhase,
        strokeWidthPx = strokeWidthPx,
        wavyConfig = wavyConfig,
        color = color,
    )
    dataList.fastForEachIndexed { index, barData ->
        drawSingleWave(index, barData, chartContext, waveCtx)
    }
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
    if (barHeight == 0f) return

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
