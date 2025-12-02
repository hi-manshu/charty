package com.himanshoe.charty.bar.internal.span

import androidx.compose.ui.geometry.Rect
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.data.SpanData
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext

/**
 * Parameters for drawing spans
 */
internal data class SpanDrawParams(
    val dataList: List<SpanData>,
    val chartContext: ChartContext,
    val barConfig: BarChartConfig,
    val axisOffset: Float,
    val minValue: Float,
    val maxValue: Float,
    val animationProgress: Float,
    val colors: ChartyColor,
    val onSpanClick: ((SpanData) -> Unit)?,
    val onSpanBoundCalculated: (Pair<Rect, SpanData>) -> Unit,
)

