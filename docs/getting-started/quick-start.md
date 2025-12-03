# Quick Start

Learn how to create your first chart with Charty in just a few minutes!

---

## Your First Chart

Let's create a simple bar chart to visualize some data:

```kotlin
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.himanshoe.charty.bar.BarChart
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.bar.config.BarChartConfig

@Composable
fun MyFirstChart() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
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
}
```

That's it! You've created your first chart. 🎉

---

## Understanding the Basics

### Data Models

Every chart in Charty uses a data model. For bar charts, we use `BarData`:

```kotlin
BarData(
    yValue = 100f,  // The value (height of the bar)
    xValue = "Jan"  // The label (X-axis label)
)
```

Different chart types have different data models:

- `LineData` for line charts
- `PieData` for pie charts
- `PointData` for point/bubble charts
- `RadarData` for radar charts
- etc.

### Configuration

Most charts accept a configuration object to customize their appearance:

```kotlin
BarChart(
    dataCollection = myData,
    config = BarChartConfig(
        showAxis = true,
        showGridLines = true,
        barWidth = 40.dp
    )
)
```

### Modifiers

Like all Compose components, charts accept modifiers:

```kotlin
BarChart(
    dataCollection = myData,
    modifier = Modifier
        .fillMaxWidth()
        .height(300.dp)
        .padding(16.dp)
)
```

---

## Common Patterns

### Loading State

Handle loading states with conditional rendering:

```kotlin
@Composable
fun ChartWithLoading(data: List<BarData>?) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            data == null -> CircularProgressIndicator()
            data.isEmpty() -> Text("No data available")
            else -> BarChart(dataCollection = data)
        }
    }
}
```

### Dynamic Data

Update charts with state:

```kotlin
@Composable
fun DynamicChart() {
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
            data = data + BarData(150f, "C")
        }) {
            Text("Add Data")
        }
    }
}
```

### Custom Colors

Customize colors for better branding:

```kotlin
import com.himanshoe.charty.common.config.AxisConfig
import com.himanshoe.charty.common.config.ChartDefaults

BarChart(
    dataCollection = myData,
    color = Color(0xFF6200EE),  // Primary color
    config = BarChartConfig(
        axisConfig = AxisConfig(
            showAxis = true,
            showGridLines = true,
            axisColor = Color.Gray
        )
    )
)
```

---

## More Chart Types

### Line Chart

```kotlin
import com.himanshoe.charty.line.LineChart
import com.himanshoe.charty.line.data.LineData

LineChart(
    dataCollection = listOf(
        LineData(10f, "Jan"),
        LineData(20f, "Feb"),
        LineData(15f, "Mar")
    )
)
```

### Pie Chart

```kotlin
import com.himanshoe.charty.pie.PieChart
import com.himanshoe.charty.pie.data.PieData

PieChart(
    dataCollection = listOf(
        PieData(30f, "Category A"),
        PieData(45f, "Category B"),
        PieData(25f, "Category C")
    )
)
```

### Point Chart (Scatter Plot)

```kotlin
import com.himanshoe.charty.point.PointChart
import com.himanshoe.charty.point.data.PointData

PointChart(
    dataCollection = listOf(
        PointData(10f, 20f),
        PointData(15f, 35f),
        PointData(20f, 25f)
    )
)
```

---

## Best Practices

### 1. Size Your Charts

Always specify chart dimensions:

```kotlin
BarChart(
    dataCollection = data,
    modifier = Modifier
        .fillMaxWidth()
        .height(300.dp)  // Specify height
)
```

### 2. Handle Empty Data

Check for empty data before rendering:

```kotlin
if (data.isNotEmpty()) {
    BarChart(dataCollection = data)
} else {
    Text("No data to display")
}
```

### 3. Use Appropriate Chart Types

Choose the right chart for your data:

- **Bar/Column**: Comparing discrete values
- **Line**: Showing trends over time
- **Pie**: Showing proportions of a whole
- **Scatter**: Showing relationships between variables
- **Radar**: Comparing multiple variables

### 4. Keep It Simple

Don't overcomplicate your charts:

```kotlin
// Good - Clear and simple
BarChart(
    dataCollection = data,
    config = BarChartConfig(showAxis = true)
)

// Avoid - Too many customizations can be overwhelming
```

---

## Next Steps

Now that you know the basics, explore more:

- [Configuration Guide](configuration.md) - Deep dive into chart configuration
- [Chart Overview](../charts/overview.md) - Explore all available charts
- [Customization Examples](../examples/customization.md) - Learn advanced customization
- [API Reference](../api-reference.md) - Complete API documentation

---

## Examples Repository

Check out the sample app in the repository for more examples:

[View Sample Code on GitHub →](https://github.com/hi-manshu/charty/tree/main/composeApp)

---

Need help? [Ask a question on GitHub Discussions](https://github.com/hi-manshu/charty/discussions)

