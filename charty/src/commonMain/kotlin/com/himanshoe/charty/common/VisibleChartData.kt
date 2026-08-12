package com.himanshoe.charty.common

import androidx.compose.runtime.Immutable

/**
 * The exact points a chart should draw, plus the optional [StreamingLayout] that animates them. When
 * [streaming] is non-null the chart is in rolling-window streaming mode: [data] is a window slice and
 * the layout slides it; when `null` the chart draws [data] statically.
 */
@Immutable
internal class VisibleChartData<T>(
    val data: List<T>,
    val streaming: StreamingLayout?,
)
