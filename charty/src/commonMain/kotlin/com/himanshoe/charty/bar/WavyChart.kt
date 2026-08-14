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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.bar.config.WavyChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.internal.bar.barAccessibility
import com.himanshoe.charty.bar.internal.bar.rememberAnimatedBarValues
import com.himanshoe.charty.bar.internal.bar.wavy.FULL_WAVE_CYCLE_RADIANS
import com.himanshoe.charty.bar.internal.bar.wavy.buildWavyGestureModifier
import com.himanshoe.charty.bar.internal.bar.wavy.drawWavyBars
import com.himanshoe.charty.bar.internal.bar.wavy.drawWavyOverlays
import com.himanshoe.charty.bar.internal.bar.wavy.populateWavyBarBounds
import com.himanshoe.charty.bar.internal.bar.wavy.populateWavyCrosshairBounds
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartEmptyState
import com.himanshoe.charty.common.ChartOverlays
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.buildInteractionModifier
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.drawInteractionOverlays
import com.himanshoe.charty.common.gesture.ChartCrosshair
import com.himanshoe.charty.common.gesture.rememberChartCrosshair
import com.himanshoe.charty.common.rememberCartesianChartState
import com.himanshoe.charty.common.streamingPan
import com.himanshoe.charty.common.streamingRender
import com.himanshoe.charty.common.theme.ChartyThemeDefaults
import com.himanshoe.charty.common.theme.orThemeCrosshair
import com.himanshoe.charty.common.theme.orThemeDefault
import com.himanshoe.charty.common.tooltip.ChartTooltip
import com.himanshoe.charty.common.tooltip.NoneTooltip
import com.himanshoe.charty.common.tooltip.isCanvas
import com.himanshoe.charty.common.tooltip.rememberTooltipManager
import com.himanshoe.charty.common.updateInteractionBounds
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
 *   guide line that snaps to the nearest bar, with a built-in or custom label drawn over it. It is
 *   a drag gesture that leaves taps alone, so tap-to-tooltip keeps working alongside it; streaming
 *   scrollback ([ChartInteractionConfig.streamingState]) does not, because the crosshair owns the
 *   drag.
 * @param tooltip How a tapped wave is presented: the built-in canvas bubble (the default), a custom
 *   Compose overlay via [ChartTooltip.compose], or [ChartTooltip.none] to disable it. A wave is a
 *   thin stroked sine curve, so a tap resolves against the whole column it occupies — its full slot
 *   width, spanning value to baseline — rather than the curve itself. Its text comes from
 *   [WavyChartConfig.tooltipFormatter], reading `label: value` by default, and the same formatter
 *   labels the crosshair.
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
    tooltip: ChartTooltip<BarData> = ChartTooltip.canvas(),
) {
    val fullDataList by remember(data) { derivedStateOf { data() } }
    if (fullDataList.isEmpty()) {
        ChartEmptyState(modifier = modifier, content = emptyContent)
        return
    }
    val crosshairConfig = crosshair?.config.orThemeCrosshair().takeIf { crosshair != null }

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
    val resolvedTooltipConfig = wavyConfig.tooltipConfig.orThemeDefault()
    val tooltipManager = rememberTooltipManager<Rect, BarData>(dataKey = dataList)
    val tapEnabled = tooltip !is NoneTooltip
    val crosshairBounds = remember { mutableListOf<Pair<Offset, BarData>>() }
    val (crosshairManager, animatedCrosshairState) =
        rememberChartCrosshair<BarData>(
            enabled = crosshairConfig != null,
            viewPortState = interactionConfig.viewPortState,
            streamingState = interactionConfig.streamingState,
        )

    val basePhase = rememberWavyBasePhase(wavyConfig)
    val strokeWidthPx = wavyConfig.strokeWidthDp.dp.value

    val gestureBase =
        buildWavyGestureModifier(
            dataList = dataList,
            wavyConfig = wavyConfig,
            barBounds = tooltipManager.bounds,
            crosshairBounds = crosshairBounds,
            crosshairManager = crosshairManager,
            dismissOnRelease = crosshairConfig?.dismissOnRelease ?: true,
            onTooltipUpdate = tooltipManager::updateTooltip,
            tapEnabled = tapEnabled,
        )
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

            populateWavyBarBounds(
                chartContext = chartContext,
                dataList = dataList,
                enabled = tapEnabled,
                minValue = minValue,
                strokeWidthPx = strokeWidthPx,
                barBounds = tooltipManager.bounds,
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

            drawWavyOverlays(
                tooltipState = tooltipManager.tooltipState,
                drawTooltipBubble = tooltip.isCanvas(),
                crosshairState = animatedCrosshairState?.resolve(),
                crosshairConfig = crosshairConfig,
                tooltipConfig = resolvedTooltipConfig,
                chartContext = chartContext,
                color = color,
                textMeasurer = textMeasurer,
            )
        }

        ChartOverlays(
            tooltip = tooltip,
            tooltipItem = tooltipManager.selectedItem,
            tooltipAnchor = tooltipManager.tooltipState,
            crosshair = crosshair,
            crosshairItem = crosshairManager?.selectedItem,
            crosshairState = animatedCrosshairState?.resolve(),
        )
    }
}

@Composable
private fun rememberWavyBasePhase(wavyConfig: WavyChartConfig): Float {
    if (!wavyConfig.animateWave) {
        return 0f
    }
    val infinite = rememberInfiniteTransition(label = "wavy-chart")
    val basePhase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = FULL_WAVE_CYCLE_RADIANS,
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
