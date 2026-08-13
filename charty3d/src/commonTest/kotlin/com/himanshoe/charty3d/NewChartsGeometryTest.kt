package com.himanshoe.charty3d

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.himanshoe.charty.heatmap.data.HeatmapCell
import com.himanshoe.charty.line.data.LineGroup
import com.himanshoe.charty3d.internal.bar3DMatrixFaces
import com.himanshoe.charty3d.internal.bar3DMatrixFit
import com.himanshoe.charty3d.internal.hitTestQuads
import com.himanshoe.charty3d.internal.matrixCellColor
import com.himanshoe.charty3d.internal.matrixGrid
import com.himanshoe.charty3d.internal.matrixValueRange
import com.himanshoe.charty3d.internal.ribbon3DFaces
import com.himanshoe.charty3d.internal.ribbon3DFit
import com.himanshoe.charty3d.internal.ribbonValueRange
import com.himanshoe.charty3d.internal.surface3DFit
import com.himanshoe.charty3d.internal.surface3DQuads
import com.himanshoe.charty3d.internal.surfaceHeightRange
import com.himanshoe.charty3d.matrix.config.Bar3DMatrixConfig
import com.himanshoe.charty3d.projection.hitTest
import com.himanshoe.charty3d.ribbon.config.Ribbon3DChartConfig
import com.himanshoe.charty3d.surface.config.Surface3DChartConfig
import com.himanshoe.charty3d.surface.data.SurfaceData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val TOL = 0.01f
private val panel = Size(width = 400f, height = 320f)

private val cells =
    listOf(
        HeatmapCell(rowLabel = "Mon", columnLabel = "AM", value = 10f),
        HeatmapCell(rowLabel = "Mon", columnLabel = "PM", value = 20f),
        HeatmapCell(rowLabel = "Tue", columnLabel = "AM", value = 30f),
        HeatmapCell(rowLabel = "Tue", columnLabel = "PM", value = 40f),
    )

class Bar3DMatrixTest {
    private fun faces(config: Bar3DMatrixConfig = Bar3DMatrixConfig()) =
        bar3DMatrixFaces(
            dataList = cells,
            grid = matrixGrid(cells),
            range = matrixValueRange(cells),
            config = config,
            progress = 1f,
            fit = bar3DMatrixFit(size = panel, config = config),
        )

    @Test
    fun rowsAndColumnsKeepTheOrderTheyWerePassedIn() {
        val grid = matrixGrid(cells)
        assertEquals(listOf("Mon", "Tue"), grid.rows)
        assertEquals(listOf("AM", "PM"), grid.columns)
    }

    @Test
    fun aGridWithOneValueThroughoutStillHasAUsableRange() {
        val flat = listOf(HeatmapCell(rowLabel = "a", columnLabel = "b", value = 5f))
        val range = matrixValueRange(flat)
        assertTrue(range.endInclusive > range.start, "a degenerate range would divide by zero downstream")
    }

    @Test
    fun everyCellContributesItsVisibleFaces() {
        assertEquals(cells.size * 3, faces().size)
    }

    @Test
    fun everyBarStaysOnTheCanvas() {
        val points = faces().flatMap { face -> face.points }
        assertTrue(
            points.all { point ->
                point.x >= -1f && point.x <= panel.width + 1f && point.y >= -1f && point.y <= panel.height + 1f
            },
        )
    }

    @Test
    fun heightAndColourCarryTheSameValue() {
        val range = matrixValueRange(cells)
        val lowest = matrixCellColor(cell = cells.first(), range = range, low = Color.Black, high = Color.White)
        val highest = matrixCellColor(cell = cells.last(), range = range, low = Color.Black, high = Color.White)
        assertTrue(highest.red > lowest.red, "the larger value must sit further along the ramp")
    }

    @Test
    fun aTapResolvesToACell() {
        val built = faces()
        val probe = built.first().points.first()
        assertTrue(built.hitTest(probe) != null)
    }

    @Test
    fun anOutOfRangeFootprintIsRejected() {
        assertFailsWith<IllegalArgumentException> { Bar3DMatrixConfig(barFootprintFraction = 1.3f) }
    }

    @Test
    fun anEmptyGridBuildsNoFaces() {
        val config = Bar3DMatrixConfig()
        assertTrue(
            bar3DMatrixFaces(
                dataList = emptyList(),
                grid = matrixGrid(emptyList()),
                range = 0f..1f,
                config = config,
                progress = 1f,
                fit = bar3DMatrixFit(size = panel, config = config),
            ).isEmpty(),
        )
    }
}

class Surface3DTest {
    private val surface = SurfaceData.of(rowCount = 5, columnCount = 6) { row, column -> (row * column).toFloat() }

    private fun quads(config: Surface3DChartConfig = Surface3DChartConfig()) =
        surface3DQuads(
            data = surface,
            config = config,
            progress = 1f,
            fit = surface3DFit(size = panel, config = config),
        )

    @Test
    fun theMeshHasOneQuadPerGridCell() {
        assertEquals((5 - 1) * (6 - 1), quads().size)
    }

    @Test
    fun anIncompleteGridIsRejectedRatherThanDrawnTorn() {
        assertFailsWith<IllegalArgumentException> {
            SurfaceData(rowCount = 3, columnCount = 3, heights = listOf(1f, 2f, 3f))
        }
    }

    @Test
    fun aGridTooSmallToHaveExtentIsRejected() {
        assertFailsWith<IllegalArgumentException> {
            SurfaceData(rowCount = 1, columnCount = 4, heights = List(4) { 0f })
        }
    }

    @Test
    fun samplingAFunctionFillsTheGridCompletely() {
        val sampled = SurfaceData.of(rowCount = 3, columnCount = 4) { row, column -> row + column.toFloat() }
        assertEquals(12, sampled.heights.size)
        assertEquals(5f, sampled.heightAt(row = 2, column = 3), TOL)
    }

    @Test
    fun aFlatSurfaceStillHasAUsableHeightRange() {
        val flat = SurfaceData.of(rowCount = 3, columnCount = 3) { _, _ -> 7f }
        val range = surfaceHeightRange(flat)
        assertTrue(range.endInclusive > range.start)
    }

    @Test
    fun nothingIsCulledBecauseAMeshIsNotAClosedSolid() {
        val fromBelow =
            Surface3DChartConfig(
                projection =
                    com.himanshoe.charty3d.projection
                        .Projection3D(pitch = -30f, yaw = 20f, perspective = 0f),
            )
        assertEquals(quads().size, quads(config = fromBelow).size, "a surface must stay whole seen from beneath")
    }

    @Test
    fun everyQuadStaysOnTheCanvas() {
        val points = quads().flatMap { quad -> quad.face.points }
        assertTrue(
            points.all { point ->
                point.x >= -1f && point.x <= panel.width + 1f && point.y >= -1f && point.y <= panel.height + 1f
            },
        )
    }

    @Test
    fun aTapResolvesToAGridCell() {
        val built = quads()
        val probe =
            built
                .first()
                .face.points
                .first()
        assertTrue(built.hitTestQuads(probe) != null)
    }

    @Test
    fun aTapOffTheMeshResolvesToNothing() {
        assertNull(quads().hitTestQuads(Offset(x = -50f, y = -50f)))
    }

    @Test
    fun anOutOfRangeWireframeAlphaIsRejected() {
        assertFailsWith<IllegalArgumentException> { Surface3DChartConfig(wireframeAlpha = 2f) }
    }
}

class Ribbon3DTest {
    private val series =
        listOf(
            LineGroup(label = "EU", values = listOf(10f, 20f, 15f, 30f)),
            LineGroup(label = "US", values = listOf(5f, 25f, 20f, 10f)),
        )

    private fun faces(config: Ribbon3DChartConfig = Ribbon3DChartConfig()) =
        ribbon3DFaces(
            dataList = series,
            config = config,
            range = ribbonValueRange(series),
            progress = 1f,
            fit = ribbon3DFit(size = panel, config = config),
        )

    @Test
    fun everySeriesContributesASegmentPerGap() {
        val withoutFill = Ribbon3DChartConfig(fillUnderRibbon = false)
        assertEquals(series.size * (4 - 1), faces(config = withoutFill).size)
    }

    @Test
    fun fillingUnderTheRibbonDoublesTheFaces() {
        val plain = faces(config = Ribbon3DChartConfig(fillUnderRibbon = false)).size
        assertEquals(plain * 2, faces(config = Ribbon3DChartConfig(fillUnderRibbon = true)).size)
    }

    @Test
    fun everyFaceNamesItsSeries() {
        assertEquals(setOf("EU", "US"), faces().map { face -> face.payload.seriesName }.toSet())
    }

    @Test
    fun theRangeSpansEverySeriesSoTheyShareOneScale() {
        val range = ribbonValueRange(series)
        assertEquals(0f, range.start, TOL)
        assertEquals(30f, range.endInclusive, TOL)
    }

    @Test
    fun aSingleValuedSeriesStillHasAUsableRange() {
        val flat = listOf(LineGroup(label = "flat", values = listOf(4f, 4f)))
        val range = ribbonValueRange(flat)
        assertTrue(range.endInclusive > range.start)
    }

    @Test
    fun aSeriesTooShortToDrawBuildsNothing() {
        val single = listOf(LineGroup(label = "one", values = listOf(1f)))
        val config = Ribbon3DChartConfig()
        assertTrue(
            ribbon3DFaces(
                dataList = single,
                config = config,
                range = 0f..1f,
                progress = 1f,
                fit = ribbon3DFit(size = panel, config = config),
            ).isEmpty(),
        )
    }

    @Test
    fun everyRibbonStaysOnTheCanvas() {
        val points = faces().flatMap { face -> face.points }
        assertTrue(
            points.all { point ->
                point.x >= -1f && point.x <= panel.width + 1f && point.y >= -1f && point.y <= panel.height + 1f
            },
        )
    }

    @Test
    fun aTapResolvesToASegment() {
        val built = faces()
        assertTrue(built.hitTest(built.first().points.first()) != null)
    }

    @Test
    fun anOutOfRangeRibbonWidthIsRejected() {
        assertFailsWith<IllegalArgumentException> { Ribbon3DChartConfig(ribbonWidthFraction = 1.2f) }
    }
}
