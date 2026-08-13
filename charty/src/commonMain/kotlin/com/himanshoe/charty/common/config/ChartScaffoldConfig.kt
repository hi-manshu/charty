package com.himanshoe.charty.common.config

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.axis.LabelRotation

/**
 * A data class that holds the configuration for the styling of a chart's scaffold.
 *
 * The scaffold includes elements like axis lines, grid lines, and labels. Build one from the ambient
 * theme with [com.himanshoe.charty.common.theme.ChartyThemeDefaults.scaffoldConfig].
 *
 * @property showAxis Determines whether the axis lines should be displayed.
 * @property showGrid Determines whether the grid lines should be displayed.
 * @property showLabels Determines whether the axis labels should be displayed.
 * @property axisColor The color of the axis lines, solid or gradient.
 * @property leftLabelRotation The rotation for the labels on the left axis. Defaults to [LabelRotation.Straight].
 * @property gridColor The color of the grid lines, solid or gradient.
 * @property axisThickness The thickness of the axis lines.
 * @property gridThickness The thickness of the grid lines.
 * @property labelTextStyle The [TextStyle] for the axis labels, allowing for full customization of their appearance.
 * @property labelTextColor Color applied to [labelTextStyle] when drawing labels, so a gradient can be
 *   used; `null` keeps the color carried by [labelTextStyle] itself.
 */
@Stable
data class ChartScaffoldConfig(
    val showAxis: Boolean = true,
    val showGrid: Boolean = true,
    val showLabels: Boolean = true,
    val axisColor: ChartyColor = ChartyColor.Solid(Color.Black),
    val leftLabelRotation: LabelRotation = LabelRotation.Straight,
    val gridColor: ChartyColor = ChartyColor.Solid(Color.LightGray),
    val axisThickness: Float = 2f,
    val gridThickness: Float = 1f,
    val labelTextStyle: TextStyle = TextStyle(color = Color.Black, fontSize = 12.sp),
    val labelTextColor: ChartyColor? = null,
) {
    init {
        require(axisThickness >= 0f) { "axisThickness must be non-negative, got: $axisThickness" }
        require(gridThickness >= 0f) { "gridThickness must be non-negative, got: $gridThickness" }
    }
}
