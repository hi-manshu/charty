package com.himanshoe.charty3d.scatter.data

import androidx.compose.runtime.Immutable
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.annotation.ChartyExperimental

/**
 * One observation in a three-dimensional scatter: three measured values and an optional label.
 *
 * This is the one place in `charty-3d` where depth means something. Everywhere else the third
 * dimension is decoration — bars extrude, discs thicken — and the honest advice is that a flat chart
 * reads better. Here [z] is a variable in its own right, so the projection is carrying data rather
 * than costing you accuracy for the look of it.
 *
 * @property x Position along the horizontal axis.
 * @property y Position along the vertical axis.
 * @property z Position along the depth axis, running away from the viewer.
 * @property label Name for this observation, shown when it is tapped and read by screen readers.
 * @property color Fill for this point; `null` takes the chart's colour.
 */
@ChartyExperimental
@Immutable
data class Scatter3DPoint(
    val x: Float,
    val y: Float,
    val z: Float,
    val label: String = "",
    val color: ChartyColor? = null,
)
