# Multi-Line Chart

# Overview
A composable function that renders a chart with multiple lines. This is useful for comparing trends of several datasets over the same continuous interval or time period. Each line has its own color configuration and data points.

# Usage
Key parameters for using this chart:

- **`data`**: A lambda function that returns a list of `MultiLineData`. Each `MultiLineData` object defines a single line on the chart and contains:
    - `data`: A list of `LineData` objects, where each `LineData` has an `xValue` and `yValue` for that point on this specific line.
    - `colorConfig`: A `LineChartColorConfig` for this particular line, specifying its stroke color (`lineColor`) and fill color (`lineFillColor` if `showFilledArea` is true).
- **`modifier`**: An optional `Modifier` for customizing the layout or drawing behavior of the chart. (Optional)
- **`smoothLineCurve`**: A `Boolean` indicating whether the lines should be drawn with smooth curves (cubic Bezier) or as straight segments. (Optional, defaults to `true`)
- **`showFilledArea`**: A `Boolean` indicating whether the area beneath each line should be filled. (Optional, defaults to `false`)
- **`showLineStroke`**: A `Boolean` indicating whether the lines themselves should be drawn (stroked). (Optional, defaults to `true`)
    *Note: At least one of `showFilledArea` or `showLineStroke` must be true.*
- **`target`**: An optional `Float` value. If provided, a single horizontal target line will be drawn across the chart at this Y-value, applicable to all lines. (Optional)
- **`targetConfig`**: A `TargetConfig` object for configuring the appearance of the `target` line. (Optional, defaults to `TargetConfig.default()`)
- **`labelConfig`**: A `LabelConfig` object for configuring the X and Y axis labels (visibility, color, text style). These labels are common for all lines. (Optional, defaults to `LabelConfig.default()`)
- **`chartConfig`**: A `LineChartConfig` object for more advanced configurations common to all lines, such as:
    - `lineConfig`: Settings for showing values directly on the lines.
    - `interactionTooltipConfig`: Configuration for tooltips that can appear on user interaction (e.g., long-press and drag). The tooltip can display values from all lines at the selected X-coordinate.
    (Optional, defaults to `LineChartConfig()`)
- **`onValueChange`**: A lambda function invoked when a tooltip is active (e.g., via long-press and drag). It receives a list of `LineData` objects, one for each line, corresponding to the data points at the current X-axis position of the tooltip. (Optional)

The X-axis values are assumed to be consistent across all lines in the `MultiLineData` list for proper alignment and tooltip functionality.
