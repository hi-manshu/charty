# Bar Chart

A standard vertical bar chart for comparing values across categories.

---

## Overview

The Bar Chart is one of the most common chart types, perfect for comparing discrete values across different categories. Each bar represents a single data point, with the height corresponding to the value.

---

## Basic Usage

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.himanshoe.charty.bar.BarChart
import com.himanshoe.charty.bar.data.BarData

@Composable
fun SimpleBarChart() {
    BarChart(
        dataCollection = listOf(
            BarData(yValue = 100f, xValue = "Jan"),
            BarData(yValue = 200f, xValue = "Feb"),
            BarData(yValue = 150f, xValue = "Mar"),
            BarData(yValue = 300f, xValue = "Apr"),
            BarData(yValue = 250f, xValue = "May")
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    )
}
```

---

## Data Model

### BarData

```kotlin
data class BarData(
    val yValue: Float,      // The value (height of the bar)
    val xValue: String,     // The label on X-axis
    val color: Color? = null // Optional: custom color for this bar
)
```

**Example:**
```kotlin
BarData(
    yValue = 100f,
    xValue = "January"
)
```

---

## Configuration

### BarChartConfig

Customize the appearance and behavior of your bar chart:

```kotlin
import com.himanshoe.charty.bar.config.BarChartConfig
import androidx.compose.ui.unit.dp

BarChart(
    dataCollection = data,
    config = BarChartConfig(
        barWidth = 40.dp,           // Width of each bar
        showAxis = true,             // Show X and Y axes
        showGridLines = true,        // Show background grid
        cornerRadius = 8.dp,         // Rounded corners on bars
        spacing = 16.dp              // Space between bars
    )
)
```

### Configuration Options

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `barWidth` | `Dp` | `40.dp` | Width of each bar |
| `showAxis` | `Boolean` | `true` | Show axes |
| `showGridLines` | `Boolean` | `false` | Show grid lines |
| `cornerRadius` | `Dp` | `0.dp` | Corner radius for rounded bars |
| `spacing` | `Dp` | `8.dp` | Space between bars |
| `animated` | `Boolean` | `true` | Enable animation |
| `animationDuration` | `Int` | `800` | Animation duration in ms |

---

## Customization Examples

### Custom Colors

```kotlin
import androidx.compose.ui.graphics.Color

BarChart(
    dataCollection = data,
    color = Color(0xFF6200EE)
)
```

### Per-Bar Colors

```kotlin
BarChart(
    dataCollection = listOf(
        BarData(100f, "Jan", color = Color.Blue),
        BarData(200f, "Feb", color = Color.Green),
        BarData(150f, "Mar", color = Color.Red)
    )
)
```

### Styled Bars

```kotlin
BarChart(
    dataCollection = data,
    color = MaterialTheme.colorScheme.primary,
    config = BarChartConfig(
        barWidth = 50.dp,
        cornerRadius = 12.dp,
        showAxis = true,
        showGridLines = true
    )
)
```

### With Axis Configuration

```kotlin
import com.himanshoe.charty.common.config.AxisConfig

BarChart(
    dataCollection = data,
    config = BarChartConfig(
        barWidth = 40.dp,
        axisConfig = AxisConfig(
            showAxis = true,
            showAxisLabels = true,
            showGridLines = true,
            axisColor = Color.Gray,
            axisStroke = 2.dp
        )
    )
)
```

---

## Advanced Features

### Click Handling

Handle bar clicks to show details or navigate:

```kotlin
var selectedBar by remember { mutableStateOf<BarData?>(null) }

BarChart(
    dataCollection = data,
    onBarClick = { barData ->
        selectedBar = barData
        // Show dialog, navigate, etc.
    }
)

selectedBar?.let { bar ->
    Text("Selected: ${bar.xValue} - ${bar.yValue}")
}
```

### Dynamic Updates

Update chart data dynamically:

```kotlin
var data by remember {
    mutableStateOf(
        listOf(
            BarData(100f, "A"),
            BarData(200f, "B")
        )
    )
}

Column {
    BarChart(dataCollection = data)
    
    Button(onClick = {
        data = data + BarData(Random.nextFloat() * 300, "New")
    }) {
        Text("Add Bar")
    }
}
```

### Negative Values

Handle negative values with custom baseline:

```kotlin
BarChart(
    dataCollection = listOf(
        BarData(100f, "Q1"),
        BarData(-50f, "Q2"),
        BarData(75f, "Q3"),
        BarData(-25f, "Q4")
    ),
    config = BarChartConfig(
        showAxis = true,
        negativeValuesDrawMode = NegativeValuesDrawMode.BelowBaseline
    )
)
```

---

## Best Practices

### 1. Appropriate Bar Count
Don't overcrowd the chart. For mobile, keep it to 5-10 bars for readability:

```kotlin
// Good
BarChart(dataCollection = data.take(8))

// Avoid
BarChart(dataCollection = veryLargeDataset) // 50+ bars
```

### 2. Consistent Sizing
Always specify chart dimensions:

```kotlin
BarChart(
    dataCollection = data,
    modifier = Modifier
        .fillMaxWidth()
        .height(300.dp) // Always specify height
)
```

### 3. Meaningful Labels
Use clear, concise labels:

```kotlin
// Good
BarData(100f, "Jan")

// Avoid
BarData(100f, "January 2025 Sales Data")
```

### 4. Color Accessibility
Ensure sufficient contrast:

```kotlin
val isDark = isSystemInDarkTheme()
val barColor = if (isDark) Color(0xFF90CAF9) else Color(0xFF1976D2)

BarChart(
    dataCollection = data,
    color = barColor
)
```

---

## Common Use Cases

### Sales Data
```kotlin
BarChart(
    dataCollection = listOf(
        BarData(15000f, "Q1"),
        BarData(18000f, "Q2"),
        BarData(22000f, "Q3"),
        BarData(25000f, "Q4")
    ),
    config = BarChartConfig(
        showAxis = true,
        showGridLines = true
    )
)
```

### Comparison Chart
```kotlin
BarChart(
    dataCollection = listOf(
        BarData(85f, "iOS", Color.Blue),
        BarData(78f, "Android", Color.Green),
        BarData(65f, "Web", Color.Red)
    ),
    config = BarChartConfig(barWidth = 60.dp)
)
```

### Survey Results
```kotlin
BarChart(
    dataCollection = listOf(
        BarData(45f, "Excellent"),
        BarData(30f, "Good"),
        BarData(15f, "Fair"),
        BarData(10f, "Poor")
    )
)
```

---

## Related Charts

- [Horizontal Bar Chart](horizontal-bar-chart.md) - Horizontal orientation
- [Stacked Bar Chart](stacked-bar-chart.md) - Multiple series stacked
- [Comparison Bar Chart](comparison-bar-chart.md) - Side-by-side comparison

---

## API Reference

For complete API documentation, see:
- [BarChart Composable](../../api-reference.md#barchart)
- [BarData](../../api-reference.md#bardata)
- [BarChartConfig](../../api-reference.md#barchartconfig)

---

## Examples

For more examples, check out:
- [Basic Usage Examples](../../examples/basic-usage.md)
- [Customization Examples](../../examples/customization.md)
- [Sample App Code](https://github.com/hi-manshu/charty/tree/main/composeApp)

