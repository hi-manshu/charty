package com.himanshoe.charty.bar.internal.bar.normalizedhorizontal

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import com.himanshoe.charty.bar.config.NormalizedHorizontalBarChartConfig
import com.himanshoe.charty.bar.config.NormalizedHorizontalBarSegment
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.common.ChartContext

/**
 * Immutable parameter bundle passed to [drawNormalizedHorizontalBars].
 */
internal data class NormalizedHorizontalBarDrawParams(
    val dataList: List<BarGroup>,
    val chartContext: ChartContext,
    val config: NormalizedHorizontalBarChartConfig,
    /** Resolved [Color] list derived from the chart-level [ChartyColor]. */
    val colorList: List<Color>,
    /** Left-side pixel offset reserved for value-axis labels. */
    val axisOffset: Float,
    val animationProgress: Float,
    val onSegmentClick: ((NormalizedHorizontalBarSegment) -> Unit)?,
    val segmentBounds: MutableList<Pair<Rect, NormalizedHorizontalBarSegment>>,
)
