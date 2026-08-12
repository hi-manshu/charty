package com.himanshoe.charty.bar

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.bar.config.WavyChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.internal.bar.barAccessibility
import com.himanshoe.charty.bar.internal.bar.rememberAnimatedBarValues
import com.himanshoe.charty.bar.internal.bar.wavy.WAVY_CHART_PHASE_TARGET_MULTIPLIER
import com.himanshoe.charty.bar.internal.bar.wavy.WavyChartOverlays
import com.himanshoe.charty.bar.internal.bar.wavy.drawWavyBars
import com.himanshoe.charty.bar.internal.bar.wavy.populateWavyCrosshairBounds
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.gesture.ChartCrosshair
import com.himanshoe.charty.common.gesture.chartCrosshairHandler
import com.himanshoe.charty.common.gesture.rememberChartCrosshair
import com.himanshoe.charty.common.rememberCartesianChartState
import com.himanshoe.charty.common.streamingPan
import com.himanshoe.charty.common.streamingRender
import com.himanshoe.charty.common.theme.ChartyThemeDefaults
import com.himanshoe.charty.common.updateInteractionBounds
import com.himanshoe.charty.line.internal.line.drawLineChartCrosshair
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min

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

    val chartState =
        rememberCartesianChartState(
            fullData = fullDataList,
            interactionConfig = interactionConfig,
            animation = Animation.Default,
            visibleWindow = wavyConfig.visibleWindow,
            displayData = {
                rememberAnimatedBarValues(
                    dataList = it,
                    animation = Animation.Default,
                    enabled = wavyConfig.animateValueChanges,
                )
            },
        ) { windowed, _ ->
            remember(windowed) {
                val values = windowed.fastMap { it.value }
                val rawMin = values.minOrNull() ?: 0f
                val rawMax = values.maxOrNull() ?: 0f
                min(rawMin, 0f) to max(rawMax, rawMin.coerceAtLeast(0f))
            }
        }
    val dataList = chartState.data
    val displayList = chartState.displayData
    val minValue = chartState.minValue
    val maxValue = chartState.maxValue

    val textMeasurer = rememberTextMeasurer()
    val crosshairBounds = remember { mutableListOf<Pair<Offset, BarData>>() }
    val (crosshairManager, animatedCrosshairState) =
        rememberChartCrosshair<BarData>(
            enabled = crosshairConfig != null,
            viewPortState = interactionConfig.viewPortState,
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
    Box(modifier = chartModifier.then(interactionConfig.streamingPan(chartState.streaming))) {
        ChartScaffold(
            accessibility =
                barAccessibility(
                    description = interactionConfig.accessibilityDescription,
                    labels = dataList.fastMap { it.label },
                    values = dataList.fastMap { it.value },
                    fallbackDescription = "Wavy chart, ${fullDataList.size} data points.",
                ),
            streaming = interactionConfig.streamingRender(chartState.streaming),
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
                        state = state,
                        config = cfg,
                        chartContext = chartContext,
                        textMeasurer = textMeasurer,
                        chartColor = color,
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
