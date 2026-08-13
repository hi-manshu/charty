package com.himanshoe.charty.heatmap.config

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.tooltip.ChartTooltip
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.heatmap.data.HeatmapCell
import com.himanshoe.charty.heatmap.internal.formatHeatmapTooltip
import com.himanshoe.charty.heatmap.internal.formatHeatmapValue

private val DEFAULT_SCALE_LOW = Color(0xFFE3F2FD)
private val DEFAULT_SCALE_HIGH = Color(0xFF0D47A1)
private val DEFAULT_EMPTY_CELL_COLOR = Color(0xFFEBEDF0)
private val DEFAULT_LABEL_COLOR = Color(0xFF57606A)
private const val DEFAULT_CELL_SPACING_DP = 2
private const val DEFAULT_CELL_CORNER_RADIUS = 4f
private const val DEFAULT_LABEL_FONT_SIZE_SP = 10

/**
 * Configuration for the appearance and behaviour of
 * [com.himanshoe.charty.heatmap.MatrixHeatmapChart].
 *
 * @property colorScale The low→high value scale. A [ChartyColor.Gradient] uses its stops as the
 *   scale (2-stop, 3-stop, or longer), interpolating between adjacent stops; a [ChartyColor.Solid]
 *   renders a single-hue alpha ramp from faint to fully opaque.
 * @property emptyCellColor Color of grid positions that have no [com.himanshoe.charty.heatmap.data.HeatmapCell];
 *   gradients are flattened to their first stop.
 * @property cellSpacing The gap between adjacent cells, applied both horizontally and vertically.
 * @property cellCornerRadius Corner radius of each cell in pixels; `0f` draws sharp rectangles.
 * @property showValues Whether to draw each cell's formatted value centred inside the cell. Text
 *   automatically switches between dark and light for contrast against the cell color.
 * @property valueFormatter Converts a cell value into the in-cell text used when [showValues] is
 *   enabled. Defaults to integers without decimals and one decimal place otherwise.
 * @property labelTextStyle [TextStyle] applied to row, column, and in-cell value labels. The label
 *   padding around the grid adapts automatically to the measured text size.
 * @property tooltipFormatter Builds the tooltip text for a tapped cell. Defaults to
 *   `row · column: value`.
 * @property tooltipConfig Appearance of the built-in canvas tooltip: shape, colors, padding, and
 *   arrow. Ignored when the chart's `tooltip` is a Compose overlay or [ChartTooltip.none].
 * @property animation Entry animation; cells fade and scale in along a diagonal sweep. See [Animation].
 *
 * Example usage:
 * ```kotlin
 * MatrixHeatmapConfig(
 *     colorScale = ChartyColor.Gradient(
 *         listOf(Color(0xFFFFF3E0), Color(0xFFFF9800), Color(0xFFBF360C)),
 *     ),
 *     showValues = true,
 *     cellCornerRadius = 6f,
 * )
 * ```
 */
@Stable
data class MatrixHeatmapConfig(
    val colorScale: ChartyColor = ChartyColor.Gradient(colors = listOf(DEFAULT_SCALE_LOW, DEFAULT_SCALE_HIGH)),
    val emptyCellColor: ChartyColor = ChartyColor.Solid(color = DEFAULT_EMPTY_CELL_COLOR),
    val cellSpacing: Dp = DEFAULT_CELL_SPACING_DP.dp,
    val cellCornerRadius: Float = DEFAULT_CELL_CORNER_RADIUS,
    val showValues: Boolean = false,
    val valueFormatter: (Float) -> String = ::formatHeatmapValue,
    val labelTextStyle: TextStyle =
        TextStyle(
            fontSize = DEFAULT_LABEL_FONT_SIZE_SP.sp,
            fontWeight = FontWeight.Normal,
            color = DEFAULT_LABEL_COLOR,
        ),
    val tooltipFormatter: (HeatmapCell) -> String = ::formatHeatmapTooltip,
    val tooltipConfig: TooltipConfig? = null,
    val animation: Animation = Animation.Default,
) {
    init {
        require(cellSpacing.value >= 0f) { "cellSpacing must not be negative" }
        require(cellCornerRadius >= 0f) { "cellCornerRadius must not be negative" }
    }
}
