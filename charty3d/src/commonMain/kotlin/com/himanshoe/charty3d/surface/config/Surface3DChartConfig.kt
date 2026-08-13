package com.himanshoe.charty3d.surface.config

import androidx.compose.runtime.Stable
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.annotation.ChartyExperimental
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty3d.projection.Projection3D

private const val DEFAULT_WIREFRAME_ALPHA = 0.18f

/**
 * How the surface is shaded across its height range.
 */
@ChartyExperimental
enum class SurfaceShading {
    /** Flat fill from the height ramp alone. */
    HEIGHT,

    /** The height ramp, darkened on faces that tilt away from the light. */
    HEIGHT_AND_SLOPE,
}

/**
 * How a [com.himanshoe.charty3d.surface.Surface3DChart] draws its mesh.
 *
 * @property projection The viewing angle the mesh is flattened by.
 * @property lowColor Fill at the lowest point of the surface.
 * @property highColor Fill at the highest. Heights between are interpolated.
 * @property shading Whether faces are also shaded by which way they tilt. Slope shading is what
 *   makes a surface read as a landscape rather than a coloured sheet — two hills of the same height
 *   look identical without it.
 * @property showWireframe Whether the grid lines of the mesh are drawn over the fill.
 * @property wireframeAlpha Opacity of those grid lines.
 * @property animation How the surface rises in. See [Animation].
 * @property plotBackground Fill painted behind the whole scene; `null` leaves the chart transparent.
 */
@ChartyExperimental
@Stable
data class Surface3DChartConfig(
    val projection: Projection3D = Projection3D.Isometric,
    val lowColor: ChartyColor? = null,
    val highColor: ChartyColor? = null,
    val shading: SurfaceShading = SurfaceShading.HEIGHT_AND_SLOPE,
    val showWireframe: Boolean = true,
    val wireframeAlpha: Float = DEFAULT_WIREFRAME_ALPHA,
    val animation: Animation = Animation.Default,
    val plotBackground: ChartyColor? = null,
) {
    init {
        require(wireframeAlpha in 0f..1f) { "wireframeAlpha must be between 0 and 1, got: $wireframeAlpha" }
    }
}
