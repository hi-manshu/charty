# Line Bar Chart

## Overview
The `LineBarChart` composable offers a distinct visual style for representing categorical data, rendering data points as thin vertical lines or "sticks" rather than traditional wider bars. This presentation is achieved by internally adjusting the bar width (typically dividing the available space by 3).

Key characteristics and use cases:
- **Stylistic Variation**: While it uses the same `BarData` structure and shares many configuration options (`BarChartConfig`, `BarChartColorConfig`, `LabelConfig`) with the standard `BarChart`, its visual output is slender lines.
- **Discrete Data Points**: Ideal for visualizing discrete data points where the emphasis is on the magnitude at each specific point along the X-axis.
- **High-Density Data**: Useful when dealing with a large number of data points where traditional bars might appear cluttered. The thinner lines allow for a cleaner representation.
- **Features**: Supports features like positive/negative values (drawn from a central axis if mixed), target lines, axis/grid customization, and click interactions (`onBarClick`).

Think of it as a bar chart that has been stylistically modified to appear as lines, making it suitable for scenarios where a less "heavy" visual representation is preferred.

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
import com.himanshoe.charty.bar.LineBarChart
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.config.BarChartColorConfig
import com.himanshoe.charty.bar.model.BarData
import com.himanshoe.charty.common.ChartColorExtensions.asSolidChartColor // Ensure this import path is correct
import com.himanshoe.charty.common.LabelConfig

@Composable
fun SampleLineBarChart() {
    val lineBarDataPoints = listOf(
        BarData(xValue = "Jan", yValue = 180f, color = Color.Red.asSolidChartColor()),
        BarData(xValue = "Feb", yValue = -120f, color = Color.Blue.asSolidChartColor()), // Negative value example
        BarData(xValue = "Mar", yValue = 220f, color = Color.Green.asSolidChartColor()),
        BarData(xValue = "Apr", yValue = 150f, color = Color.Magenta.asSolidChartColor()),
        BarData(xValue = "May", yValue = 250f, color = Color.Yellow.asSolidChartColor()),
        BarData(xValue = "Jun", yValue = -90f, color = Color.Cyan.asSolidChartColor())
    )

    LineBarChart(
        data = { lineBarDataPoints },
        modifier = Modifier
            .height(300.dp)
            .fillMaxWidth()
            .padding(16.dp),
        barChartConfig = BarChartConfig.default().copy(
            showCurvedBar = false, // Curves might not be prominent on thin lines but can be enabled
            drawNegativeValueChart = true, // Important for handling negative values correctly
            showGridLines = true
        ),
        labelConfig = LabelConfig.default().copy(
            showXLabel = true,
            showYLabel = true
        ),
        barChartColorConfig = BarChartColorConfig.default().copy(
            // You can set a default fill color here, or rely on colors in BarData
            // fillBarColor = Color.DarkGray.asSolidChartColor(),
            axisLineColor = Color.Gray.asSolidChartColor()
        ),
        onBarClick = { index, barData ->
            println("Clicked LineBar: ${barData.xValue} (${barData.yValue}), Index: $index")
        }
    )
}

@Preview(showBackground = true)
@Composable
fun SampleLineBarChartPreview() {
    SampleLineBarChart()
}
```

## Screenshots
![Line Bar Chart Screenshot](line_bar_chart_screenshot.png) <!-- TODO: Add actual screenshot -->

## Usage
The `LineBarChart` utilizes the same data structures and configuration objects as the standard `BarChart`. Refer to the `BarChart` documentation for detailed explanations of these parameters. Below is a summary:

- **`data`**: A lambda function returning a list of `BarData`. Each `BarData` defines a line's X-position, Y-height, and color.
    - `xValue: Any`: Category label for the X-axis.
    - `yValue: Float`: Numerical value for the Y-axis (height of the line).
    - `color: ChartColor`: Color of the individual line.
    - `barBackgroundColor: ChartColor`: (Optional) Background color for the line's track.
- **`modifier`**: `Modifier` for layout customization. (Optional)
- **`target`**: Optional `Float` for displaying a horizontal target line. (Optional)
- **`targetConfig`**: `TargetConfig` for the target line's appearance. (Optional, defaults to `TargetConfig.default()`)
- **`barChartConfig`**: `BarChartConfig` for general chart settings. (Optional, defaults to `BarChartConfig.default()`)
    - `showCurvedBar: Boolean`: Applies rounding to the top of the thin lines.
    - `drawNegativeValueChart: Boolean`: Enables rendering of negative values from a central axis.
    - `showAxisLines: Boolean`, `showGridLines: Boolean`: Control visibility of axis and grid lines.
- **`labelConfig`**: `LabelConfig` for X and Y axis labels. (Optional, defaults to `LabelConfig.default()`)
- **`barChartColorConfig`**: `BarChartColorConfig` for default colors of lines, axis, and grid. Individual line colors in `BarData` take precedence. (Optional, defaults to `BarChartColorConfig.default()`)
- **`onBarClick`**: Lambda `(Int, BarData) -> Unit` invoked when a line is clicked. (Optional)

The primary distinction from `BarChart` is the visual rendering of data points as thin lines due to an internal adjustment of bar width.
