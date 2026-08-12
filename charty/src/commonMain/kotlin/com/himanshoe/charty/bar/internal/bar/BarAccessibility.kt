package com.himanshoe.charty.bar.internal.bar

import com.himanshoe.charty.common.accessibility.ChartAccessibility
import com.himanshoe.charty.common.accessibility.buildDataPointDescriptions

/**
 * Builds the accessibility payload shared by the bar-family charts: a chart-level description and one
 * traversable description per drawn data point.
 *
 * @param description Caller-supplied chart description; `null` falls back to [fallbackDescription].
 * @param labels The category label of each drawn item.
 * @param values The value of each drawn item.
 * @param fallbackDescription Description used when [description] is `null`.
 * @return The assembled [ChartAccessibility].
 */
internal fun barAccessibility(
    description: String?,
    labels: List<String>,
    values: List<Float>,
    fallbackDescription: String,
): ChartAccessibility =
    ChartAccessibility(
        contentDescription = description ?: fallbackDescription,
        dataPointDescriptions = buildDataPointDescriptions(labels = labels, values = values),
    )
