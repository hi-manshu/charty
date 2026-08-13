package com.himanshoe.charty3d

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty3d.bar.config.Bar3DChartConfig
import com.himanshoe.charty3d.bar.config.toBar3DLabel
import com.himanshoe.charty3d.internal.bar3DFaces
import com.himanshoe.charty3d.projection.FaceSide
import com.himanshoe.charty3d.projection.ProjectedFace
import com.himanshoe.charty3d.projection.Projection3D
import com.himanshoe.charty3d.projection.hitTest
import com.himanshoe.charty3d.projection.shadeForSide
import com.himanshoe.charty3d.projection.sortedFarToNear
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val TOLERANCE = 0.001f

/** One step of Compose's 8-bit-per-channel colour storage, which `0.5f` cannot round-trip exactly. */
private const val CHANNEL_TOLERANCE = 1f / 255f
private val canvas = Size(width = 400f, height = 300f)
private val bars =
    listOf(
        BarData(label = "a", value = 50f),
        BarData(label = "b", value = 100f),
        BarData(label = "c", value = 25f),
    )

private fun facesFor(
    config: Bar3DChartConfig = Bar3DChartConfig(animation = Animation.Disabled),
    progress: Float = 1f,
) = bar3DFaces(size = canvas, dataList = bars, maxValue = 100f, config = config, progress = progress)

class Bar3DGeometryTest {
    @Test
    fun everyBarContributesItsThreeVisibleFaces() {
        assertEquals(bars.size * 3, facesFor().size)
    }

    @Test
    fun aBarBuildsOneFaceOfEachSide() {
        val sides = facesFor().take(3).map { face -> face.side }.toSet()
        assertEquals(setOf(FaceSide.FRONT, FaceSide.TOP, FaceSide.SIDE), sides)
    }

    @Test
    fun everyFaceCarriesItsOwnBarSoATapCanResolveBackToData() {
        val payloads = facesFor().map { face -> face.payload.label }.toSet()
        assertEquals(setOf("a", "b", "c"), payloads)
    }

    @Test
    fun emptyDataBuildsNoFaces() {
        val faces =
            bar3DFaces(size = canvas, dataList = emptyList(), maxValue = 1f, config = Bar3DChartConfig(), progress = 1f)
        assertTrue(faces.isEmpty())
    }

    @Test
    fun zeroProgressCollapsesEveryBarOntoTheFloor() {
        val flat = Projection3D(pitch = 0f, yaw = 0f, perspective = 0f)
        val faces = facesFor(config = Bar3DChartConfig(projection = flat), progress = 0f)
        val front = faces.first { face -> face.side == FaceSide.FRONT }
        val heights = front.points.map { point -> point.y }
        assertEquals(heights.min(), heights.max(), TOLERANCE)
    }

    @Test
    fun theTallestBarReachesHigherThanTheShortest() {
        val flat = Projection3D(pitch = 0f, yaw = 0f, perspective = 0f)
        val faces = facesFor(config = Bar3DChartConfig(projection = flat))

        fun topOf(label: String) =
            faces
                .filter { face -> face.payload.label == label }
                .flatMap { face -> face.points }
                .minOf { point -> point.y }
        assertTrue(topOf("b") < topOf("a"), "the 100 bar must rise above the 50 bar")
        assertTrue(topOf("a") < topOf("c"), "the 50 bar must rise above the 25 bar")
    }

    @Test
    fun flatBarsStillBuildFacesWhenDepthIsZero() {
        val faces = facesFor(config = Bar3DChartConfig(barDepthFraction = 0f))
        assertEquals(bars.size * 3, faces.size)
    }

    @Test
    fun barWidthFractionOutsideZeroToOneIsRejected() {
        assertFailsWith<IllegalArgumentException> { Bar3DChartConfig(barWidthFraction = 1.4f) }
    }

    @Test
    fun negativeDepthFractionIsRejected() {
        assertFailsWith<IllegalArgumentException> { Bar3DChartConfig(barDepthFraction = -0.2f) }
    }
}

class FaceOrderingTest {
    private fun face(
        depth: Float,
        label: String,
        corners: List<Offset>,
    ) = ProjectedFace(points = corners, side = FaceSide.FRONT, depth = depth, payload = label)

    private val square = listOf(Offset(0f, 0f), Offset(10f, 0f), Offset(10f, 10f), Offset(0f, 10f))

    @Test
    fun facesAreDrawnFurthestFirst() {
        val ordered =
            listOf(
                face(depth = 1f, label = "near", corners = square),
                face(depth = 9f, label = "far", corners = square),
                face(depth = 5f, label = "mid", corners = square),
            ).sortedFarToNear()
        assertEquals(listOf("far", "mid", "near"), ordered.map { it.payload })
    }

    @Test
    fun aTapResolvesToTheNearestFaceNotTheOneBehindIt() {
        val overlapping =
            listOf(
                face(depth = 9f, label = "behind", corners = square),
                face(depth = 1f, label = "in front", corners = square),
            )
        assertEquals("in front", overlapping.hitTest(Offset(5f, 5f)))
    }

    @Test
    fun aTapOutsideEveryFaceResolvesToNothing() {
        assertNull(listOf(face(depth = 1f, label = "only", corners = square)).hitTest(Offset(50f, 50f)))
    }

    @Test
    fun aDegenerateFaceCannotBeHit() {
        val line = listOf(Offset(0f, 0f), Offset(10f, 0f))
        assertNull(listOf(face(depth = 1f, label = "line", corners = line)).hitTest(Offset(5f, 0f)))
    }

    @Test
    fun theTopFaceIsLighterAndTheSideDarkerThanTheFront() {
        val base = Color(red = 0.4f, green = 0.4f, blue = 0.4f, alpha = 1f)
        assertTrue(shadeForSide(color = base, side = FaceSide.TOP).red > base.red)
        assertTrue(shadeForSide(color = base, side = FaceSide.SIDE).red < base.red)
        assertEquals(base.red, shadeForSide(color = base, side = FaceSide.FRONT).red, CHANNEL_TOLERANCE)
    }

    @Test
    fun shadingKeepsAlphaAndCannotOverflowAChannel() {
        val bright = Color(red = 1f, green = 1f, blue = 1f, alpha = 0.5f)
        val lit = shadeForSide(color = bright, side = FaceSide.TOP)
        assertEquals(1f, lit.red, CHANNEL_TOLERANCE)
        assertEquals(0.5f, lit.alpha, CHANNEL_TOLERANCE)
    }
}

class Bar3DLabelTest {
    @Test
    fun wholeNumbersLoseTheirTrailingDecimalOnEveryPlatform() {
        assertEquals("20", 20f.toBar3DLabel())
        assertEquals("0", 0f.toBar3DLabel())
        assertEquals("-7", (-7f).toBar3DLabel())
    }

    @Test
    fun fractionsKeepTheirDecimals() {
        assertEquals("20.5", 20.5f.toBar3DLabel())
    }
}
