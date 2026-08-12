package com.himanshoe.charty.bar.internal.bar.horizontal

import androidx.compose.ui.util.fastMap
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.accessibility.ChartAccessibility
import com.himanshoe.charty.common.accessibility.buildDataPointDescriptions
import com.himanshoe.charty.common.axis.AxisConfig

internal fun createHorizontalAxisConfig(
    minValue: Float,
    maxValue: Float,
    drawAxisAtZero: Boolean,
): AxisConfig =
    AxisConfig(
        minValue = minValue,
        maxValue = maxValue,
        steps = HORIZONTAL_DEFAULT_AXIS_STEPS,
        drawAxisAtZero = drawAxisAtZero,
    )

internal fun calculateHorizontalBaselineX(
    drawAxisAtZero: Boolean,
    minValue: Float,
    maxValue: Float,
    chartContext: ChartContext,
): Float {
    val range = maxValue - minValue
    if (!drawAxisAtZero || range == 0f) {
        return chartContext.left
    }
    val zeroNormalized = (0f - minValue) / range
    return chartContext.left + (zeroNormalized * chartContext.width)
}

internal fun calculateHorizontalBarDimensions(
    isNegative: Boolean,
    isBelowAxisMode: Boolean,
    baselineX: Float,
    barValueX: Float,
    animationProgress: Float,
): Pair<Float, Float> =
    if (isNegative && isBelowAxisMode) {
        val fullBarWidth = baselineX - barValueX
        val barWidth = fullBarWidth * animationProgress
        val barLeft = barValueX
        barLeft to barWidth
    } else {
        val fullBarWidth = barValueX - baselineX
        val barWidth = fullBarWidth * animationProgress
        val barLeft = baselineX
        barLeft to barWidth
    }

/**
 * Builds the accessibility payload for a horizontal bar chart: the chart-level summary plus one
 * description per bar so a screen reader can traverse the data.
 *
 * @param description The generated chart summary, or `null` when accessibility is off.
 * @param dataList The bars currently drawn, top to bottom.
 * @return The accessibility payload handed to the chart scaffold.
 */
internal fun horizontalBarAccessibility(
    description: String?,
    dataList: List<BarData>,
): ChartAccessibility =
    ChartAccessibility(
        contentDescription = description,
        dataPointDescriptions =
            buildDataPointDescriptions(
                labels = dataList.fastMap { it.label },
                values = dataList.fastMap { it.value },
            ),
    )
