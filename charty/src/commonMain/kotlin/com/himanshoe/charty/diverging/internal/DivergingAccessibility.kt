package com.himanshoe.charty.diverging.internal

import androidx.compose.ui.util.fastMapIndexed
import com.himanshoe.charty.common.accessibility.ChartAccessibility
import com.himanshoe.charty.diverging.data.DivergingData

/**
 * Builds the chart-level screen-reader summary: how many rows are shown, and which row leans
 * furthest to each side, since the lean is what a reader looks for in a diverging chart.
 *
 * @param dataList The rows currently drawn.
 * @param leftSeriesName The name of the left-hand series.
 * @param rightSeriesName The name of the right-hand series.
 * @param valueFormatter Formats a magnitude for speech.
 * @return A sentence describing the chart.
 */
internal fun generateDivergingChartDescription(
    dataList: List<DivergingData>,
    leftSeriesName: String,
    rightSeriesName: String,
    valueFormatter: (Float) -> String,
): String {
    if (dataList.isEmpty()) {
        return "Empty diverging bar chart."
    }
    val widestLeft = dataList.maxByOrNull { row -> row.leftValue }
    val widestRight = dataList.maxByOrNull { row -> row.rightValue }
    return buildString {
        append("Diverging bar chart, ${dataList.size} categories, $leftSeriesName versus $rightSeriesName. ")
        widestLeft?.let { row ->
            append("Largest $leftSeriesName: ${valueFormatter(row.leftValue)} (${row.label}). ")
        }
        widestRight?.let { row ->
            append("Largest $rightSeriesName: ${valueFormatter(row.rightValue)} (${row.label}).")
        }
    }
}

/**
 * Bundles the chart summary with one description per row, so assistive technology can traverse the
 * rows and hear both halves of each.
 *
 * @param description The chart summary, or `null` when the caller suppressed it.
 * @param dataList The rows currently drawn.
 * @param leftSeriesName The name of the left-hand series.
 * @param rightSeriesName The name of the right-hand series.
 * @param valueFormatter Formats a magnitude for speech.
 * @return The accessibility payload handed to the chart scaffold.
 */
internal fun divergingAccessibility(
    description: String?,
    dataList: List<DivergingData>,
    leftSeriesName: String,
    rightSeriesName: String,
    valueFormatter: (Float) -> String,
): ChartAccessibility =
    ChartAccessibility(
        contentDescription = description,
        dataPointDescriptions =
            dataList.fastMapIndexed { index, row ->
                "Row ${index + 1} of ${dataList.size}: ${row.label}, " +
                    "$leftSeriesName ${valueFormatter(row.leftValue)}, " +
                    "$rightSeriesName ${valueFormatter(row.rightValue)}"
            },
    )
