package com.himanshoe.charty.line.internal.area

import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.ChartContext
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.util.fastMapIndexed
import com.himanshoe.charty.line.data.LineData

internal const val DEFAULT_AXIS_STEPS = 6
internal const val TAP_RADIUS_MULTIPLIER = 2.5f

/**
 * Creates axis configuration for area chart.
 */
internal fun createAxisConfig(
    minValue: Float,
    maxValue: Float,
    isBelowAxisMode: Boolean
): AxisConfig {
    return AxisConfig(
        minValue = minValue,
        maxValue = maxValue,
        steps = DEFAULT_AXIS_STEPS,
        drawAxisAtZero = isBelowAxisMode,
    )
}

/**
 * Calculates point positions for all data points.
 */
internal fun calculatePointPositions(
    dataList: List<LineData>,
    chartContext: ChartContext,
    onPointCalculated: (Pair<Offset, LineData>) -> Unit
): List<Offset> {
    return dataList.fastMapIndexed { index, point ->
        val position = Offset(
            x = chartContext.calculateCenteredXPosition(index, dataList.size),
            y = chartContext.convertValueToYPosition(point.value),
        )
        onPointCalculated(position to point)
        position
    }
}

/**
 * Calculates baseline Y position for the area fill.
 */
internal fun calculateBaselineY(
    minValue: Float,
    isBelowAxisMode: Boolean,
    chartContext: ChartContext
): Float {
    return if (minValue < 0f && isBelowAxisMode) {
        chartContext.convertValueToYPosition(0f)
    } else {
        chartContext.bottom
    }
}

