# Circle Chart

# Overview
A composable function that displays a circle chart. This chart type is often used to visualize progress for one or more data series as concentric rings. Each ring represents a data point, and its arc length corresponds to its value (typically a percentage).

# Usage
Key parameters for using this chart:

- **`data`**: A lambda function that returns a list of `CircleData`. Each `CircleData` object represents a ring in the chart and should contain:
    - `value`: A `Float` representing the percentage (0 to 100) to be filled for this ring.
    - `color`: A `ChartColor` for the filled portion (arc) of the ring.
    - `trackColor`: A `ChartColor` for the background track of the ring.
- **`modifier`**: A `Modifier` for customizing the layout or drawing behavior of the chart, typically used to define its size. (Optional)
- **`circleChartConfig`**: A `CircleChartConfig` object for configuring the chart's appearance. (Optional, defaults to `CircleChartConfig.default()`) This includes:
    - **`showEndIndicator`**: A `Boolean` (default `true`) to display a small circular shadow at the end of the progress arc, giving it a visual endpoint.
    - **`startingPosition`**: A `StartingPosition` enum (e.g., `Top`, `Bottom`, `Left`, `Right`, default `Top`) that determines where the arc for each ring begins.
- **`onCircleClick`**: A lambda function that is invoked when a specific ring in the chart is clicked. It receives the `CircleData` of the clicked ring. (Optional)

When a ring is clicked, it might slightly scale up for visual feedback. The rings are drawn with rounded end caps for the progress arcs.
