package com.himanshoe.charty.calendar.config

/**
 * Defines the visual shape of each cell in the [com.himanshoe.charty.calendar.CalendarHeatmapChart].
 *
 * Supported shapes:
 * - [Square] — plain square cells.
 * - [RoundedSquare] — square cells with rounded corners (GitHub default).
 * - [Circle] — perfectly circular cells.
 * - [Diamond] — 45°-rotated square cells.
 *
 * Example usage:
 * ```kotlin
 * CalendarHeatmapConfig(cellShape = CellShape.Circle)
 * CalendarHeatmapConfig(cellShape = CellShape.RoundedSquare(cornerRadius = 4f))
 * ```
 */
sealed class CellShape {

    /** Plain square cell with no rounding. */
    data object Square : CellShape()

    /**
     * Square cell with rounded corners.
     *
     * @param cornerRadius The corner radius in pixels. Defaults to `2f`.
     */
    data class RoundedSquare(val cornerRadius: Float = 2f) : CellShape()

    /** Circular cell. The diameter equals [com.himanshoe.charty.calendar.config.CalendarHeatmapConfig.cellSize]. */
    data object Circle : CellShape()

    /** Diamond-shaped cell (a square rotated 45°). */
    data object Diamond : CellShape()
}
