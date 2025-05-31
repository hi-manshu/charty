# Comparison Bar Chart

# Overview
A composable function that displays a comparison bar chart. This chart is useful for comparing multiple series of data side-by-side for different categories.

# Usage
Key parameters for using this chart:

- **`data`**: A lambda function that returns a list of `ComparisonBarData`. Each `ComparisonBarData` object represents a group of bars and contains a label for the group and a list of float values for the bars in that group, along with their respective colors.
- **`modifier`**: A `Modifier` for customizing the layout or drawing behavior of the chart. (Optional)
- **`labelConfig`**: A `LabelConfig` object for configuring the appearance of labels on the chart (e.g., X-axis labels, Y-axis labels). (Optional, defaults to `LabelConfig.default()`)
- **`comparisonBarChartConfig`**: A `ComparisonBarChartConfig` object for configuring the chart's specific appearance and behavior, such as showing axis lines, grid lines, or curved bars. (Optional, defaults to `ComparisonBarChartConfig.default()`)
- **`onGroupClicked`**: A lambda function that is invoked when a group of bars is clicked. It receives the index of the clicked group as a parameter. (Optional)
