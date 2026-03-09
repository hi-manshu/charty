package com.himanshoe.charty.calendar.config

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.calendar.data.CalendarData
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.tooltip.TooltipConfig

private val MONTH_ABBREVS = arrayOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
)

internal fun calendarMonthName(month: Int): String =
    if (month in 1..12) MONTH_ABBREVS[month - 1] else ""

/**
 * Controls which day of the week the calendar grid columns start from.
 */
enum class WeekStartDay {
    /** Weeks start on Sunday (GitHub default). */
    SUNDAY,

    /** Weeks start on Monday (ISO 8601 default). */
    MONDAY,
}

/**
 * Configuration for the appearance and behaviour of
 * [com.himanshoe.charty.calendar.CalendarHeatmapChart].
 *
 * @property intensityColors Ordered list of colors representing contribution intensity from
 *   lowest (first) to highest (last). Must contain at least one color.
 * @property emptyColor Color used for days that have no data (value ≤ 0).
 * @property cellShape The visual shape of each cell; see [CellShape].
 * @property cellSize The width and height of each cell.
 * @property cellSpacing The gap between adjacent cells.
 * @property showMonthLabels Whether to render month abbreviation labels above the grid.
 * @property showDayLabels Whether to render Mon/Wed/Fri labels to the left of the grid.
 * @property weekStartDay The day that begins each week column; see [WeekStartDay].
 * @property labelTextStyle [TextStyle] applied to month and day-of-week labels. Increasing
 *   `fontSize` here automatically expands the label padding so the grid never overlaps text.
 * @property animation Entrance animation for the chart; see [Animation].
 * @property tooltipConfig Appearance of the tap-to-show tooltip; see [TooltipConfig].
 * @property tooltipFormatter Converts a tapped [CalendarData] entry into the tooltip string.
 *
 * Example usage:
 * ```kotlin
 * CalendarHeatmapConfig(
 *     intensityColors = listOf(
 *         Color(0xFF9BE9A8), Color(0xFF40C463),
 *         Color(0xFF30A14E), Color(0xFF216E39),
 *     ),
 *     cellShape = CellShape.Circle,
 *     weekStartDay = WeekStartDay.MONDAY,
 * )
 * ```
 */
data class CalendarHeatmapConfig(
    val intensityColors: List<Color> = listOf(
        Color(0xFF9BE9A8),
        Color(0xFF40C463),
        Color(0xFF30A14E),
        Color(0xFF216E39),
    ),
    val emptyColor: Color = Color(0xFFEBEDF0),
    val cellShape: CellShape = CellShape.RoundedSquare(cornerRadius = 2f),
    val cellSize: Dp = 14.dp,
    val cellSpacing: Dp = 2.dp,
    val showMonthLabels: Boolean = true,
    val showDayLabels: Boolean = true,
    val weekStartDay: WeekStartDay = WeekStartDay.SUNDAY,
    val labelTextStyle: TextStyle = TextStyle(
        fontSize = 10.sp,
        fontWeight = FontWeight.Normal,
        color = Color(0xFF57606A),
    ),
    val animation: Animation = Animation.Default,
    val tooltipConfig: TooltipConfig = TooltipConfig(),
    val tooltipFormatter: (CalendarData) -> String = { data ->
        val monthStr = calendarMonthName(data.month)
        "${data.value.toInt()} on $monthStr ${data.day}, ${data.year}"
    },
) {
    init {
        require(intensityColors.isNotEmpty()) { "intensityColors must not be empty" }
    }
}
