# Line Bar Chart

# Overview
A composable function that displays a line bar chart. This chart type typically represents data points as thin vertical lines or "sticks". It can be useful for visualizing discrete data points over a continuous range or time.

# Usage
Key parameters for using this chart:

- **`data`**: A lambda function that returns a list of `BarData`. Each `BarData` object represents a line/bar and includes its X-value (label) and Y-value (height).
- **`modifier`**: A `Modifier` for customizing the layout or drawing behavior of the chart. (Optional)
- **`target`**: An optional target value to be displayed on the chart, usually as a horizontal line. (Optional)
- **`targetConfig`**: A `TargetConfig` object for configuring the appearance of the target line (e.g., color, stroke width). (Optional, defaults to `TargetConfig.default()`)
- **`barChartConfig`**: A `BarChartConfig` object for configuring aspects like showing axis lines, grid lines, curved tops for the lines (though less common for line bar charts), and minimum bar count. (Optional, defaults to `BarChartConfig.default()`)
- **`labelConfig`**: A `LabelConfig` object for configuring the appearance of labels on the chart (e.g., X-axis labels, Y-axis labels). (Optional, defaults to `LabelConfig.default()`)
- **`barChartColorConfig`**: A `BarChartColorConfig` object for configuring the colors of the lines/bars, axis lines, and grid lines. (Optional, defaults to `BarChartColorConfig.default()`)
- **`onBarClick`**: A lambda function that is invoked when a line/bar is clicked. It receives the index of the clicked item and the corresponding `BarData` as parameters. (Optional)
