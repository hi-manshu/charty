# WavyChart

Best for drawing attention to trends using aesthetically animated wavy bar shapes.

```kotlin
WavyChart(
    data = {
        listOf(
            BarData(label = "Mon", value = 150f),
            BarData(label = "Tue", value = 90f),
            BarData(label = "Wed", value = 210f),
            BarData(label = "Thu", value = 130f),
        )
    },
    modifier = Modifier.fillMaxWidth().height(300.dp),
    color = ChartyColor.Solid(Color(0xFF6650A4)),
    wavyConfig = WavyChartConfig(
        barWidthFraction = 0.8f,
        waveAmplitudeFractionOfBarWidth = 0.25f,
        waveSegments = 40,
        animationDurationMillis = 800,
        animationEasing = FastOutSlowInEasing,  // smooth deceleration
        phaseOffsetPerBar = 0.3f,               // cascading ripple across bars
    ),
    crosshairConfig = ChartCrosshairConfig(),
)
```

**Key config options:**
- `waveAmplitudeFractionOfBarWidth` — how pronounced the wave edges are (`0f` = straight bars, `0.33f` = default)
- `waveSegments` — higher values produce smoother curves at the cost of draw calls (40 is a good default)
- `animationEasing` — swap `LinearEasing` for `FastOutSlowInEasing` to get a smooth deceleration feel
- `phaseOffsetPerBar` — staggers the wave phase across bars for a cascading ripple effect (`0f` = all bars in sync)
