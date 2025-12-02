package com.himanshoe.charty.bar.internal.bar.comparison

import androidx.compose.ui.geometry.Rect
import com.himanshoe.charty.bar.config.ComparisonBarChartConfig
import com.himanshoe.charty.bar.config.ComparisonBarSegment
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.common.ChartContext

/**
 * Parameters for drawing comparison bars
 */
internal data class ComparisonBarDrawParams(
    val dataList: List<BarGroup>,
    val chartContext: ChartContext,
    val comparisonConfig: ComparisonBarChartConfig,
    val baselineY: Float,
    val onBarClick: ((ComparisonBarSegment) -> Unit)?,
    val barBounds: MutableList<Pair<Rect, ComparisonBarSegment>>,
)

