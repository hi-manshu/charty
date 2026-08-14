# Synced Crosshair Across Charts

Stacked charts of the same x-domain — price above volume, revenue above costs —
read much better when one guide line moves across all of them at once. Wrap them in
`CrosshairSyncScope` and they share a single crosshair position.

---

## The basics

```kotlin
import com.himanshoe.charty.common.gesture.ChartCrosshair
import com.himanshoe.charty.common.gesture.CrosshairSyncScope

CrosshairSyncScope {
    LineChart(
        data = { revenue },
        color = ChartyColor.Solid(ChartyColors.Blue),
        crosshair = ChartCrosshair(),
        interactionConfig = revenueConfig,
    )
    LineChart(
        data = { costs },
        color = ChartyColor.Solid(ChartyColors.Orange),
        crosshair = ChartCrosshair(),
        interactionConfig = costsConfig,
    )
}
```

**Charts enrol themselves — there is no per-chart sync parameter.** Any eligible
chart composed inside the scope joins the group.

Drag either chart and both guide lines move together. Ownership is
**last-writer-wins**: the moment a drag starts on a different chart, that chart
takes the crosshair over and the previous owner becomes an observer.

---

## The requirement that actually matters

A chart participates only when it has **both**:

1. **a crosshair configured** — a `crosshair = ChartCrosshair(...)` parameter, or a
   `crosshairConfig` inside the chart's config; **and**
2. **a source of plot geometry in its `interactionConfig`** — either a
   `ViewPortState` (zoom/pan) or a `StreamingState` (rolling window).

Miss either and the chart keeps its crosshair entirely to itself. It still works
locally; it just neither publishes nor mirrors.

The second requirement exists because syncing is expressed as a **normalised
fraction** (`0f` = plot left edge, `1f` = plot right edge) rather than a pixel
offset, so charts of different widths and different axis-gutter sizes line up
correctly. Converting between pixels and that fraction needs the plot's left edge
and width, and those two state holders are what carry them.

```kotlin
// Participates — has a crosshair AND a viewport.
LineChart(
    data = { revenue },
    crosshair = ChartCrosshair(),
    interactionConfig = ChartInteractionConfig(viewPortState = rememberViewPortState(initialVisibleFraction = 0.3f)),
)

// Does NOT participate — crosshair, but no geometry source.
LineChart(
    data = { revenue },
    crosshair = ChartCrosshair(),
)
```

### Streaming charts can mirror too

Because `StreamingState` is also a geometry source, a rolling-window chart
participates without giving up its window:

```kotlin
val topStream = rememberStreamingState()
val bottomStream = rememberStreamingState()

CrosshairSyncScope {
    LineChart(
        data = { throughput },
        lineConfig = LineChartConfig(visibleWindow = 40),
        crosshair = ChartCrosshair(),
        interactionConfig = ChartInteractionConfig(streamingState = topStream),
    )
    LineChart(
        data = { latency },
        lineConfig = LineChartConfig(visibleWindow = 40),
        crosshair = ChartCrosshair(),
        interactionConfig = ChartInteractionConfig(streamingState = bottomStream),
    )
}
```

Note the trade-off from [Streaming](streaming.md#the-one-real-limitation-crosshair-vs-scrollback):
these charts get a synced crosshair, so their scrollback is inert — the crosshair
owns the drag. Each chart keeps following the newest data.

---

## What a mirrored guide shows

**A mirrored guide is a line, not a reading.** Only the chart you are actually
dragging snaps to one of its own data points; the observers receive a horizontal
position, not a data item. Because there is no snapped item, no label is drawn for
a mirrored guide — the vertical line is the shared element.

This is intentional. Two stacked charts rarely share x-values exactly, and inventing
a "nearest point" on the observer would put a confident-looking number under a
position that never corresponded to a sample.

---

## Sharing state across scopes

`CrosshairSyncScope` remembers its own `CrosshairSyncState` by default. Pass one
explicitly when you need to read the shared position, or share it across parts of
the tree that are not siblings:

```kotlin
import com.himanshoe.charty.common.gesture.rememberCrosshairSync

val sync = rememberCrosshairSync()

CrosshairSyncScope(sync = sync) {
    LineChart(data = { revenue }, crosshair = ChartCrosshair(), interactionConfig = revenueConfig)
    LineChart(data = { costs }, crosshair = ChartCrosshair(), interactionConfig = costsConfig)
}

Text(text = "Guide at ${sync.fraction ?: "—"} (owner ${sync.owner ?: "none"})")
```

| Member | Meaning |
|---|---|
| `fraction` | Shared position as `0f..1f` across the plot width, or `null` when no participant shows a crosshair. |
| `owner` | Id of the participant whose gesture produced `fraction`, or `null`. |
| `publish(ownerId, fraction)` | Makes `ownerId` the active owner. `fraction` is coerced into `0f..1f`. |
| `clear(ownerId)` | Clears the shared crosshair **only if** `ownerId` is the current owner, so an observer can never cancel the owner's active gesture. |
| `fractionFor(observerId)` | The fraction an observer should mirror, or `null` — a chart never mirrors its own crosshair. |

### Converting positions yourself

The same two conversions the group uses are public, which is useful for custom
overlays drawn alongside a synced group:

```kotlin
import com.himanshoe.charty.common.gesture.crosshairXForFraction
import com.himanshoe.charty.common.gesture.normalizedCrosshairFraction

val fraction = normalizedCrosshairFraction(x = pixelX, plotLeft = plotLeft, plotWidth = plotWidth)
val pixelX = crosshairXForFraction(fraction = fraction, plotLeft = plotLeft, plotWidth = plotWidth)
```

Both clamp into their valid ranges, and both degrade safely when `plotWidth <= 0f`
(yielding `0f` and `plotLeft` respectively), so a gesture arriving before the first
layout is harmless.

---

## Next steps

- **[Interactions](../configurations/interactions.md)** — crosshair styling and which gestures can coexist.
- **[Streaming](streaming.md)** — rolling windows, scrollback, and the crosshair trade-off.
