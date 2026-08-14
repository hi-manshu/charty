package com.himanshoe.charty.diverging.internal

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.util.fastForEach
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.constants.ChartConstants
import com.himanshoe.charty.diverging.config.DivergingSideScaling
import com.himanshoe.charty.diverging.data.DivergingData
import com.himanshoe.charty.diverging.data.DivergingSide
import kotlin.math.abs

private const val HALF = 2f
private const val EMPTY_SCALE = 0f

/**
 * Resolves the scale of each half.
 *
 * [DivergingSideScaling.SHARED] gives both halves the maximum magnitude found across **both**
 * series, so equal bar lengths mean equal values; [DivergingSideScaling.INDEPENDENT] gives each half
 * its own maximum, so both reach the plot edge but lengths are no longer comparable across the axis.
 *
 * @param dataList The rows currently drawn.
 * @param sideScaling Which of the two scaling policies to apply.
 * @return The scale for each half; a half whose values are all zero reports a scale of `0f`, which
 *   [divergingBarRect] renders as a zero-length bar rather than a division by zero.
 */
internal fun divergingScales(
    dataList: List<DivergingData>,
    sideScaling: DivergingSideScaling,
): DivergingScales {
    var leftMax = EMPTY_SCALE
    var rightMax = EMPTY_SCALE
    dataList.fastForEach { row ->
        if (row.leftValue > leftMax) {
            leftMax = row.leftValue
        }
        if (row.rightValue > rightMax) {
            rightMax = row.rightValue
        }
    }
    return when (sideScaling) {
        DivergingSideScaling.SHARED -> {
            val shared = maxOf(leftMax, rightMax)
            DivergingScales(left = shared, right = shared)
        }
        DivergingSideScaling.INDEPENDENT -> DivergingScales(left = leftMax, right = rightMax)
    }
}

/**
 * The x-coordinate of the centre axis: the horizontal midpoint of the plot, so the two halves get
 * the same amount of room whatever their values.
 *
 * @param chartContext The chart's coordinate context.
 * @return The centre axis position in pixels.
 */
internal fun divergingCenterX(chartContext: ChartContext): Float = chartContext.left + chartContext.width / HALF

/**
 * The drawable length of one half: half the plot, less its share of the centre gap.
 *
 * @param chartContext The chart's coordinate context.
 * @param centerGapFraction Fraction of the plot width left empty at the centre axis.
 * @return The maximum bar length on either side, in pixels.
 */
internal fun divergingHalfWidth(
    chartContext: ChartContext,
    centerGapFraction: Float,
): Float = ((chartContext.width - chartContext.width * centerGapFraction) / HALF).coerceAtLeast(0f)

/**
 * The pixel rectangle of one half-bar, grown outwards from the centre axis.
 *
 * @param chartContext The chart's coordinate context.
 * @param index The row's index among the drawn rows.
 * @param rowCount The number of drawn rows.
 * @param side Which half of the row this bar occupies.
 * @param value The bar's magnitude.
 * @param scale The magnitude that fills this half, from [divergingScales].
 * @param barWidthFraction Fraction of the row's height that the bar occupies.
 * @param centerGapFraction Fraction of the plot width left empty at the centre axis.
 * @param animationProgress The entry animation's progress from `0f` to `1f`.
 * @return The bar's bounds; a zero [scale] or a zero [value] yields a zero-width rectangle anchored
 *   on the centre gap's edge.
 */
@Suppress("LongParameterList")
internal fun divergingBarRect(
    chartContext: ChartContext,
    index: Int,
    rowCount: Int,
    side: DivergingSide,
    value: Float,
    scale: Float,
    barWidthFraction: Float,
    centerGapFraction: Float,
    animationProgress: Float,
): Rect {
    val slotHeight = chartContext.calculateSlotHeight(totalItems = rowCount)
    val slotTop = chartContext.calculateSlotTopPosition(index = index, totalItems = rowCount)
    val barHeight = slotHeight * barWidthFraction
    val top = slotTop + (slotHeight - barHeight) / HALF
    val halfGap = chartContext.width * centerGapFraction / HALF
    val centerX = divergingCenterX(chartContext = chartContext)
    val available = divergingHalfWidth(chartContext = chartContext, centerGapFraction = centerGapFraction)
    val length =
        if (scale <= EMPTY_SCALE) {
            0f
        } else {
            (value / scale).coerceIn(0f, 1f) * available * animationProgress
        }
    return when (side) {
        DivergingSide.LEFT ->
            Rect(
                left = centerX - halfGap - length,
                top = top,
                right = centerX - halfGap,
                bottom = top + barHeight,
            )
        DivergingSide.RIGHT ->
            Rect(
                left = centerX + halfGap,
                top = top,
                right = centerX + halfGap + length,
                bottom = top + barHeight,
            )
    }
}

/**
 * The symmetric value range the chart plots against: `-axisScale` to `+axisScale`, so the zero point
 * — and with it the axis line the scaffold draws at zero — lands on the plot's horizontal midpoint.
 *
 * @param scales The resolved per-half scales.
 * @return The `(min, max)` pair handed to the chart state and the axis.
 */
internal fun divergingValueRange(scales: DivergingScales): Pair<Float, Float> {
    val scale = scales.axisScale
    return -scale to scale
}

/**
 * Builds the value axis: symmetric around the centre, with ticks labelled by magnitude rather than
 * by the signed plot coordinate, because a value's side — not its sign — carries its direction.
 * Under [DivergingSideScaling.INDEPENDENT] the two halves no longer share one scale, so the tick
 * labels would be true of only one of them and are suppressed instead.
 *
 * @param scales The resolved per-half scales.
 * @param sideScaling The scaling policy in force.
 * @param valueFormatter Formats a magnitude for display.
 * @return The axis configuration for the scaffold.
 */
internal fun divergingAxisConfig(
    scales: DivergingScales,
    sideScaling: DivergingSideScaling,
    valueFormatter: (Float) -> String,
): AxisConfig {
    val (minValue, maxValue) = divergingValueRange(scales = scales)
    return AxisConfig(
        minValue = minValue,
        maxValue = maxValue,
        steps = ChartConstants.DEFAULT_AXIS_STEPS,
        drawAxisAtZero = true,
        valueFormatter = { value ->
            if (sideScaling == DivergingSideScaling.INDEPENDENT) {
                ""
            } else {
                valueFormatter(abs(value))
            }
        },
    )
}
