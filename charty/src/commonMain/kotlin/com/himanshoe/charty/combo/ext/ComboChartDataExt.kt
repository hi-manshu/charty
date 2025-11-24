package com.himanshoe.charty.combo.ext

import com.himanshoe.charty.combo.data.ComboChartData

/**
 * Extension functions for ComboChartData list operations
 */

internal fun List<ComboChartData>.getLabels(): List<String> = map { it.label }

internal fun List<ComboChartData>.getBarValues(): List<Float> = map { it.barValue }

internal fun List<ComboChartData>.getLineValues(): List<Float> = map { it.lineValue }

internal fun List<ComboChartData>.getAllValues(): List<Float> = flatMap { listOf(it.barValue, it.lineValue) }
