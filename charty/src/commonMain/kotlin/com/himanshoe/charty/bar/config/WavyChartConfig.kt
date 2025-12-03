package com.himanshoe.charty.bar.config

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing

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
 * @param barWidthFraction Fraction of horizontal slot width occupied by each bar (0–1).
 * @param waveAmplitudeFractionOfBarWidth Amplitude of the wave as a fraction of the bar width.
 * A value of `1f / 3f` means the wave will deviate up to one third of the bar width
 * from its center line.
 * @param waveSegments Number of vertical segments used to approximate the sine wave.
 * Higher values produce smoother curves at the cost of more draw operations.
 * @param animationDurationMillis Duration (in milliseconds) of one full wave cycle.
 * @param animationEasing Easing function used for the wave phase animation.
 * @param strokeWidthDp Stroke width of the wavy line in dp.
 * @param phaseOffsetPerBar Optional additional phase offset applied per bar index.
 * Use `0f` to keep all bars in sync, or a small positive value to create a cascading effect.
 */
data class WavyChartConfig(
    val barWidthFraction: Float = DEFAULT_BAR_WIDTH_FRACTION,
    val waveAmplitudeFractionOfBarWidth: Float = DEFAULT_WAVE_AMPLITUDE_FRACTION,
    val waveSegments: Int = DEFAULT_WAVE_SEGMENTS,
    val animationDurationMillis: Int = DEFAULT_ANIMATION_DURATION_MS,
    val animationEasing: Easing = LinearEasing,
    val strokeWidthDp: Float = DEFAULT_STROKE_WIDTH_DP,
    val phaseOffsetPerBar: Float = 0f,
)
