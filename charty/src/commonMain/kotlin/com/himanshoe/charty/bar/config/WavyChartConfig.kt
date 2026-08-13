package com.himanshoe.charty.bar.config

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.Stable
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.common.config.PersistentMarker
import com.himanshoe.charty.common.config.requireValidVisibleWindow
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.common.tooltip.TooltipPosition
import com.himanshoe.charty.common.util.toChartLabel

private const val DEFAULT_BAR_WIDTH_FRACTION = 0.8f
private const val DEFAULT_WAVE_AMPLITUDE_FRACTION = 1f / 3f
private const val DEFAULT_WAVE_SEGMENTS = 40
private const val DEFAULT_ANIMATION_DURATION_MS = 500
private const val DEFAULT_STROKE_WIDTH_DP = 3f

/**
 * Configuration options for [com.himanshoe.charty.bar.WavyChart].
 *
 * This config controls the visual appearance and animation behavior of the
 * wavy lines that represent each bar.
 *
 * @property barWidthFraction Fraction of horizontal slot width occupied by each bar (0–1).
 * @property waveAmplitudeFractionOfBarWidth Amplitude of the wave as a fraction of the bar width.
 * A value of `1f / 3f` means the wave will deviate up to one third of the bar width
 * from its center line.
 * @property waveSegments Number of vertical segments used to approximate the sine wave.
 * Higher values produce smoother curves at the cost of more draw operations.
 * @property animationDurationMillis Duration (in milliseconds) of one full wave cycle.
 * @property animationEasing Easing function used for the wave phase animation.
 * @property strokeWidthDp Stroke width of the wavy line in dp.
 * @property phaseOffsetPerBar Optional additional phase offset applied per bar index.
 * Use `0f` to keep all bars in sync, or a small positive value to create a cascading effect.
 * @property animateWave Whether the wave travels along each bar. When `false` the wave is drawn at
 *   a fixed phase and no continuous animation runs at all, which is what a caller wants when
 *   animations are switched off; leaving one running would occupy the frame clock forever.
 * @property animateValueChanges When `true`, wave heights tween from their previous values to the
 *   new ones whenever the data changes, using the same default spec that eases the chart's value
 *   axis; when `false` (default) new data appears instantly. This is independent of the continuous
 *   wave motion driven by [animationDurationMillis].
 * @property markers Persistent markers pinned to specific waves, drawn at all times regardless of
 *   touch (see [PersistentMarker]). A marker is anchored at the top-centre of its wave.
 *   `PersistentMarker(dataIndex = -1)` is the idiomatic way to label the latest value — the rightmost
 *   wave. Empty (the default) draws none.
 * @property visibleWindow Rolling "show last N" window; `null` (default) shows every point and
 *   changes nothing. As data is appended the window advances to the latest. Must be `>= 2`.
 * @property tooltipConfig Appearance of the built-in canvas tooltip shown when a wave is tapped.
 *   `null`, the default, takes it from the ambient
 *   [ChartyTheme][com.himanshoe.charty.common.theme.ChartyTheme].
 * @property tooltipPosition Preferred position for the tooltip (ABOVE, BELOW, or AUTO).
 * @property tooltipFormatter Converts a tapped wave into the string shown in its tooltip.
 */
@Stable
data class WavyChartConfig(
    val barWidthFraction: Float = DEFAULT_BAR_WIDTH_FRACTION,
    val waveAmplitudeFractionOfBarWidth: Float = DEFAULT_WAVE_AMPLITUDE_FRACTION,
    val waveSegments: Int = DEFAULT_WAVE_SEGMENTS,
    val animationDurationMillis: Int = DEFAULT_ANIMATION_DURATION_MS,
    val animationEasing: Easing = FastOutSlowInEasing,
    val strokeWidthDp: Float = DEFAULT_STROKE_WIDTH_DP,
    val phaseOffsetPerBar: Float = 0f,
    val animateWave: Boolean = true,
    val animateValueChanges: Boolean = false,
    val markers: List<PersistentMarker> = emptyList(),
    val visibleWindow: Int? = null,
    val tooltipConfig: TooltipConfig? = null,
    val tooltipPosition: TooltipPosition = TooltipPosition.AUTO,
    val tooltipFormatter: (BarData) -> String = { barData ->
        "${barData.label}: ${barData.value.toChartLabel()}"
    },
) {
    init {
        requireValidVisibleWindow(visibleWindow)
        require(barWidthFraction in 0f..1f) { "barWidthFraction must be between 0 and 1" }
        require(waveAmplitudeFractionOfBarWidth >= 0f) { "waveAmplitudeFractionOfBarWidth must be non-negative" }
        require(waveSegments >= 1) { "waveSegments must be at least 1" }
        require(animationDurationMillis > 0) { "animationDurationMillis must be positive" }
        require(strokeWidthDp > 0f) { "strokeWidthDp must be positive" }
    }
}
