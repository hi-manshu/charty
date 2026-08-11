package com.himanshoe.charty.bar.internal.bar.stackedhorizontal

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import com.himanshoe.charty.bar.config.StackedHorizontalBarChartConfig
import com.himanshoe.charty.bar.config.StackedHorizontalBarSegment
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.common.ChartContext

/**
 * Immutable parameter bundle passed to [drawStackedHorizontalBars].
 *
 * Grouping all draw parameters into a single data class keeps the function signature
 * stable when new options are added to the configuration.
 */
internal data class StackedHorizontalBarDrawParams(
    val dataList: List<BarGroup>,
    val chartContext: ChartContext,
    val config: StackedHorizontalBarChartConfig,
    /** Resolved list of [Color] values derived from the caller's [ChartyColor]. */
    val colorList: List<Color>,
    /** Pre-computed maximum total across all groups — used as the value axis upper bound. */
    val maxTotal: Float,
    val animationProgress: Float,
    val onSegmentClick: ((StackedHorizontalBarSegment) -> Unit)?,
    val segmentBounds: MutableList<Pair<Rect, StackedHorizontalBarSegment>>,
    val recordBounds: Boolean = onSegmentClick != null,
)
