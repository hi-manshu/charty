package com.himanshoe.charty.combo.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMapIndexed
import com.himanshoe.charty.bar.config.NegativeValuesDrawMode
import com.himanshoe.charty.combo.config.ComboChartConfig
import com.himanshoe.charty.combo.data.ComboChartData
import com.himanshoe.charty.combo.ext.getAllValues
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.axis.AxisConfig

private const val COMBO_SECONDARY_AXIS_STEPS = 6

/** Builds the secondary [AxisConfig] for a `(min, max)` line range, or `null` when the range is null. */
internal fun Pair<Float, Float>?.toSecondaryAxisConfig(): AxisConfig? =
    this?.let { AxisConfig(minValue = it.first, maxValue = it.second, steps = COMBO_SECONDARY_AXIS_STEPS) }

/**
 * Computes the primary (left) value-axis range for a combo chart. Uses only the bar values when
 * [secondaryAxisForLine] is set (the line then gets its own right axis); otherwise both series.
 */
internal fun comboPrimaryRange(
    dataList: List<ComboChartData>,
    negativeValuesDrawMode: NegativeValuesDrawMode,
    secondaryAxisForLine: Boolean,
): Pair<Float, Float> {
    val values = if (secondaryAxisForLine) dataList.map { it.barValue } else dataList.getAllValues()
    val calculatedMin = values.minOrNull() ?: 0f
    val calculatedMax = values.maxOrNull() ?: 0f
    val minVal =
        if (negativeValuesDrawMode == NegativeValuesDrawMode.BELOW_AXIS) minOf(calculatedMin, 0f) else calculatedMin
    val maxVal = maxOf(calculatedMax, if (minVal < 0f) 0f else calculatedMin)
    return minVal to maxVal
}

/**
 * Computes the secondary (right) axis range from the line values, or `null` when the line shares the
 * primary axis (i.e. [secondaryAxisForLine] is `false`).
 */
internal fun comboLineRange(
    dataList: List<ComboChartData>,
    secondaryAxisForLine: Boolean,
): Pair<Float, Float>? =
    if (secondaryAxisForLine) {
        val lineValues = dataList.map { it.lineValue }
        (lineValues.minOrNull() ?: 0f) to (lineValues.maxOrNull() ?: 0f)
    } else {
        null
    }

/**
 * Calculate positions for line points
 */
internal fun ChartContext.calculateLinePointPositions(
    dataList: List<ComboChartData>,
    lineRange: Pair<Float, Float>? = null,
): List<Offset> =
    dataList.fastMapIndexed { index, comboData ->
        Offset(
            x = calculateCenteredXPosition(index = index, totalItems = dataList.size),
            y =
                if (lineRange == null) {
                    convertValueToYPosition(comboData.lineValue)
                } else {
                    convertValueToYPosition(
                        value = comboData.lineValue,
                        rangeMin = lineRange.first,
                        rangeMax = lineRange.second,
                    )
                },
        )
    }

/**
 * Add hit areas for point interaction
 */
internal fun MutableList<Pair<Rect, ComboChartData>>.addPointHitAreas(
    pointPositions: List<Offset>,
    dataList: List<ComboChartData>,
    comboConfig: ComboChartConfig,
    animationProgress: Float,
) {
    pointPositions.fastForEachIndexed { index, position ->
        val pointProgress = index.toFloat() / (pointPositions.size - 1).coerceAtLeast(1)
        if (pointProgress <= animationProgress) {
            val hitRadius = comboConfig.pointRadius * ComboChartConstants.TWO
            add(
                Rect(
                    left = position.x - hitRadius,
                    top = position.y - hitRadius,
                    right = position.x + hitRadius,
                    bottom = position.y + hitRadius,
                ) to dataList[index],
            )
        }
    }
}

/**
 * Calculate bar dimensions with animation
 */
internal data class BarDimensions(
    val top: Float,
    val height: Float,
)

internal fun calculateAnimatedBarDimensions(
    barValueY: Float,
    baselineY: Float,
    isNegative: Boolean,
    animationProgress: Float,
): BarDimensions =
    if (isNegative) {
        val fullBarHeight = barValueY - baselineY
        BarDimensions(
            top = baselineY,
            height = fullBarHeight * animationProgress,
        )
    } else {
        val fullBarHeight = baselineY - barValueY
        val animatedBarHeight = fullBarHeight * animationProgress
        BarDimensions(
            top = baselineY - animatedBarHeight,
            height = animatedBarHeight,
        )
    }
