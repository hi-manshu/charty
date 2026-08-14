package com.himanshoe.charty.line.internal.area

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextMeasurer
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineData

/**
 * Everything [drawAreaChart] needs for one canvas pass, bundled so the drawing entry point keeps a
 * single parameter.
 *
 * @property dataList The points being drawn, in the order they appear along the x-axis.
 * @property pointPositions The pixel position of every point in [dataList], at the same indices.
 * @property baselineY The y pixel the filled area closes down to.
 * @property config The line/point styling, markers, reference band, and reference line to honour.
 * @property color The area's colour or gradient.
 * @property fillAlpha The opacity applied to the area fill, from `0f` to `1f`.
 * @property animationProgress The reveal progress, from `0f` (nothing drawn) to `1f` (fully drawn).
 * @property chartContext The pixel bounds and value range of the plotting area.
 * @property textMeasurer Measurer used for reference-band and marker labels.
 */
internal data class AreaChartDrawParams(
    val dataList: List<LineData>,
    val pointPositions: List<Offset>,
    val baselineY: Float,
    val config: LineChartConfig,
    val color: ChartyColor,
    val fillAlpha: Float,
    val animationProgress: Float,
    val chartContext: ChartContext,
    val textMeasurer: TextMeasurer,
)
