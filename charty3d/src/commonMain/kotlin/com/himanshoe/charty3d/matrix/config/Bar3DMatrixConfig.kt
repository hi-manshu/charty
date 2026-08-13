package com.himanshoe.charty3d.matrix.config

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.annotation.ChartyExperimental
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty3d.projection.Projection3D

private const val DEFAULT_BAR_FOOTPRINT = 0.7f
private const val LABEL_SP = 10f

/**
 * How a [com.himanshoe.charty3d.matrix.Bar3DMatrixChart] draws its bars.
 *
 * @property projection The viewing angle the grid is flattened by.
 * @property barFootprintFraction How much of its cell each bar occupies across both floor axes.
 * @property animation How bars grow in. See [Animation].
 * @property lowColor Fill for the smallest value in the grid.
 * @property highColor Fill for the largest. Values between are interpolated, so height and colour
 *   carry the same variable and reinforce each other rather than competing.
 * @property showAxisLabels Whether the row and column names are printed along the floor edges.
 * @property labelStyle Text style for those names.
 * @property plotBackground Fill painted behind the whole scene; `null` leaves the chart transparent.
 */
@ChartyExperimental
@Stable
data class Bar3DMatrixConfig(
    val projection: Projection3D = Projection3D.Isometric,
    val barFootprintFraction: Float = DEFAULT_BAR_FOOTPRINT,
    val animation: Animation = Animation.Default,
    val lowColor: ChartyColor? = null,
    val highColor: ChartyColor? = null,
    val showAxisLabels: Boolean = true,
    val labelStyle: TextStyle = TextStyle(fontSize = LABEL_SP.sp),
    val plotBackground: ChartyColor? = null,
) {
    init {
        require(barFootprintFraction in 0f..1f) {
            "barFootprintFraction must be between 0 and 1, got: $barFootprintFraction"
        }
    }
}
