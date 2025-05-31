# Comparison Bar Chart

## Overview
The `ComparisonBarChart` composable function is designed to display grouped bar charts. It excels at comparing multiple series of data side-by-side across different categories (groups). For example, you could use it to compare the sales figures of different products (multiple bars within a group) across several quarters (each quarter being a group).

Key features include:
- **Grouped Data Representation**: Each primary category (group) on the X-axis can contain multiple bars, each representing a sub-category or series.
- **Variable Bar Count per Group**: Different groups can have a varying number of bars, allowing for flexible data representation.
- **Individual Bar Styling**: Each bar within a group can have its own distinct color, defined in the `ComparisonBarData`.
- **Customizable Appearance**: Chart aesthetics like axis lines, grid lines, and bar curvature can be configured using `ComparisonBarChartConfig`.
- **Labeling**: X and Y axis labels can be configured for clarity using `LabelConfig`.
- **Click Interactions**: Supports click events on entire groups of bars via the `onGroupClicked` callback.

This chart is particularly useful when you need to highlight both individual values and their collective performance within and across groups.

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
import com.himanshoe.charty.bar.ComparisonBarChart
import com.himanshoe.charty.bar.config.ComparisonBarChartConfig
import com.himanshoe.charty.bar.model.ComparisonBarData
import com.himanshoe.charty.common.ChartColor // Assuming ChartColor is directly usable or this is a typealias
import com.himanshoe.charty.common.ChartColorExtensions.asSolidChartColor // Ensure this import path is correct

@Composable
fun SampleComparisonBarChart() {
    val comparisonData = listOf(
        ComparisonBarData(
            label = "Product A", // Category group
            bars = listOf(100f, 150f, 120f), // Values for Series 1, Series 2, Series 3
            colors = listOf(
                Color.Red.asSolidChartColor(),
                Color.Green.asSolidChartColor(),
                Color.Blue.asSolidChartColor()
            )
        ),
        ComparisonBarData(
            label = "Product B",
            bars = listOf(130f, 110f), // Only two series for this product
            colors = listOf(
                Color.Red.asSolidChartColor(),
                Color.Green.asSolidChartColor()
            )
        ),
        ComparisonBarData(
            label = "Product C",
            bars = listOf(80f, 160f, 90f, 140f), // Four series
            colors = listOf(
                Color.Red.asSolidChartColor(),
                Color.Green.asSolidChartColor(),
                Color.Blue.asSolidChartColor(),
                Color.Magenta.asSolidChartColor()
            )
        )
    )

    ComparisonBarChart(
        data = { comparisonData },
        modifier = Modifier
            .height(350.dp)
            .padding(16.dp)
            .fillMaxWidth(),
        comparisonBarChartConfig = ComparisonBarChartConfig.default().copy(
            showCurvedBar = true,
            showGridLines = true
        )
        // onGroupClicked = { index -> println("Group $index clicked") }
    )
}

@Preview(showBackground = true)
@Composable
fun SampleComparisonBarChartPreview() {
    SampleComparisonBarChart()
}
```

## Screenshots
![Comparison Bar Chart Screenshot](comparison_bar_chart_screenshot.png) <!-- TODO: Add actual screenshot -->

## Usage
Key parameters for using this chart:

- **`data`**: A lambda function that returns a list of `ComparisonBarData`. Each `ComparisonBarData` object represents a group of bars on the X-axis and contains:
    - `label: String`: The label for this group, displayed on the X-axis.
    - `bars: List<Float>`: A list of float values, where each value corresponds to the height of a bar within this group.
    - `colors: List<ChartColor>`: A list of `ChartColor` objects, where each color corresponds to a bar in the `bars` list. The size of `colors` should match the size of `bars`.
- **`modifier`**: A `Modifier` for customizing the layout (e.g., size, padding) or drawing behavior of the chart. (Optional)
- **`labelConfig`**: A `LabelConfig` object for configuring the appearance of labels on the chart, such as X-axis group labels and Y-axis value labels. (Optional, defaults to `LabelConfig.default()`)
- **`comparisonBarChartConfig`**: A `ComparisonBarChartConfig` object for configuring the chart's specific appearance and behavior. (Optional, defaults to `ComparisonBarChartConfig.default()`) Key properties include:
    - `showAxisLines: Boolean`: Toggles visibility of X and Y axis lines.
    - `showGridLines: Boolean`: Toggles visibility of horizontal grid lines.
    - `showCurvedBar: Boolean`: Whether to render bars with rounded tops.
- **`onGroupClicked`**: A lambda function `(Int) -> Unit` that is invoked when a group of bars (representing one `ComparisonBarData` item) is clicked. It receives the index of the clicked group. (Optional)
