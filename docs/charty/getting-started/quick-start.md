# Quick Start

This guide takes you from zero to a rendered chart in under two minutes.

## Minimal bar chart

```kotlin
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.himanshoe.charty.bar.BarChart
import com.himanshoe.charty.bar.data.BarData

BarChart(
    data = {
        listOf(
            BarData(label = "Jan", value = 120f),
            BarData(label = "Feb", value = 95f),
            BarData(label = "Mar", value = 160f),
            BarData(label = "Apr", value = 80f),
            BarData(label = "May", value = 200f),
        )
    },
    modifier = Modifier
        .fillMaxWidth()
        .height(300.dp),
)
```

`data` is always a **lambda** (`() -> List<BarData>`), not a bare list. This
lets Charty skip unnecessary recompositions when the surrounding state changes.

## Customising the colour

Use `ChartyColor.Solid` for a flat colour or `ChartyColor.Gradient` for a
multi-stop gradient. `ChartyColors` provides a ready-made palette of named
constants.

```kotlin
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.color.ChartyColors
import androidx.compose.ui.graphics.Color

// Solid colour from the built-in palette
BarChart(
    data = { myData },
    color = ChartyColor.Solid(ChartyColors.Blue),   // Material Blue 500
)

// Custom solid colour
BarChart(
    data = { myData },
    color = ChartyColor.Solid(Color(0xFF6200EE)),
)

// Two-stop gradient (top → bottom)
BarChart(
    data = { myData },
    color = ChartyColor.Gradient(
        listOf(ChartyColors.Blue, ChartyColors.Teal)
    ),
)
```

Available `ChartyColors` constants: `Blue`, `Red`, `Green`, `Orange`, `Purple`,
`Pink`, `Teal`, `Cyan`, `Indigo`, `Amber`. Pre-built gradients:
`DefaultGradient`, `DefaultMultiline`, `ModernPalette`, `WarmPalette`,
`CoolPalette`, `NaturePalette`.

## Enabling animation

Pass an `Animation` value inside `BarChartConfig`. Three presets are available:

```kotlin
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.common.config.Animation

BarChart(
    data = { myData },
    barConfig = BarChartConfig(
        animation = Animation.Default,   // 800 ms tween (recommended)
        // animation = Animation.Fast,   // 400 ms
        // animation = Animation.Slow,   // 1 200 ms
        // animation = Animation.Smooth, // physics-based spring
        // animation = Animation.Bouncy, // spring with a gentle bounce
        // animation = Animation.Enabled(duration = 600),  // custom tween
        // animation = Animation.Disabled,                 // no animation
    ),
)
```

`animation` drives the chart's **entry reveal**. To also tween whenever the *data* changes, set `animateValueChanges = true` alongside it:

```kotlin
BarChart(
    data = { liveData },
    barConfig = BarChartConfig(
        animation = Animation.Fast,
        animateValueChanges = true,
    ),
)
```

## Handling clicks

Supply a lambda to `onBarClick`. It receives the `BarData` of the tapped bar.

```kotlin
BarChart(
    data = { myData },
    onBarClick = { barData ->
        println("Tapped: ${barData.label} = ${barData.value}")
    },
)
```

## Putting it all together

```kotlin
BarChart(
    data = {
        listOf(
            BarData(label = "Jan", value = 120f),
            BarData(label = "Feb", value = 95f),
            BarData(label = "Mar", value = 160f),
        )
    },
    modifier = Modifier
        .fillMaxWidth()
        .height(300.dp),
    color = ChartyColor.Gradient(
        listOf(ChartyColors.Blue, ChartyColors.Teal)
    ),
    barConfig = BarChartConfig(
        animation = Animation.Default,
        showDataLabels = true,
    ),
    onBarClick = { barData ->
        println("Tapped: ${barData.label} = ${barData.value}")
    },
)
```

## A line chart with interactions

```kotlin
import com.himanshoe.charty.common.gesture.ChartCrosshair
import com.himanshoe.charty.line.LineChart
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.common.config.LineInterpolation

LineChart(
    data = { priceData },
    modifier = Modifier.fillMaxWidth().height(300.dp),
    color = ChartyColor.Solid(ChartyColors.Blue),
    lineConfig = LineChartConfig(
        interpolation = LineInterpolation.SMOOTH,
        downsampleThreshold = 800,        // stay smooth over very large series
    ),
    crosshair = ChartCrosshair(),         // draggable guide line + label
)
```

## Next steps

### Guides

- **[Streaming and live data](../guides/streaming.md)** — rolling windows, scrollback, and "jump to latest".
- **[Exporting charts as PNG](../guides/exporting-charts.md)** — capture and share a chart on every platform.
- **[Datetime axis and localization](../guides/datetime-axis.md)** — smart time ticks, translated and reformatted.
- **[Synced crosshair](../guides/synced-crosshair.md)** — one guide line across a stack of charts.

### Reference

- **More charts** — explore `LineChart`, `PieChart`, `StackedBarChart`, and
  20 other chart types in the chart reference.
- **[Common configuration](../configurations/common-config.md)** — axes, grid,
  reference lines, tooltips, markers, and rolling windows.
- **[Interactions](../configurations/interactions.md)** — crosshair, zoom/pan,
  brush selection, and which gestures can share a chart.
- **[Colors and animations](../customization/colors-and-animations.md)** —
  `ChartyColor`, the palette, and the `Animation` type.
- **[Theming](../customization/theming.md)** — map your design system's colors,
  typography, shapes, and metrics onto every chart at once.
- **Accessibility** — provide a custom content description through
  `ChartInteractionConfig.accessibilityDescription`.
