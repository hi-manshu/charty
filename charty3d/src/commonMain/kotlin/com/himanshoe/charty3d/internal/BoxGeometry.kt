package com.himanshoe.charty3d.internal

import androidx.compose.ui.geometry.Offset
import com.himanshoe.charty3d.projection.FaceSide
import com.himanshoe.charty3d.projection.Point3D
import com.himanshoe.charty3d.projection.ProjectedFace
import com.himanshoe.charty3d.projection.Projection3D

private const val FACE_CORNERS = 4

/**
 * The three faces of an upright box that can ever face the viewer, projected and ready to draw.
 *
 * A box has six faces, but from any single viewing angle at most three are visible — the back,
 * bottom, and one side always point away. Building only the visible three halves the work and, more
 * importantly, keeps the painter's algorithm honest: a face that should be hidden is never drawn at
 * all rather than relying on being painted over.
 *
 * @param projection The viewing angle to flatten by.
 * @param origin The canvas position the scene's origin sits at.
 * @param nearLeft The box's near-left-bottom corner in scene space.
 * @param width The box's extent along x.
 * @param height The box's extent along y; negative values rise above [nearLeft], matching the
 *   canvas convention where y runs down.
 * @param depth The box's extent along z, away from the viewer.
 * @param payload The chart item this box represents, carried onto every face for hit-testing.
 */
internal fun <T> boxFaces(
    projection: Projection3D,
    origin: Offset,
    nearLeft: Point3D,
    width: Float,
    height: Float,
    depth: Float,
    payload: T,
): List<ProjectedFace<T>> {
    fun corner(
        dx: Float,
        dy: Float,
        dz: Float,
    ) = Point3D(x = nearLeft.x + dx, y = nearLeft.y + dy, z = nearLeft.z + dz)

    val faces = mutableListOf<ProjectedFace<T>>()

    fun face(
        side: FaceSide,
        corners: List<Point3D>,
    ) {
        val projected = corners.map { point -> projection.project(point = point, origin = origin) }
        val meanDepth = corners.sumOf { point -> projection.toViewSpace(point).z.toDouble() } / FACE_CORNERS
        faces.add(
            ProjectedFace(
                points = projected,
                side = side,
                depth = meanDepth.toFloat(),
                payload = payload,
            ),
        )
    }

    face(
        side = FaceSide.FRONT,
        corners =
            listOf(
                corner(dx = 0f, dy = 0f, dz = 0f),
                corner(dx = width, dy = 0f, dz = 0f),
                corner(dx = width, dy = height, dz = 0f),
                corner(dx = 0f, dy = height, dz = 0f),
            ),
    )
    face(
        side = FaceSide.TOP,
        corners =
            listOf(
                corner(dx = 0f, dy = height, dz = 0f),
                corner(dx = width, dy = height, dz = 0f),
                corner(dx = width, dy = height, dz = depth),
                corner(dx = 0f, dy = height, dz = depth),
            ),
    )
    face(
        side = FaceSide.SIDE,
        corners =
            listOf(
                corner(dx = width, dy = 0f, dz = 0f),
                corner(dx = width, dy = 0f, dz = depth),
                corner(dx = width, dy = height, dz = depth),
                corner(dx = width, dy = height, dz = 0f),
            ),
    )
    return faces
}
