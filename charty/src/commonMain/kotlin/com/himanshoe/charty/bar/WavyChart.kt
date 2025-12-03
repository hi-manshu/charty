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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.bar.config.WavyChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.ChartyColors
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.data.getLabels
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

private const val WAVY_CHART_PHASE_TARGET_MULTIPLIER = 2f
private const val MIN_WAVE_SEGMENTS = 4
private const val MIN_BAR_WIDTH_FRACTION = 0.1f

/**
 * Wavy Chart - Bar-like chart with animated sine wave lines for each bar.
 *
 * This chart renders each [BarData] entry as a vertical "bar" whose outline is a
 * continuously animating sine wave, giving a fluid, wavy appearance instead of
 * a solid rectangle. All bars animate in sync (or with optional per-bar phase offset),
 * producing a rhythmic wave motion.
 *
 * @param data Lambda returning list of bar data to display.
 * @param modifier Modifier for the chart.
 * @param wavyConfig Configuration for wave appearance and animation.
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels.
 */
@Composable
fun WavyChart(
    data: () -> List<BarData>,
    modifier: Modifier = Modifier,
    color: ChartyColor = ChartyColor.Solid(ChartyColors.Blue),
    wavyConfig: WavyChartConfig = WavyChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
) {
    val dataList = remember(data) { data() }
    require(dataList.isNotEmpty()) { "Wavy chart data cannot be empty" }

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

    ChartScaffold(
        modifier = modifier,
        xLabels = dataList.getLabels(),
        yAxisConfig = AxisConfig(minValue = minValue, maxValue = maxValue, steps = 5, drawAxisAtZero = minValue < 0f),
        config = scaffoldConfig,
    ) { chartContext ->
        val barCount = dataList.size
        if (barCount == 0) return@ChartScaffold

        val barSpacing = chartContext.width / (barCount * WAVY_CHART_PHASE_TARGET_MULTIPLIER)
        val barWidth = barSpacing * wavyConfig.barWidthFraction.coerceIn(MIN_BAR_WIDTH_FRACTION, 1f)
        val waveAmplitude = barWidth * wavyConfig.waveAmplitudeFractionOfBarWidth

        val baselineY = if (minValue < 0f) chartContext.convertValueToYPosition(0f) else chartContext.bottom

        val segments = wavyConfig.waveSegments.coerceAtLeast(MIN_WAVE_SEGMENTS)

        dataList.fastForEachIndexed { index, barData ->
            val xCenter = chartContext.left + barSpacing * (1 + index * 2)
            val valueTop = chartContext.convertValueToYPosition(barData.value)

            val top: Float
            val bottom: Float

            if (barData.value >= 0f) {
                // Positive values extend upwards from the baseline
                top = valueTop
                bottom = baselineY
            } else {
                // Negative values extend downwards from the baseline
                top = baselineY
                bottom = valueTop
            }

            val barHeight = bottom - top
            if (barHeight == 0f) return@fastForEachIndexed

            val path = Path()
            val dy = barHeight / segments

            // Optional per-bar phase offset for cascading effect
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
            val brush = Brush.verticalGradient(barData.color?.value ?: ChartyColors.CoolPalette.colors)
            drawPath(
                path = path,
                brush = brush,
                style = Stroke(width = strokeWidthPx),
            )
        }
    }
}
