package com.himanshoe.charty3d.projection

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import com.himanshoe.charty.common.annotation.ChartyExperimental
import kotlin.math.cos
import kotlin.math.sin

private const val DEGREES_TO_RADIANS = 0.017453292f

/**
 * The scene extent a [Projection3D.depth] of `1f` stands for. Depth is given as a fraction so a
 * projection reads the same whatever size the chart lays its scene out at; this is what turns that
 * fraction back into the distance the perspective divide needs.
 */
private const val PERSPECTIVE_REFERENCE_EXTENT = 140f

/**
 * The closest the perspective divide is allowed to get to zero. A point at or behind the eye has no
 * honest projection, and letting the divide run away turns one corner of a solid into a spike that
 * shoots off the canvas. Clamping stretches such a point instead, which is wrong but bounded.
 */
private const val MIN_PERSPECTIVE_DENOMINATOR = 0.25f

/**
 * A point in the chart's own three-dimensional space, before any rotation or projection.
 *
 * The axes follow the screen's convention rather than a mathematician's, so that reading the code
 * next to a 2D chart's does not require flipping signs in your head: [x] runs right, [y] runs **down**
 * (matching Compose's canvas), and [z] runs away from the viewer into the scene.
 */
@ChartyExperimental
@Immutable
data class Point3D(
    val x: Float,
    val y: Float,
    val z: Float,
)

/**
 * How a three-dimensional scene is flattened onto the canvas.
 *
 * @property pitch Rotation about the horizontal axis, in degrees; positive tips the far edge of the
 *   scene upward, which is the view that reads as "looking down at" the chart.
 * @property yaw Rotation about the vertical axis, in degrees; positive swings the right-hand side of
 *   the scene toward the viewer.
 * @property depth How far back the scene extends, as a fraction of the plot's own extent. It is the
 *   reference distance the perspective divide measures against, so a smaller depth makes the same
 *   [perspective] converge harder. It has no effect at all when [perspective] is `0f`.
 * @property perspective Strength of the perspective foreshortening, from `0f` for a purely
 *   isometric projection — every equal length stays equal, which keeps bars comparable — up to `1f`
 *   for a strongly convergent one, which looks more photographic but distorts value comparison.
 */
@ChartyExperimental
@Immutable
data class Projection3D(
    val pitch: Float = DEFAULT_PITCH,
    val yaw: Float = DEFAULT_YAW,
    val depth: Float = DEFAULT_DEPTH,
    val perspective: Float = DEFAULT_PERSPECTIVE,
) {
    init {
        require(depth >= 0f) { "depth must be non-negative, got: $depth" }
        require(perspective in 0f..1f) { "perspective must be between 0 and 1, got: $perspective" }
    }

    private val pitchCos = cos(pitch * DEGREES_TO_RADIANS)
    private val pitchSin = sin(pitch * DEGREES_TO_RADIANS)
    private val yawCos = cos(yaw * DEGREES_TO_RADIANS)
    private val yawSin = sin(yaw * DEGREES_TO_RADIANS)

    /**
     * Rotates [point] by [yaw] then [pitch], leaving it in view space where `z` is distance from the
     * viewer. Callers that need to sort by depth want this rather than [project], because projecting
     * discards `z`.
     */
    fun toViewSpace(point: Point3D): Point3D {
        val yawedX = point.x * yawCos + point.z * yawSin
        val yawedZ = -point.x * yawSin + point.z * yawCos
        val pitchedY = point.y * pitchCos - yawedZ * pitchSin
        val pitchedZ = point.y * pitchSin + yawedZ * pitchCos
        return Point3D(x = yawedX, y = pitchedY, z = pitchedZ)
    }

    /**
     * Flattens [point] onto the canvas, relative to an [origin] that the scene's own `(0, 0, 0)`
     * lands on.
     *
     * With [perspective] at `0f` this is a parallel projection: the `z` coordinate only shifts a
     * point, never scales it. Above `0f`, points further from the viewer are pulled toward [origin]
     * in proportion to their depth, which is what makes the far edge of a scene converge.
     *
     * @param point The point to flatten, in the chart's own space.
     * @param origin The canvas position the scene's origin sits at.
     * @return The canvas position [point] draws at.
     */
    fun project(
        point: Point3D,
        origin: Offset,
    ): Offset {
        val view = toViewSpace(point)
        if (perspective == 0f) {
            return Offset(x = origin.x + view.x, y = origin.y + view.y)
        }
        val reference = depthOrOne() * PERSPECTIVE_REFERENCE_EXTENT
        val denominator = (1f + perspective * view.z / reference).coerceAtLeast(MIN_PERSPECTIVE_DENOMINATOR)
        return Offset(x = origin.x + view.x / denominator, y = origin.y + view.y / denominator)
    }

    /** The depth fraction, guarding a zero-depth scene against dividing by zero. */
    private fun depthOrOne(): Float =
        if (depth <= 0f) {
            1f
        } else {
            depth
        }

    /**
     * Preset viewing angles, so a caller who only wants "a 3D chart" does not have to invent one.
     */
    companion object {
        /** A gentle tilt that keeps bars comparable; the default. */
        val Default = Projection3D()

        /** A true isometric view: no perspective at all, so equal lengths stay equal everywhere. */
        val Isometric = Projection3D(pitch = 30f, yaw = 45f, perspective = 0f)

        /** A shallow, nearly head-on view, for when depth should read as a hint rather than a subject. */
        val Subtle = Projection3D(pitch = 8f, yaw = 8f, depth = 0.25f, perspective = 0f)

        /** A pronounced, photographic view. Values are hardest to compare in this one. */
        val Dramatic = Projection3D(pitch = 34f, yaw = 28f, depth = 0.7f, perspective = 0.6f)
    }
}

private const val DEFAULT_PITCH = 18f
private const val DEFAULT_YAW = 14f
private const val DEFAULT_DEPTH = 0.45f
private const val DEFAULT_PERSPECTIVE = 0f
