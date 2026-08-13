package com.himanshoe.charty3d.projection

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import com.himanshoe.charty.common.annotation.ChartyExperimental

private const val TOP_FACE_LIGHTNESS = 1.18f
private const val SIDE_FACE_LIGHTNESS = 0.82f
private const val FRONT_FACE_LIGHTNESS = 1f
private const val CHANNEL_CEILING = 1f

/**
 * Which side of a solid a face belongs to. The three visible sides of an upright box take different
 * amounts of light, and naming them lets a chart shade consistently instead of picking a tint per
 * call site.
 */
@ChartyExperimental
enum class FaceSide {
    /** Faces the viewer; drawn at the series colour unchanged. */
    FRONT,

    /** Faces upward; lightened, as though lit from above. */
    TOP,

    /** Faces left or right; darkened, so the solid reads as having a corner. */
    SIDE,
}

/**
 * One flat polygon of a projected solid, already flattened to canvas coordinates.
 *
 * @property points The face's corners in canvas space, in winding order.
 * @property side Which side of the solid this is, deciding how it is shaded.
 * @property depth The face's distance from the viewer, used to draw far faces before near ones.
 * @property payload The chart's own item this face belongs to, so a tap can resolve back to data.
 */
@ChartyExperimental
@Immutable
data class ProjectedFace<T>(
    val points: List<Offset>,
    val side: FaceSide,
    val depth: Float,
    val payload: T,
) {
    /** The face as a [Path] ready to fill. */
    fun toPath(): Path =
        Path().apply {
            points.forEachIndexed { index, point ->
                if (index == 0) {
                    moveTo(point.x, point.y)
                } else {
                    lineTo(point.x, point.y)
                }
            }
            close()
        }

    /** Whether [position] falls inside this face, by the even-odd crossing rule. */
    fun contains(position: Offset): Boolean {
        if (points.size < MIN_POLYGON_POINTS) {
            return false
        }
        var inside = false
        var j = points.lastIndex
        for (i in points.indices) {
            val a = points[i]
            val b = points[j]
            val straddles = (a.y > position.y) != (b.y > position.y)
            if (straddles) {
                val crossingX = (b.x - a.x) * (position.y - a.y) / (b.y - a.y) + a.x
                if (position.x < crossingX) {
                    inside = !inside
                }
            }
            j = i
        }
        return inside
    }

    private companion object {
        const val MIN_POLYGON_POINTS = 3
    }
}

/**
 * Shades [color] for the given [side], so the three visible sides of one solid read as one object
 * lit from above rather than three flat shapes that happen to touch.
 */
@ChartyExperimental
fun shadeForSide(
    color: Color,
    side: FaceSide,
): Color {
    val factor =
        when (side) {
            FaceSide.FRONT -> FRONT_FACE_LIGHTNESS
            FaceSide.TOP -> TOP_FACE_LIGHTNESS
            FaceSide.SIDE -> SIDE_FACE_LIGHTNESS
        }
    return Color(
        red = (color.red * factor).coerceIn(0f, CHANNEL_CEILING),
        green = (color.green * factor).coerceIn(0f, CHANNEL_CEILING),
        blue = (color.blue * factor).coerceIn(0f, CHANNEL_CEILING),
        alpha = color.alpha,
    )
}

/**
 * Orders [faces] so the ones furthest from the viewer are drawn first — the painter's algorithm.
 *
 * A projected chart has no depth buffer, so nearer solids can only hide further ones by being
 * painted over them. Sorting here rather than at each call site is what keeps a chart's occlusion
 * correct when its viewing angle changes.
 */
@ChartyExperimental
fun <T> List<ProjectedFace<T>>.sortedFarToNear(): List<ProjectedFace<T>> = sortedByDescending { face -> face.depth }

/**
 * Finds the item drawn at [position], searching **near faces first** so a tap resolves to the solid
 * you can actually see rather than one hidden behind it.
 *
 * @receiver Faces in any order; this walks them from nearest to furthest itself.
 * @param position The tapped canvas position.
 * @return The payload of the nearest face under [position], or `null` when the tap missed every face.
 */
@ChartyExperimental
fun <T> List<ProjectedFace<T>>.hitTest(position: Offset): T? =
    sortedBy { face -> face.depth }
        .firstOrNull { face -> face.contains(position) }
        ?.payload
