package com.himanshoe.charty.diverging.internal

import androidx.compose.ui.geometry.Rect
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.diverging.config.DivergingBarChartConfig
import com.himanshoe.charty.diverging.data.DivergingData
import com.himanshoe.charty.diverging.data.DivergingSelection

/**
 * Everything [drawDivergingBars] needs for one frame.
 *
 * @property dataList The rows currently drawn, top to bottom.
 * @property chartContext The chart's coordinate context.
 * @property config The chart's appearance configuration.
 * @property scales The resolved per-half scales.
 * @property leftColor The default colour of the left half.
 * @property rightColor The default colour of the right half.
 * @property animationProgress The entry animation's progress from `0f` to `1f`.
 * @property labels Pre-measured value labels, or `null` when labels are off.
 * @property onBarBoundCalculated Receives each drawn bar's bounds paired with the selection it
 *   represents, for hit testing.
 * @property recordBounds Whether bounds are worth recording at all, i.e. whether anything consumes them.
 */
internal data class DivergingDrawParams(
    val dataList: List<DivergingData>,
    val chartContext: ChartContext,
    val config: DivergingBarChartConfig,
    val scales: DivergingScales,
    val leftColor: ChartyColor,
    val rightColor: ChartyColor,
    val animationProgress: Float,
    val labels: DivergingValueLabels?,
    val onBarBoundCalculated: (Pair<Rect, DivergingSelection>) -> Unit,
    val recordBounds: Boolean,
)
