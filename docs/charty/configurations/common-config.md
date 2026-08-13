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
    val axisColor: ChartyColor = ChartyColor.Solid(Color.Black),
    val leftLabelRotation: LabelRotation = LabelRotation.Straight,
    val gridColor: ChartyColor = ChartyColor.Solid(Color.LightGray),
    val axisThickness: Float = 2f,
    val gridThickness: Float = 1f,
    val labelTextStyle: TextStyle = TextStyle(color = Color.Black, fontSize = 12.sp),
    val labelTextColor: ChartyColor? = null,
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
    data = { barData },
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
    val isEnabled: Boolean = true,
    val value: Float,
    val color: ChartyColor = ChartyColor.Solid(Color.Red),
    val strokeWidth: Float = 2f,
    val strokeStyle: ReferenceLineStrokeStyle = ReferenceLineStrokeStyle.DASHED,
    val dashIntervals: FloatArray? = null,
    val label: String? = null,
    val showValueInLabelWhenNoText: Boolean = true,
    val labelTextStyle: TextStyle = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black),
    val labelPosition: ReferenceLineLabelPosition = ReferenceLineLabelPosition.ABOVE,
    val labelOffset: Float = 4f,
)
```

| Property | Default | Description |
|---|---|---|
| `isEnabled` | `true` | Whether the line is rendered at all. |
| `value` | required | The value on the chart's numeric axis at which to draw the line. |
| `color` | `Color.Red` | Color of the reference line. |
| `strokeWidth` | `2f` | Stroke width in pixels. Must be positive. |
| `strokeStyle` | `DASHED` | `ReferenceLineStrokeStyle.SOLID` or `DASHED`. **The default is dashed, not solid.** |
| `dashIntervals` | `null` | A `FloatArray` of alternating dash/gap lengths. When `null` and the style is `DASHED`, a default pattern is used. |
| `label` | `null` | Optional text rendered near the line. |
| `showValueInLabelWhenNoText` | `true` | When `label` is `null`, show the numeric `value` instead. |
| `labelTextStyle` | 12 sp bold black | Text style for the label. |
| `labelPosition` | `ABOVE` | `START`, `CENTER`, `END`, `ABOVE`, or `BELOW`. |
| `labelOffset` | `4f` | Pixels to move the label away from the line. Must be non-negative. |

> The property is `dashIntervals`, **not** `dashPattern`, and there is no way to get a solid line by passing `null` — set `strokeStyle = ReferenceLineStrokeStyle.SOLID`.

**Supported charts:** BarChart, HorizontalBarChart, StackedBarChart, StackedHorizontalBarChart, GroupedHorizontalBarChart, LineChart, AreaChart, PointChart, ComboChart.

### Example: dashed target line with label

```kotlin
BarChart(
    data = { barData },
    barConfig = BarChartConfig(
        referenceLine = ReferenceLineConfig(
            value = 100f,
            color = Color(0xFF388E3C),
            strokeWidth = 2f,
            label = "Target",
            dashIntervals = floatArrayOf(10f, 5f),
        )
    ),
)
```

For a solid baseline on a line chart:

```kotlin
LineChart(
    data = { lineData },
    lineConfig = LineChartConfig(
        referenceLine = ReferenceLineConfig(
            value = 50f,
            label = "Baseline",
            strokeStyle = ReferenceLineStrokeStyle.SOLID,
            labelPosition = ReferenceLineLabelPosition.END,
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
    val shape: Shape = RoundedCornerShape(8.dp),
    val cornerRadius: Dp = 8.dp,
    val backgroundColor: ChartyColor = ChartyColor.Solid(Color(0xFF2D2D2D)),
    val borderColor: ChartyColor? = null,
    val borderWidth: Dp = 1.dp,
    val textStyle: TextStyle = TextStyle(color = Color.White, fontSize = 14.sp),
    val textColor: ChartyColor? = null,
    val padding: TooltipPadding = TooltipPadding(),
    val elevation: Dp = 4.dp,
    val offsetY: Dp = 8.dp,
    val minDistanceFromEdge: Dp = 16.dp,
    val showArrow: Boolean = true,
    val arrowSize: Dp = 8.dp,
)
```

| Property | Default | Description |
|---|---|---|
| `shape` | `RoundedCornerShape(8.dp)` | Shape of the tooltip background. |
| `backgroundColor` | `Color(0xFF2D2D2D)` | Background fill of the tooltip bubble. |
| `borderColor` | `null` | Border colour; `null` draws no border. |
| `borderWidth` | `1.dp` | Border width, used when `borderColor` is set. |
| `textStyle` | White, 14 sp | `TextStyle` applied to the tooltip text — this is where the text colour lives. |
| `padding` | `TooltipPadding(horizontal = 12.dp, vertical = 8.dp)` | Internal padding around the text. |
| `elevation` | `4.dp` | Shadow elevation. |
| `offsetY` | `8.dp` | Vertical offset from the anchor; negative moves it up. |
| `minDistanceFromEdge` | `16.dp` | Minimum distance from the chart edges before the tooltip is repositioned. |
| `showArrow` | `true` | Whether to draw an arrow pointing at the data point. |
| `arrowSize` | `8.dp` | Size of that arrow. |

> There is no `textColor` or `cornerRadius` property. Use `textStyle.color` and `shape`.

### tooltipFormatter

Pass a lambda to format the tooltip text from the chart's data object. Each chart type passes the relevant data class to the formatter.

```kotlin
BarChart(
    data = { barData },
    barConfig = BarChartConfig(
        tooltipConfig = TooltipConfig(
            backgroundColor = Color(0xFF212121),
            textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
            shape = RoundedCornerShape(12.dp),
        ),
        tooltipFormatter = { bar ->
            "Sales: ${bar.value.toInt()} units"
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
    data = { lineData },
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

> Tap tooltips are only displayed when a click handler (`onBarClick`, `onPointClick`, etc.) is provided. If no click handler is set, `TooltipConfig` has no effect on the tap tooltip — though it is still used for a crosshair's label via `ChartCrosshairConfig.tooltipConfig`.

> `tooltipFormatter` is **not** nullable. Every chart config ships a default formatter (`"${label}: ${value}"`), so omitting it gives you a sensible string rather than nothing.

> Avoid `"%.1f".format(value)` in `commonMain` — `String.format` is JVM-only and will not compile for iOS, JS, or Wasm targets.

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

| Value | Radius |
|---|---|
| `None` | 0 |
| `Small` | 4 |
| `Medium` | 8 (the default on `BarChartConfig`) |
| `Large` | 12 |
| `ExtraLarge` | 16 |
| `Custom(radius)` | The exact value you provide; must be non-negative |

`CornerRadius.Custom(radius)` sits alongside the five presets, so you are never limited to them — use it whenever the presets do not match your design system's scale.

### Example

```kotlin
BarChart(
    data = { barData },
    barConfig = BarChartConfig(
        cornerRadius = CornerRadius.Medium,
    ),
)

// Custom radius
BarChart(
    data = { barData },
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
| `dataLabelFormatter` | `(BarData) -> String` | Prints whole numbers without a decimal point | Converts a data point to a display string. Not nullable — there is always a default. |
| `dataLabelStyle` | `TextStyle` | 10 sp, SemiBold, `Color.DarkGray` | Text style applied to each data label. |

### Example

```kotlin
BarChart(
    data = { barData },
    barConfig = BarChartConfig(
        showDataLabels = true,
        dataLabelFormatter = { bar ->
            "${bar.value.toInt()} units"
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
    data = { barData },
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
    data = { barGroups },
    stackedConfig = StackedBarChartConfig(
        showDataLabels = true,
        dataLabelFormatter = { group -> "${group.values.sum().toInt()}" },
    ),
)
```

---

## Rolling window — `visibleWindow`

`visibleWindow` shows only the most recent *N* points, and advances as data is appended to `data`. It is the foundation of live/streaming charts.

```kotlin
LineChart(
    data = { readings },
    color = ChartyColor.Solid(ChartyColors.Blue),
    lineConfig = LineChartConfig(visibleWindow = 30),
)
```

| | |
|---|---|
| Type | `Int?` |
| Default | `null` — the whole series is drawn |
| Minimum | `2`; the config's `init` throws below that |
| Precedence | Ignored when `interactionConfig.viewPortState` is set |

It is available on every Cartesian chart config: `BarChartConfig`, `StackedBarChartConfig`, `StackedHorizontalBarChartConfig`, `GroupedHorizontalBarChartConfig`, `NormalizedHorizontalBarChartConfig`, `MosaicBarChartConfig`, `WaterfallChartConfig`, `LollipopBarChartConfig`, `BubbleBarChartConfig`, `ComparisonBarChartConfig`, `WavyChartConfig`, `LineChartConfig`, `PointChartConfig`, `CandlestickChartConfig`, and `ComboChartConfig`.

Add a `StreamingState` to let the reader scroll back through history — see the **[streaming guide](../guides/streaming.md)**.

---

## Persistent markers

`markers` pins a permanently-drawn callout to specific data points, independent of touch — unlike the transient crosshair or tooltip. Use it to flag a peak, a target, or "today".

```kotlin
import com.himanshoe.charty.common.config.PersistentMarker

LineChart(
    data = { readings },
    color = ChartyColor.Solid(ChartyColors.Blue),
    lineConfig = LineChartConfig(
        markers = listOf(
            PersistentMarker(dataIndex = 4, label = "Launch"),
            PersistentMarker(dataIndex = -1),
        ),
    ),
)
```

### `PersistentMarker(dataIndex = -1)` means "the newest point"

A **negative** `dataIndex` counts back from the end of the drawn data, so `-1` marks the latest point. This is the idiomatic way to keep a label pinned to the current value of a rolling `visibleWindow` — the rightmost bar on a vertical chart, the bottom bar on a horizontal one.

Markers whose index falls outside the currently drawn data are simply skipped.

| Property | Default | Description |
|---|---|---|
| `dataIndex` | required | Index of the point to mark. Negative counts from the end; `-1` is the newest. |
| `label` | `null` | Callout text. When `null`, the point's formatted value is used. |
| `showDot` / `dotRadius` | `true` / `6f` | The emphasised dot on the point. |
| `dotColor` | Blue `ChartyColor.Solid` | Dot fill; accepts a gradient. |
| `dotRingColor` / `dotRingWidth` | `Color.White` / `2f` | Contrast ring around the dot; `null` draws none. |
| `showLabel` | `true` | Whether to draw the callout pill above the dot. |
| `labelTextStyle` | 11 sp SemiBold white | Text style for the callout. |
| `labelBackgroundColor` | Dark `ChartyColor.Solid` | Callout pill fill; accepts a gradient. |
| `labelPadding` / `labelCornerRadius` | `6f` / `6f` | Callout pill geometry, in pixels. |
| `showGuideLine` | `false` | Drop a thin guide line from the point to the value axis. |
| `guideLineColor` / `guideLineWidth` | Translucent blue / `1f` | Guide line styling. |

`markers` is available on the same 15 Cartesian chart configs as `visibleWindow`.

---

## Animating data changes — `animateValueChanges`

`animation` controls the chart's **entry reveal**. `animateValueChanges` is a separate opt-in that tweens values whenever the *data* changes afterwards, so bars and points glide from their previous heights to the new ones instead of jumping.

```kotlin
BarChart(
    data = { liveSales },
    color = ChartyColor.Solid(ChartyColors.Blue),
    barConfig = BarChartConfig(
        animation = Animation.Fast,
        animateValueChanges = true,
    ),
)
```

| | |
|---|---|
| Type | `Boolean` |
| Default | `false` — new data appears instantly |
| Driven by | the config's `animation`; has **no effect** when that is `Animation.Disabled` |

A change in the *number* of points snaps to the new shape rather than tweening — there is no sensible per-index correspondence between lists of different sizes.

Available on the same 15 Cartesian chart configs as `visibleWindow`.

---

## Large series — `downsampleThreshold`

When set, the visible series is reduced to at most this many points using the shape-preserving **LTTB** (Largest-Triangle-Three-Buckets) algorithm before drawing, keeping series of tens of thousands of points at interactive frame rates.

```kotlin
LineChart(
    data = { fiftyThousandPoints },
    color = ChartyColor.Solid(ChartyColors.Blue),
    lineConfig = LineChartConfig(
        downsampleThreshold = 800,
        showPoints = false,
    ),
)
```

| | |
|---|---|
| Type | `Int?` |
| Default | `null` — every point is drawn |
| Minimum | `3`; the config's `init` throws below that |
| Available on | `LineChartConfig` and `PointChartConfig` only |

For the multi-series charts (`MultilineChart`, `StackedAreaChart`) selection is driven by the per-x sum of all series, so every series keeps the same x-indices and stays aligned.

> **Downsampling is skipped while streaming.** A rolling `visibleWindow` is already small, and the streaming layout indexes the window slice directly, so thinning it would break the position mapping. Setting both means only the window applies.
