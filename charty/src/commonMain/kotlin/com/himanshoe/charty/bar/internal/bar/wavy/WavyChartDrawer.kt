package com.himanshoe.charty.bar.internal.bar.wavy

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.bar.config.WavyChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.draw.drawPersistentMarkers
import com.himanshoe.charty.common.draw.formatMarkerValue
import kotlin.math.PI
import kotlin.math.sin

/**
 * Draws every bar as a vertical sine wave running from its value to the baseline, then the
 * persistent markers on top. The wave geometry shared by all bars is resolved once into a
 * [WaveDrawContext] before the per-bar loop.
 *
 * @param dataList The bars to draw, in x order.
 * @param chartContext The pixel bounds and value range of the plotting area.
 * @param wavyConfig The wave styling: bar width, amplitude, segment count, and phase offset.
 * @param color The fallback colour used for bars that carry no colour of their own.
 * @param minValue The lowest value on the axis, which decides whether the baseline is the zero line.
 * @param basePhase The animated phase shared by every bar, in radians.
 * @param strokeWidthPx The wave stroke width, in pixels.
 * @param textMeasurer Measurer used for the marker labels.
 */
internal fun DrawScope.drawWavyBars(
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
    if (wavyConfig.markers.isNotEmpty()) {
        val values = dataList.fastMap { it.value }
        drawPersistentMarkers(
            chartContext = chartContext,
            markers = wavyConfig.markers,
            pointPositions = wavyPointPositions(chartContext = chartContext, values = values),
            valueLabelFor = { index -> formatMarkerValue(values[index]) },
            textMeasurer = textMeasurer,
        )
    }
}

/**
 * Draws one bar's wave: a sine curve sampled at [WaveDrawContext.segments] points, running between
 * the bar's value and the baseline, phase-shifted by its index so neighbouring bars stay out of
 * step. Draws nothing for a zero-height bar.
 *
 * @param index The bar's position, which sets both its x centre and its phase offset.
 * @param barData The bar being drawn, supplying its value and optional own colour.
 * @param chartContext The pixel bounds and value range of the plotting area.
 * @param waveCtx The wave geometry shared by every bar in this pass.
 */
internal fun DrawScope.drawSingleWave(
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
