package com.himanshoe.charty.bar

import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.util.fastForEachIndexed
import com.himanshoe.charty.bar.config.WaterfallChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.internal.bar.waterfall.calculateWaterfallBarParams
import com.himanshoe.charty.bar.internal.bar.waterfall.calculateWaterfallRange
import com.himanshoe.charty.bar.internal.bar.waterfall.createWaterfallClickModifier
import com.himanshoe.charty.bar.internal.bar.waterfall.drawWaterfallBar
import com.himanshoe.charty.bar.internal.bar.waterfall.rememberCumulativeValues
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.animation.rememberChartAnimation
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.tooltip.TooltipState
import com.himanshoe.charty.common.tooltip.drawTooltip

/**
 * Waterfall Chart - visualizes cumulative effect of sequential gains/losses.
 *
 * @param data Lambda returning list of bar data to display
 * @param modifier Modifier for the chart
 * @param config Configuration for waterfall chart appearance
 * @param scaffoldConfig Chart styling configuration for axis, grid, and labels
 * @param onBarClick Optional callback when a bar is clicked
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun WaterfallChart(
    data: () -> List<BarData>,
    modifier: Modifier = Modifier,
    config: WaterfallChartConfig = WaterfallChartConfig(),
    scaffoldConfig: ChartScaffoldConfig = ChartScaffoldConfig(),
    onBarClick: ((BarData) -> Unit)? = null,
) {
    val items = remember(data) { data() }
    require(items.isNotEmpty()) { "Waterfall chart data cannot be empty" }

    val cumulativeValues = rememberCumulativeValues(items)
    val (minValue, maxValue) = remember(cumulativeValues) {
        calculateWaterfallRange(cumulativeValues)
    }

    val animationProgress = rememberChartAnimation(config.animation)
    var tooltipState by remember { mutableStateOf<TooltipState?>(null) }
    val barBounds = remember { mutableListOf<Pair<Rect, BarData>>() }
    val textMeasurer = rememberTextMeasurer()

    ChartScaffold(
        modifier = modifier.then(
            createWaterfallClickModifier(items, config, barBounds, onBarClick) { tooltipState = it }
        ),
        xLabels = items.map { it.label },
        yAxisConfig = AxisConfig(
            minValue = minValue,
            maxValue = maxValue,
            steps = 6,
            drawAxisAtZero = true,
        ),
        config = scaffoldConfig,
    ) { chartContext ->
        barBounds.clear()

        items.fastForEachIndexed { index, bar ->
            val barParams = calculateWaterfallBarParams(
                index = index,
                bar = bar,
                items = items,
                cumulativeValues = cumulativeValues,
                config = config,
                chartContext = chartContext,
                animationProgress = animationProgress.value,
            )

            if (onBarClick != null) {
                barBounds.add(barParams.bounds to bar)
            }

            drawWaterfallBar(
                brush = barParams.brush,
                x = barParams.x,
                y = barParams.y,
                width = barParams.width,
                height = barParams.height,
                cornerRadius = config.cornerRadius.value,
            )
        }

        tooltipState?.let { state ->
            drawTooltip(
                tooltipState = state,
                config = config.tooltipConfig,
                textMeasurer = textMeasurer,
                chartWidth = chartContext.right,
                chartTop = chartContext.top,
                chartBottom = chartContext.bottom,
            )
        }
    }
}


