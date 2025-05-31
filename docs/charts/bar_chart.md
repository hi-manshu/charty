# Bar Chart

## Overview
The `BarChart` composable function provides a versatile way to display categorical data using vertical bars. It's designed to be highly customizable and supports a variety of features to enhance data visualization.

Key features include:
- **Positive and Negative Values**: Can render bars for both positive and negative Y-values, with options to draw them from a central axis.
- **Customizable Appearance**: Bar colors, background colors, axis lines, and grid lines can all be configured using `BarChartColorConfig`. The shape of the bars (e.g., curved tops) and other behaviors like minimum bar count can be set via `BarChartConfig`.
- **Tooltips**: Supports displaying tooltips (`BarTooltip`) when a bar is interacted with, providing more detailed information about the data point.
- **Target Lines**: An optional target line can be displayed using the `target` and `targetConfig` parameters, useful for highlighting goals or thresholds.
- **Labels**: X and Y axis labels can be configured using `LabelConfig` for better readability.
- **Click Interactions**: Provides an `onBarClick` callback to handle user interactions with individual bars.

The Bar Chart is ideal for comparing the magnitude of different categories or showing changes over discrete time periods.

## Sample Invocation

```kotlin
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.himanshoe.charty.bar.BarChart
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.config.BarChartColorConfig
import com.himanshoe.charty.bar.model.BarData
import com.himanshoe.charty.common.ChartColorExtensions.asSolidChartColor // Ensure this import path is correct

@Composable
fun SampleBarChart() {
    val barDataList = listOf(
        BarData(xValue = "Jan", yValue = 100f, color = Color.Red.asSolidChartColor()),
        BarData(xValue = "Feb", yValue = 220f, color = Color.Blue.asSolidChartColor()),
        BarData(xValue = "Mar", yValue = -150f, color = Color.Green.asSolidChartColor()), // Example with a negative value
        BarData(xValue = "Apr", yValue = 80f, color = Color.Yellow.asSolidChartColor(), barBackgroundColor = Color.LightGray.copy(alpha = 0.5f).asSolidChartColor()),
        BarData(xValue = "May", yValue = 120f, color = Color.Cyan.asSolidChartColor())
    )

    BarChart(
        data = { barDataList },
        modifier = Modifier
            .height(350.dp)
            .padding(16.dp)
            .fillMaxWidth(),
        barChartConfig = BarChartConfig.default().copy(
            showCurvedBar = true,
            drawNegativeValueChart = true // Enable to properly display negative values from center
        ),
        barChartColorConfig = BarChartColorConfig.default().copy(
            axisLineColor = Color.DarkGray.asSolidChartColor()
        ),
        onBarClick = { index, barData ->
            // Handle bar click, e.g., show a toast or navigate
            println("Clicked on: ${barData.xValue}, Index: $index")
        }
    )
}

@Preview(showBackground = true)
@Composable
fun SampleBarChartPreview() {
    SampleBarChart()
}
```

## Screenshots
![Bar Chart Screenshot](bar_chart_screenshot.png) <!-- TODO: Add actual screenshot -->

## Usage
Key parameters for using this chart:

- **`data`**: A lambda function that returns a list of `BarData` objects. Each `BarData` represents a single bar and contains:
    - `xValue: Any`: The value for the X-axis, typically a `String` label for the category.
    - `yValue: Float`: The numerical value for the Y-axis, determining the bar's height. Can be positive or negative.
    - `color: ChartColor`: The primary color of the bar.
    - `barBackgroundColor: ChartColor`: (Optional) The background color for the bar's track. Defaults to transparent if not specified.
    - `data: Any?`: (Optional) Additional data to associate with this bar, accessible in callbacks.
- **`modifier`**: A `Modifier` for customizing the layout (e.g., size, padding) or drawing behavior of the chart. (Optional)
- **`target`**: An optional `Float` value. If provided, a horizontal line will be drawn at this Y-value on the chart, useful for indicating a goal or threshold. (Optional)
- **`targetConfig`**: A `TargetConfig` object for configuring the appearance of the `target` line (e.g., color, stroke width, path effect). (Optional, defaults to `TargetConfig.default()`)
- **`barChartConfig`**: A `BarChartConfig` object for configuring the chart's appearance and behavior. (Optional, defaults to `BarChartConfig.default()`) Key properties include:
    - `showCurvedBar: Boolean`: Whether to render bars with rounded tops.
    - `drawNegativeValueChart: Boolean`: Set to `true` to enable rendering of negative `yValue`s from a central axis.
    - `minimumBarCount: Int`: Ensures a minimum number of bar slots are drawn, even if data is less.
    - `showAxisLines: Boolean`: Toggles visibility of X and Y axis lines.
    - `showGridLines: Boolean`: Toggles visibility of horizontal grid lines.
- **`labelConfig`**: A `LabelConfig` object for configuring the X and Y axis labels (visibility, color, text style). (Optional, defaults to `LabelConfig.default()`)
- **`barTooltip`**: An optional `BarTooltip` enum (`BarTop` or `GraphTop`) to display a tooltip showing the Y-value when a bar is interacted with. (Optional)
- **`barChartColorConfig`**: A `BarChartColorConfig` object for configuring the colors of various chart elements like axis lines, grid lines, and default bar background color. (Optional, defaults to `BarChartColorConfig.default()`)
- **`onBarClick`**: A lambda function `(Int, BarData) -> Unit` that is invoked when a bar is clicked. It receives the index of the clicked bar and its corresponding `BarData`. (Optional)
