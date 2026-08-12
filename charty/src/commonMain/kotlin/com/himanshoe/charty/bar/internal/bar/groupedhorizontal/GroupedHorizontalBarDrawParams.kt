package com.himanshoe.charty.bar.internal.bar.groupedhorizontal

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextMeasurer
import com.himanshoe.charty.bar.config.GroupedHorizontalBarChartConfig
import com.himanshoe.charty.bar.config.GroupedHorizontalBarEntry
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.common.ChartContext

/**
 * Immutable parameter bundle for [drawGroupedHorizontalBars].
 *
 * Grouping all draw parameters into a single data class keeps the function signature
 * stable as the configuration grows.
 */
internal data class GroupedHorizontalBarDrawParams(
    val dataList: List<BarGroup>,
    val chartContext: ChartContext,
    val config: GroupedHorizontalBarChartConfig,
    /** Resolved [Color] list derived from the chart-level [ChartyColor]. */
    val colorList: List<Color>,
    /** X pixel position of the zero baseline (or [ChartContext.left] when mode is FROM_MIN_VALUE). */
    val baselineX: Float,
    val minValue: Float,
    val maxValue: Float,
    val animationProgress: Float,
    val onBarClick: ((GroupedHorizontalBarEntry) -> Unit)?,
    val barBounds: MutableList<Pair<Rect, GroupedHorizontalBarEntry>>,
    val recordBounds: Boolean = onBarClick != null,
    /** Measures persistent-marker labels; `null` skips marker drawing. */
    val textMeasurer: TextMeasurer? = null,
)
