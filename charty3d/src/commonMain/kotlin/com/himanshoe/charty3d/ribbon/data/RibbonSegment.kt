package com.himanshoe.charty3d.ribbon.data

import androidx.compose.runtime.Immutable
import com.himanshoe.charty.common.annotation.ChartyExperimental

/**
 * What a tap on a ribbon resolved to: which series, which point along it, and the value there.
 *
 * The series is reported by both index and name because the index is what decides its depth slot —
 * and so what hides what — while the name is what a reader recognises.
 *
 * @property seriesIndex Position of the series in the list passed to the chart, front to back.
 * @property seriesName The series' own label.
 * @property pointIndex Index of the segment's near end along the shared x axis.
 * @property value The value at that point.
 */
@ChartyExperimental
@Immutable
data class RibbonSegment(
    val seriesIndex: Int,
    val seriesName: String,
    val pointIndex: Int,
    val value: Float,
)
