package com.himanshoe.charty3d.scatter.config

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.annotation.ChartyExperimental
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty3d.projection.Projection3D

private const val DEFAULT_POINT_RADIUS = 7f
private const val DEFAULT_NEAR_SCALE = 1.25f
private const val DEFAULT_FAR_SCALE = 0.7f
private const val LABEL_SP = 10f

/**
 * How a [com.himanshoe.charty3d.scatter.Scatter3DChart] draws its points.
 *
 * @property projection The viewing angle the cloud is flattened by. A scatter needs some yaw and
 *   pitch to separate the depth axis from the other two; a head-on view collapses z entirely.
 * @property pointRadius Radius of a point at mid-depth, in pixels before the depth scaling below.
 * @property nearScale Size multiplier applied to the nearest point. Together with [farScale] this is
 *   what makes depth legible: without it, a projected cloud is a flat spray with no near or far.
 * @property farScale Size multiplier applied to the furthest point.
 * @property showBox Whether the wireframe box bounding the data is drawn. It gives the eye a frame
 *   to read positions against, without which a projected point cloud is close to unreadable.
 * @property boxColor Colour of the wireframe box.
 * @property boxStrokeWidth Stroke width of the wireframe box, in pixels.
 * @property showDropLines Whether a line drops from each point to the floor of the box, which fixes
 *   its depth for the eye far better than its size alone.
 * @property sphereShading Whether each point is shaded as a lit sphere rather than filled flat. A
 *   flat disc reads as a hole punched in the page; the same highlight and falloff the solid charts
 *   use on their faces is what makes it read as a ball sitting in the box.
 * @property showFloorShadows Whether each point casts a shadow on the floor of the box. The shadow
 *   is squashed by the viewing pitch, the way a real one would be, and it does more to place a point
 *   in space than either the drop line or the size does.
 * @property floorShadowAlpha Opacity of those shadows at their darkest.
 * @property dropLineAlpha Opacity of the drop lines.
 * @property animation How points fade and rise in. See [Animation].
 * @property showLabels Whether each point's label is drawn beside it.
 * @property labelStyle Text style for the point labels.
 * @property plotBackground Fill painted behind the whole scene; `null` leaves the chart transparent.
 */
@ChartyExperimental
@Stable
data class Scatter3DChartConfig(
    val projection: Projection3D = Projection3D.Default,
    val pointRadius: Float = DEFAULT_POINT_RADIUS,
    val nearScale: Float = DEFAULT_NEAR_SCALE,
    val farScale: Float = DEFAULT_FAR_SCALE,
    val showBox: Boolean = true,
    val boxColor: ChartyColor? = null,
    val boxStrokeWidth: Float = 1f,
    val showDropLines: Boolean = true,
    val dropLineAlpha: Float = 0.25f,
    val sphereShading: Boolean = true,
    val showFloorShadows: Boolean = true,
    val floorShadowAlpha: Float = 0.18f,
    val animation: Animation = Animation.Default,
    val showLabels: Boolean = false,
    val labelStyle: TextStyle = TextStyle(fontSize = LABEL_SP.sp),
    val plotBackground: ChartyColor? = null,
) {
    init {
        require(pointRadius > 0f) { "pointRadius must be positive, got: $pointRadius" }
        require(nearScale > 0f) { "nearScale must be positive, got: $nearScale" }
        require(farScale > 0f) { "farScale must be positive, got: $farScale" }
        require(boxStrokeWidth >= 0f) { "boxStrokeWidth must be non-negative, got: $boxStrokeWidth" }
        require(dropLineAlpha in 0f..1f) { "dropLineAlpha must be between 0 and 1, got: $dropLineAlpha" }
        require(floorShadowAlpha in 0f..1f) { "floorShadowAlpha must be between 0 and 1, got: $floorShadowAlpha" }
    }
}
