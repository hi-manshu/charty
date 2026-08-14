package com.himanshoe.charty.bar.internal.bar.barchart

import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.common.accessibility.ChartAccessibility
import com.himanshoe.charty.common.accessibility.buildDataPointDescriptions
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.config.NegativeValuesDrawMode
import com.himanshoe.charty.common.constants.ChartConstants
import com.himanshoe.charty.common.data.getLabels
import com.himanshoe.charty.common.data.getValues
import com.himanshoe.charty.common.util.baselineValueRange
import com.himanshoe.charty.common.util.calculateMaxValue
import com.himanshoe.charty.common.util.calculateMinValue

private const val MIN_BAR_WIDTH = 1f

/**
 * Shrinks a bar's slot width by [barSpacing] pixels so adjacent bars are visually separated, never
 * going below [MIN_BAR_WIDTH]. A [barSpacing] of `0` leaves the width unchanged.
 *
 * @param fullBarWidth The bar width from [barWidthFraction][com.himanshoe.charty.bar.config.BarChartConfig.barWidthFraction] alone.
 * @param barSpacing The configured pixel gap to carve out of the bar's width.
 * @return The width to actually draw the bar at, centered within its slot.
 */
internal fun effectiveBarWidth(
    fullBarWidth: Float,
    barSpacing: Float,
): Float = (fullBarWidth - barSpacing).coerceAtLeast(MIN_BAR_WIDTH)

/**
 * Computes the `(min, max)` value axis range for a bar chart, honoring [negativeValuesDrawMode].
 *
 * - [NegativeValuesDrawMode.BELOW_AXIS] clamps the minimum to `0` unless the data actually goes
 *   negative, so bars grow from a zero baseline and negatives drop below it.
 * - [NegativeValuesDrawMode.FROM_MIN_VALUE] keeps the real (nice-rounded) minimum, so the axis
 *   starts at the lowest data value instead of zero.
 */
internal fun barValueRange(
    values: List<Float>,
    negativeValuesDrawMode: NegativeValuesDrawMode,
): Pair<Float, Float> =
    when (negativeValuesDrawMode) {
        NegativeValuesDrawMode.FROM_MIN_VALUE -> calculateMinValue(values) to calculateMaxValue(values)
        NegativeValuesDrawMode.BELOW_AXIS -> baselineValueRange(values)
    }

/**
 * Computes `(barTop, barHeight)` for a vertical bar.
 *
 * The "grow downward from the baseline" treatment is applied **only** in
 * [BELOW_AXIS][NegativeValuesDrawMode.BELOW_AXIS] mode, where the baseline is the zero line. In
 * [FROM_MIN_VALUE][NegativeValuesDrawMode.FROM_MIN_VALUE] mode the baseline is the bottom edge, so
 * every bar (including negative-valued ones, which still sit above the axis minimum) grows upward.
 */
internal fun calculateVerticalBarDimensions(
    isNegative: Boolean,
    isBelowAxisMode: Boolean,
    baselineY: Float,
    barValueY: Float,
    animationProgress: Float,
): Pair<Float, Float> =
    if (isNegative && isBelowAxisMode) {
        baselineY to (barValueY - baselineY) * animationProgress
    } else {
        val animatedBarHeight = (baselineY - barValueY) * animationProgress
        (baselineY - animatedBarHeight) to animatedBarHeight
    }

/**
 * Helper function to create the axis configuration for the Y axis
 */
internal fun createBarAxisConfig(
    minValue: Float,
    maxValue: Float,
    isBelowAxisMode: Boolean,
): AxisConfig =
    AxisConfig(
        minValue = minValue,
        maxValue = maxValue,
        steps = ChartConstants.DEFAULT_AXIS_STEPS,
        drawAxisAtZero = isBelowAxisMode,
    )

/**
 * Builds the accessibility payload for a vertical bar chart: the chart-level summary plus one
 * description per bar so a screen reader can traverse the data.
 *
 * @param description The generated chart summary, or `null` when accessibility is off.
 * @param dataList The bars currently drawn.
 * @return The accessibility payload handed to the chart scaffold.
 */
internal fun barChartAccessibility(
    description: String?,
    dataList: List<BarData>,
): ChartAccessibility =
    ChartAccessibility(
        contentDescription = description,
        dataPointDescriptions =
            buildDataPointDescriptions(
                labels = dataList.getLabels(),
                values = dataList.getValues(),
            ),
    )
