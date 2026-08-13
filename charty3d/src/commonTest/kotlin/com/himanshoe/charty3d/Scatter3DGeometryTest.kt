package com.himanshoe.charty3d

import androidx.compose.ui.geometry.Size
import com.himanshoe.charty3d.internal.hitTestPoints
import com.himanshoe.charty3d.internal.scatter3DBoxEdges
import com.himanshoe.charty3d.internal.scatter3DFit
import com.himanshoe.charty3d.internal.scatter3DPoints
import com.himanshoe.charty3d.internal.scatter3DRanges
import com.himanshoe.charty3d.internal.scatter3DScenePoint
import com.himanshoe.charty3d.projection.Projection3D
import com.himanshoe.charty3d.scatter.config.Scatter3DChartConfig
import com.himanshoe.charty3d.scatter.data.Scatter3DPoint
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val SCATTER_TOLERANCE = 0.01f
private val scatterCanvas = Size(width = 400f, height = 300f)
private val cloud =
    listOf(
        Scatter3DPoint(x = 0f, y = 0f, z = 0f, label = "near"),
        Scatter3DPoint(x = 10f, y = 20f, z = 5f, label = "mid"),
        Scatter3DPoint(x = 20f, y = 40f, z = 10f, label = "far"),
    )

private fun projected(
    config: Scatter3DChartConfig = Scatter3DChartConfig(),
    progress: Float = 1f,
) = scatter3DPoints(
    dataList = cloud,
    config = config,
    ranges = scatter3DRanges(cloud),
    progress = progress,
    fit = scatter3DFit(size = scatterCanvas, config = config),
)

class Scatter3DRangeTest {
    @Test
    fun eachAxisIsMeasuredIndependentlySoNoneSquashesTheOthers() {
        val ranges = scatter3DRanges(cloud)
        assertEquals(0f, ranges.x.min, SCATTER_TOLERANCE)
        assertEquals(20f, ranges.x.max, SCATTER_TOLERANCE)
        assertEquals(0f, ranges.y.min, SCATTER_TOLERANCE)
        assertEquals(40f, ranges.y.max, SCATTER_TOLERANCE)
        assertEquals(0f, ranges.z.min, SCATTER_TOLERANCE)
        assertEquals(10f, ranges.z.max, SCATTER_TOLERANCE)
    }

    @Test
    fun anAxisWhereEveryValueIsTheSameCollapsesToTheMiddleRatherThanDividingByZero() {
        val flat = listOf(Scatter3DPoint(x = 5f, y = 1f, z = 0f), Scatter3DPoint(x = 5f, y = 2f, z = 0f))
        val ranges = scatter3DRanges(flat)
        assertEquals(50f, ranges.x.normalize(5f), SCATTER_TOLERANCE)
        assertTrue(ranges.x.normalize(5f).isFinite())
    }

    @Test
    fun anEmptyCloudYieldsAUsableRangeRatherThanCrashing() {
        val ranges = scatter3DRanges(emptyList())
        assertTrue(ranges.x.normalize(0f).isFinite())
    }

    @Test
    fun largerYValuesRiseUpTheCanvas() {
        val ranges = scatter3DRanges(cloud)
        val low = scatter3DScenePoint(point = cloud[0], ranges = ranges)
        val high = scatter3DScenePoint(point = cloud[2], ranges = ranges)
        assertTrue(high.y < low.y, "a larger value must sit higher, which is a smaller y on the canvas")
    }
}

class Scatter3DPointTest {
    @Test
    fun everyObservationIsProjected() {
        assertEquals(cloud.size, projected().size)
    }

    @Test
    fun nearerPointsAreDrawnLargerThanFurtherOnes() {
        val points = projected()
        val nearest = points.minBy { it.depth }
        val furthest = points.maxBy { it.depth }
        assertTrue(nearest.radius > furthest.radius, "depth must be legible through size, not just position")
    }

    @Test
    fun turningOffTheDepthScalingMakesEveryPointTheSameSize() {
        val flat = Scatter3DChartConfig(nearScale = 1f, farScale = 1f)
        val radii = projected(config = flat).map { it.radius }.toSet()
        assertEquals(1, radii.size)
    }

    @Test
    fun everyPointLandsOnTheCanvas() {
        assertTrue(
            projected().all { point ->
                point.position.x >= -1f &&
                    point.position.x <= scatterCanvas.width + 1f &&
                    point.position.y >= -1f &&
                    point.position.y <= scatterCanvas.height + 1f
            },
        )
    }

    @Test
    fun zeroProgressRestsEveryPointOnTheFloor() {
        val resting = projected(progress = 0f)
        resting.forEach { point ->
            assertEquals(point.floor.y, point.position.y, SCATTER_TOLERANCE)
        }
    }

    @Test
    fun aTapOnAPointResolvesToThatObservation() {
        val points = projected()
        val target = points.first()
        assertEquals(target.data.label, points.hitTestPoints(target.position)?.label)
    }

    @Test
    fun aTapResolvesToTheNearestPointWhenTwoOverlap() {
        val points = projected()
        val nearest = points.minBy { it.depth }
        assertEquals(nearest.data.label, points.hitTestPoints(nearest.position)?.label)
    }

    @Test
    fun aTapOnEmptySpaceResolvesToNothing() {
        assertNull(
            projected().hitTestPoints(
                androidx.compose.ui.geometry
                    .Offset(x = 1f, y = 1f),
            ),
        )
    }

    @Test
    fun anEmptyCloudProjectsNoPoints() {
        val config = Scatter3DChartConfig()
        val points =
            scatter3DPoints(
                dataList = emptyList(),
                config = config,
                ranges = scatter3DRanges(emptyList()),
                progress = 1f,
                fit = scatter3DFit(size = scatterCanvas, config = config),
            )
        assertTrue(points.isEmpty())
    }
}

class Scatter3DBoxTest {
    @Test
    fun theBoundingBoxHasTwelveEdges() {
        assertEquals(12, scatter3DBoxEdges().size)
    }

    @Test
    fun everyBoxEdgeJoinsTwoCornersThatDifferOnExactlyOneAxis() {
        scatter3DBoxEdges().forEach { (from, to) ->
            val differing = listOf(from.x != to.x, from.y != to.y, from.z != to.z).count { it }
            assertEquals(1, differing, "a box edge runs along one axis only")
        }
    }

    @Test
    fun everyPresetKeepsTheBoxOnTheCanvas() {
        listOf(Projection3D.Default, Projection3D.Isometric, Projection3D.Subtle, Projection3D.Dramatic)
            .forEach { projection ->
                val config = Scatter3DChartConfig(projection = projection)
                val fit = scatter3DFit(size = scatterCanvas, config = config)
                val corners =
                    com.himanshoe.charty3d.internal
                        .scatter3DBoxCorners()
                        .map { point ->
                            fit.apply(
                                config.projection.project(
                                    point = point,
                                    origin = androidx.compose.ui.geometry.Offset.Zero,
                                ),
                            )
                        }
                assertTrue(
                    corners.all { corner ->
                        corner.x >= -1f &&
                            corner.x <= scatterCanvas.width + 1f &&
                            corner.y >= -1f &&
                            corner.y <= scatterCanvas.height + 1f
                    },
                    "$projection pushed the box off the canvas",
                )
            }
    }
}

class Scatter3DConfigTest {
    @Test
    fun aNonPositivePointRadiusIsRejected() {
        assertFailsWith<IllegalArgumentException> { Scatter3DChartConfig(pointRadius = 0f) }
    }

    @Test
    fun anOutOfRangeDropLineAlphaIsRejected() {
        assertFailsWith<IllegalArgumentException> { Scatter3DChartConfig(dropLineAlpha = 1.5f) }
    }

    @Test
    fun theBoxAndDropLinesAreOnByDefaultBecauseACloudIsUnreadableWithoutThem() {
        val config = Scatter3DChartConfig()
        assertTrue(config.showBox)
        assertTrue(config.showDropLines)
    }
}
