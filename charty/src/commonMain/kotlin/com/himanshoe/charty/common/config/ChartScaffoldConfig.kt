package com.himanshoe.charty.common.config

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.common.axis.LabelRotation

/**
 * A data class that holds the configuration for the styling of a chart's scaffold.
 *
 * The scaffold includes elements like axis lines, grid lines, and labels.
 *
 * @property showAxis Determines whether the axis lines should be displayed.
 * @property showGrid Determines whether the grid lines should be displayed.
 * @property showLabels Determines whether the axis labels should be displayed.
 * @property axisColor The color of the axis lines.
 * @property leftLabelRotation The rotation for the labels on the left axis. Defaults to [LabelRotation.Straight].
 * @property gridColor The color of the grid lines.
 * @property axisThickness The thickness of the axis lines.
 * @property gridThickness The thickness of the grid lines.
 * @property labelTextStyle The [TextStyle] for the axis labels, allowing for full customization of their appearance.
 */
data class ChartScaffoldConfig(
    val showAxis: Boolean = true,
    val showGrid: Boolean = true,
    val showLabels: Boolean = true,
    val axisColor: Color = Color.Companion.Black,
    val leftLabelRotation: LabelRotation = LabelRotation.Straight,
    val gridColor: Color = Color.Companion.LightGray,
    val axisThickness: Float = 2f,
    val gridThickness: Float = 1f,
    val labelTextStyle: TextStyle = TextStyle(color = Color.Companion.Black, fontSize = 12.sp),
)
