package com.himanshoe.charty.candlestick.ext

import com.himanshoe.charty.candlestick.data.CandleData

private const val CHART_PADDING_MULTIPLIER_HIGH = 1.05f
private const val CHART_PADDING_MULTIPLIER_LOW = 0.95f

/**
 * Extension functions for CandleData list operations
 */

/**
 * Extract all labels from candlestick data
 */
internal fun List<CandleData>.getLabels(): List<String> = map { it.label }

/**
 * Get all high values from candlestick data
 */
internal fun List<CandleData>.getHighValues(): List<Float> = map { it.high }

/**
 * Get all low values from candlestick data
 */
internal fun List<CandleData>.getLowValues(): List<Float> = map { it.low }

/**
 * Calculate maximum value considering all high values
 */
internal fun calculateMaxValue(data: List<CandleData>): Float {
    val maxHigh = data.maxOfOrNull { it.high } ?: 0f
    return maxHigh * CHART_PADDING_MULTIPLIER_HIGH // Add 5% padding
}

/**
 * Calculate minimum value considering all low values
 */
internal fun calculateMinValue(data: List<CandleData>): Float {
    val minLow = data.minOfOrNull { it.low } ?: 0f
    return minLow * CHART_PADDING_MULTIPLIER_LOW // Subtract 5% padding
}
