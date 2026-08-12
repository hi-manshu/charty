package com.himanshoe.charty.bar.internal.bar.wavy

import com.himanshoe.charty.bar.config.WavyChartConfig
import com.himanshoe.charty.color.ChartyColor

/**
 * The wave geometry that is identical for every bar in a pass, computed once and handed to each
 * [drawSingleWave] call so the per-bar loop stays cheap and keeps a short parameter list.
 *
 * @property barSpacing Half the horizontal slot each bar occupies, in pixels.
 * @property baselineY The y pixel every wave is measured from: the zero line when the data has
 *   negative values, otherwise the bottom of the plotting area.
 * @property waveAmplitude The horizontal swing of the sine wave, in pixels.
 * @property segments How many line segments each wave is approximated with.
 * @property basePhase The animated phase shared by every bar, in radians.
 * @property strokeWidthPx The wave stroke width, in pixels.
 * @property wavyConfig The wave's per-bar phase offset and the rest of its styling.
 * @property color The fallback colour used for bars that carry no colour of their own.
 */
internal data class WaveDrawContext(
    val barSpacing: Float,
    val baselineY: Float,
    val waveAmplitude: Float,
    val segments: Int,
    val basePhase: Float,
    val strokeWidthPx: Float,
    val wavyConfig: WavyChartConfig,
    val color: ChartyColor,
)
