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
import com.himanshoe.charty.common.gesture.ChartCrosshairConfig
import com.himanshoe.charty.common.gesture.CrosshairState
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.tooltip.drawTooltip
import com.himanshoe.charty.line.internal.line.drawLineChartCrosshair
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
    val slotHalfWidth = waveSlotHalfWidth(chartContext = chartContext, barCount = barCount)
    val barWidth = slotHalfWidth * wavyConfig.barWidthFraction.coerceIn(MIN_BAR_WIDTH_FRACTION, 1f)
    val waveCtx =
        WaveDrawContext(
            slotHalfWidth = slotHalfWidth,
            baselineY = wavyBaselineY(chartContext = chartContext, minValue = minValue),
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
    val slotHalfWidth = waveCtx.slotHalfWidth
    val baselineY = waveCtx.baselineY
    val waveAmplitude = waveCtx.waveAmplitude
    val segments = waveCtx.segments
    val basePhase = waveCtx.basePhase
    val wavyConfig = waveCtx.wavyConfig
    val strokeWidthPx = waveCtx.strokeWidthPx
    val xCenter = waveSlotCenterX(chartContext = chartContext, slotHalfWidth = slotHalfWidth, index = index)
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

/**
 * Draws the wavy chart's own overlays for this pass: the built-in canvas tooltip for the tapped
 * wave, then the crosshair line snapped to the dragged one. Each is skipped when it is not showing —
 * a Compose-overlay tooltip is hosted above the canvas instead of drawn here — and they are drawn
 * independently, so a chart configured with both shows both.
 *
 * @param tooltipState The active tooltip anchor, or `null` when nothing is selected.
 * @param drawTooltipBubble Whether the tooltip is the built-in canvas bubble.
 * @param crosshairState The crosshair's resolved position, or `null` when it is not showing.
 * @param crosshairConfig The crosshair styling, or `null` when the crosshair is off.
 * @param tooltipConfig Styling for the tooltip bubble, already resolved against the theme.
 * @param chartContext The pixel bounds and value range of the plotting area.
 * @param color The chart colour, which tints the crosshair's snap dot.
 * @param textMeasurer Measurer used for the tooltip text.
 */
internal fun DrawScope.drawWavyOverlays(
    tooltipState: TooltipState?,
    drawTooltipBubble: Boolean,
    crosshairState: CrosshairState?,
    crosshairConfig: ChartCrosshairConfig?,
    tooltipConfig: TooltipConfig,
    chartContext: ChartContext,
    color: ChartyColor,
    textMeasurer: TextMeasurer,
) {
    tooltipState?.takeIf { drawTooltipBubble }?.let { state ->
        drawTooltip(
            tooltipState = state,
            config = tooltipConfig,
            textMeasurer = textMeasurer,
            chartWidth = chartContext.right,
            chartTop = chartContext.top,
            chartBottom = chartContext.bottom,
        )
    }
    crosshairState?.let { state ->
        crosshairConfig?.let { config ->
            drawLineChartCrosshair(
                state = state,
                config = config,
                chartContext = chartContext,
                textMeasurer = textMeasurer,
                chartColor = color,
            )
        }
    }
}
