# Horizontal Bar Chart

# Overview
A composable function that displays a horizontal bar chart. This chart is suitable for comparing quantities of different categories, especially when category names are long. It can also display negative values.

# Usage
Key parameters for using this chart:

- **`data`**: A lambda function that returns a list of `BarData` objects. Each `BarData` represents a bar and includes its X-value (label) and Y-value (length), and optionally its color.
- **`modifier`**: A `Modifier` for customizing the layout or drawing behavior of the chart. (Optional)
- **`barChartConfig`**: A `BarChartConfig` object for configuring general chart appearance and behavior, such as showing axis lines, grid lines, curved bars, and setting a minimum bar count. (Optional, defaults to `BarChartConfig.default()`)
- **`barChartColorConfig`**: A `BarChartColorConfig` object for configuring the colors of the bars (fill, negative, background), axis lines, and grid lines. (Optional, defaults to `BarChartColorConfig.default()`)
- **`horizontalBarLabelConfig`**: A `HorizontalBarLabelConfig` object for configuring the appearance of labels displayed on or near the bars, including text color, background color, and handling of overlapping labels. (Optional, defaults to `HorizontalBarLabelConfig.default()`)
- **`onBarClick`**: A lambda function that is invoked when a bar is clicked. It receives the `BarData` of the clicked bar as a parameter. (Optional)
