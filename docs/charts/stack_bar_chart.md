# Stack Bar Chart

# Overview
A composable function that displays a stacked bar chart. This type of chart is used to show how a larger category is divided into smaller sub-categories and what the proportion of each sub-category is to the total. Each bar represents a total, and segments within the bar represent different parts of that total.

# Usage
Key parameters for using this chart:

- **`data`**: A lambda function that returns a list of `StackBarData`. Each `StackBarData` object represents a single bar and contains:
    - `label`: A `String` for the X-axis label of the bar.
    - `values`: A list of `Float` values, where each value is a segment in the stack.
    - `colors`: A list of `ChartColor` for each corresponding segment in `values`.
- **`modifier`**: A `Modifier` for customizing the layout or drawing behavior of the chart. (Optional)
- **`target`**: An optional `Float` value to be displayed as a horizontal target line on the chart. (Optional)
- **`targetConfig`**: A `TargetConfig` object for configuring the appearance of the target line (e.g., color, stroke width, path effect). (Optional, defaults to `TargetConfig.default()`)
- **`stackBarConfig`**: A `StackBarConfig` object for configuring chart-specific aspects like showing axis lines, grid lines, whether to curve the top of the bars, and a minimum bar count. (Optional, defaults to `StackBarConfig.default()`)
- **`barChartColorConfig`**: A `BarChartColorConfig` object primarily used here for configuring the background color of the bars, axis line colors, and grid line colors. The actual segment colors are defined within each `StackBarData` item. (Optional, defaults to `BarChartColorConfig.default()`)
- **`labelConfig`**: A `LabelConfig` object for configuring the appearance of X-axis and Y-axis labels. (Optional, defaults to `LabelConfig.default()`)
- **`onBarClick`**: A lambda function that is invoked when a bar (a full stack) is clicked. It receives the index of the clicked bar and the corresponding `StackBarData`. (Optional)
