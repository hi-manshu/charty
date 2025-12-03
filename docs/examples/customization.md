# Examples - Customization

Learn how to customize Charty charts to match your app's design.

---

## Custom Colors

### Material Theme Integration

```kotlin
@Composable
fun ThemedChart() {
    BarChart(
        dataCollection = data,
        color = MaterialTheme.colorScheme.primary,
        config = BarChartConfig(
            axisConfig = AxisConfig(
                axisColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        )
    )
}
```

### Custom Color Palette

```kotlin
@Composable
fun CustomColorChart() {
    val brandColors = listOf(
        Color(0xFF6200EE),
        Color(0xFF03DAC5),
        Color(0xFFFF6200)
    )
    
    BarChart(
        dataCollection = data.mapIndexed { index, item ->
            item.copy(color = brandColors[index % brandColors.size])
        }
    )
}
```

---

## Styling

### Rounded Corners

```kotlin
BarChart(
    dataCollection = data,
    config = BarChartConfig(
        cornerRadius = 16.dp,
        barWidth = 50.dp
    )
)
```

### Custom Line Styles

```kotlin
LineChart(
    dataCollection = data,
    config = LineChartConfig(
        lineWidth = 4.dp,
        isCurved = true,
        showPoints = true,
        pointRadius = 6.dp
    )
)
```

---

## Dark Mode Support

```kotlin
@Composable
fun AdaptiveChart() {
    val isDark = isSystemInDarkTheme()
    val chartColor = if (isDark) Color(0xFF90CAF9) else Color(0xFF1976D2)
    val axisColor = if (isDark) Color.White else Color.Black
    
    BarChart(
        dataCollection = data,
        color = chartColor,
        config = BarChartConfig(
            axisConfig = AxisConfig(
                axisColor = axisColor.copy(alpha = 0.5f)
            )
        )
    )
}
```

---

## Animations

### Custom Animation Duration

```kotlin
BarChart(
    dataCollection = data,
    config = BarChartConfig(
        animated = true,
        animationDuration = 1200 // milliseconds
    )
)
```

### Disable Animations

```kotlin
BarChart(
    dataCollection = data,
    config = BarChartConfig(
        animated = false
    )
)
```

---

## Responsive Design

### Adaptive Sizing

```kotlin
@Composable
fun ResponsiveChart() {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val chartHeight = maxHeight * 0.4f
        val isWide = maxWidth > 600.dp
        
        BarChart(
            dataCollection = data,
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight),
            config = BarChartConfig(
                barWidth = if (isWide) 50.dp else 30.dp
            )
        )
    }
}
```

---

For more examples, see [Advanced Examples](advanced.md).

