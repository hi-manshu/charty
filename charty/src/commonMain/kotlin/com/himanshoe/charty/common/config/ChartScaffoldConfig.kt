package com.himanshoe.charty.common.config

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.himanshoe.charty.common.axis.LabelRotation

/**
 * Configuration for chart scaffold styling.
 *
 * @param showAxis Whether to show axis lines
 * @param showGrid Whether to show grid lines
 * @param showLabels Whether to show axis labels
 * @param axisColor Color of the axis lines
 * @param gridColor Color of the grid lines
 * @param axisThickness Thickness of axis lines
 * @param gridThickness Thickness of grid lines
 * @param labelTextStyle TextStyle for axis labels - allows full customization of text appearance
 * @param leftLabelRotation Rotation for left-side labels. Default is LabelRotation.Straight. Use LabelRotation.Angle45Negative for -45-degree rotation.
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
