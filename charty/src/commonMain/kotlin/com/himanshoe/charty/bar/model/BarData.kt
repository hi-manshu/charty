package com.himanshoe.charty.bar.model

import androidx.compose.ui.graphics.Color
import com.himanshoe.charty.common.ChartColor
import com.himanshoe.charty.common.asSolidChartColor

/**
 * Data class representing a single bar in a bar chart.
 *
 * @property yValue The y-axis value of the bar.
 * @property xValue The x-axis value of the bar, can be of any type.
 * @property barColor The color of the bar, defaults to an unspecified color.
 * @property barBackgroundColor The background color of the bar, defaults to a light gray color.
 */
data class BarData(
    val yValue: Float,
    val xValue: Any,
    val barColor: ChartColor = Color.Unspecified.asSolidChartColor(),
    val barBackgroundColor: ChartColor = Color(0x40D3D3D3).asSolidChartColor(),
)
