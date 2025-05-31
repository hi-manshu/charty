# Bar Chart

# Overview
A composable function that displays a bar chart.

# Usage
Key parameters for using this chart:

- **`data`**: A lambda function that returns a list of `BarData` representing the data points for the bar chart.
- **`modifier`**: A `Modifier` for customizing the layout or drawing behavior of the chart. (Optional)
- **`target`**: An optional target value to be displayed on the chart. (Optional)
- **`targetConfig`**: A `TargetConfig` object for configuring the appearance of the target line. (Optional, defaults to `TargetConfig.default()`)
- **`barChartConfig`**: A `BarChartConfig` object for configuring the chart's appearance and behavior. (Optional, defaults to `BarChartConfig.default()`)
- **`labelConfig`**: A `LabelConfig` object for configuring the labels on the chart. (Optional, defaults to `LabelConfig.default()`)
- **`barTooltip`**: An optional `BarTooltip` to display a tooltip when a bar is clicked. (Optional)
- **`barChartColorConfig`**: A `BarChartColorConfig` object for configuring the colors of the bars, axis lines, and grid lines. (Optional, defaults to `BarChartColorConfig.default()`)
- **`onBarClick`**: A lambda function to handle click events on the bars. It receives the index of the clicked bar and the corresponding `BarData` as parameters. (Optional)
