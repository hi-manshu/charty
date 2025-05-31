# Pie Chart

# Overview
A composable function to draw a Pie Chart or a Donut Chart. Pie charts are circular statistical graphics, divided into slices to illustrate numerical proportion. In a pie chart, the arc length of each slice (and consequently its central angle and area) is proportional to the quantity it represents.

# Usage
Key parameters for using this chart:

- **`data`**: A lambda function that returns a list of `PieChartData`. Each `PieChartData` object represents a slice of the pie and should contain:
    - `value`: A `Float` representing the value of this slice. The chart will calculate the proportion of this value to the total sum of all slice values to determine the slice's angle.
    - `label`: A `String` label for this slice. If `isDonutChart` is `false`, this label is drawn on the slice.
    - `color`: A `ChartColor` for the fill of this slice.
    - `labelColor`: A `ChartColor` for the text of the `label`.
- **`modifier`**: An optional `Modifier` for customizing the layout or drawing behavior of the chart, typically used to define its size. (Optional)
- **`isDonutChart`**: A `Boolean` that, if `true`, renders the chart as a donut chart (a pie chart with a hole in the center). If `false` (the default), it renders as a standard, filled pie chart. (Optional, defaults to `false`)
- **`onPieChartSliceClick`**: A lambda function that is invoked when a slice of the pie chart is clicked. It receives the `PieChartData` of the clicked slice. (Optional)

When a slice is clicked, it may slightly scale up for visual feedback. Labels are displayed directly on the slices for standard pie charts but are typically omitted for donut charts in this implementation.
