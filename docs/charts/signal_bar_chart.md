# Signal Bar Chart

# Overview
A composable function that displays a signal bar chart (often styled as a progress bar). This chart is typically used to represent a level or progress, like signal strength or battery level, using a series of stacked blocks that fill up based on the progress value. The filling of the topmost active block can be partial, representing fine-grained progress.

# Usage
Key parameters for using this chart:

- **`progress`**: A lambda function that returns the current progress value. This can be a `Float` or an `Int`.
- **`maxProgress`**: The maximum value that `progress` can reach. For the `Float` version, this defaults to `100F`. For the `Int` version, this parameter is required.
- **`modifier`**: A `Modifier` for customizing the layout or drawing behavior of the chart. (Optional)
- **`totalBlocks`**: The total number of discrete blocks the signal bar is divided into. (Optional, defaults to `10`)
- **`trackColor`**: The `ChartColor` for the unfilled portion (track) of the blocks. (Optional, defaults to `Color.Gray`)
- **`progressColor`**: The `ChartColor` for the filled portion (progress) of the blocks. (Optional, defaults to `Color.Green`)
- **`gapRatio`**: A `Float` value that defines the size of the gap between blocks, relative to the height of a single block. For example, `0.1F` means the gap is 10% of a block's height. (Optional, defaults to `0.1F`)
