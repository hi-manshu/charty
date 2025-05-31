# Line Stack Bar Chart

# Overview
A composable function that displays a line stacked bar chart. This chart is a variation of a stacked bar chart where each segment of the stack is represented as a thin line or "stick", and these are stacked vertically. It's used to show how a total is divided into parts and how these parts change over categories.

# Usage
Key parameters for using this chart:

- **`data`**: A lambda function that returns a list of `StackBarData`. Each `StackBarData` object represents a full stack (a single "line" in this context) and contains a label, a list of float values for each segment in the stack, and a list of `ChartColor` for each segment.
- **`modifier`**: A `Modifier` for customizing the layout or drawing behavior of the chart. (Optional)
- **`target`**: An optional target value to be displayed as a horizontal line on the chart. (Optional)
- **`targetConfig`**: A `TargetConfig` object for configuring the appearance of the target line. (Optional, defaults to `TargetConfig.default()`)
- **`stackBarConfig`**: A `StackBarConfig` object for configuring chart aspects like showing axis lines, grid lines, curved tops for the stacks, and a minimum bar count. (Optional, defaults to `StackBarConfig.default()`)
- **`barChartColorConfig`**: A `BarChartColorConfig` object for configuring the colors of the axis lines and grid lines. Note that bar segment colors are defined within `StackBarData`. (Optional, defaults to `BarChartColorConfig.default()`)
- **`labelConfig`**: A `LabelConfig` object for configuring the appearance of labels (e.g., X-axis, Y-axis). (Optional, defaults to `LabelConfig.default()`)
- **`onBarClick`**: A lambda function that is invoked when a stack (a "line") is clicked. It receives the index of the clicked stack and the corresponding `StackBarData`. (Optional)
