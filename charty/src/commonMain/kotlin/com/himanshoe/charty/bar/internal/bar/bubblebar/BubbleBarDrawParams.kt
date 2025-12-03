package com.himanshoe.charty.bar.internal.bar.bubblebar

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.TextMeasurer
import com.himanshoe.charty.bar.config.BubbleBarChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext

/**
 * Parameters for drawing bubble bars
 */
internal data class BubbleBarDrawParams(
    val dataList: List<BarData>,
    val chartContext: ChartContext,
    val bubbleConfig: BubbleBarChartConfig,
    val baselineY: Float,
    val animationProgress: Float,
    val color: ChartyColor,
    val onBarClick: ((BarData) -> Unit)?,
    val barBounds: MutableList<Pair<Rect, BarData>>,
    val textMeasurer: TextMeasurer,
)

