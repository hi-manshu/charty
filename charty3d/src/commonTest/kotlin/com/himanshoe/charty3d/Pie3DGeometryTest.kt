package com.himanshoe.charty3d

import androidx.compose.ui.geometry.Size
import com.himanshoe.charty.pie.data.PieData
import com.himanshoe.charty3d.internal.pie3DFaces
import com.himanshoe.charty3d.internal.pie3DFit
import com.himanshoe.charty3d.internal.pie3DProjection
import com.himanshoe.charty3d.internal.pie3DSweeps
import com.himanshoe.charty3d.pie.config.Pie3DChartConfig
import com.himanshoe.charty3d.projection.FaceSide
import com.himanshoe.charty3d.projection.Projection3D
import com.himanshoe.charty3d.projection.hitTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

private const val PIE_TOLERANCE = 0.01f
private val pieCanvas = Size(width = 400f, height = 300f)
private val slices =
    listOf(
        PieData(label = "A", value = 50f),
        PieData(label = "B", value = 30f),
        PieData(label = "C", value = 20f),
    )

private fun pieFaces(
    config: Pie3DChartConfig = Pie3DChartConfig(),
    progress: Float = 1f,
) = pie3DFaces(
    dataList = slices,
    config = config,
    progress = progress,
    fit = pie3DFit(size = pieCanvas, config = config),
)

class Pie3DSweepTest {
    @Test
    fun sweepsDivideTheWholeTurnInProportionToValue() {
        val sweeps = pie3DSweeps(dataList = slices, progress = 1f)
        assertEquals(180f, sweeps[0].sweepDegrees, PIE_TOLERANCE)
        assertEquals(108f, sweeps[1].sweepDegrees, PIE_TOLERANCE)
        assertEquals(72f, sweeps[2].sweepDegrees, PIE_TOLERANCE)
    }

    @Test
    fun sweepsMeetExactlySoNoGapOpensBetweenSlices() {
        val sweeps = pie3DSweeps(dataList = slices, progress = 1f)
        sweeps.zipWithNext().forEach { (current, next) ->
            assertEquals(current.startDegrees + current.sweepDegrees, next.startDegrees, PIE_TOLERANCE)
        }
    }

    @Test
    fun theWholeTurnIsAccountedFor() {
        val sweeps = pie3DSweeps(dataList = slices, progress = 1f)
        assertEquals(360f, sweeps.sumOf { it.sweepDegrees.toDouble() }.toFloat(), PIE_TOLERANCE)
    }

    @Test
    fun progressSweepsThePieOpenProportionally() {
        val half = pie3DSweeps(dataList = slices, progress = 0.5f)
        assertEquals(180f, half.sumOf { it.sweepDegrees.toDouble() }.toFloat(), PIE_TOLERANCE)
    }

    @Test
    fun anEmptyTotalYieldsNoSweepRatherThanNaN() {
        val sweeps = pie3DSweeps(dataList = emptyList(), progress = 1f)
        assertTrue(sweeps.isEmpty())
    }

    @Test
    fun theMidAngleSitsHalfwayThroughTheSlice() {
        val sweeps = pie3DSweeps(dataList = slices, progress = 1f)
        assertEquals(sweeps[0].startDegrees + 90f, sweeps[0].midDegrees, PIE_TOLERANCE)
    }
}

class Pie3DFaceTest {
    @Test
    fun aSolidPieBuildsTopSurfacesAndOuterWalls() {
        val sides = pieFaces().map { face -> face.side }.toSet()
        assertTrue(FaceSide.TOP in sides, "the disc must show its top surface")
        assertTrue(FaceSide.FRONT in sides, "the disc must show its outer wall, which is what makes it a solid")
    }

    @Test
    fun everyFaceCarriesItsOwnSliceSoATapResolvesBackToData() {
        val payloads = pieFaces().map { face -> face.payload.label }.toSet()
        assertEquals(setOf("A", "B", "C"), payloads)
    }

    @Test
    fun aFlatDiscBuildsNoWalls() {
        val flat = pieFaces(config = Pie3DChartConfig(thicknessFraction = 0f))
        assertTrue(
            flat.none { face ->
                face.side == FaceSide.FRONT &&
                    face.points.isNotEmpty() &&
                    faceHasArea(face.points)
            },
        )
    }

    @Test
    fun aRingBuildsAnInnerWallThatASolidPieDoesNot() {
        fun innerWalls(hole: Float) =
            pieFaces(config = Pie3DChartConfig(holeFraction = hole)).count { face -> face.side == FaceSide.SIDE }
        assertTrue(innerWalls(hole = 0.5f) > innerWalls(hole = 0f), "a ring must have an inner wall")
    }

    @Test
    fun everySliceStaysOnTheCanvas() {
        val points = pieFaces().flatMap { face -> face.points }
        assertTrue(
            points.all { point ->
                point.x >= -1f &&
                    point.x <= pieCanvas.width + 1f &&
                    point.y >= -1f &&
                    point.y <= pieCanvas.height + 1f
            },
        )
    }

    @Test
    fun explodingKeepsTheDiscOnTheCanvasToo() {
        val exploded = pieFaces(config = Pie3DChartConfig(explodeFraction = 0.3f))
        val points = exploded.flatMap { face -> face.points }
        assertTrue(
            points.all { point ->
                point.x >= -1f &&
                    point.x <= pieCanvas.width + 1f &&
                    point.y >= -1f &&
                    point.y <= pieCanvas.height + 1f
            },
        )
    }

    @Test
    fun anEdgeOnPitchIsRaisedSoTheDiscIsNeverInvisible() {
        val edgeOn = Pie3DChartConfig(projection = Projection3D(pitch = 0f, yaw = 0f, perspective = 0f))
        assertTrue(pie3DProjection(edgeOn).pitch > 0f, "a zero pitch would cull every face and draw nothing")
        assertTrue(pieFaces(config = edgeOn).isNotEmpty())
    }

    @Test
    fun aTapInsideASliceTopResolvesToThatSlice() {
        val faces = pieFaces()
        val top = faces.first { face -> face.side == FaceSide.TOP }
        val centroid =
            androidx.compose.ui.geometry.Offset(
                x = top.points.sumOf { it.x.toDouble() }.toFloat() / top.points.size,
                y = top.points.sumOf { it.y.toDouble() }.toFloat() / top.points.size,
            )
        assertEquals(top.payload.label, faces.hitTest(centroid)?.label)
    }

    @Test
    fun eachSliceTopIsOnePolygonSoNoSeamsShowAcrossIt() {
        val tops = pieFaces().filter { face -> face.side == FaceSide.TOP }
        assertEquals(slices.size, tops.size, "one top polygon per slice, not a fan of quads")
    }

    @Test
    fun aTapOnTheBackgroundResolvesToNothing() {
        assertTrue(
            pieFaces().hitTest(
                androidx.compose.ui.geometry
                    .Offset(x = 1f, y = 1f),
            ) == null,
        )
    }

    @Test
    fun zeroProgressDrawsNothing() {
        assertTrue(pieFaces(progress = 0f).isEmpty())
    }

    @Test
    fun tooFewArcSegmentsIsRejected() {
        assertFailsWith<IllegalArgumentException> { Pie3DChartConfig(arcSegments = 3) }
    }

    @Test
    fun aHoleFractionOutsideZeroToOneIsRejected() {
        assertFailsWith<IllegalArgumentException> { Pie3DChartConfig(holeFraction = 1.5f) }
    }

    @Test
    fun negativeThicknessIsRejected() {
        assertFailsWith<IllegalArgumentException> { Pie3DChartConfig(thicknessFraction = -0.1f) }
    }
}

private fun faceHasArea(points: List<androidx.compose.ui.geometry.Offset>): Boolean {
    var area = 0f
    for (i in points.indices) {
        val current = points[i]
        val next = points[(i + 1) % points.size]
        area += current.x * next.y - next.x * current.y
    }
    return kotlin.math.abs(area) > PIE_TOLERANCE
}
