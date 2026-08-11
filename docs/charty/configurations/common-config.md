# Common Configuration

Every Cartesian chart in Charty shares a set of configuration objects that control the scaffold (axes, grid, labels), reference lines, tooltips, corner radii, and data labels. This page documents each of these shared objects and shows how to customise them.

---

## ChartScaffoldConfig

`ChartScaffoldConfig` controls the visual chrome that surrounds every Cartesian chart: axes, grid lines, and axis labels.

```kotlin
data class ChartScaffoldConfig(
    val showAxis: Boolean = true,
    val showGrid: Boolean = true,
    val showLabels: Boolean = true,
    val axisColor: Color = Color.Black,
    val gridColor: Color = Color.LightGray,
    val axisThickness: Float = 2f,
    val gridThickness: Float = 1f,
    val labelTextStyle: TextStyle = TextStyle(color = Color.Black, fontSize = 12.sp),
    val leftLabelRotation: LabelRotation = LabelRotation.Straight,
)
```

| Property | Default | Description |
|---|---|---|
| `showAxis` | `true` | Whether to draw the x-axis and y-axis lines. |
| `showGrid` | `true` | Whether to draw horizontal grid lines. |
| `showLabels` | `true` | Whether to draw value labels along the axes. |
| `axisColor` | `Color.Black` | Color of both axis lines. |
| `gridColor` | `Color.LightGray` | Color of grid lines. |
| `axisThickness` | `2f` | Stroke width of axis lines in pixels. |
| `gridThickness` | `1f` | Stroke width of grid lines in pixels. |
| `labelTextStyle` | 12 sp black | Text style applied to axis value labels. |
| `leftLabelRotation` | `Straight` | `LabelRotation.Straight` renders labels horizontally; `LabelRotation.Rotated` renders them at 45°. |

### Example: hide the grid, custom axis color, rotated labels

```kotlin
BarChart(
    data = barData,
    barConfig = BarChartConfig(
        // bar-specific settings here
    ),
    scaffoldConfig = ChartScaffoldConfig(
        showGrid = false,
        axisColor = Color(0xFF6200EE),
        axisThickness = 3f,
        leftLabelRotation = LabelRotation.Rotated,
        labelTextStyle = TextStyle(
            color = Color(0xFF6200EE),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
        ),
    ),
)
```

---

## ReferenceLineConfig

A reference line is a horizontal line drawn at a fixed y-value across the chart area. It is useful for marking targets, thresholds, or averages.

```kotlin
data class ReferenceLineConfig(
    val value: Float,
    val color: Color = Color.Red,
    val strokeWidth: Float = 2f,
    val label: String? = null,
    val dashPattern: FloatArray? = null,
)
```

| Property | Default | Description |
|---|---|---|
| `value` | required | The y-axis value at which to draw the line. |
| `color` | `Color.Red` | Color of the reference line. |
| `strokeWidth` | `2f` | Stroke width of the line in pixels. |
| `label` | `null` | Optional text rendered beside the line. |
| `dashPattern` | `null` (solid) | A `FloatArray` of alternating dash/gap lengths, e.g. `floatArrayOf(10f, 5f)`. Pass `null` for a solid line. |

**Supported charts:** BarChart, HorizontalBarChart, StackedBarChart, StackedHorizontalBarChart, GroupedHorizontalBarChart, LineChart, AreaChart, PointChart, ComboChart.

### Example: dashed target line with label

```kotlin
BarChart(
    data = barData,
    barConfig = BarChartConfig(
        referenceLine = ReferenceLineConfig(
            value = 100f,
            color = Color(0xFF388E3C),
            strokeWidth = 2f,
            label = "Target",
            dashPattern = floatArrayOf(10f, 5f),
        )
    ),
)
```

For a line chart:

```kotlin
LineChart(
    data = lineData,
    lineConfig = LineChartConfig(
        referenceLine = ReferenceLineConfig(
            value = 50f,
            label = "Baseline",
            dashPattern = floatArrayOf(8f, 4f),
        )
    ),
)
```

---

## TooltipConfig and tooltipFormatter

When a click handler is supplied to a chart, a tooltip is automatically displayed near the tapped point. `TooltipConfig` controls the tooltip appearance and `tooltipFormatter` controls the text.

### TooltipConfig

```kotlin
data class TooltipConfig(
    val backgroundColor: Color = Color.Black.copy(alpha = 0.8f),
    val textColor: Color = Color.White,
    val cornerRadius: Float = 8f,
    val padding: Float = 8f,
    val textStyle: TextStyle = TextStyle(fontSize = 12.sp),
)
```

| Property | Default | Description |
|---|---|---|
| `backgroundColor` | Semi-transparent black | Background fill of the tooltip bubble. |
| `textColor` | `Color.White` | Color of the text inside the tooltip. |
| `cornerRadius` | `8f` | Corner radius of the tooltip bubble in pixels. |
| `padding` | `8f` | Internal padding around the text in pixels. |
| `textStyle` | 12 sp | `TextStyle` applied to the tooltip text. |

### tooltipFormatter

Pass a lambda to format the tooltip text from the chart's data object. Each chart type passes the relevant data class to the formatter.

```kotlin
BarChart(
    data = barData,
    barConfig = BarChartConfig(
        tooltipConfig = TooltipConfig(
            backgroundColor = Color(0xFF212121),
            textColor = Color.White,
            cornerRadius = 12f,
        ),
        tooltipFormatter = { barData ->
            "Sales: ${"%.1f".format(barData.value)} units"
        },
    ),
    onBarClick = { clickedBar ->
        // handle click
    },
)
```

For a line chart:

```kotlin
LineChart(
    data = lineData,
    lineConfig = LineChartConfig(
        tooltipConfig = TooltipConfig(
            textStyle = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold),
        ),
        tooltipFormatter = { point ->
            "${point.label}: ${point.value}"
        },
    ),
    onPointClick = { point -> },
)
```

> Tooltips are only displayed when a click handler (`onBarClick`, `onPointClick`, etc.) is provided. If no click handler is set, `TooltipConfig` has no effect.

---

## CornerRadius

`CornerRadius` is a sealed class that controls the rounding applied to bar edges. It is available in bar chart configs that expose a corner radius option.

```kotlin
sealed class CornerRadius(val value: Float) {
    data object None       : CornerRadius(0f)
    data object Small      : CornerRadius(4f)
    data object Medium     : CornerRadius(8f)
    data object Large      : CornerRadius(12f)
    data object ExtraLarge : CornerRadius(16f)
    data class  Custom(val radius: Float) : CornerRadius(radius)
}
```

| Value | Approximate pixel radius |
|---|---|
| `None` | 0 |
| `Small` | 4 |
| `Medium` | 8 |
| `Large` | 12 |
| `ExtraLarge` | 16 |
| `Custom(radius)` | Exact value you provide |

### Example

```kotlin
BarChart(
    data = barData,
    barConfig = BarChartConfig(
        cornerRadius = CornerRadius.Medium,
    ),
)

// Custom radius
BarChart(
    data = barData,
    barConfig = BarChartConfig(
        cornerRadius = CornerRadius.Custom(12f),
    ),
)
```

---

## Data Labels

Data labels render the numeric value of each bar directly on or above the bar. They are available for `BarChart`, `HorizontalBarChart`, and `StackedBarChart`.

The relevant properties live inside the respective chart config:

| Property | Type | Default | Description |
|---|---|---|---|
| `showDataLabels` | `Boolean` | `false` | Whether to draw labels on each bar. |
| `dataLabelFormatter` | `((BarData) -> String)?` | `null` | Lambda to convert a data point to a display string. Defaults to the raw value when `null`. |
| `dataLabelStyle` | `TextStyle` | 10 sp black | Text style applied to each data label. |

### Example

```kotlin
BarChart(
    data = barData,
    barConfig = BarChartConfig(
        showDataLabels = true,
        dataLabelFormatter = { barData ->
            "%.1f".format(barData.value)
        },
        dataLabelStyle = TextStyle(
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray,
        ),
    ),
)
```

For `HorizontalBarChart`, the same three properties are available on `BarChartConfig`:

```kotlin
HorizontalBarChart(
    data = barData,
    barConfig = BarChartConfig(
        showDataLabels = true,
        dataLabelFormatter = { barData -> "${barData.value.toInt()}" },
        dataLabelStyle = TextStyle(fontSize = 9.sp, color = Color.White),
    ),
)
```

For `StackedBarChart`, the label shows the total stack value and the formatter receives the entire `BarGroup`:

```kotlin
StackedBarChart(
    data = barGroups,
    stackedConfig = StackedBarChartConfig(
        showDataLabels = true,
        dataLabelFormatter = { group -> "%.0f".format(group.values.sum()) },
    ),
)
```
