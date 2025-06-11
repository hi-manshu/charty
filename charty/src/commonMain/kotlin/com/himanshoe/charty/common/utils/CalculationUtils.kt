package com.himanshoe.charty.common.utils

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.himanshoe.charty.common.config.LabelConfig

internal data class ChartPaddings(
    val top: Dp,
    val bottom: Dp,
    val start: Dp,
    val end: Dp
)

internal fun calculateChartPaddings(
    labelConfig: LabelConfig,
    yAxisLabelWidth: Dp,
    xAxisLabelHeight: Dp, // Base height for X-axis labels
    xAxisLabelHeightWhenNegative: Dp, // Specific height when X-axis is centered (canDrawNegativeChart)
    hasNegativeValuesForXAxis: Boolean, // If X-axis itself is centered (not just labels below it)
    canDrawNegativeChart: Boolean, // BarChart specific: implies centered X-axis and special bottom padding for labels
    allYValuesAreZero: Boolean, // Affects Y label padding visibility
    hasTopFixedPadding: Boolean,
    topFixedPaddingValue: Dp
): ChartPaddings {

    val finalTopPadding = if (hasTopFixedPadding) topFixedPaddingValue else 0.dp
    val finalStartPadding = if (labelConfig.showYLabel && !allYValuesAreZero) yAxisLabelWidth else 0.dp

    // Determine bottom padding based on X-label visibility and chart type specifics
    val baseBottomPadding = if (labelConfig.showXLabel && !hasNegativeValuesForXAxis) {
        xAxisLabelHeight // Normal case, X labels at bottom, X-axis not centered
    } else if (labelConfig.showXLabel && hasNegativeValuesForXAxis && !canDrawNegativeChart) {
        // This case might be for future charts where X-axis is centered but labels are still simple height
        xAxisLabelHeight
    }
    else {
        0.dp
    }

    val finalBottomPadding = if (labelConfig.showXLabel && canDrawNegativeChart) {
        // BarChart with centered X-axis: labels need more space, potentially different from simple xAxisLabelHeight
        xAxisLabelHeightWhenNegative
    } else {
        baseBottomPadding
    }

    return ChartPaddings(
        top = finalTopPadding,
        bottom = finalBottomPadding,
        start = finalStartPadding,
        end = 0.dp // end padding is not currently used but included for completeness
    )
}

internal fun <T> calculateValueRange(
    data: List<T>,
    yValueSelector: (T) -> Float,
    handleAllZeroAsSpecialMax: Boolean = false,
    defaultMinIfAllPositive: Float? = 0f,
    defaultMaxIfAllNegative: Float? = 0f
): Pair<Float, Float> {
    if (data.isEmpty()) {
        return 0f to 0f // Or throw an exception, depending on desired behavior for empty data
    }

    val yValues = data.map(yValueSelector)

    val allValuesAreZero = yValues.all { it == 0f }
    val allValuesArePositiveOrZero = yValues.all { it >= 0f }
    val allValuesAreNegativeOrZero = yValues.all { it <= 0f }

    val actualMin = yValues.minOrNull() ?: 0f
    val actualMax = yValues.maxOrNull() ?: 0f

    val finalMin: Float = if (defaultMinIfAllPositive != null && allValuesArePositiveOrZero) {
        defaultMinIfAllPositive
    } else {
        actualMin
    }

    val finalMax: Float = if (handleAllZeroAsSpecialMax && allValuesAreZero) {
        -1f // BarChart specific case for all zero values
    } else if (defaultMaxIfAllNegative != null && allValuesAreNegativeOrZero) {
        defaultMaxIfAllNegative
    } else {
        actualMax
    }

    // Ensure min is not greater than max, except for the special -1f case for BarChart
    if (finalMin > finalMax && finalMax != -1f) {
        return finalMax to finalMin // Should not happen with current logic but as a safeguard
    }

    return finalMin to finalMax
}
