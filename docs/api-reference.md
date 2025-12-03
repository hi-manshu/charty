# API Reference

Complete API reference for Charty library.

---

## Core Components

### BarChart

```kotlin
@Composable
fun BarChart(
    dataCollection: List<BarData>,
    modifier: Modifier = Modifier,
    color: Color = ChartyColors.Blue,
    config: BarChartConfig = BarChartConfig(),
    onBarClick: ((BarData) -> Unit)? = null
)
```

### LineChart

```kotlin
@Composable
fun LineChart(
    dataCollection: List<LineData>,
    modifier: Modifier = Modifier,
    color: Color = ChartyColors.Blue,
    config: LineChartConfig = LineChartConfig()
)
```

### PieChart

```kotlin
@Composable
fun PieChart(
    dataCollection: List<PieData>,
    modifier: Modifier = Modifier,
    config: PieChartConfig = PieChartConfig()
)
```

---

## Data Models

### BarData

```kotlin
data class BarData(
    val yValue: Float,
    val xValue: String,
    val color: Color? = null
)
```

### LineData

```kotlin
data class LineData(
    val yValue: Float,
    val xValue: String
)
```

### PieData

```kotlin
data class PieData(
    val value: Float,
    val label: String,
    val color: Color? = null
)
```

---

## Configuration Classes

### BarChartConfig

```kotlin
data class BarChartConfig(
    val barWidth: Dp = 40.dp,
    val showAxis: Boolean = true,
    val showGridLines: Boolean = false,
    val cornerRadius: Dp = 0.dp,
    val spacing: Dp = 8.dp,
    val animated: Boolean = true,
    val animationDuration: Int = 800,
    val axisConfig: AxisConfig = AxisConfig()
)
```

### LineChartConfig

```kotlin
data class LineChartConfig(
    val lineWidth: Dp = 3.dp,
    val showPoints: Boolean = true,
    val pointRadius: Dp = 5.dp,
    val isCurved: Boolean = false,
    val showAxis: Boolean = true,
    val showGridLines: Boolean = false,
    val axisConfig: AxisConfig = AxisConfig()
)
```

### PieChartConfig

```kotlin
data class PieChartConfig(
    val showSliceLabels: Boolean = true,
    val showPercentage: Boolean = true,
    val donut: Boolean = false,
    val donutRadius: Float = 0.6f,
    val animated: Boolean = true,
    val animationDuration: Int = 800
)
```

---

## Common Configurations

### AxisConfig

```kotlin
data class AxisConfig(
    val showAxis: Boolean = true,
    val showAxisLabels: Boolean = true,
    val showGridLines: Boolean = false,
    val axisColor: Color = Color.Gray,
    val axisStroke: Dp = 2.dp
)
```

---

## Colors

### ChartyColors

```kotlin
object ChartyColors {
    val Blue: Color
    val Green: Color
    val Red: Color
    val Yellow: Color
    val Purple: Color
    // ... more colors
}
```

---

For complete source code documentation, visit the [GitHub repository](https://github.com/hi-manshu/charty).

