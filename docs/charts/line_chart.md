# Line Chart

# Overview
A composable function that renders a line chart. Line charts are used to display quantitative values over a continuous interval or time period. They are ideal for showing trends in data at equal intervals.

# Usage
Key parameters for using this chart:

- **`data`**: A lambda function that returns a list of `LineData`. Each `LineData` object represents a point on the line and should contain:
    - `xValue`: The value for the X-axis (e.g., a timestamp, category index).
    - `yValue`: The value for the Y-axis, which determines the point's vertical position.
- **`modifier`**: An optional `Modifier` for customizing the layout or drawing behavior of the LineChart. (Optional)
- **`smoothLineCurve`**: A `Boolean` indicating whether the line connecting data points should be drawn with smooth curves (cubic Bezier) or as straight segments. (Optional, defaults to `true`)
- **`showFilledArea`**: A `Boolean` indicating whether the area beneath the line should be filled with a color. (Optional, defaults to `false`)
- **`showLineStroke`**: A `Boolean` indicating whether the line itself should be drawn (stroked). (Optional, defaults to `true`)
    *Note: At least one of `showFilledArea` or `showLineStroke` must be true.*
- **`showOnClickBar`**: A `Boolean` that, if true, displays a vertical bar highlighting the selected data point when the chart is clicked. (Optional, defaults to `true`)
- **`colorConfig`**: A `LineChartColorConfig` object for configuring the colors of:
    - `lineColor`: The color of the line stroke.
    - `lineFillColor`: The color of the filled area beneath the line (if `showFilledArea` is true).
    - `selectionBarColor`: The color of the vertical bar shown on click (if `showOnClickBar` is true).
    (Optional, defaults to `LineChartColorConfig.default()`)
- **`labelConfig`**: A `LabelConfig` object for configuring the X and Y axis labels (visibility, color, text style). (Optional, defaults to `LabelConfig.default()`)
- **`target`**: An optional `Float` value. If provided, a horizontal line will be drawn at this Y-value on the chart. (Optional)
- **`targetConfig`**: A `TargetConfig` object for configuring the appearance of the `target` line (e.g., color, stroke width, path effect). (Optional, defaults to `TargetConfig.default()`)
- **`chartConfig`**: A `LineChartConfig` object for more advanced configurations, such as:
    - `lineConfig`: Settings for showing values directly on the line.
    - `interactionTooltipConfig`: Configuration for tooltips that can appear on user interaction (e.g., long-press and drag).
    (Optional, defaults to `LineChartConfig()`)
- **`onClick`**: A lambda function that is invoked when a data point area on the line chart is clicked. It receives the `LineData` corresponding to the clicked point. (Optional)

The chart also supports an alternative interaction mode via a different overload, where a tooltip can be shown by long-pressing and dragging on the chart, configured through `chartConfig.interactionTooltipConfig`.
