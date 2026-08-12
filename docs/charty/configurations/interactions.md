# Interactions

Charty supports several interaction modes: crosshair inspection, tap-to-click with tooltips, zoom and pan, brush (range) selection, and streaming scrollback. This page explains each mode, how to enable it, and — most importantly — which modes can share one chart.

## Which gestures can coexist

Compose delivers one gesture stream per chart, so the rules come down to *which gesture type* each feature uses.

| Feature | Gesture | Coexists with a tap tooltip? |
|---|---|---|
| Tap tooltip (`onXxxClick`) | Tap | — |
| Crosshair (`crosshair = ChartCrosshair()`) | Drag | ✅ Yes |
| Streaming scrollback (`streamingState`) | Drag | ✅ Yes |
| Zoom / pan (`viewPortState`) | Drag + pinch | ✅ Yes |
| Brush selection (`brushSelectionState`) | Drag | ✅ Yes |
| Drag-to-track tooltip (`dragTooltipEnabled`) | Drag | — |

**A tap tooltip always coexists with a drag feature.** Drag detection only reports movement once the pointer has travelled past touch slop, so a plain tap falls through to the tooltip handler untouched.

**Two drag features on one chart do not coexist.** In particular:

- **Crosshair vs. streaming scrollback** — the crosshair wins. While a crosshair is configured, the streaming pan gesture consumes nothing and moves nothing: the window keeps following the newest data, never detaches, and no "jump to latest" control appears. See [Streaming](../guides/streaming.md#the-one-real-limitation-crosshair-vs-scrollback).
- **`dragTooltipEnabled` vs. zoom/pan or brush** — drag-to-track tooltips are automatically suppressed whenever `viewPortState` or `brushSelectionState` is set.

Zoom/pan also takes precedence over a rolling `visibleWindow`: with a `viewPortState` configured, the window is ignored and the chart draws statically.

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
    data = { barData },
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
    data = { lineData },
    lineConfig = LineChartConfig(
        tooltipFormatter = { point -> "${point.label}: ${point.value}" },
    ),
    onPointClick = { point ->
        selectedPoint = point
    },
)
```

> If no click handler is provided, no hit bounds are recorded and no tap tooltip is shown. The chart may still be interactive through a crosshair, zoom/pan, brush selection, or streaming scrollback — those are independent of the click callback.

---

## Tooltip

Tooltips are shown automatically at the tapped position whenever a click handler is registered. You do not need to manage tooltip state manually. Without a click handler no hit bounds are recorded, so no tooltip appears.

`TooltipConfig` controls the visual appearance. Note that the bubble is styled with a Compose `Shape` and a `TooltipPadding`, not raw pixel values:

```kotlin
data class TooltipConfig(
    val shape: Shape = RoundedCornerShape(8.dp),
    val backgroundColor: Color = Color(0xFF2D2D2D),
    val borderColor: Color? = null,
    val borderWidth: Dp = 1.dp,
    val textStyle: TextStyle = TextStyle(color = Color.White, fontSize = 14.sp),
    val padding: TooltipPadding = TooltipPadding(),
    val elevation: Dp = 4.dp,
    val offsetY: Dp = 8.dp,
    val minDistanceFromEdge: Dp = 16.dp,
    val showArrow: Boolean = true,
    val arrowSize: Dp = 8.dp,
)

data class TooltipPadding(
    val horizontal: Dp = 12.dp,
    val vertical: Dp = 8.dp,
)
```

There is no `textColor` or `cornerRadius` property — set the text colour through `textStyle.color` and the rounding through `shape`.

Pass it through the chart's config object:

```kotlin
BarChart(
    data = { barData },
    barConfig = BarChartConfig(
        tooltipConfig = TooltipConfig(
            backgroundColor = Color(0xFF1A237E),
            shape = RoundedCornerShape(12.dp),
            textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
            padding = TooltipPadding(horizontal = 14.dp, vertical = 10.dp),
        ),
        tooltipFormatter = { bar -> "${bar.label}: ${bar.value}" },
    ),
    onBarClick = { },
)
```

**Tooltip position** — `tooltipPosition` on the chart config takes `TooltipPosition.ABOVE`, `BELOW`, or `AUTO` (the default), which places the tooltip to keep it on screen.

### Choosing how the tooltip is rendered

Every chart takes a `tooltip: ChartTooltip<T>` parameter that selects the rendering strategy:

```kotlin
tooltip = ChartTooltip.canvas()                  // built-in bubble drawn on the canvas (default)
tooltip = ChartTooltip.none()                    // no tooltip
tooltip = ChartTooltip.compose { MyCard(title = text, value = data.value) }  // your Composable
```

`ChartTooltip.compose { }` positions your composable over the selected point with edge-collision handling. Inside the block you have a `TooltipScope<T>` exposing `data`, the formatted `text`, and the `anchor` position.

### Drag-to-track tooltips

Set `dragTooltipEnabled = true` on `ChartInteractionConfig` and dragging across a rectangular chart tracks the item under the finger and shows its tooltip, dismissing on release. It is automatically suppressed whenever `viewPortState` or `brushSelectionState` is set, since those consume drags too.

---

## Crosshair

The crosshair is an inspection mode where a vertical and optional horizontal line follows the user's finger or pointer across the chart, snapping to the nearest data point and showing a label. This is more useful than tap-to-click for continuous inspection of line or area data.

### Enabling it

Every crosshair-capable chart takes a unified `crosshair: ChartCrosshair<T>?` parameter. `null` (the default) turns it off.

```kotlin
import com.himanshoe.charty.common.gesture.ChartCrosshair

LineChart(
    data = { lineData },
    color = ChartyColor.Solid(ChartyColors.Blue),
    crosshair = ChartCrosshair(),
)
```

```kotlin
data class ChartCrosshair<T>(
    val config: ChartCrosshairConfig = ChartCrosshairConfig(),
    val label: (@Composable CrosshairScope<T>.() -> Unit)? = null,
)
```

`config` styles the guide line; `label` is a **Composable drawn over the line** at the snapped point. Leave it `null` for the built-in pill label.

```kotlin
LineChart(
    data = { lineData },
    color = ChartyColor.Solid(ChartyColors.Blue),
    crosshair =
        ChartCrosshair(
            config = ChartCrosshairConfig(
                verticalLineColor = ChartyColor.Solid(ChartyColors.Purple),
                showHorizontalLine = false,
                dismissOnRelease = false,
            ),
            label = { Text(text = "${data.label}: ${data.value}") },
        ),
)
```

Inside the `label` block you are in a `CrosshairScope<T>`, which exposes `data` (the point under the crosshair) and `text` (the chart's formatted label for it).

**Charts with a `crosshair` parameter:** `LineChart`, `AreaChart`, `MultilineChart`, `StackedAreaChart`, `PointChart`, `BubbleChart`, `BarChart`, `HorizontalBarChart`, `WavyChart`, and `ComboChart`.

### ChartCrosshairConfig

```kotlin
data class ChartCrosshairConfig(
    val verticalLineColor: ChartyColor = ChartyColor.Solid(Color.Black.copy(alpha = 0.4f)),
    val horizontalLineColor: ChartyColor = ChartyColor.Solid(Color.Black.copy(alpha = 0.15f)),
    val lineWidth: Float = 1.5f,
    val showHorizontalLine: Boolean = true,
    val dotRadius: Float = 8f,
    val showLabel: Boolean = true,
    val tooltipConfig: TooltipConfig = TooltipConfig(showArrow = false),
    val dismissOnRelease: Boolean = true,
)
```

| Property | Default | Description |
|---|---|---|
| `verticalLineColor` | 40% black | Colour of the vertical line. A gradient is applied top-to-bottom along it. |
| `horizontalLineColor` | 15% black | Colour of the horizontal line. A gradient is applied left-to-right. |
| `lineWidth` | `1.5f` | Stroke width for both lines, in pixels. |
| `showHorizontalLine` | `true` | Whether to draw the horizontal line at all. |
| `dotRadius` | `8f` | Radius of the highlight circle at the snapped point. |
| `showLabel` | `true` | Whether to display the value label. |
| `tooltipConfig` | Arrow-less default | Appearance of the built-in label bubble. |
| `dismissOnRelease` | `true` | Hide the crosshair when the finger lifts. `false` pins it after release. |

You can also set `crosshairConfig` directly inside a chart's config (`LineChartConfig`, `BarChartConfig`, `ComboChartConfig`, …). Passing a `crosshair` to the composable is the preferred form — it also gives you the custom-label slot — and it overrides the config's `crosshairConfig` when both are set.

### A crosshair does **not** disable tap-to-click

A crosshair is a **drag** gesture and a tooltip is a **tap**, so the two coexist happily: tapping a point still raises its tooltip while dragging still moves the guide line.

```kotlin
LineChart(
    data = { lineData },
    crosshair = ChartCrosshair(),
    onPointClick = { point -> selected = point },   // still fires
)
```

What a crosshair *does* displace is **streaming scrollback** — the other drag gesture. See the coexistence table at the top of this page and [Streaming](../guides/streaming.md#the-one-real-limitation-crosshair-vs-scrollback).

### Sharing one crosshair across stacked charts

Wrap several charts in `CrosshairSyncScope { }` and they mirror a single guide position. Charts enrol automatically, but only when they have both a crosshair and a `ViewPortState` or `StreamingState` in their `interactionConfig`. See the [synced crosshair guide](../guides/synced-crosshair.md).

---

## Zoom and Pan

Zoom and pan allow users to pinch-zoom and drag the visible chart window. This is especially useful for dense time-series data.

Create the state in a `@Composable` scope and pass it via `ChartInteractionConfig`:

```kotlin
val viewPortState = rememberViewPortState(initialVisibleItems = 10)

BarChart(
    data = { barData },
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
    data = { lineData },
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
    data = { lineData },
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

## Streaming scrollback

On a chart with a rolling `visibleWindow`, a `StreamingState` lets the reader drag back through history while new data keeps accumulating off-screen instead of yanking the view to the newest point.

```kotlin
val streaming = rememberStreamingState()

LineChart(
    data = { points },
    color = ChartyColor.Solid(ChartyColors.Blue),
    lineConfig = LineChartConfig(visibleWindow = 30),
    interactionConfig =
        ChartInteractionConfig(
            streamingState = streaming,
            jumpToLatest = { state -> ChartJumpToLatestPill(state = state) },
        ),
)
```

The drag settles on the nearest whole data index when the finger lifts, and `jumpToLatest` renders only while the window is detached. Requires **both** a `streamingState` and a `visibleWindow`; a chart with neither gains no gesture it did not already have.

A crosshair on the same chart makes scrollback inert. Full details in the [streaming guide](../guides/streaming.md).

---

## ChartInteractionConfig

All interaction states are grouped into a single `ChartInteractionConfig` object passed to Cartesian charts:

```kotlin
class ChartInteractionConfig(
    val viewPortState: ViewPortState? = null,
    val brushSelectionState: BrushSelectionState? = null,
    val onRangeSelect: ((startIndex: Int, endIndex: Int) -> Unit)? = null,
    val annotations: List<ChartAnnotation> = emptyList(),
    val accessibilityDescription: String? = null,
    val dragTooltipEnabled: Boolean = false,
    val autoScrollToLatest: Boolean = false,
    val edgeFade: ScrollEdgeFadeConfig? = null,
    val streamingState: StreamingState? = null,
    val jumpToLatest: (@Composable (StreamingState) -> Unit)? = null,
)
```

| Property | Description |
|---|---|
| `viewPortState` | Enables zoom and pan. Create with `rememberViewPortState(initialVisibleItems)`. |
| `brushSelectionState` | Enables brush range selection. Create with `rememberBrushSelectionState()`. |
| `onRangeSelect` | Called with `(startIndex, endIndex)` when a brush selection gesture completes. |
| `annotations` | List of `ChartAnnotation` markers rendered on top of the chart content. |
| `accessibilityDescription` | Overrides the auto-generated `contentDescription` for screen readers. Pass `""` to suppress the description entirely. |
| `dragTooltipEnabled` | Dragging tracks the item under the finger and shows its tooltip. Automatically suppressed while `viewPortState` or `brushSelectionState` is set. |
| `autoScrollToLatest` | With `viewPortState` set, the viewport follows the end of the data as it grows, keeping the current zoom. No effect without a viewport. |
| `edgeFade` | With `viewPortState` set, draws a scrim at the edges while data is scrolled off-screen, hinting there is more to pan to. |
| `streamingState` | Enables scrollback on a chart with a rolling `visibleWindow`. Create with `rememberStreamingState()`. Inert when a crosshair is configured. |
| `jumpToLatest` | Your "jump to latest" control, rendered over the bottom centre of the plot while the window is detached. Requires both `streamingState` and a `visibleWindow`. |

Note it is a plain `class`, not a `data class` — there is no `copy()`.

### Full example

```kotlin
@Composable
fun SalesChart(data: List<BarData>) {
    val viewPortState = rememberViewPortState(initialVisibleItems = 12)
    val brushState = rememberBrushSelectionState()

    BarChart(
        data = { data },
        color = ChartyColor.Solid(ChartyColors.Blue),
        barConfig = BarChartConfig(
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
    data = { pieData },
    accessibilityDescription = "Revenue breakdown by product category",
)

// Suppress description entirely
PieChart(
    data = { pieData },
    accessibilityDescription = "",
)
```
