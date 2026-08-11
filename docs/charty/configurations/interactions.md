# Interactions

Charty supports several interaction modes: crosshair inspection, tap-to-click with tooltips, zoom and pan, and brush (range) selection. This page explains each mode, how to enable it, and how the modes relate to each other.

---

## Click handlers

Every chart exposes an optional click callback. When the user taps a data point, Charty computes which element was hit and invokes the callback with the relevant data object. A tooltip is automatically shown near the tapped element whenever the callback is set.

The parameter name varies by chart type but the pattern is always the same:

| Chart | Callback parameter | Data type passed |
|---|---|---|
| `BarChart` | `onBarClick` | `BarData` |
| `HorizontalBarChart` | `onBarClick` | `BarData` |
| `StackedBarChart` | `onSegmentClick` | `StackedBarSegment` |
| `StackedHorizontalBarChart` | `onSegmentClick` | `StackedHorizontalBarSegment` |
| `GroupedHorizontalBarChart` | `onBarClick` | `GroupedHorizontalBarEntry` |
| `NormalizedHorizontalBarChart` | `onSegmentClick` | `NormalizedHorizontalBarSegment` |
| `MosaicBarChart` | `onSegmentClick` | `MosaicBarSegment` |
| `SpanChart` | `onSpanClick` | `SpanData` |
| `WaterfallChart` | `onBarClick` | `BarData` |
| `LineChart` | `onPointClick` | `LineData` |
| `AreaChart` | `onPointClick` | `LineData` |
| `MultilineChart` | `onPointClick` | `MultilinePoint` |
| `StackedAreaChart` | `onAreaClick` | `StackedAreaPoint` |
| `PointChart` | `onPointClick` | `PointData` |
| `BubbleChart` | `onBubbleClick` | `BubbleData` |
| `ComboChart` | `onDataClick` | `ComboChartData` |
| `PieChart` | `onSliceClick` | `(PieData, Int)` |
| `MultipleRadarChart` | `onDataSetClick` | `(label: String, index: Int)` |

### Example

```kotlin
BarChart(
    data = barData,
    barConfig = BarChartConfig(
        tooltipConfig = TooltipConfig(
            backgroundColor = Color.Black.copy(alpha = 0.85f),
        ),
        tooltipFormatter = { barData -> "Value: ${barData.value}" },
    ),
    onBarClick = { clickedBar ->
        println("Tapped bar with value ${clickedBar.value}")
    },
)
```

```kotlin
LineChart(
    data = lineData,
    lineConfig = LineChartConfig(
        tooltipFormatter = { point -> "${point.label}: ${point.value}" },
    ),
    onPointClick = { point ->
        selectedPoint = point
    },
)
```

> If no click handler is provided, the chart is non-interactive and no tooltip is shown.

---

## Tooltip

Tooltips are shown automatically at the tapped position whenever a click handler is registered. You do not need to manage tooltip state manually.

`TooltipConfig` controls the visual appearance:

```kotlin
TooltipConfig(
    backgroundColor = Color.Black.copy(alpha = 0.8f),
    textColor = Color.White,
    cornerRadius = 8f,
    padding = 8f,
    textStyle = TextStyle(fontSize = 12.sp),
)
```

Pass it through the chart's config object:

```kotlin
BarChart(
    data = barData,
    barConfig = BarChartConfig(
        tooltipConfig = TooltipConfig(
            backgroundColor = Color(0xFF1A237E),
            cornerRadius = 12f,
        ),
        tooltipFormatter = { barData -> "%.2f".format(barData.value) },
    ),
    onBarClick = { },
)
```

**Tooltip position** — Charty automatically positions the tooltip above or below the data point to keep it on screen (`AUTO` mode). This behaviour is built into the library and does not require configuration.

---

## Crosshair

The crosshair is an inspection mode where a vertical and optional horizontal line follows the user's finger or pointer across the chart. A tooltip snaps to the nearest data point. This is more useful than tap-to-click for continuous inspection of line or area data.

### ChartCrosshairConfig

```kotlin
data class ChartCrosshairConfig(
    val verticalLineColor: ChartyColor = ChartyColor.Solid(Color.Gray),
    val horizontalLineColor: ChartyColor = ChartyColor.Solid(Color.Gray),
    val tooltipConfig: TooltipConfig = TooltipConfig(),
    val dismissOnRelease: Boolean = true,
)
```

| Property | Default | Description |
|---|---|---|
| `verticalLineColor` | Gray | Color of the vertical crosshair line. Accepts `ChartyColor.Solid` or `ChartyColor.Gradient`. |
| `horizontalLineColor` | Gray | Color of the horizontal crosshair line. |
| `tooltipConfig` | Default tooltip | Controls the tooltip that appears at the snap point. |
| `dismissOnRelease` | `true` | When `true`, the crosshair disappears when the pointer/finger is lifted. Set to `false` to keep it visible. |

### Where to pass it

The crosshair config is a direct parameter on some charts and lives inside the chart config on others:

**Inside the chart config (`LineChartConfig`):**

```kotlin
// LineChart, AreaChart, MultilineChart, StackedAreaChart — pass through LineChartConfig
LineChart(
    data = lineData,
    lineConfig = LineChartConfig(
        crosshairConfig = ChartCrosshairConfig(
            verticalLineColor = ChartyColor.Solid(Color(0xFF6200EE)),
            tooltipConfig = TooltipConfig(
                backgroundColor = Color(0xFF6200EE).copy(alpha = 0.9f),
            ),
            dismissOnRelease = false,
        ),
    ),
)
```

**Direct composable parameter:**

```kotlin
// PointChart, BubbleChart — direct parameter on the composable
PointChart(
    data = pointData,
    crosshairConfig = ChartCrosshairConfig(),
)
```

**Inside a chart-specific config:**

```kotlin
// MultilineChart example (still uses LineChartConfig)
MultilineChart(
    data = seriesData,
    lineConfig = LineChartConfig(
        crosshairConfig = ChartCrosshairConfig(),
    ),
)

// ComboChart — pass through ComboChartConfig
ComboChart(
    data = comboData,
    comboConfig = ComboChartConfig(
        crosshairConfig = ChartCrosshairConfig(
            verticalLineColor = ChartyColor.Solid(Color.DarkGray),
        ),
    ),
)

// WavyChart — direct parameter
WavyChart(
    data = wavyData,
    crosshairConfig = ChartCrosshairConfig(),
)
```

### Mutual exclusion with click

**When a crosshair config is active, the tap-to-click interaction is replaced by drag-to-snap crosshair.** You cannot use both simultaneously on the same chart. If you need a click handler, omit `crosshairConfig` (or pass `null`).

---

## Zoom and Pan

Zoom and pan allow users to pinch-zoom and drag the visible chart window. This is especially useful for dense time-series data.

Create the state in a `@Composable` scope and pass it via `ChartInteractionConfig`:

```kotlin
val viewPortState = rememberViewPortState(initialVisibleItems = 10)

BarChart(
    data = barData,
    interactionConfig = ChartInteractionConfig(
        viewPortState = viewPortState,
    ),
)
```

`initialVisibleItems` controls how many data points are visible at once when the chart first renders. The user can then pinch to zoom out (show more items) or in (show fewer).

```kotlin
// Show 7 items initially; user can zoom to see the full dataset
val viewPortState = rememberViewPortState(initialVisibleItems = 7)

LineChart(
    data = lineData,
    interactionConfig = ChartInteractionConfig(
        viewPortState = viewPortState,
    ),
)
```

You can read the current viewport from `viewPortState` (e.g., `viewPortState.visibleRange`) if you need to synchronise multiple charts.

---

## Brush Selection

Brush selection lets the user drag across the chart to select a range of data. This is useful for date-range pickers or any scenario where you need to identify a subset of the data.

```kotlin
val brushState = rememberBrushSelectionState()

LineChart(
    data = lineData,
    interactionConfig = ChartInteractionConfig(
        brushSelectionState = brushState,
    ),
)

// Read the selected range elsewhere in your composable
val selectedRange = brushState.selectedRange
if (selectedRange != null) {
    Text("Selected: ${selectedRange.start} – ${selectedRange.endInclusive}")
}
```

Brush selection and zoom/pan can be combined in a single `ChartInteractionConfig`:

```kotlin
ChartInteractionConfig(
    viewPortState = rememberViewPortState(initialVisibleItems = 15),
    brushSelectionState = rememberBrushSelectionState(),
)
```

---

## ChartInteractionConfig

All interaction states are grouped into a single `ChartInteractionConfig` object passed to Cartesian charts:

```kotlin
data class ChartInteractionConfig(
    val viewPortState: ViewPortState? = null,
    val brushSelectionState: BrushSelectionState? = null,
    val onRangeSelect: ((startIndex: Int, endIndex: Int) -> Unit)? = null,
    val annotations: List<ChartAnnotation> = emptyList(),
    val accessibilityDescription: String? = null,
)
```

| Property | Description |
|---|---|
| `viewPortState` | Enables zoom and pan. Create with `rememberViewPortState(initialVisibleItems)`. |
| `brushSelectionState` | Enables brush range selection. Create with `rememberBrushSelectionState()`. |
| `onRangeSelect` | Called with `(startIndex, endIndex)` when a brush selection gesture completes. |
| `annotations` | List of `ChartAnnotation` markers rendered on top of the chart content. |
| `accessibilityDescription` | Overrides the auto-generated `contentDescription` for screen readers. Pass `""` to suppress the description entirely. |

### Full example

```kotlin
@Composable
fun SalesChart(data: List<BarData>) {
    val viewPortState = rememberViewPortState(initialVisibleItems = 12)
    val brushState = rememberBrushSelectionState()

    BarChart(
        data = data,
        barConfig = BarChartConfig(
            tooltipConfig = TooltipConfig(),
            tooltipFormatter = { bar -> "Sales: ${bar.value}" },
        ),
        interactionConfig = ChartInteractionConfig(
            viewPortState = viewPortState,
            brushSelectionState = brushState,
            accessibilityDescription = "Monthly sales bar chart, showing the last 12 months",
        ),
        onBarClick = { bar ->
            println("Selected month value: ${bar.value}")
        },
    )
}
```

---

## Accessibility

All Charty charts generate a `contentDescription` automatically from the data, enabling TalkBack and VoiceOver support without any extra work. To override the auto-generated description, use:

- **Cartesian charts** — `ChartInteractionConfig(accessibilityDescription = "...")`
- **Non-Cartesian charts** (PieChart, RadarChart, CircularProgressIndicator, BlockBar) — direct `accessibilityDescription: String?` parameter

```kotlin
// Override description on a PieChart
PieChart(
    data = pieData,
    accessibilityDescription = "Revenue breakdown by product category",
)

// Suppress description entirely
PieChart(
    data = pieData,
    accessibilityDescription = "",
)
```
