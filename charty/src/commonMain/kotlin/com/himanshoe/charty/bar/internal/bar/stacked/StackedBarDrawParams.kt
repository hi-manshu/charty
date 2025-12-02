package com.himanshoe.charty.bar.internal.bar.stacked

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import com.himanshoe.charty.bar.config.StackedBarChartConfig
import com.himanshoe.charty.bar.config.StackedBarSegment
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.common.ChartContext

/**
 * Parameters for drawing stacked bars
 */
internal data class StackedBarDrawParams(
    val dataList: List<BarGroup>,
    val chartContext: ChartContext,
    val stackedConfig: StackedBarChartConfig,
    val colorList: List<Color>,
    val animationProgress: Float,
    val onSegmentClick: ((StackedBarSegment) -> Unit)?,
    val segmentBounds: MutableList<Pair<Rect, StackedBarSegment>>,
)

