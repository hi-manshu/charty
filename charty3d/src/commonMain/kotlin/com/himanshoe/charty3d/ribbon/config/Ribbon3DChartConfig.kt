package com.himanshoe.charty3d.ribbon.config

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.annotation.ChartyExperimental
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty3d.projection.Projection3D

private const val DEFAULT_RIBBON_WIDTH = 0.55f
private const val LABEL_SP = 10f

/**
 * How a [com.himanshoe.charty3d.ribbon.Ribbon3DChart] draws its series.
 *
 * @property projection The viewing angle the series are flattened by.
 * @property ribbonWidthFraction How much of its depth slot each ribbon fills. Below `1f` the series
 *   are separated by a visible gap, which is the whole point of the chart.
 * @property fillUnderRibbon Whether the area below each ribbon is filled down to the floor, making
 *   each series a solid wall rather than a floating band.
 * @property animation How the series rise in. See [Animation].
 * @property showSeriesLabels Whether each series' name is printed at its near end.
 * @property labelStyle Text style for those names.
 * @property plotBackground Fill painted behind the whole scene; `null` leaves the chart transparent.
 */
@ChartyExperimental
@Stable
data class Ribbon3DChartConfig(
    val projection: Projection3D = Projection3D.Default,
    val ribbonWidthFraction: Float = DEFAULT_RIBBON_WIDTH,
    val fillUnderRibbon: Boolean = true,
    val animation: Animation = Animation.Default,
    val showSeriesLabels: Boolean = true,
    val labelStyle: TextStyle = TextStyle(fontSize = LABEL_SP.sp),
    val plotBackground: ChartyColor? = null,
) {
    init {
        require(ribbonWidthFraction in 0f..1f) {
            "ribbonWidthFraction must be between 0 and 1, got: $ribbonWidthFraction"
        }
    }
}
