package com.himanshoe.charty.line.internal.stackedarea

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineGroup
import com.himanshoe.charty.line.data.StackedAreaPoint

/**
 * Parameters for drawing a stacked area series
 */
internal data class StackedAreaSeriesParams(
    val seriesIndex: Int,
    val seriesColor: Color,
    val cumulativePositions: List<Offset>,
    val lowerPositions: List<Offset>,
    val startX: Float,
    val baselineY: Float,
    val lineConfig: LineChartConfig,
    val fillAlpha: Float,
    val animationProgress: Float,
    val dataList: List<LineGroup>,
    val onSegmentBoundsCalculated: ((Triple<Rect, Path, StackedAreaPoint>) -> Unit)?,
)

