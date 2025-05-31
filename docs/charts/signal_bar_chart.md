# Signal ProgressBar Chart

## Overview
The `SignalProgressBarChart` composable is designed to visually represent progress levels, akin to a signal strength indicator on a phone, a steps counter reaching a daily goal, or a battery level display. It's not a traditional bar chart for comparing distinct categories but rather a specialized progress indicator.

Key features:
- **Discrete Blocks**: The chart is composed of a series of stacked rectangular blocks.
- **Progressive Fill**: As the `progress` value increases, these blocks fill up from bottom to top.
- **Granular Partial Fill**: The topmost active block (the one currently being filled) can show a partial fill, providing a more fine-grained visual representation of the progress between discrete block levels.
- **Animation**: The chart includes a built-in animation effect when the `progress` value changes, smoothly transitioning the fill level.
- **Customizable**: The number of blocks, colors for the filled and unfilled track portions, and the gap between blocks are all configurable.

This chart is ideal for at-a-glance status updates where a simple, clear visual of completion or level is needed.

## Sample Invocation

```kotlin
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.himanshoe.charty.bar.SignalProgressBarChart // Correct import path
import com.himanshoe.charty.common.ChartColorExtensions.asSolidChartColor // Ensure this import path is correct
import kotlinx.coroutines.delay

@Composable
fun SampleSignalProgressBarChart() {
    var currentProgress by remember { mutableStateOf(0f) }

    // Example of how you might update progress
    LaunchedEffect(Unit) {
        // Simulate progress change from 0% to 75%
        for (i in 0..75 step 5) {
            currentProgress = i.toFloat()
            delay(300)
        }
    }

    SignalProgressBarChart(
        progress = { currentProgress }, // Lambda returning current progress (0f to maxProgress)
        maxProgress = 100f,             // The value 'progress' maps to 100% fill
        modifier = Modifier
            .width(80.dp)   // Adjust width as needed
            .height(180.dp) // Adjust height as needed
            .padding(8.dp),
        totalBlocks = 10,               // Number of segments in the bar
        trackColor = Color.LightGray.asSolidChartColor(), // Color of the unfilled part of blocks
        progressColor = Color(0xFF4CAF50).asSolidChartColor(), // Color of the filled part
        gapRatio = 0.15f                // Gap between blocks, relative to block height
    )
}

// Optional: Preview with controls to test
@Preview(showBackground = true)
@Composable
fun InteractiveSampleSignalProgressBarChartPreview() {
    var progress by remember { mutableStateOf(25f) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Text("Progress: ${progress.toInt()}%")
        SampleSignalProgressBarChart() // You'd pass 'progress' to this if it took it as a direct param
                                     // For this sample, the internal LaunchedEffect drives its own progress.
                                     // To make this preview interactive with external control,
                                     // the SampleSignalProgressBarChart would need to accept progress as a parameter.
        Button(onClick = { progress = (progress + 10f).coerceAtMost(100f) }, modifier = Modifier.padding(top = 8.dp)) {
            Text("Increase Progress")
        }
        Button(onClick = { progress = (progress - 10f).coerceAtLeast(0f) }) {
            Text("Decrease Progress")
        }
    }
}
```

## Screenshots
![Signal Bar Chart Screenshot](signal_bar_chart_screenshot.png) <!-- TODO: Add actual screenshot -->

## Usage
The `SignalProgressBarChart` is available in two overloads, one accepting a `Float` progress and another an `Int` progress.

Key parameters for using this chart:

- **`progress`**: A lambda function that returns the current progress value.
    - For the `Float` version: `() -> Float`. The value typically ranges from `0.0f` to `maxProgress`.
    - For the `Int` version: `() -> Int`. The value typically ranges from `0` to `maxProgress`.
- **`maxProgress`**: The maximum value that `progress` can reach, representing a full bar.
    - For the `Float` version: `Float`, defaults to `100F`.
    - For the `Int` version: `Int`, this parameter is required.
- **`modifier`**: A `Modifier` for customizing the layout (e.g., size, padding) of the chart. (Optional)
- **`totalBlocks`**: An `Int` representing the total number of discrete blocks the signal bar is divided into. For example, a phone signal might have 4-5 blocks. (Optional, defaults to `10`)
- **`trackColor`**: The `ChartColor` for the unfilled portion (the "track") of each block. This color is visible for blocks not yet filled or for the unfilled part of a partially filled block. (Optional, defaults to `Color.Gray`)
- **`progressColor`**: The `ChartColor` for the filled portion (the "progress") of each block. (Optional, defaults to `Color.Green`)
- **`gapRatio`**: A `Float` value that defines the size of the gap between blocks. This ratio is relative to the height of a single block. For example, a `gapRatio` of `0.1F` means the gap will be 10% of a block's height. (Optional, defaults to `0.1F`)
