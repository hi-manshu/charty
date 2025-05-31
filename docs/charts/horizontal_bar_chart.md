# Horizontal Bar Chart

## Overview
The `HorizontalBarChart` composable function displays data as horizontal bars. This orientation is particularly effective for comparing quantities across different categories, especially when the category labels (defined by `xValue` in `BarData`) are long, as they can be displayed clearly along the vertical axis without truncation or rotation.

Key features include:
- **Ideal for Long Labels**: Provides ample space for category labels.
- **Positive and Negative Values**: Capable of rendering both positive and negative values. Positive bars extend from the Y-axis to the right, and negative bars extend to the left. If both positive and negative values are present, they typically originate from a central vertical axis.
- **Customizable Bar Appearance**: Bar colors, curvature (`showCurvedBar` in `barChartConfig`), and background colors can be configured.
- **Labeling on Bars**: Labels for each bar (derived from `xValue`) can be drawn directly on or next to the bars, with customizable text color, background, and rotation, configured via `horizontalBarLabelConfig`.
- **Axis and Grid Lines**: Supports display of axis lines and vertical grid lines for better value interpretation, configurable through `barChartConfig` and `barChartColorConfig`.
- **Click Interactions**: Allows handling of click events on individual bars through the `onBarClick` callback.

## Sample Invocation

```kotlin
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.himanshoe.charty.bar.HorizontalBarChart
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.config.BarChartColorConfig
import com.himanshoe.charty.bar.config.HorizontalBarLabelConfig
import com.himanshoe.charty.bar.model.BarData
import com.himanshoe.charty.common.ChartColorExtensions.asSolidChartColor // Ensure this import path is correct

@Composable
fun SampleHorizontalBarChart() {
    val dataPoints = listOf(
        BarData(xValue = "Electronics & Gadgets", yValue = 280f, color = Color(0xFFFFA726).asSolidChartColor()),
        BarData(xValue = "Books and Stationery", yValue = 320f, color = Color(0xFF66BB6A).asSolidChartColor()),
        BarData(xValue = "Home Appliances (Returned)", yValue = -150f, color = Color(0xFFEF5350).asSolidChartColor()),
        BarData(xValue = "Fashion & Apparel", yValue = 200f, color = Color(0xFF29B6F6).asSolidChartColor()),
        BarData(xValue = "Groceries", yValue = 180f, color = Color(0xFFAB47BC).asSolidChartColor())
    )

    HorizontalBarChart(
        data = { dataPoints },
        modifier = Modifier
            .height(450.dp) // Height accommodates more bars and longer labels
            .fillMaxWidth()
            .padding(16.dp),
        barChartConfig = BarChartConfig.default().copy(
            showCurvedBar = true,
            showGridLines = true, // Vertical grid lines
            showAxisLines = true  // Y-axis line (vertical)
        ),
        barChartColorConfig = BarChartColorConfig.default().copy(
            gridLineColor = Color.LightGray.copy(alpha = 0.5f).asSolidChartColor()
        ),
        horizontalBarLabelConfig = HorizontalBarLabelConfig.default().copy(
            showLabel = true, // Show labels on bars
            textColor = Color.Black.asSolidChartColor(),
            textBackgroundColors = Color.White.copy(alpha = 0.6f).asSolidChartColor()
        ),
        onBarClick = { barData ->
            println("Clicked: ${barData.xValue} - ${barData.yValue}")
        }
    )
}

@Preview(showBackground = true)
@Composable
fun SampleHorizontalBarChartPreview() {
    SampleHorizontalBarChart()
}
```

## Screenshots
![Horizontal Bar Chart Screenshot](horizontal_bar_chart_screenshot.png) <!-- TODO: Add actual screenshot -->

## Usage
Key parameters for using this chart:

- **`data`**: A lambda function that returns a list of `BarData` objects. Each `BarData` represents a single horizontal bar and contains:
    - `xValue: Any`: The value for the Y-axis label (the category name).
    - `yValue: Float`: The numerical value determining the bar's length. Can be positive (extends right) or negative (extends left).
    - `color: ChartColor`: The primary color of the bar.
    - `barBackgroundColor: ChartColor`: (Optional) Background color for the bar's track.
    - `data: Any?`: (Optional) Additional data associated with the bar.
- **`modifier`**: A `Modifier` for customizing the layout (e.g., size, padding). (Optional)
- **`barChartConfig`**: A `BarChartConfig` object for general chart appearance. (Optional, defaults to `BarChartConfig.default()`)
    - `showCurvedBar: Boolean`: If true, bar ends are rounded.
    - `showAxisLines: Boolean`: Toggles visibility of the main Y-axis line (vertical line from which bars originate).
    - `showGridLines: Boolean`: Toggles visibility of vertical grid lines corresponding to Y-axis values.
    - `minimumBarCount: Int`: Ensures a minimum number of bar slots.
- **`barChartColorConfig`**: A `BarChartColorConfig` object for colors of bars, axis lines, and grid lines. (Optional, defaults to `BarChartColorConfig.default()`)
    - `fillBarColor`: Default color for positive bars if not specified in `BarData`.
    - `negativeBarColors`: Default color for negative bars if not specified in `BarData`.
    - `axisLineColor`: Color for the Y-axis line.
    - `gridLineColor`: Color for the vertical grid lines.
- **`horizontalBarLabelConfig`**: A `HorizontalBarLabelConfig` object for configuring labels displayed on or near the bars. (Optional, defaults to `HorizontalBarLabelConfig.default()`)
    - `showLabel: Boolean`: If `true`, displays the `xValue` as a label on or near its bar.
    - `textColor: ChartColor`: Color of the label text.
    - `textBackgroundColors: ChartColor`: Background color for the label text (can be semi-transparent).
    - `hasOverlappingLabel: Boolean`: Influences label rotation logic. If true, labels are drawn horizontally; otherwise, they might be rotated (e.g., 90 degrees) based on bar orientation and value sign.
- **`onBarClick`**: A lambda function `(BarData) -> Unit` that is invoked when a bar is clicked. It receives the `BarData` of the clicked bar. (Optional)
