package com.himanshoe.charty.heatmap

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.heatmap.config.MatrixHeatmapConfig
import com.himanshoe.charty.heatmap.data.HeatmapCell
import com.himanshoe.charty.heatmap.internal.HEATMAP_MIN_RAMP_ALPHA
import com.himanshoe.charty.heatmap.internal.buildMatrixHeatmapDescription
import com.himanshoe.charty.heatmap.internal.computeMatrixGrid
import com.himanshoe.charty.heatmap.internal.flattenCells
import com.himanshoe.charty.heatmap.internal.formatHeatmapTooltip
import com.himanshoe.charty.heatmap.internal.formatHeatmapValue
import com.himanshoe.charty.heatmap.internal.heatmapContrastTextColor
import com.himanshoe.charty.heatmap.internal.heatmapTooltipState
import com.himanshoe.charty.heatmap.internal.interpolateHeatmapColor
import com.himanshoe.charty.heatmap.internal.lerpColorLinear
import com.himanshoe.charty.heatmap.internal.matrixCellProgress
import com.himanshoe.charty.heatmap.internal.normalizeHeatmapValue
import com.himanshoe.charty.heatmap.internal.resolveHeatmapCellAt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

private const val CHANNEL_TOLERANCE = 0.01f

private fun assertColorEquals(
    expected: Color,
    actual: Color,
    message: String? = null,
) {
    assertEquals(expected.red, actual.red, absoluteTolerance = CHANNEL_TOLERANCE, message = message)
    assertEquals(expected.green, actual.green, absoluteTolerance = CHANNEL_TOLERANCE, message = message)
    assertEquals(expected.blue, actual.blue, absoluteTolerance = CHANNEL_TOLERANCE, message = message)
    assertEquals(expected.alpha, actual.alpha, absoluteTolerance = CHANNEL_TOLERANCE, message = message)
}

class MatrixHeatmapHelpersTest {
    @Test
    fun computeMatrixGrid_ordersRowsAndColumnsByFirstAppearance() {
        val grid =
            computeMatrixGrid(
                listOf(
                    HeatmapCell(rowLabel = "B", columnLabel = "x", value = 1f),
                    HeatmapCell(rowLabel = "A", columnLabel = "y", value = 2f),
                    HeatmapCell(rowLabel = "B", columnLabel = "z", value = 3f),
                    HeatmapCell(rowLabel = "A", columnLabel = "x", value = 4f),
                ),
            )
        assertEquals(listOf("B", "A"), grid.rowLabels)
        assertEquals(listOf("x", "y", "z"), grid.columnLabels)
        assertEquals(1f, grid.cellMap.getValue(0 to 0).value)
        assertEquals(2f, grid.cellMap.getValue(1 to 1).value)
        assertEquals(3f, grid.cellMap.getValue(0 to 2).value)
        assertEquals(4f, grid.cellMap.getValue(1 to 0).value)
    }

    @Test
    fun computeMatrixGrid_tracksMinAndMax_andLastDuplicateWins() {
        val grid =
            computeMatrixGrid(
                listOf(
                    HeatmapCell(rowLabel = "r", columnLabel = "c", value = 5f),
                    HeatmapCell(rowLabel = "r", columnLabel = "d", value = 9f),
                    HeatmapCell(rowLabel = "r", columnLabel = "c", value = 7f),
                ),
            )
        assertEquals(5f, grid.minValue)
        assertEquals(9f, grid.maxValue)
        assertEquals(7f, grid.cellMap.getValue(0 to 0).value)
        assertEquals(2, grid.cellMap.size)
    }

    @Test
    fun computeMatrixGrid_emptyInput_yieldsEmptyGrid() {
        val grid = computeMatrixGrid(emptyList())
        assertTrue(grid.rowLabels.isEmpty())
        assertTrue(grid.columnLabels.isEmpty())
        assertTrue(grid.cellMap.isEmpty())
        assertEquals(0f, grid.minValue)
        assertEquals(0f, grid.maxValue)
    }

    @Test
    fun flattenCells_isRowMajorWithNullsForGaps() {
        val grid =
            computeMatrixGrid(
                listOf(
                    HeatmapCell(rowLabel = "A", columnLabel = "x", value = 1f),
                    HeatmapCell(rowLabel = "A", columnLabel = "y", value = 2f),
                    HeatmapCell(rowLabel = "B", columnLabel = "y", value = 3f),
                ),
            )
        val flat = grid.flattenCells()
        assertEquals(4, flat.size)
        assertEquals(1f, flat[0]?.value)
        assertEquals(2f, flat[1]?.value)
        assertEquals(null, flat[2])
        assertEquals(3f, flat[3]?.value)
    }

    @Test
    fun flattenCells_emptyGridIsEmpty() {
        assertTrue(computeMatrixGrid(emptyList()).flattenCells().isEmpty())
    }

    @Test
    fun normalize_spansRangeAndClamps() {
        assertEquals(0f, normalizeHeatmapValue(value = 0f, minValue = 0f, maxValue = 10f))
        assertEquals(0.5f, normalizeHeatmapValue(value = 5f, minValue = 0f, maxValue = 10f))
        assertEquals(1f, normalizeHeatmapValue(value = 10f, minValue = 0f, maxValue = 10f))
        assertEquals(0f, normalizeHeatmapValue(value = -3f, minValue = 0f, maxValue = 10f))
        assertEquals(1f, normalizeHeatmapValue(value = 42f, minValue = 0f, maxValue = 10f))
        assertEquals(0.25f, normalizeHeatmapValue(value = -5f, minValue = -10f, maxValue = 10f))
    }

    @Test
    fun normalize_equalMinAndMax_returnsFullIntensity() {
        assertEquals(1f, normalizeHeatmapValue(value = 7f, minValue = 7f, maxValue = 7f))
        assertEquals(1f, normalizeHeatmapValue(value = 0f, minValue = 3f, maxValue = 3f))
    }

    @Test
    fun normalize_invertedRange_returnsFullIntensity() {
        assertEquals(1f, normalizeHeatmapValue(value = 5f, minValue = 10f, maxValue = 0f))
    }

    @Test
    fun normalize_singleCellDataset_rendersAtFullIntensity() {
        val grid = computeMatrixGrid(listOf(HeatmapCell(rowLabel = "r", columnLabel = "c", value = 42f)))
        assertEquals(
            1f,
            normalizeHeatmapValue(value = 42f, minValue = grid.minValue, maxValue = grid.maxValue),
        )
    }

    @Test
    fun lerpColorLinear_interpolatesEachChannel() {
        val mid = lerpColorLinear(start = Color.Red, stop = Color.Blue, fraction = 0.5f)
        assertColorEquals(expected = Color(red = 0.5f, green = 0f, blue = 0.5f), actual = mid)
        assertColorEquals(
            expected = Color.Red,
            actual = lerpColorLinear(start = Color.Red, stop = Color.Blue, fraction = 0f),
        )
        assertColorEquals(
            expected = Color.Blue,
            actual = lerpColorLinear(start = Color.Red, stop = Color.Blue, fraction = 1f),
        )
    }

    @Test
    fun interpolate_twoStopGradient() {
        val scale = ChartyColor.Gradient(colors = listOf(Color.Red, Color.Blue))
        assertColorEquals(expected = Color.Red, actual = interpolateHeatmapColor(colorScale = scale, fraction = 0f))
        assertColorEquals(expected = Color.Blue, actual = interpolateHeatmapColor(colorScale = scale, fraction = 1f))
        assertColorEquals(
            expected = Color(red = 0.5f, green = 0f, blue = 0.5f),
            actual = interpolateHeatmapColor(colorScale = scale, fraction = 0.5f),
        )
    }

    @Test
    fun interpolate_threeStopGradient() {
        val scale = ChartyColor.Gradient(colors = listOf(Color.Red, Color.Green, Color.Blue))
        assertColorEquals(expected = Color.Red, actual = interpolateHeatmapColor(colorScale = scale, fraction = 0f))
        assertColorEquals(
            expected = Color.Green,
            actual = interpolateHeatmapColor(colorScale = scale, fraction = 0.5f),
        )
        assertColorEquals(expected = Color.Blue, actual = interpolateHeatmapColor(colorScale = scale, fraction = 1f))
        assertColorEquals(
            expected = Color(red = 0.5f, green = 0.5f, blue = 0f),
            actual = interpolateHeatmapColor(colorScale = scale, fraction = 0.25f),
        )
    }

    @Test
    fun interpolate_clampsOutOfRangeFractions() {
        val scale = ChartyColor.Gradient(colors = listOf(Color.Red, Color.Blue))
        assertColorEquals(expected = Color.Red, actual = interpolateHeatmapColor(colorScale = scale, fraction = -1f))
        assertColorEquals(expected = Color.Blue, actual = interpolateHeatmapColor(colorScale = scale, fraction = 2f))
    }

    @Test
    fun interpolate_solidColor_rampsAlphaOverSingleHue() {
        val scale = ChartyColor.Solid(color = Color.Red)
        val low = interpolateHeatmapColor(colorScale = scale, fraction = 0f)
        val high = interpolateHeatmapColor(colorScale = scale, fraction = 1f)
        assertEquals(HEATMAP_MIN_RAMP_ALPHA, low.alpha, absoluteTolerance = CHANNEL_TOLERANCE)
        assertEquals(1f, high.alpha, absoluteTolerance = CHANNEL_TOLERANCE)
        assertEquals(1f, low.red, absoluteTolerance = CHANNEL_TOLERANCE)
        assertEquals(1f, high.red, absoluteTolerance = CHANNEL_TOLERANCE)
    }

    @Test
    fun interpolate_singleStopGradient_behavesLikeSolidRamp() {
        val scale = ChartyColor.Gradient(colors = listOf(Color.Blue))
        val low = interpolateHeatmapColor(colorScale = scale, fraction = 0f)
        val high = interpolateHeatmapColor(colorScale = scale, fraction = 1f)
        assertEquals(HEATMAP_MIN_RAMP_ALPHA, low.alpha, absoluteTolerance = CHANNEL_TOLERANCE)
        assertEquals(1f, high.alpha, absoluteTolerance = CHANNEL_TOLERANCE)
    }

    @Test
    fun matrixCellProgress_completesEveryCellAtFullProgress() {
        for (row in 0 until 3) {
            for (column in 0 until 4) {
                val progress =
                    matrixCellProgress(
                        progress = 1f,
                        rowIndex = row,
                        columnIndex = column,
                        rowCount = 3,
                        columnCount = 4,
                    )
                assertEquals(1f, progress, "cell ($row, $column)")
            }
        }
    }

    @Test
    fun matrixCellProgress_staggersDiagonals() {
        val early =
            matrixCellProgress(progress = 0.5f, rowIndex = 0, columnIndex = 0, rowCount = 3, columnCount = 3)
        val middle =
            matrixCellProgress(progress = 0.5f, rowIndex = 1, columnIndex = 1, rowCount = 3, columnCount = 3)
        val late =
            matrixCellProgress(progress = 0.5f, rowIndex = 2, columnIndex = 2, rowCount = 3, columnCount = 3)
        assertEquals(1f, early)
        assertEquals(0.5f, middle)
        assertEquals(0f, late)
    }

    @Test
    fun matrixCellProgress_singleCellGrid_tracksOverallProgress() {
        val progress =
            matrixCellProgress(progress = 0.3f, rowIndex = 0, columnIndex = 0, rowCount = 1, columnCount = 1)
        assertEquals(0.3f, progress)
    }

    @Test
    fun formatHeatmapValue_dropsDecimalForWholeNumbers() {
        assertEquals("42", formatHeatmapValue(42f))
        assertEquals("2", formatHeatmapValue(2.0f))
        assertEquals("3.5", formatHeatmapValue(3.5f))
        assertEquals("3.3", formatHeatmapValue(3.26f))
    }

    @Test
    fun contrastTextColor_picksReadableColor() {
        assertEquals(Color.White, heatmapContrastTextColor(Color(0xFF0D47A1)))
        assertTrue(heatmapContrastTextColor(Color.White) != Color.White)
        assertTrue(heatmapContrastTextColor(Color(0xFFE3F2FD)) != Color.White)
    }

    @Test
    fun description_summarisesGrid() {
        val grid =
            computeMatrixGrid(
                listOf(
                    HeatmapCell(rowLabel = "Mon", columnLabel = "9h", value = 1f),
                    HeatmapCell(rowLabel = "Tue", columnLabel = "10h", value = 9f),
                ),
            )
        val description = buildMatrixHeatmapDescription(grid)
        assertEquals(
            "Matrix heatmap with 2 rows and 2 columns. 2 of 4 cells have data. Values range from 1 to 9.",
            description,
        )
    }

    @Test
    fun description_emptyGrid() {
        assertEquals("Empty matrix heatmap", buildMatrixHeatmapDescription(computeMatrixGrid(emptyList())))
    }

    @Test
    fun config_rejectsNegativeCornerRadius() {
        assertFailsWith<IllegalArgumentException> {
            MatrixHeatmapConfig(cellCornerRadius = -1f)
        }
    }

    @Test
    fun config_rejectsNegativeCellSpacing() {
        assertFailsWith<IllegalArgumentException> {
            MatrixHeatmapConfig(cellSpacing = (-2).dp)
        }
    }

    @Test
    fun config_defaultsAreValid() {
        val config = MatrixHeatmapConfig()
        assertTrue(config.colorScale is ChartyColor.Gradient)
        assertEquals("7.5", config.valueFormatter(7.5f))
    }

    @Test
    fun tooltipFormatter_defaultShowsRowColumnAndValue() {
        val cell = HeatmapCell(rowLabel = "Mon", columnLabel = "9", value = 12f)
        assertEquals("Mon · 9: 12", MatrixHeatmapConfig().tooltipFormatter(cell))
        assertEquals(
            "Mon · 9: 12.5",
            formatHeatmapTooltip(HeatmapCell(rowLabel = "Mon", columnLabel = "9", value = 12.45f)),
        )
    }

    @Test
    fun resolveHeatmapCellAt_returnsCellContainingPosition() {
        val first = HeatmapCell(rowLabel = "Mon", columnLabel = "9", value = 1f)
        val second = HeatmapCell(rowLabel = "Mon", columnLabel = "10", value = 2f)
        val bounds =
            listOf(
                Rect(left = 0f, top = 0f, right = 10f, bottom = 10f) to first,
                Rect(left = 12f, top = 0f, right = 22f, bottom = 10f) to second,
            )
        assertEquals(
            second,
            resolveHeatmapCellAt(cellBounds = bounds, position = Offset(x = 15f, y = 5f))?.second,
        )
        assertEquals(
            first,
            resolveHeatmapCellAt(cellBounds = bounds, position = Offset(x = 1f, y = 1f))?.second,
        )
    }

    @Test
    fun resolveHeatmapCellAt_returnsNullOutsideEveryCell() {
        val cell = HeatmapCell(rowLabel = "Mon", columnLabel = "9", value = 1f)
        val bounds = listOf(Rect(left = 0f, top = 0f, right = 10f, bottom = 10f) to cell)
        assertNull(resolveHeatmapCellAt(cellBounds = bounds, position = Offset(x = 11f, y = 11f)))
        assertNull(resolveHeatmapCellAt(cellBounds = emptyList(), position = Offset(x = 1f, y = 1f)))
    }

    @Test
    fun heatmapTooltipState_anchorsToCellTopLeadingEdge() {
        val state =
            heatmapTooltipState(
                bounds = Rect(left = 10f, top = 20f, right = 30f, bottom = 50f),
                content = "Mon · 9: 12",
            )
        assertEquals("Mon · 9: 12", state.content)
        assertEquals(10f, state.x)
        assertEquals(20f, state.y)
        assertEquals(20f, state.barWidth)
    }

    @Test
    fun heatmapTooltipResolvesToTheCellCentre() {
        val bounds = Rect(left = 10f, top = 20f, right = 30f, bottom = 50f)
        val state = heatmapTooltipState(bounds = bounds, content = "Mon · 9: 12")
        val resolvedCentre = state.x + state.barWidth / 2f
        assertEquals(bounds.left + bounds.width / 2f, resolvedCentre)
    }
}
