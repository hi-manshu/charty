# Speedometer ProgressBar

# Overview
A composable function that displays a speedometer-style progress bar. It features an arc representing the progress, an optional title and subtitle (displaying the percentage) at its center, and an animated indicator that moves along the arc. This is useful for visualizing a single value within a defined range, like a gauge.

# Usage
Key parameters for using this chart:

- **`progress`**: A lambda function that returns the current progress as a `Float` value, normalized between `0.0` (0%) and `1.0` (100%).
- **`title`**: A `String` for the text to be displayed in the center of the speedometer, above the progress percentage.
- **`color`**: The `ChartColor` for the main progress arc that fills up.
- **`progressIndicatorColor`**: The `ChartColor` for the small circular indicator at the tip of the progress arc.
- **`trackColor`**: The `ChartColor` for the background track of the arc.
- **`modifier`**: A `Modifier` for customizing the layout or drawing behavior of the chart. (Optional)
- **`dotConfig`**: A `DotConfig` object to configure decorative dots along the arc. (Optional, defaults to `DotConfig.default()`)
    - **`showDots`**: `Boolean` to control the visibility of these dots.
    - **`count`**: `Int` specifying the number of dots.
    - **`fillDotColor`**: `ChartColor` for dots that are within the current progress range.
    - **`trackDotColor`**: `ChartColor` for dots that are beyond the current progress range.
- **`titleTextConfig`**: A `TextConfig` object for customizing the appearance of the central `title` text (e.g., font size, color, style, visibility). (Optional, defaults to `TextConfig.default()`)
- **`subTitleTextConfig`**: A `TextConfig` object for customizing the appearance of the subtitle text, which displays the numerical progress percentage (e.g., "75%"). (Optional, defaults to `TextConfig.default(fontSize = 20.sp)`)

The progress animation is handled internally when the `progress` value changes. The arc sweep is typically 270 degrees, starting from the bottom-left and moving clockwise.
