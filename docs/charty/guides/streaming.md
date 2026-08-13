# Streaming and Live Data

Charty renders live series with a **rolling window**: instead of squeezing an
ever-growing list into a fixed width, the chart shows only the most recent *N*
points and slides as new ones arrive.

Everything on this page is opt-in. A chart you do not configure for streaming
behaves exactly as it always has.

---

## The rolling window

Set `visibleWindow = N` in the chart's config. The chart then draws only the last
`N` points of whatever `data` returns, and advances as your list grows.

```kotlin
import androidx.compose.runtime.mutableStateListOf
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.ChartyColors
import com.himanshoe.charty.line.LineChart
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineData

val points = remember { mutableStateListOf<LineData>() }

LaunchedEffect(Unit) {
    while (true) {
        delay(timeMillis = 700)
        points += LineData(label = nextLabel(), value = nextValue())
    }
}

LineChart(
    data = { points },
    color = ChartyColor.Solid(ChartyColors.Blue),
    lineConfig = LineChartConfig(visibleWindow = 30),
)
```

You keep appending to one list — you never trim it yourself. The window is a
**view**, not a data operation; the full series stays intact and is still what
the accessibility summary describes.

| | |
|---|---|
| Type | `Int?` |
| Default | `null` — the whole series is drawn, nothing changes |
| Minimum | `2` (a smaller value throws from the config's `init`) |
| Precedence | Ignored when `interactionConfig.viewPortState` is set — zoom/pan wins and the chart draws statically |

`visibleWindow` is available on the config of every Cartesian chart: `BarChartConfig`,
`StackedBarChartConfig`, `StackedHorizontalBarChartConfig`,
`GroupedHorizontalBarChartConfig`, `NormalizedHorizontalBarChartConfig`,
`MosaicBarChartConfig`, `WaterfallChartConfig`, `LollipopBarChartConfig`,
`BubbleBarChartConfig`, `ComparisonBarChartConfig`, `WavyChartConfig`,
`LineChartConfig`, `PointChartConfig`, `CandlestickChartConfig`, and
`ComboChartConfig`.

### How the slide is driven

The window's scroll target is `size - visibleWindow`. Each appended point advances
that target by one, and the chart's `animation` eases the scroll towards it, which
is what you see as the slide: old points sweep out of the leading edge while the
new point eases in at the trailing edge. Retargeting is continuous, so a burst of
rapid appends catches up smoothly instead of restarting the animation.

With `animation = Animation.Disabled` the scroll snaps instead — the plain
"show last N" behaviour, no slide.

### The axis eases too

When a new extreme enters (or an old one leaves) the window, the value range is
re-derived and the axis **glides** to the new scale, taking every plotted point
with it, rather than teleporting.

This one animation runs even when you set `Animation.Disabled`, falling back to
`Animation.Fast`. That is deliberate: `Animation` governs a chart's *entry reveal*,
whereas a rescale is a continuous response to the window moving, and an axis that
jumps under a sliding window reads as a rendering glitch.

### Downsampling is skipped while streaming

`downsampleThreshold` and `visibleWindow` do not stack. A rolling window is already
small, and the streaming layout indexes the window slice directly, so thinning it
would break the position mapping. When a chart is streaming, downsampling is not
applied.

---

## Letting the reader look back

By default a streaming chart always follows the newest point — drag it and nothing
happens. Pass a `StreamingState` to add **scrollback**: dragging the plot backwards
detaches the window, and from that moment new data accumulates off-screen instead
of yanking the view to the end.

```kotlin
import com.himanshoe.charty.common.config.ChartInteractionConfig
import com.himanshoe.charty.common.streaming.rememberStreamingState

val streaming = rememberStreamingState()

LineChart(
    data = { points },
    color = ChartyColor.Solid(ChartyColors.Blue),
    lineConfig = LineChartConfig(visibleWindow = 30),
    interactionConfig = ChartInteractionConfig(streamingState = streaming),
)
```

The scroll position is held in **data indices**, not pixels: `0` puts the very
first point at the plot's leading edge and `maxScroll` shows the newest. It is
therefore independent of pixel size and survives a resize.

A drag **settles on the nearest whole index** when the finger lifts, so the window
always comes to rest on aligned slots instead of leaving the leading and trailing
items sliced in half by the plot edges. There is no fling — a chart being read
backwards should stop where it was released.

### Reading the state

| Member | Meaning |
|---|---|
| `isFollowing` | `true` while the window is pinned to the newest point. Dragging backwards clears it. |
| `pendingCount` | How many points arrived since the window detached; `0` while following. |
| `maxScroll` | The scroll position that shows the newest point (`dataSize - windowSize`). |
| `currentScroll` | The scroll position currently rendered, in data indices. |
| `detach()` | Detaches programmatically, e.g. when your own control takes over. |
| `jumpToLatest(animation)` | `suspend`. Animates back to the newest point, resumes following, and clears `pendingCount`. `Animation.Disabled` jumps instantly. |

---

## The "jump to latest" control

`ChartInteractionConfig.jumpToLatest` is a composable slot rendered over the bottom
centre of the plot **while the window is detached**, and hidden again as soon as it
follows the newest data. You write no visibility logic.

### The built-in pill

```kotlin
import com.himanshoe.charty.common.streaming.ChartJumpToLatestPill

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

`ChartJumpToLatestPill` is built purely on `compose.foundation`, so it carries no
Material dependency and inherits none of a host theme's typography. Style it
directly:

```kotlin
ChartJumpToLatestPill(
    state = state,
    text = { count -> "$count new readings" },
    backgroundColor = ChartyColor.Gradient(listOf(ChartyColors.Blue, ChartyColors.Teal)),
    textStyle = TextStyle(color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    animation = Animation.Fast,
)
```

Its `backgroundColor` defaults to the ambient `ChartyTheme` primary colour.

### A fully custom overlay

The slot receives the same `StreamingState` you passed, so any composable works:

```kotlin
ChartInteractionConfig(
    streamingState = streaming,
    jumpToLatest = { state ->
        val scope = rememberCoroutineScope()
        Button(onClick = { scope.launch { state.jumpToLatest() } }) {
            Text(text = "${state.pendingCount} behind — catch up")
        }
    },
)
```

The chart re-drives the scroll animation itself once following resumes, so it
finishes the slide even though your overlay — and the coroutine scope that started
the jump — disappears the instant `isFollowing` flips back to `true`.

The slot requires **both** a `streamingState` and a rolling `visibleWindow`. Missing
either, nothing is rendered.

---

## Marking the newest value

`PersistentMarker` accepts a negative `dataIndex` that counts back from the end of
the drawn data, so `-1` keeps a label pinned to the newest point as the window rolls.

```kotlin
LineChart(
    data = { points },
    color = ChartyColor.Solid(ChartyColors.Blue),
    lineConfig =
        LineChartConfig(
            visibleWindow = 30,
            markers = listOf(PersistentMarker(dataIndex = -1)),
        ),
)
```

With no `label` the point's formatted value is used, which makes this the idiomatic
"current reading" badge. See [Common Configuration](../configurations/common-config.md#persistent-markers)
for the full marker surface.

---

## The one real limitation: crosshair vs. scrollback

**A crosshair and streaming scrollback cannot share a chart.** Both are drag
gestures, and the crosshair wins.

When a chart has a crosshair configured it claims the drag from the streaming state
for as long as that chart is in composition. The streaming pan gesture then consumes
nothing and moves nothing, so:

- the window keeps following the newest data and never detaches;
- because it never detaches, a `jumpToLatest` control never appears;
- `pendingCount` stays `0`.

**Tap-to-tooltip is unaffected either way.** Drag detection only reports movement
once the pointer passes touch slop, so a plain tap still reaches the chart's tooltip
handler whichever drag gesture is installed.

```kotlin
// Inspect values while streaming — no scrollback.
LineChart(
    data = { points },
    lineConfig = LineChartConfig(visibleWindow = 30),
    crosshair = ChartCrosshair(),
    interactionConfig = ChartInteractionConfig(streamingState = streaming), // scrollback inert
)

// Scroll back through history — no crosshair, but taps still raise tooltips.
LineChart(
    data = { points },
    lineConfig = LineChartConfig(visibleWindow = 30),
    interactionConfig =
        ChartInteractionConfig(
            streamingState = streaming,
            jumpToLatest = { state -> ChartJumpToLatestPill(state = state) },
        ),
    onPointClick = { point -> selected = point },
)
```

Pick one per chart. If you need both behaviours, stack two charts of the same
series and give each one a different job.

---

## Putting it together

```kotlin
@Composable
fun LiveThroughput(readings: List<LineData>) {
    val streaming = rememberStreamingState()

    LineChart(
        data = { readings },
        modifier = Modifier.fillMaxWidth().height(280.dp),
        color = ChartyColor.Gradient(listOf(ChartyColors.Blue, ChartyColors.Teal)),
        lineConfig =
            LineChartConfig(
                visibleWindow = 40,
                interpolation = LineInterpolation.SMOOTH,
                showPoints = false,
                animation = Animation.Fast,
                markers = listOf(PersistentMarker(dataIndex = -1)),
            ),
        interactionConfig =
            ChartInteractionConfig(
                streamingState = streaming,
                jumpToLatest = { state -> ChartJumpToLatestPill(state = state) },
            ),
    )
}
```

---

## Next steps

- **[Interactions](../configurations/interactions.md)** — which gestures can coexist on one chart.
- **[Synced crosshair](synced-crosshair.md)** — streaming charts can still mirror one crosshair across a stack.
- **[Colors and animations](../customization/colors-and-animations.md)** — the `Animation` type driving the slide.
