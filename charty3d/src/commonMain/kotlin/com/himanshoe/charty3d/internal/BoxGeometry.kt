package com.himanshoe.charty3d.internal

import androidx.compose.ui.geometry.Offset
import com.himanshoe.charty3d.projection.FaceSide
import com.himanshoe.charty3d.projection.Point3D
import com.himanshoe.charty3d.projection.ProjectedFace
import com.himanshoe.charty3d.projection.Projection3D

private const val FACE_CORNERS = 4

/**
 * The faces of an upright box that actually face the viewer, projected and ready to draw.
 *
 * A box has six faces and at most three can be seen at once, but *which* three depends entirely on
 * the viewing angle: swing the yaw negative and the left flank becomes visible instead of the right,
 * tip the pitch below the horizon and you see the underside instead of the top. Deciding that by
 * looking at the geometry — keeping only the faces whose outward normal turns toward the viewer —
 * is what keeps a box looking solid at every angle. Hard-coding one set of three works only for the
 * quadrant it was written for, and comes apart everywhere else.
 *
 * @param projection The viewing angle to flatten by.
 * @param origin The canvas position the scene's origin sits at.
 * @param nearLeft The box's near-left-bottom corner in scene space.
 * @param width The box's extent along x.
 * @param height The box's extent along y; negative values rise above [nearLeft], matching the canvas
 *   convention where y runs down.
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
        val view = corners.map { point -> projection.toViewSpace(point) }
        if (!facesViewer(view)) {
            return
        }
        faces.add(
            ProjectedFace(
                points = corners.map { point -> projection.project(point = point, origin = origin) },
                side = side,
                depth = view.sumOf { point -> point.z.toDouble() }.toFloat() / FACE_CORNERS,
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
        side = FaceSide.FRONT,
        corners =
            listOf(
                corner(dx = width, dy = 0f, dz = depth),
                corner(dx = 0f, dy = 0f, dz = depth),
                corner(dx = 0f, dy = height, dz = depth),
                corner(dx = width, dy = height, dz = depth),
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
        side = FaceSide.TOP,
        corners =
            listOf(
                corner(dx = 0f, dy = 0f, dz = depth),
                corner(dx = width, dy = 0f, dz = depth),
                corner(dx = width, dy = 0f, dz = 0f),
                corner(dx = 0f, dy = 0f, dz = 0f),
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
    face(
        side = FaceSide.SIDE,
        corners =
            listOf(
                corner(dx = 0f, dy = 0f, dz = depth),
                corner(dx = 0f, dy = 0f, dz = 0f),
                corner(dx = 0f, dy = height, dz = 0f),
                corner(dx = 0f, dy = height, dz = depth),
            ),
    )
    return faces
}

/**
 * Whether a face turns toward the viewer, given its corners in view space.
 *
 * Every face here is wound counter-clockwise as seen from outside the box, so the sign of the
 * projected polygon's signed area says which way it now faces: the winding survives rotation but
 * reverses the moment a face turns away. Because the canvas runs y downward, a front-facing polygon
 * comes out negative.
 *
 * A face with no area — what a zero-depth box produces, and what any face seen exactly edge-on
 * becomes — is treated as hidden rather than drawn as a sliver.
 */
private fun facesViewer(view: List<Point3D>): Boolean {
    if (view.size < FACE_CORNERS) {
        return false
    }
    var signedArea = 0f
    for (i in view.indices) {
        val current = view[i]
        val next = view[(i + 1) % view.size]
        signedArea += current.x * next.y - next.x * current.y
    }
    return signedArea < 0f
}
