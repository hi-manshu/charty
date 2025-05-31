# Line Stacked Bar Chart

## Overview
The `LineStackedBarChart` offers a unique way to visualize stacked data. It's a variation of a standard stacked bar chart, but instead of wider bars, each stack is rendered as a thin vertical line composed of multiple segments. This "line" appearance is achieved by internally making the bars narrower.

Key characteristics and use cases:
- **Segmented Line Stacks**: Each data point on the X-axis is represented by a single thin vertical line, which is itself segmented to show the constituent parts of a total.
- **Composition Over Categories**: Effectively displays how a total amount is divided into parts for different categories, and how these compositions compare across categories.
- **Visual Style**: The slender "line" style for the stacks can be preferable when dealing with many categories, or when a less "heavy" visual aesthetic is desired compared to traditional stacked bars.
- **Data Structure**: Uses `StackBarData`, where each item defines a complete stack (one vertical line) with its segments, their values, and colors.
- **Configuration**: Shares configuration options for axes, grids, and general behavior with `StackedBarChart` through `StackBarConfig` and `LabelConfig`.

This chart is suitable for showing part-to-whole relationships across multiple categories, with a visually lighter footprint than a standard `StackedBarChart`.

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
import com.himanshoe.charty.bar.LineStackedBarChart // Correct import for LineStackedBarChart
import com.himanshoe.charty.bar.config.StackBarConfig
import com.himanshoe.charty.bar.config.BarChartColorConfig // For axis/grid colors
import com.himanshoe.charty.bar.model.StackBarData
import com.himanshoe.charty.common.ChartColorExtensions.asSolidChartColor // Ensure this import path is correct
import com.himanshoe.charty.common.LabelConfig

@Composable
fun SampleLineStackedBarChart() {
    val dataPoints = listOf(
        StackBarData(
            label = "Region 1",
            values = listOf(50f, 30f, 20f), // Segments: Product A, Product B, Product C
            colors = listOf(
                Color(0xFF4CAF50).asSolidChartColor(), // Green
                Color(0xFF2196F3).asSolidChartColor(), // Blue
                Color(0xFFFFC107).asSolidChartColor()  // Amber
            )
        ),
        StackBarData(
            label = "Region 2",
            values = listOf(40f, 40f, 20f, 10f), // Four segments
            colors = listOf(
                Color(0xFF4CAF50).asSolidChartColor(),
                Color(0xFF2196F3).asSolidChartColor(),
                Color(0xFFFFC107).asSolidChartColor(),
                Color(0xFFF44336).asSolidChartColor() // Red
            )
        ),
        StackBarData(
            label = "Region 3",
            values = listOf(25f, 35f, 40f),
            colors = listOf(
                Color(0xFF4CAF50).asSolidChartColor(),
                Color(0xFF2196F3).asSolidChartColor(),
                Color(0xFFFFC107).asSolidChartColor()
            )
        )
    )

    LineStackedBarChart(
        data = { dataPoints },
        modifier = Modifier
            .height(350.dp)
            .fillMaxWidth()
            .padding(16.dp),
        stackBarConfig = StackBarConfig.default().copy(
            showCurvedBar = false, // Curves are less impactful on very thin lines
            showGridLines = true
        ),
        labelConfig = LabelConfig.default().copy(
            showXLabel = true,
            showYLabel = true
        ),
        barChartColorConfig = BarChartColorConfig.default().copy( // For axis, grid lines
            axisLineColor = Color.DarkGray.asSolidChartColor()
        ),
        onBarClick = { index, stackData ->
            println("Clicked Line Stack: ${stackData.label}, Index: $index")
        }
    )
}

@Preview(showBackground = true)
@Composable
fun SampleLineStackedBarChartPreview() {
    SampleLineStackedBarChart()
}
```

## Screenshots
![Line Stacked Bar Chart Screenshot](line_stack_bar_chart_screenshot.png) <!-- TODO: Add actual screenshot -->

## Usage
Key parameters for using this chart:

- **`data`**: A lambda function that returns a list of `StackBarData`. Each `StackBarData` object represents a full vertical "line" stack and contains:
    - `label: String`: The label for this stack, displayed on the X-axis.
    - `values: List<Float>`: A list of float values, where each value is a segment in the stack. The height of each segment is proportional to its value relative to the total sum of values in this stack.
    - `colors: List<ChartColor>`: A list of `ChartColor` for each corresponding segment in `values`. The size of this list should match the `values` list.
- **`modifier`**: A `Modifier` for customizing the layout (e.g., size, padding) of the chart. (Optional)
- **`target`**: An optional `Float` value. If provided, a horizontal line will be drawn at this Y-value on the chart. (Optional)
- **`targetConfig`**: A `TargetConfig` object for configuring the appearance of the target line. (Optional, defaults to `TargetConfig.default()`)
- **`stackBarConfig`**: A `StackBarConfig` object for configuring chart aspects. (Optional, defaults to `StackBarConfig.default()`) Key properties include:
    - `showCurvedBar: Boolean`: Whether to render the top segment of each stack with a rounded top.
    - `showAxisLines: Boolean`: Toggles visibility of X and Y axis lines.
    - `showGridLines: Boolean`: Toggles visibility of horizontal grid lines.
    - `minimumBarCount: Int`: Ensures a minimum number of stack slots are drawn.
- **`barChartColorConfig`**: A `BarChartColorConfig` object used here primarily for configuring the colors of axis lines and grid lines. The colors for the stack segments themselves are defined within each `StackBarData` item. (Optional, defaults to `BarChartColorConfig.default()`)
- **`labelConfig`**: A `LabelConfig` object for configuring the appearance of X-axis and Y-axis labels. (Optional, defaults to `LabelConfig.default()`)
- **`onBarClick`**: A lambda function `(Int, StackBarData) -> Unit` that is invoked when a "line" stack is clicked. It receives the index of the clicked stack and the corresponding `StackBarData`. (Optional)

The "line" in `LineStackedBarChart` refers to the thin visual appearance of the entire stack, not to a line graph. It's a stylistic choice for presenting stacked bar data.
