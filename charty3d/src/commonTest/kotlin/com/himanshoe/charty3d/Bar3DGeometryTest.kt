package com.himanshoe.charty3d

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty3d.bar.config.Bar3DChartConfig
import com.himanshoe.charty3d.bar.config.toBar3DLabel
import com.himanshoe.charty3d.internal.bar3DFaces
import com.himanshoe.charty3d.internal.bar3DFit
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
) = bar3DFaces(
    dataList = bars,
    maxValue = 100f,
    config = config,
    progress = progress,
    fit = bar3DFit(size = canvas, dataList = bars, maxValue = 100f, config = config, progress = progress),
)

class Bar3DGeometryTest {
    @Test
    fun aTiltedBarShowsExactlyItsThreeFacingSides() {
        assertEquals(bars.size * 3, facesFor().size)
    }

    @Test
    fun aBarBuildsOneFaceOfEachSide() {
        val sides = facesFor().take(3).map { face -> face.side }.toSet()
        assertEquals(setOf(FaceSide.FRONT, FaceSide.TOP, FaceSide.SIDE), sides)
    }

    @Test
    fun aHeadOnBarShowsOnlyItsFrontBecauseTheOthersAreEdgeOn() {
        val headOn = Bar3DChartConfig(projection = Projection3D(pitch = 0f, yaw = 0f, perspective = 0f))
        val faces = facesFor(config = headOn)
        assertEquals(bars.size, faces.size)
        assertTrue(faces.all { face -> face.side == FaceSide.FRONT })
    }

    @Test
    fun swingingTheYawTheOtherWayStillShowsThreeFaces() {
        val leftward = Bar3DChartConfig(projection = Projection3D(pitch = 18f, yaw = -14f, perspective = 0f))
        assertEquals(bars.size * 3, facesFor(config = leftward).size)
    }

    @Test
    fun aNegativeYawShowsTheOppositeFlankToAPositiveOne() {
        fun flankCentreX(yaw: Float): Float {
            val config = Bar3DChartConfig(projection = Projection3D(pitch = 18f, yaw = yaw, perspective = 0f))
            val faces = facesFor(config = config).filter { face -> face.payload.label == "b" }
            val side = faces.first { face -> face.side == FaceSide.SIDE }
            val bar = faces.first { face -> face.side == FaceSide.FRONT }
            return side.points
                .map { it.x }
                .average()
                .toFloat() -
                bar.points
                    .map { it.x }
                    .average()
                    .toFloat()
        }
        assertTrue(
            flankCentreX(yaw = 20f) * flankCentreX(yaw = -20f) < 0f,
            "the visible flank must swap sides with the yaw, not stay pinned to one",
        )
    }

    @Test
    fun tippingThePitchBelowTheHorizonShowsTheUndersideNotTheTop() {
        val fromBelow = Bar3DChartConfig(projection = Projection3D(pitch = -25f, yaw = 14f, perspective = 0f))
        val faces = facesFor(config = fromBelow)
        assertEquals(bars.size * 3, faces.size)
        assertTrue(faces.any { face -> face.side == FaceSide.TOP })
    }

    @Test
    fun everyFaceCarriesItsOwnBarSoATapCanResolveBackToData() {
        val payloads = facesFor().map { face -> face.payload.label }.toSet()
        assertEquals(setOf("a", "b", "c"), payloads)
    }

    @Test
    fun emptyDataBuildsNoFaces() {
        val config = Bar3DChartConfig()
        val faces =
            bar3DFaces(
                dataList = emptyList(),
                maxValue = 1f,
                config = config,
                progress = 1f,
                fit = bar3DFit(size = canvas, dataList = emptyList(), maxValue = 1f, config = config, progress = 1f),
            )
        assertTrue(faces.isEmpty())
    }

    @Test
    fun zeroProgressLeavesNoStandingBarToDraw() {
        val flat = Projection3D(pitch = 0f, yaw = 0f, perspective = 0f)
        assertTrue(facesFor(config = Bar3DChartConfig(projection = flat), progress = 0f).isEmpty())
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
    fun zeroDepthBarsShowOnlyTheirFrontFace() {
        val faces = facesFor(config = Bar3DChartConfig(barDepthFraction = 0f))
        assertEquals(bars.size, faces.size)
        assertTrue(faces.all { face -> face.side == FaceSide.FRONT })
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

class SceneFitTest {
    private fun fitFor(config: Bar3DChartConfig) =
        bar3DFit(size = canvas, dataList = bars, maxValue = 100f, config = config, progress = 1f)

    @Test
    fun everyFaceLandsInsideTheCanvasAtAnExtremeAngle() {
        val extreme =
            Bar3DChartConfig(projection = Projection3D(pitch = 55f, yaw = -50f, depth = 0.9f, perspective = 0.7f))
        val faces =
            bar3DFaces(
                dataList = bars,
                maxValue = 100f,
                config = extreme,
                progress = 1f,
                fit = fitFor(extreme),
            )
        val points = faces.flatMap { face -> face.points }
        assertTrue(points.all { point -> point.x >= -1f && point.x <= canvas.width + 1f }, "scene ran off horizontally")
        assertTrue(points.all { point -> point.y >= -1f && point.y <= canvas.height + 1f }, "scene ran off vertically")
    }

    @Test
    fun theSceneFillsTheCanvasRatherThanHuddlingInACorner() {
        val faces =
            bar3DFaces(
                dataList = bars,
                maxValue = 100f,
                config = Bar3DChartConfig(),
                progress = 1f,
                fit = fitFor(Bar3DChartConfig()),
            )
        val points = faces.flatMap { face -> face.points }
        val spanX = points.maxOf { it.x } - points.minOf { it.x }
        val spanY = points.maxOf { it.y } - points.minOf { it.y }
        assertTrue(spanX > canvas.width * 0.5f || spanY > canvas.height * 0.5f, "the scene was fitted far too small")
    }
}
