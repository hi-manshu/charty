package com.himanshoe.charty3d

import androidx.compose.ui.geometry.Offset
import com.himanshoe.charty3d.projection.Point3D
import com.himanshoe.charty3d.projection.Projection3D
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

private const val TOLERANCE = 0.001f
private val origin = Offset(x = 100f, y = 200f)

/** Far beyond any sane projection of a scene this size; anything past it is a runaway divide. */
private const val RUNAWAY_LIMIT = 1000f

class ProjectionTest {
    @Test
    fun headOnProjectionLeavesAPointWhereItStarted() {
        val flat = Projection3D(pitch = 0f, yaw = 0f, perspective = 0f)
        val projected = flat.project(point = Point3D(x = 10f, y = -20f, z = 0f), origin = origin)
        assertEquals(110f, projected.x, TOLERANCE)
        assertEquals(180f, projected.y, TOLERANCE)
    }

    @Test
    fun depthShiftsAPointWithoutScalingItWhenPerspectiveIsOff() {
        val parallel = Projection3D(pitch = 20f, yaw = 20f, perspective = 0f)
        val near = parallel.project(point = Point3D(x = 0f, y = 0f, z = 0f), origin = origin)
        val far = parallel.project(point = Point3D(x = 0f, y = 0f, z = 50f), origin = origin)
        assertTrue(abs(far.x - near.x) > TOLERANCE || abs(far.y - near.y) > TOLERANCE)
    }

    @Test
    fun parallelProjectionKeepsEqualLengthsEqualWhereverTheySit() {
        val parallel = Projection3D(pitch = 25f, yaw = 30f, perspective = 0f)

        fun barLength(z: Float): Float {
            val foot = parallel.project(point = Point3D(x = 0f, y = 0f, z = z), origin = origin)
            val head = parallel.project(point = Point3D(x = 0f, y = -60f, z = z), origin = origin)
            return abs(head.y - foot.y)
        }
        assertEquals(barLength(z = 0f), barLength(z = 120f), TOLERANCE)
    }

    @Test
    fun perspectiveMakesTheSameBarShorterFurtherBack() {
        val convergent = Projection3D(pitch = 25f, yaw = 0f, depth = 100f, perspective = 0.8f)

        fun barLength(z: Float): Float {
            val foot = convergent.project(point = Point3D(x = 0f, y = 0f, z = z), origin = origin)
            val head = convergent.project(point = Point3D(x = 0f, y = -60f, z = z), origin = origin)
            return abs(head.y - foot.y)
        }
        assertTrue(
            barLength(z = 150f) < barLength(z = 0f),
            "perspective must foreshorten the further bar; that is the readability cost it buys",
        )
    }

    @Test
    fun viewSpaceOrdersPointsByDistanceFromTheViewer() {
        val tilted = Projection3D(pitch = 20f, yaw = 15f)
        val near = tilted.toViewSpace(Point3D(x = 0f, y = 0f, z = 0f))
        val far = tilted.toViewSpace(Point3D(x = 0f, y = 0f, z = 80f))
        assertTrue(far.z > near.z, "a point further along z must sit further from the viewer")
    }

    @Test
    fun zeroDepthSceneDoesNotDivideByZeroUnderPerspective() {
        val degenerate = Projection3D(pitch = 20f, yaw = 20f, depth = 0f, perspective = 1f)
        val projected = degenerate.project(point = Point3D(x = 5f, y = 5f, z = 5f), origin = origin)
        assertTrue(projected.x.isFinite() && projected.y.isFinite())
    }

    @Test
    fun negativeDepthIsRejected() {
        assertFailsWith<IllegalArgumentException> { Projection3D(depth = -1f) }
    }

    @Test
    fun perspectiveOutsideZeroToOneIsRejected() {
        assertFailsWith<IllegalArgumentException> { Projection3D(perspective = 1.5f) }
    }

    @Test
    fun everyPresetKeepsASceneSizedPointNearTheOrigin() {
        val presets =
            listOf(
                Projection3D.Default,
                Projection3D.Isometric,
                Projection3D.Subtle,
                Projection3D.Dramatic,
            )
        presets.forEach { projection ->
            val far = projection.project(point = Point3D(x = 100f, y = -62f, z = 45f), origin = origin)
            assertTrue(
                abs(far.x - origin.x) < RUNAWAY_LIMIT && abs(far.y - origin.y) < RUNAWAY_LIMIT,
                "a scene-sized point must not fly off under $projection; that is the spike a bad " +
                    "perspective divide produces",
            )
        }
    }

    @Test
    fun aPointBehindTheEyeIsStretchedRatherThanSentToInfinity() {
        val convergent = Projection3D(pitch = 30f, yaw = 30f, depth = 0.1f, perspective = 1f)
        val behind = convergent.project(point = Point3D(x = 40f, y = -40f, z = 4000f), origin = origin)
        assertTrue(behind.x.isFinite() && behind.y.isFinite())
        assertTrue(abs(behind.x - origin.x) < RUNAWAY_LIMIT && abs(behind.y - origin.y) < RUNAWAY_LIMIT)
    }

    @Test
    fun isometricPresetCarriesNoPerspectiveSoValuesStayComparable() {
        assertEquals(0f, Projection3D.Isometric.perspective, TOLERANCE)
    }

    @Test
    fun defaultPresetCarriesNoPerspectiveEither() {
        assertEquals(0f, Projection3D.Default.perspective, TOLERANCE)
    }
}
