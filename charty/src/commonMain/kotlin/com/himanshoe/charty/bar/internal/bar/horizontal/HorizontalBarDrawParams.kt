package com.himanshoe.charty.bar.internal.bar.horizontal

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.TextMeasurer
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext

/**
 * Parameters for drawing horizontal bars
 */
internal data class HorizontalBarDrawParams(
    val dataList: List<BarData>,
    val chartContext: ChartContext,
    val barConfig: BarChartConfig,
    val baselineX: Float,
    val animationProgress: Float,
    val color: ChartyColor,
    val isBelowAxisMode: Boolean,
    val minValue: Float,
    val maxValue: Float,
    val onBarBoundCalculated: (Pair<Rect, BarData>) -> Unit,
    val textMeasurer: TextMeasurer? = null,
    val recordBounds: Boolean,
)
