package com.himanshoe.charty.common.accessibility

import androidx.compose.ui.util.fastFlatMap
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.block.data.BlockData
import com.himanshoe.charty.calendar.data.CalendarData
import com.himanshoe.charty.candlestick.data.CandleData
import com.himanshoe.charty.circular.data.CircularRingData
import com.himanshoe.charty.combo.data.ComboChartData
import com.himanshoe.charty.line.data.LineData
import com.himanshoe.charty.line.data.LineGroup
import com.himanshoe.charty.pie.data.PieData
import com.himanshoe.charty.point.data.BubbleData
import com.himanshoe.charty.point.data.PointData
import com.himanshoe.charty.radar.data.RadarDataSet

/**
 * Generates a human-readable accessibility description for a line or area chart.
 *
 * Example output:
 * `"Line chart, 12 data points. Range: 10 to 87. Peak: 87 at Jun 3. Lowest: 10 at Jan 1."`
 *
 * @param data The list of [LineData] points displayed by the chart.
 * @param minValue The chart's y-axis minimum value.
 * @param maxValue The chart's y-axis maximum value.
 * @return A descriptive string suitable for use as a `contentDescription`.
 */
fun generateLineChartDescription(
    data: List<LineData>,
    minValue: Float,
    maxValue: Float,
): String {
    if (data.isEmpty()) {
        return "Empty line chart."
    }
    val peak = data.maxByOrNull { it.value }
    val trough = data.minByOrNull { it.value }
    return buildString {
        append("Line chart, ${data.size} data points. ")
        append("Range: ${minValue.toReadableString()} to ${maxValue.toReadableString()}. ")
        peak?.let { append("Peak: ${it.value.toReadableString()} at ${it.label}. ") }
        trough?.let { append("Lowest: ${it.value.toReadableString()} at ${it.label}.") }
    }
}

/**
 * Generates a human-readable accessibility description for a bar chart.
 *
 * Example output:
 * `"Bar chart, 4 bars. Range: 20 to 150. Highest: 150 (Apr). Lowest: 20 (Jan)."`
 *
 * @param data The list of [BarData] items displayed by the chart.
 * @param minValue The chart's y-axis minimum value.
 * @param maxValue The chart's y-axis maximum value.
 * @return A descriptive string suitable for use as a `contentDescription`.
 */
fun generateBarChartDescription(
    data: List<BarData>,
    minValue: Float,
    maxValue: Float,
): String {
    if (data.isEmpty()) {
        return "Empty bar chart."
    }
    val highest = data.maxByOrNull { it.value }
    val lowest = data.minByOrNull { it.value }
    return buildString {
        append("Bar chart, ${data.size} bars. ")
        append("Range: ${minValue.toReadableString()} to ${maxValue.toReadableString()}. ")
        highest?.let { append("Highest: ${it.value.toReadableString()} (${it.label}). ") }
        lowest?.let { append("Lowest: ${it.value.toReadableString()} (${it.label}).") }
    }
}

/**
 * Generates a human-readable accessibility description for a grouped or stacked bar chart.
 *
 * Example output:
 * `"Grouped bar chart, 3 groups, 2 series each. Range: 10 to 90."`
 *
 * @param data The list of [BarGroup] items displayed by the chart.
 * @param chartTypeName A human-readable name for the chart type (e.g. "grouped bar", "stacked bar").
 * @return A descriptive string suitable for use as a `contentDescription`.
 */
fun generateBarGroupChartDescription(
    data: List<BarGroup>,
    chartTypeName: String = "grouped bar",
): String {
    if (data.isEmpty()) {
        return "Empty $chartTypeName chart."
    }
    val seriesCount = data.getOrNull(0)?.values?.size ?: 0
    val allValues = data.fastFlatMap { it.values }
    val maxVal = allValues.maxOrNull() ?: 0f
    val minVal = allValues.minOrNull() ?: 0f
    return buildString {
        append("${chartTypeName.replaceFirstChar { it.uppercaseChar() }} chart, ${data.size} groups")
        if (seriesCount > 0) {
            append(", $seriesCount series each")
        }
        append(". ")
        append("Range: ${minVal.toReadableString()} to ${maxVal.toReadableString()}.")
    }
}

/**
 * Generates a human-readable accessibility description for a point (scatter) chart.
 *
 * Example output:
 * `"Point chart, 5 data points. Range: 30 to 70. Peak: 70 at Thu. Lowest: 30 at Mon."`
 *
 * @param data The list of [PointData] items displayed by the chart.
 * @param minValue The chart's y-axis minimum value.
 * @param maxValue The chart's y-axis maximum value.
 * @return A descriptive string suitable for use as a `contentDescription`.
 */
fun generatePointChartDescription(
    data: List<PointData>,
    minValue: Float,
    maxValue: Float,
): String {
    if (data.isEmpty()) {
        return "Empty point chart."
    }
    val peak = data.maxByOrNull { it.value }
    val trough = data.minByOrNull { it.value }
    return buildString {
        append("Point chart, ${data.size} data points. ")
        append("Range: ${minValue.toReadableString()} to ${maxValue.toReadableString()}. ")
        peak?.let { append("Peak: ${it.value.toReadableString()} at ${it.label}. ") }
        trough?.let { append("Lowest: ${it.value.toReadableString()} at ${it.label}.") }
    }
}

/**
 * Generates a human-readable accessibility description for a bubble chart.
 *
 * Example output:
 * `"Bubble chart, 3 bubbles. Y-axis range: 50 to 75. Largest bubble: Product B (size 200)."`
 *
 * @param data The list of [BubbleData] items displayed by the chart.
 * @return A descriptive string suitable for use as a `contentDescription`.
 */
fun generateBubbleChartDescription(data: List<BubbleData>): String {
    if (data.isEmpty()) {
        return "Empty bubble chart."
    }
    val minY = data.minOfOrNull { it.yValue } ?: 0f
    val maxY = data.maxOfOrNull { it.yValue } ?: 0f
    val largest = data.maxByOrNull { it.size }
    return buildString {
        append("Bubble chart, ${data.size} bubbles. ")
        append("Y-axis range: ${minY.toReadableString()} to ${maxY.toReadableString()}. ")
        largest?.let { append("Largest bubble: ${it.label} (size ${it.size.toReadableString()}).") }
    }
}

/**
 * Generates a human-readable accessibility description for a multiline or stacked area chart.
 *
 * Example output:
 * `"Multiline chart, 4 data points, 3 series. Range: 5 to 80."`
 *
 * @param data The list of [LineGroup] items displayed by the chart.
 * @param chartTypeName A human-readable name for the chart type (e.g. "multiline", "stacked area").
 * @return A descriptive string suitable for use as a `contentDescription`.
 */
fun generateLineGroupChartDescription(
    data: List<LineGroup>,
    chartTypeName: String = "multiline",
): String {
    if (data.isEmpty()) {
        return "Empty $chartTypeName chart."
    }
    val seriesCount = data.getOrNull(0)?.values?.size ?: 0
    val allValues = data.fastFlatMap { it.values }
    val maxVal = allValues.maxOrNull() ?: 0f
    val minVal = allValues.minOrNull() ?: 0f
    return buildString {
        append("${chartTypeName.replaceFirstChar { it.uppercaseChar() }} chart, ${data.size} data points")
        if (seriesCount > 0) {
            append(", $seriesCount series")
        }
        append(". ")
        append("Range: ${minVal.toReadableString()} to ${maxVal.toReadableString()}.")
    }
}

/**
 * Generates a human-readable accessibility description for a pie or donut chart.
 *
 * Example output:
 * `"Pie chart, 3 slices. Largest slice: Product A (45%). Smallest slice: Product C (25%)."`
 *
 * @param data The list of [PieData] slices displayed by the chart.
 * @param chartTypeName A human-readable name for the chart type (e.g. "Pie", "Donut").
 * @return A descriptive string suitable for use as a `contentDescription`.
 */
fun generatePieChartDescription(
    data: List<PieData>,
    chartTypeName: String = "Pie",
): String {
    if (data.isEmpty()) {
        return "Empty $chartTypeName chart."
    }
    val total = data.sumOf { it.value.toDouble() }.toFloat()
    val largest = data.maxByOrNull { it.value }
    val smallest = data.minByOrNull { it.value }
    return buildString {
        append("$chartTypeName chart, ${data.size} slices. ")
        largest?.let {
            val pct = if (total > 0f) ((it.value / total) * 100).toInt() else 0
            append("Largest slice: ${it.label} ($pct%). ")
        }
        smallest?.let {
            val pct = if (total > 0f) ((it.value / total) * 100).toInt() else 0
            append("Smallest slice: ${it.label} ($pct%).")
        }
    }
}

/**
 * Generates a human-readable accessibility description for a candlestick chart.
 *
 * Example output:
 * `"Candlestick chart, 5 candles. Price range: 95 to 120. Latest close: 112."`
 *
 * @param data The list of [CandleData] items displayed by the chart.
 * @return A descriptive string suitable for use as a `contentDescription`.
 */
fun generateCandlestickChartDescription(data: List<CandleData>): String {
    if (data.isEmpty()) {
        return "Empty candlestick chart."
    }
    val minLow = data.minOfOrNull { it.low } ?: 0f
    val maxHigh = data.maxOfOrNull { it.high } ?: 0f
    val latest = data.lastOrNull()
    return buildString {
        append("Candlestick chart, ${data.size} candles. ")
        append("Price range: ${minLow.toReadableString()} to ${maxHigh.toReadableString()}. ")
        latest?.let { append("Latest close: ${it.close.toReadableString()}.") }
    }
}

/**
 * Generates a human-readable accessibility description for a combo (bar + line) chart.
 *
 * Example output:
 * `"Combo chart, 6 data points. Highest bar: 200 at Jun. Highest line: 180 at Jun."`
 *
 * @param data The list of [ComboChartData] items displayed by the chart.
 * @return A descriptive string suitable for use as a `contentDescription`.
 */
fun generateComboChartDescription(data: List<ComboChartData>): String {
    if (data.isEmpty()) {
        return "Empty combo chart."
    }
    val topBar = data.maxByOrNull { it.barValue }
    val topLine = data.maxByOrNull { it.lineValue }
    return buildString {
        append("Combo chart, ${data.size} data point${if (data.size == 1) "" else "s"}. ")
        topBar?.let { append("Highest bar: ${it.barValue.toReadableString()} at ${it.label}. ") }
        topLine?.let { append("Highest line: ${it.lineValue.toReadableString()} at ${it.label}.") }
    }
}

/**
 * Generates a human-readable accessibility description for a radar chart.
 *
 * Example output (multiple datasets):
 * `"Radar chart, 2 datasets, 5 axes each. Dataset: Player 1. Dataset: Player 2."`
 *
 * Example output (single dataset):
 * `"Radar chart, 1 dataset, 5 axes each. Strongest axis: Speed (80%)."`
 *
 * @param data The list of [RadarDataSet] items displayed by the chart.
 * @param chartTypeName A human-readable name for the chart type (e.g. "Radar", "Multiple radar").
 * @return A descriptive string suitable for use as a `contentDescription`.
 */
fun generateRadarChartDescription(
    data: List<RadarDataSet>,
    chartTypeName: String = "Radar",
): String {
    if (data.isEmpty()) {
        return "Empty $chartTypeName chart."
    }
    val axisCount = data.firstOrNull()?.axes?.size ?: 0
    return buildString {
        append("$chartTypeName chart, ${data.size} dataset${if (data.size == 1) "" else "s"}")
        if (axisCount > 0) {
            append(", $axisCount axes each")
        }
        append(". ")
        if (data.size == 1) {
            val strongest = data.first().axes.maxByOrNull { it.getNormalizedValue() }
            strongest?.let { append("Strongest axis: ${it.label} (${(it.getNormalizedValue() * 100).toInt()}%).") }
        } else {
            data.forEach { append("Dataset: ${it.label}. ") }
        }
    }
}

/**
 * Generates a human-readable accessibility description for a circular progress indicator.
 *
 * Example output:
 * `"Circular progress indicator, 3 rings. Move: 75%. Exercise: 83%. Stand: 100%."`
 *
 * @param data The list of [CircularRingData] rings displayed by the chart.
 * @return A descriptive string suitable for use as a `contentDescription`.
 */
fun generateCircularProgressDescription(data: List<CircularRingData>): String {
    if (data.isEmpty()) {
        return "Empty circular progress indicator."
    }
    return buildString {
        append("Circular progress indicator, ${data.size} ring${if (data.size == 1) "" else "s"}. ")
        data.forEach { ring ->
            append("${ring.label}: ${ring.calculatePercentage().toInt()}%. ")
        }
    }.trimEnd()
}

/**
 * Generates a human-readable accessibility description for a block bar chart.
 *
 * Example output:
 * `"Block bar chart, 4 segments. Largest segment: 45% of total."`
 *
 * @param data The list of [BlockData] segments displayed by the chart.
 * @return A descriptive string suitable for use as a `contentDescription`.
 */
fun generateBlockBarDescription(data: List<BlockData>): String {
    if (data.isEmpty()) {
        return "Empty block bar chart."
    }
    val total = data.sumOf { it.value.toDouble() }.toFloat()
    val largest = data.maxByOrNull { it.value }
    return buildString {
        append("Block bar chart, ${data.size} segment${if (data.size == 1) "" else "s"}. ")
        largest?.let {
            val pct = if (total > 0f) ((it.value / total) * 100).toInt() else 0
            append("Largest segment: $pct% of total.")
        }
    }
}

/**
 * Builds a screen-reader description for a calendar heatmap: the number of days with data and the
 * busiest day (by value).
 *
 * @param data The calendar entries shown in the heatmap.
 * @return A localized-English sentence describing the heatmap.
 */
fun generateCalendarHeatmapDescription(data: List<CalendarData>): String {
    if (data.isEmpty()) {
        return "Empty calendar heatmap."
    }
    val busiest = data.maxByOrNull { it.value }
    return buildString {
        append("Calendar heatmap, ${data.size} day${if (data.size == 1) "" else "s"} with data. ")
        busiest?.let {
            append("Busiest day: ${it.year}-${it.month}-${it.day} at ${it.value.toReadableString()}.")
        }
    }
}

private fun Float.toReadableString(): String {
    val long = toLong()
    return if (this == long.toFloat()) long.toString() else toString()
}
