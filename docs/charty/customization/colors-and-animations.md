# Colors and Animations

Charty uses two custom abstractions — `ChartyColor` and `Animation` — across all chart types. This page explains both, documents the built-in color palette, and shows how to customise colors per data item, configure animation speed, and use gradient fills.

---

## ChartyColor

`ChartyColor` is a sealed class that represents either a solid color or a multi-stop gradient. All chart APIs that accept a color accept a `ChartyColor` rather than a raw `Color`.

```kotlin
sealed class ChartyColor {
    data class Solid(val color: Color) : ChartyColor()
    data class Gradient(val colors: List<Color>) : ChartyColor()
}
```

Internally the library calls `.value` on a `ChartyColor` to obtain a `List<Color>`. For `Solid`, this returns a single-element list; for `Gradient`, it returns the full color list. You never need to call `.value` yourself.

### Creating colors

```kotlin
// Solid color
val red = ChartyColor.Solid(Color.Red)
val brand = ChartyColor.Solid(Color(0xFF6200EE))

// Gradient (three-stop)
val sunset = ChartyColor.Gradient(
    listOf(Color(0xFFFF6F00), Color(0xFFE91E63), Color(0xFF9C27B0))
)

// Two-stop gradient
val oceanGradient = ChartyColor.Gradient(
    listOf(Color(0xFF006064), Color(0xFF00BCD4))
)
```

### Using ChartyColor in charts

`color` (or `colors` on multi-series charts) is a top-level parameter on every chart composable, not nested inside the config object:

```kotlin
BarChart(
    data = barData,
    color = ChartyColor.Solid(Color(0xFF1565C0)),
)

LineChart(
    data = lineData,
    color = ChartyColor.Gradient(
        listOf(Color(0xFF1565C0), Color(0xFF42A5F5))
    ),
)
```

### Per-item color override

`BarData` accepts an optional `color: ChartyColor?` field. `PieData` accepts an optional `color: Color?` field. When set, this overrides the chart-level color for that specific data point.

```kotlin
val barData = listOf(
    BarData(value = 40f, label = "Jan"),                              // uses chart-level color
    BarData(value = 75f, label = "Feb", color = ChartyColor.Solid(Color.Red)), // highlighted
    BarData(value = 55f, label = "Mar"),
    BarData(value = 90f, label = "Apr", color = ChartyColor.Solid(Color(0xFF388E3C))), // green
)

BarChart(
    data = { barData },
    color = ChartyColor.Solid(Color(0xFF1565C0)), // default for unlabeled bars
)
```

The same pattern applies to `PieData`:

```kotlin
val pieData = listOf(
    PieData(value = 30f, label = "A", color = Color(0xFF1565C0)),
    PieData(value = 20f, label = "B", color = Color(0xFFE91E63)),
    PieData(value = 50f, label = "C"),
)
```

---

## ChartyColors palette

`ChartyColors` is an object that exposes a set of ready-to-use `Color` and `ChartyColor` values that match Charty's design language.

| Name | Type | Notes |
|---|---|---|
| `ChartyColors.Blue` | `Color` | Primary blue (#2196F3) |
| `ChartyColors.Red` | `Color` | Accent red (#F44336) |
| `ChartyColors.Green` | `Color` | Success green (#4CAF50) |
| `ChartyColors.Orange` | `Color` | Warning orange (#FF9800) |
| `ChartyColors.Purple` | `Color` | Purple (#9C27B0) |
| `ChartyColors.Pink` | `Color` | Pink (#E91E63) |
| `ChartyColors.Cyan` | `Color` | Cyan (#00BCD4) |
| `ChartyColors.Teal` | `Color` | Teal (#009688) |
| `ChartyColors.Indigo` | `Color` | Indigo (#3F51B5) |
| `ChartyColors.Amber` | `Color` | Amber (#FFC107) |
| `ChartyColors.DefaultSolid` | `ChartyColor.Solid` | Default solid color (Blue) |
| `ChartyColors.DefaultGradient` | `ChartyColor.Gradient` | Blue → Green → Orange; default for stacked/multi-value charts |
| `ChartyColors.DefaultMultiline` | `ChartyColor.Gradient` | Pink → Blue → Green; default for multiline and comparison charts |
| `ChartyColors.ModernPalette` | `ChartyColor.Gradient` | Blue, Cyan, Purple, Pink, Orange |
| `ChartyColors.WarmPalette` | `ChartyColor.Gradient` | Red, Orange, Amber, Pink |
| `ChartyColors.CoolPalette` | `ChartyColor.Gradient` | Blue, Cyan, Teal, Indigo |
| `ChartyColors.NaturePalette` | `ChartyColor.Gradient` | Green, Teal, Cyan, Blue |
| `ChartyColors.VibrantPalette` | `ChartyColor.Gradient` | 8-stop high-contrast palette |
| `ChartyColors.PastelPalette` | `ChartyColor.Gradient` | Soft, muted pastel colors |
| `ChartyColors.DarkPalette` | `ChartyColor.Gradient` | Deep, rich dark shades |
| `ChartyColors.MonochromeBlue` | `ChartyColor.Gradient` | Five shades of blue |
| `ChartyColors.BusinessPalette` | `ChartyColor.Gradient` | Professional corporate colors |
| `ChartyColors.FinancialGradient` | `ChartyColor.Gradient` | Red → Orange → Amber → Green |

### Example usage

```kotlin
// Solid color from the palette
BarChart(
    data = barData,
    color = ChartyColor.Solid(ChartyColors.Teal),
)

// Pre-built gradient as the chart color
LineChart(
    data = lineData,
    color = ChartyColors.DefaultGradient,
)

// Per-segment color overrides in a BarGroup
val groupData = listOf(
    BarGroup(
        label = "Q1",
        values = listOf(60f, 45f),
        colors = listOf(
            ChartyColor.Solid(ChartyColors.Blue),
            ChartyColor.Solid(ChartyColors.Orange),
        )
    ),
)
```

---

## Animation

All chart configs expose an `animation` property of type `Animation`. The library uses this to control how the chart draws itself when first composed or when the data changes.

```kotlin
sealed interface Animation {
    data object Disabled : Animation
    data class Enabled(val duration: Int = 800) : Animation

    companion object {
        val Default = Enabled(800)   // 800 ms
        val Fast    = Enabled(400)   // 400 ms
        val Slow    = Enabled(1200)  // 1200 ms
    }
}
```

### Presets

| Preset | Duration |
|---|---|
| `Animation.Default` | 800 ms |
| `Animation.Fast` | 400 ms |
| `Animation.Slow` | 1 200 ms |
| `Animation.Disabled` | No animation |

### Using presets

```kotlin
BarChart(
    data = barData,
    barConfig = BarChartConfig(
        animation = Animation.Fast,
    ),
)

LineChart(
    data = lineData,
    lineConfig = LineChartConfig(
        animation = Animation.Slow,
    ),
)
```

### Custom duration

Pass any integer millisecond value to `Animation.Enabled`:

```kotlin
BarChart(
    data = barData,
    barConfig = BarChartConfig(
        animation = Animation.Enabled(duration = 600),
    ),
)
```

### Disabling animation

Use `Animation.Disabled` to skip the entrance animation entirely. This is useful in tests or on lower-end devices:

```kotlin
BarChart(
    data = barData,
    barConfig = BarChartConfig(
        animation = Animation.Disabled,
    ),
)
```

---

## Smooth rendering

Beyond the entrance `Animation`, two additional techniques produce visually smoother charts.

### Smooth curves for line charts

Set `smoothCurve = true` in `LineChartConfig` to replace straight point-to-point segments with cubic Bézier curves. This applies to `LineChart`, `AreaChart`, `MultilineChart`, and `StackedAreaChart`.

```kotlin
LineChart(
    data = lineData,
    color = ChartyColor.Solid(Color(0xFF6650A4)),
    lineConfig = LineChartConfig(
        smoothCurve = true,
        animation = Animation.Default,
    ),
)
```

```kotlin
MultilineChart(
    data = seriesData,
    colors = ChartyColor.Gradient(
        listOf(ChartyColors.Blue, ChartyColors.Pink, ChartyColors.Teal)
    ),
    lineConfig = LineChartConfig(
        smoothCurve = true,
        animation = Animation.Default,
        showGradientFill = true,
        gradientFillAlpha = 0.2f,
    ),
)
```

### Smooth easing for WavyChart

`WavyChartConfig` exposes `animationEasing: Easing`. The default is `FastOutSlowInEasing`, which gives a natural acceleration-then-deceleration feel. You can swap it for any other Compose `Easing`:

```kotlin
WavyChart(
    data = wavyData,
    color = ChartyColor.Solid(Color(0xFF6650A4)),
    wavyConfig = WavyChartConfig(
        waveSegments = 40,
        animationDurationMillis = 800,
        animationEasing = FastOutSlowInEasing,
        phaseOffsetPerBar = 0.3f,  // ripple each bar slightly out of phase
    ),
)
```

| Easing | Feel |
|---|---|
| `FastOutSlowInEasing` | Accelerates then decelerates — most natural (default) |
| `LinearEasing` | Constant speed |
| `EaseOutBounce` | Bouncy overshoot on completion |

### Recommended defaults for a polished look

```kotlin
// Bar chart — smooth entrance, rounded tops
BarChartConfig(
    cornerRadius = CornerRadius.Large,
    animation = Animation.Default,      // 800 ms ease-in-out
)

// Line chart — bezier curves + gradient fill
LineChartConfig(
    smoothCurve = true,
    animation = Animation.Default,
    showGradientFill = true,
    gradientFillAlpha = 0.2f,
)

// Wavy chart — smooth deceleration + ripple
WavyChartConfig(
    waveSegments = 40,
    animationDurationMillis = 800,
    animationEasing = FastOutSlowInEasing,
    phaseOffsetPerBar = 0.3f,
)
```

---

## MultilineChart — per-series color assignment

`MultilineChart` accepts a `colors: ChartyColor` parameter. Each series is assigned a color by index from the gradient's color list using a wrap-around:

```
seriesColor = colors.value[seriesIndex % colors.value.size]
```

The default is `ChartyColors.DefaultMultiline` (Pink → Blue → Green). Pass a `ChartyColor.Gradient` with one color per series to override:

```kotlin
MultilineChart(
    data = seriesData,
    colors = ChartyColor.Gradient(
        listOf(
            ChartyColors.Blue,
            ChartyColors.Red,
            ChartyColors.Green,
            ChartyColors.Orange,
        )
    ),
    lineConfig = LineChartConfig(),
)
```

If you have more series than colors in the gradient list, the colors repeat from the beginning, so you only need to supply as many colors as you want in rotation.

---

## Legend color coordination

When `legendLabels` is set on `LineChartConfig`, Charty renders a legend row below (or above) the chart. Each legend swatch is automatically colored using the same per-series color that the chart line uses, so the legend always stays in sync with the data without any extra work.

```kotlin
MultilineChart(
    data = seriesData,
    colors = ChartyColor.Gradient(
        listOf(ChartyColors.Blue, ChartyColors.Red, ChartyColors.Green)
    ),
    lineConfig = LineChartConfig(
        legendLabels = listOf("Revenue", "Expenses", "Profit"),
        legendTextStyle = TextStyle(
            fontSize = 12.sp,
            color = Color.Unspecified, // Color.Unspecified lets each label inherit its series color
        ),
    ),
)
```

The same `legendLabels` / `legendTextStyle` properties are available on `StackedAreaChart`:

```kotlin
StackedAreaChart(
    data = areaData,
    lineConfig = LineChartConfig(
        legendLabels = listOf("Layer A", "Layer B", "Layer C"),
    ),
)
```

> Pass `Color.Unspecified` as `legendTextStyle.color` to make each legend label render in its own series color. Pass any explicit color (e.g. `Color.Black`) to use the same color for all labels.

---

## Gradient fill in MultilineChart

`MultilineChart` supports an optional shaded area beneath each line. The fill uses the same color as the line, drawn at a reduced alpha so the lines remain legible.

Enable it via `LineChartConfig`:

```kotlin
MultilineChart(
    data = seriesData,
    lineConfig = LineChartConfig(
        showGradientFill = true,
        gradientFillAlpha = 0.3f, // 0.0 (transparent) to 1.0 (opaque)
    ),
)
```

| Property | Default | Description |
|---|---|---|
| `showGradientFill` | `false` | When `true`, draws a filled area from each line down to the x-axis. |
| `gradientFillAlpha` | `0.3f` | Alpha applied to the line's color when rendering the filled area. Lower values produce a lighter fill. |

The fill is drawn before the line in the rendering order, so the line always appears on top of the shaded area regardless of overlap between series.

```kotlin
// Subtle fill
LineChartConfig(showGradientFill = true, gradientFillAlpha = 0.15f)

// Strong fill
LineChartConfig(showGradientFill = true, gradientFillAlpha = 0.5f)
```

> `showGradientFill` is only available on `MultilineChart`. `AreaChart` and `StackedAreaChart` always render filled areas as part of their core visual.
