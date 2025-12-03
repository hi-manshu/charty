# Configuration

Learn how to configure and customize your charts in Charty.

---

## Configuration Overview

Every chart in Charty can be configured using its respective configuration class. These configuration objects allow you to customize the appearance and behavior of your charts.

---

## Common Configuration Options

### Axis Configuration

Most charts support axis configuration through `AxisConfig`:

```kotlin
import com.himanshoe.charty.common.config.AxisConfig

val axisConfig = AxisConfig(
    showAxis = true,              // Show X and Y axes
    showGridLines = true,         // Show grid lines
    showAxisLabels = true,        // Show axis labels
    axisColor = Color.Gray,       // Color of axes
    axisStroke = 2.dp             // Stroke width of axes
)
```

### Chart Defaults

Access default configurations through `ChartDefaults`:

```kotlin
import com.himanshoe.charty.common.config.ChartDefaults

// Use default colors
val defaultColor = ChartDefaults.color

// Use default axis config
val defaultAxisConfig = ChartDefaults.axisConfig
```

---

## Chart-Specific Configurations

### Bar Chart Configuration

```kotlin
import com.himanshoe.charty.bar.config.BarChartConfig

BarChart(
    dataCollection = data,
    config = BarChartConfig(
        barWidth = 40.dp,          // Width of each bar
        showAxis = true,            // Show axes
        showGridLines = true,       // Show grid lines
        axisConfig = AxisConfig(
            showAxis = true,
            axisColor = Color.Gray
        )
    )
)
```

### Line Chart Configuration

```kotlin
import com.himanshoe.charty.line.config.LineChartConfig

LineChart(
    dataCollection = data,
    config = LineChartConfig(
        lineWidth = 3.dp,          // Width of the line
        showAxis = true,            // Show axes
        showGridLines = true,       // Show grid lines
        showPoints = true,          // Show data points
        pointRadius = 5.dp,         // Size of data points
        isCurved = true,            // Curve the line
        axisConfig = AxisConfig(
            showAxis = true,
            axisColor = Color.Gray
        )
    )
)
```

### Pie Chart Configuration

```kotlin
import com.himanshoe.charty.pie.config.PieChartConfig

PieChart(
    dataCollection = data,
    config = PieChartConfig(
        showSliceLabels = true,     // Show labels on slices
        showPercentage = true,      // Show percentage values
        donut = false,              // Make it a donut chart
        donutRadius = 0.6f,         // Inner radius (if donut)
        animated = true,            // Enable animation
        animationDuration = 800     // Animation duration in ms
    )
)
```

### Radar Chart Configuration

```kotlin
import com.himanshoe.charty.radar.config.RadarChartConfig

RadarChart(
    dataCollection = data,
    config = RadarChartConfig(
        showAxis = true,            // Show axes
        showLabels = true,          // Show labels
        showGridLines = true,       // Show grid lines
        numberOfLevels = 5,         // Number of concentric levels
        strokeWidth = 2.dp          // Width of the radar line
    )
)
```

### Candlestick Chart Configuration

```kotlin
import com.himanshoe.charty.candlestick.config.CandlestickChartConfig

CandlestickChart(
    dataCollection = data,
    config = CandlestickChartConfig(
        candleWidth = 20.dp,        // Width of candles
        showWicks = true,           // Show high/low wicks
        upColor = Color.Green,      // Color for bullish candles
        downColor = Color.Red,      // Color for bearish candles
        showAxis = true,
        showGridLines = true
    )
)
```

---

## Color Customization

### Single Color

Set a single color for the entire chart:

```kotlin
BarChart(
    dataCollection = data,
    color = Color(0xFF6200EE)
)
```

### Custom Color Scheme

Use Charty's color system:

```kotlin
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.ChartyColors

// Use predefined color schemes
BarChart(
    dataCollection = data,
    color = ChartyColors.Blue
)

// Or create custom colors
val customColor = ChartyColor(
    primary = Color(0xFF6200EE),
    secondary = Color(0xFF03DAC5)
)
```

### Multi-Color Charts

For charts with multiple series or slices:

```kotlin
// Pie chart with different colors per slice
PieChart(
    dataCollection = listOf(
        PieData(30f, "A", color = Color.Blue),
        PieData(45f, "B", color = Color.Green),
        PieData(25f, "C", color = Color.Red)
    )
)
```

---

## Animation Configuration

### Enable/Disable Animations

```kotlin
import com.himanshoe.charty.common.animation.AnimationHelper

BarChart(
    dataCollection = data,
    config = BarChartConfig(
        animated = true,             // Enable animation
        animationDuration = 1000     // Duration in milliseconds
    )
)
```

### Custom Animation Specs

```kotlin
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

// Smooth tween animation
val tweenSpec = tween<Float>(durationMillis = 1000)

// Spring animation
val springSpec = spring<Float>(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessLow
)
```

---

## Responsive Charts

### Adaptive Sizing

Use modifiers to make charts responsive:

```kotlin
@Composable
fun ResponsiveChart() {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val chartHeight = maxHeight * 0.4f
        val barWidth = maxWidth / 20
        
        BarChart(
            dataCollection = data,
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight),
            config = BarChartConfig(
                barWidth = barWidth
            )
        )
    }
}
```

### Orientation Support

Handle different screen orientations:

```kotlin
@Composable
fun OrientationAwareChart() {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    BarChart(
        dataCollection = data,
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isLandscape) 400.dp else 300.dp)
    )
}
```

---

## Accessibility

### Content Descriptions

Add content descriptions for accessibility:

```kotlin
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics

BarChart(
    dataCollection = data,
    modifier = Modifier.semantics {
        contentDescription = "Bar chart showing monthly sales data"
    }
)
```

---

## Performance Optimization

### Large Datasets

For large datasets, consider:

1. **Data Sampling**: Show a subset of data points
2. **Lazy Loading**: Load data on demand
3. **Caching**: Cache computed values

```kotlin
@Composable
fun OptimizedChart(largeDataset: List<BarData>) {
    // Sample data for better performance
    val sampledData = remember(largeDataset) {
        if (largeDataset.size > 50) {
            largeDataset.filterIndexed { index, _ -> index % 2 == 0 }
        } else {
            largeDataset
        }
    }
    
    BarChart(dataCollection = sampledData)
}
```

---

## Theme Integration

### Material Theme Colors

Integrate with Material Theme:

```kotlin
import androidx.compose.material3.MaterialTheme

@Composable
fun ThemedChart() {
    BarChart(
        dataCollection = data,
        color = MaterialTheme.colorScheme.primary,
        config = BarChartConfig(
            axisConfig = AxisConfig(
                axisColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        )
    )
}
```

### Dark Mode Support

Handle dark/light themes automatically:

```kotlin
import androidx.compose.foundation.isSystemInDarkTheme

@Composable
fun AdaptiveChart() {
    val isDark = isSystemInDarkTheme()
    val axisColor = if (isDark) Color.White else Color.Black
    
    BarChart(
        dataCollection = data,
        config = BarChartConfig(
            axisConfig = AxisConfig(
                axisColor = axisColor.copy(alpha = 0.5f)
            )
        )
    )
}
```

---

## Advanced Configuration Examples

### Custom Grid Lines

```kotlin
BarChart(
    dataCollection = data,
    config = BarChartConfig(
        showGridLines = true,
        axisConfig = AxisConfig(
            showGridLines = true,
            gridLineColor = Color.Gray.copy(alpha = 0.2f),
            gridLineWidth = 1.dp
        )
    )
)
```

### Combined Configurations

```kotlin
BarChart(
    dataCollection = data,
    color = MaterialTheme.colorScheme.primary,
    modifier = Modifier
        .fillMaxWidth()
        .height(300.dp)
        .padding(16.dp),
    config = BarChartConfig(
        barWidth = 40.dp,
        showAxis = true,
        showGridLines = true,
        animated = true,
        animationDuration = 800,
        axisConfig = AxisConfig(
            showAxis = true,
            showAxisLabels = true,
            showGridLines = true,
            axisColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            axisStroke = 2.dp
        )
    )
)
```

---

## Next Steps

- [Chart Overview](../charts/overview.md) - Explore all available charts
- [Customization Examples](../examples/customization.md) - See real-world examples
- [API Reference](../api-reference.md) - Complete API documentation

---

Need help? [Open an issue on GitHub](https://github.com/hi-manshu/charty/issues)

