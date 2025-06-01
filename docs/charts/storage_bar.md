🧮 StorageBar

# Overview
A composable function that displays a storage bar. This type of chart is typically used to visualize proportions of a whole, such as disk space usage where different categories (e.g., photos, apps, documents) consume parts of the total storage. The segments are laid out horizontally, and their widths are proportional to their values.

# Usage
Key parameters for using this chart:

- **`data`**: A lambda function that returns a list of `StorageData`. Each `StorageData` object represents a segment in the bar and should contain:
    - `value`: A `Float` representing the proportion or amount of this segment relative to others. The actual width will be calculated based on the sum of all values.
    - `color`: A `ChartColor` for this segment.
- **`trackColor`**: The `ChartColor` for the unfilled portion of the bar, displayed if the sum of the `value` properties in `data` does not fill the entire available width. (Optional, defaults to `Color(0xD3D3D3DE)`)
- **`modifier`**: A `Modifier` for customizing the layout or drawing behavior of the chart, typically used to set its height, as the width is often `fillMaxWidth`. (Optional)
- **`onClick`**: A lambda function that is invoked when a segment of the bar is clicked. It receives the `StorageData` of the clicked segment. (Optional)

The corners of the first and last segments can be rounded. Clicking a segment can also slightly increase its height for emphasis.
