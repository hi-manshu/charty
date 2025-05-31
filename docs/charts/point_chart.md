# Point Chart (Scatter Plot)

# Overview
A composable function to display a point chart, also commonly known as a scatter plot. Scatter plots are used to display values for typically two variables for a set of data. Each data entry is represented as a point whose position is determined by its X and Y values.

# Usage
Key parameters for using this chart:

- **`data`**: A lambda function that returns a list of `PointData`. Each `PointData` object represents a single point on the chart and should contain:
    - `xValue`: The value for the X-axis (can be categorical, though typically points are distributed along this axis based on their index if not otherwise specified by drawing logic).
    - `yValue`: The value for the Y-axis, which determines the point's vertical position.
- **`modifier`**: An optional `Modifier` for customizing the layout or drawing behavior of the chart. (Optional)
- **`labelConfig`**: A `LabelConfig` object for configuring the X and Y axis labels (visibility, color, text style). (Optional, defaults to `LabelConfig.default()`)
- **`colorConfig`**: A `PointChartColorConfig` object for configuring the colors of various chart elements. (Optional, defaults to `PointChartColorConfig.default()`) This includes:
    - `circleColor`: `ChartColor` for the fill of the data points.
    - `strokeColor`: `ChartColor` for the outline (stroke) of the data points.
    - `selectionBarColor`: `ChartColor` for the vertical bar that can be shown when a point is clicked (if `chartConfig.showClickedBar` is true).
    - `axisColor`: `ChartColor` for the X and Y axis lines.
    - `gridColor`: `ChartColor` for the grid lines.
- **`chartConfig`**: A `PointChartConfig` object for configuring the appearance and behavior of the points and chart interactions. (Optional, defaults to `PointChartConfig()`) This includes:
    - `circleRadius`: The radius of each data point.
    - `showClickedBar`: A `Boolean` to enable/disable the display of a vertical bar highlighting the column of a clicked point.
    - `animatePoints`: A `Boolean` to enable an animation for the points when the chart is first displayed.
    - `animationDurationMillis`: Duration of the point animation in milliseconds.
    - `animationEasing`: The easing function for the point animation.
- **`target`**: An optional `Float` value. If provided, a horizontal line will be drawn at this Y-value on the chart. (Optional)
- **`targetConfig`**: A `TargetConfig` object for configuring the appearance of the `target` line (e.g., color, stroke width, path effect). (Optional, defaults to `TargetConfig.default()`)
- **`onPointClick`**: A lambda function that is invoked when the region corresponding to a data point is clicked. It receives the index of the clicked point and its `PointData`. (Optional)

Points can be animated on initial display. Clicking a point can also highlight it by increasing its radius and showing a vertical selection bar.
