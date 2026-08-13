package com.himanshoe.charty.calendar.config

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.calendar.data.CalendarData
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.tooltip.TooltipConfig

private val MONTH_ABBREVS =
    arrayOf(
        "Jan",
        "Feb",
        "Mar",
        "Apr",
        "May",
        "Jun",
        "Jul",
        "Aug",
        "Sep",
        "Oct",
        "Nov",
        "Dec",
    )

internal fun calendarMonthName(month: Int): String =
    if (month in 1..12) {
        MONTH_ABBREVS[month - 1]
    } else {
        ""
    }

private val DEFAULT_INTENSITY_COLOR_1 = Color(0xFF9BE9A8)
private val DEFAULT_INTENSITY_COLOR_2 = Color(0xFF40C463)
private val DEFAULT_INTENSITY_COLOR_3 = Color(0xFF30A14E)
private val DEFAULT_INTENSITY_COLOR_4 = Color(0xFF216E39)
private val DEFAULT_EMPTY_COLOR = Color(0xFFEBEDF0)
private val DEFAULT_LABEL_COLOR = Color(0xFF57606A)
private const val DEFAULT_CELL_CORNER_RADIUS = 2f
private const val DEFAULT_CELL_SIZE_DP = 14
private const val DEFAULT_CELL_SPACING_DP = 2
private const val DEFAULT_LABEL_FONT_SIZE_SP = 10

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
@Stable
data class CalendarHeatmapConfig(
    val intensityColors: List<Color> =
        listOf(
            DEFAULT_INTENSITY_COLOR_1,
            DEFAULT_INTENSITY_COLOR_2,
            DEFAULT_INTENSITY_COLOR_3,
            DEFAULT_INTENSITY_COLOR_4,
        ),
    val emptyColor: Color = DEFAULT_EMPTY_COLOR,
    val cellShape: CellShape = CellShape.RoundedSquare(cornerRadius = DEFAULT_CELL_CORNER_RADIUS),
    val cellSize: Dp = DEFAULT_CELL_SIZE_DP.dp,
    val cellSpacing: Dp = DEFAULT_CELL_SPACING_DP.dp,
    val showMonthLabels: Boolean = true,
    val showDayLabels: Boolean = true,
    val weekStartDay: WeekStartDay = WeekStartDay.SUNDAY,
    val labelTextStyle: TextStyle =
        TextStyle(
            fontSize = DEFAULT_LABEL_FONT_SIZE_SP.sp,
            fontWeight = FontWeight.Normal,
            color = DEFAULT_LABEL_COLOR,
        ),
    val animation: Animation = Animation.Default,
    val tooltipConfig: TooltipConfig? = null,
    val tooltipFormatter: (CalendarData) -> String = { data ->
        val monthStr = calendarMonthName(data.month)
        "${data.value.toInt()} on $monthStr ${data.day}, ${data.year}"
    },
) {
    init {
        require(intensityColors.isNotEmpty()) { "intensityColors must not be empty" }
    }
}
