# BlockBarChart

Best for a single categorical distribution shown as one horizontal row of coloured blocks — a compact alternative to a stacked bar when the visual presence of each category matters more than precise proportions.

![BlockBarChart](../../img/block_bar_chart.png)

```kotlin
BlockBarChart(
    data = {
        listOf(
            BlockData(value = 40f, color = ChartyColor.Solid(Color(0xFF6650A4))),
            BlockData(value = 25f, color = ChartyColor.Solid(Color(0xFFE91E63))),
            BlockData(value = 20f, color = ChartyColor.Solid(Color(0xFF00BCD4))),
            BlockData(value = 15f, color = ChartyColor.Solid(Color(0xFFFFB300))),
        )
    },
    modifier = Modifier.fillMaxWidth(),
    blockBarConfig = BlockBarChartConfig(
        cornerRadius = CornerRadius.Small,
        gapBetweenBlocks = 4.dp,
        barHeight = 16.dp,
    ),
    accessibilityDescription = "Market share: Segment A 40%, B 25%, C 20%, D 15%",
)
```

The composable is named **`BlockBarChart`** (it lives in `com.himanshoe.charty.block`).

Each `BlockData.value` sets the proportional width of its block, normalised so the blocks fill the available width. **Non-positive values are filtered out silently** before drawing.

`BlockData.color` is a required `ChartyColor`, so each block can be `Solid` or `Gradient`. There is no chart-level colour parameter.

## Sizing

The chart always fills the full available width (`Modifier.fillMaxWidth()` is applied internally) and takes its height from `blockBarConfig.barHeight` — setting a height on `modifier` has no effect.

```kotlin
blockBarConfig = BlockBarChartConfig(barHeight = 24.dp)
```

## Corner radius

```kotlin
blockBarConfig = BlockBarChartConfig(cornerRadius = CornerRadius.Custom(radius = 10f))
```

`None`, `Small` (the default), `Medium`, `Large`, `ExtraLarge`, or `Custom(radius)`.

## Accessibility

The chart attaches a generated summary ("Block bar chart, 4 segments. Largest segment: 40% of total."). Because there are no axes or labels, supplying a meaningful `accessibilityDescription` is worth the effort here more than on most charts. Pass an empty string to suppress it.

## `BlockBarChartConfig`

| Property | Type | Default | Description |
| --- | --- | --- | --- |
| `cornerRadius` | `CornerRadius` | `CornerRadius.Small` | Rounding of each block; also accepts `CornerRadius.Custom(radius)` |
| `gapBetweenBlocks` | `Dp` | `4.dp` | Gap between adjacent blocks; non-negative |
| `barHeight` | `Dp` | `16.dp` | Height of the row; must be positive |

## Limitations

- Not a Cartesian chart: no `visibleWindow`, no `markers`, no `animateValueChanges`, no crosshair.
- **No click callback, no tooltip, and no entry animation** — `BlockBarChart` has no `onBlockClick`, no `tooltip` parameter, and no `Animation` on its config.
