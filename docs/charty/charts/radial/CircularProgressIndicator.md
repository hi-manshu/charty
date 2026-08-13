# CircularProgressIndicator

Best for one or more concentric progress rings — activity rings, goal completion, multi-metric progress.

![CircularProgressIndicator](../../img/circular_progress_indicator.png)

```kotlin
CircularProgressIndicator(
    rings = {
        listOf(
            CircularRingData(
                label = "Steps",
                progress = 7500f,
                maxValue = 10000f,
                color = ChartyColor.Solid(Color(0xFFE91E63)),
            ),
            CircularRingData(
                label = "Calories",
                progress = 420f,
                maxValue = 600f,
                color = ChartyColor.Solid(Color(0xFF6650A4)),
            ),
            CircularRingData(
                label = "Active Minutes",
                progress = 35f,
                maxValue = 60f,
                color = ChartyColor.Solid(Color(0xFF00BCD4)),
            ),
        )
    },
    modifier = Modifier.size(280.dp),
    config = CircularProgressConfig(
        animation = Animation.Default,
        gapBetweenRings = 10f,
        strokeCap = StrokeCap.Round,
    ),
    onRingClick = { ring, index -> println("Tapped ${ring.label} (index $index)") },
    centerContent = {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Activity", style = MaterialTheme.typography.labelMedium)
            Text(text = "Today", style = MaterialTheme.typography.bodySmall)
        }
    },
    accessibilityDescription = "Daily activity rings: Steps 75%, Calories 70%, Active Minutes 58%",
)
```

Note the parameter name: `rings`, not `data`. `onRingClick` receives `(ring: CircularRingData, index: Int)`.

Rings are drawn concentrically from outermost (index 0) inward. `CircularRingData` requires a non-blank `label`, non-negative `progress`, positive `maxValue`, and a `color: ChartyColor`. Its helpers — `calculatePercentage()`, `calculateSweepAngle()`, `isComplete()`, `withClampedProgress()` — all clamp progress into `[0, maxValue]`, so an overshooting ring never draws past a full turn.

Each ring may also carry a `backgroundColor` (defaults to its own colour at 20% alpha), plus `shadowColor` and `shadowRadius` for use with `enableShadows`.

## Centre content

```kotlin
centerContent = {
    Text(text = "78%", style = MaterialTheme.typography.headlineMedium)
}
```

`centerContent` is a `BoxScope` composable rendered centred over the rings. When it is `null` and `config.showCenterText` is `true`, the indicator instead draws **the first (outermost) ring's percentage** as text — not its label, and not the innermost ring.

```kotlin
config = CircularProgressConfig(
    showCenterText = true,
    centerTextStyle = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold),
)
```

## Rotation

`rotationEnabled` starts a **continuous automatic rotation** of the whole indicator, looping every `rotationDurationMs`. It is a decorative animation, not a user gesture — there is no drag-to-rotate.

```kotlin
config = CircularProgressConfig(
    rotationEnabled = true,
    rotationDurationMs = 4000,
)
```

## Geometry

```kotlin
config = CircularProgressConfig(
    startAngleDegrees = -90f,
    ringDirection = RingDirection.COUNTER_CLOCKWISE,
    gapBetweenRings = 12f,
    centerHoleRatio = 0.3f,
    paddingDp = 24f,
)
```

`centerHoleRatio` reserves an untouched hole at the middle (`0f..0.5f`) and also shrinks the tappable area accordingly.

## Accessibility

The indicator attaches a generated summary ("Circular progress indicator, 3 rings. Steps: 75%. Calories: 70%. …"). Override it with `accessibilityDescription`, or pass an empty string to suppress it.

## `CircularProgressConfig`

| Property | Type | Default | Description |
| --- | --- | --- | --- |
| `gapBetweenRings` | `Float` | `8f` | Gap between concentric rings; non-negative |
| `startAngleDegrees` | `Float` | `-90f` | Angle progress starts from (12 o'clock) |
| `ringDirection` | `RingDirection` | `CLOCKWISE` | `CLOCKWISE` or `COUNTER_CLOCKWISE` |
| `strokeCap` | `StrokeCap` | `StrokeCap.Round` | Cap of each ring's stroke |
| `animation` | `Animation` | `Animation.Default` | Progress sweep-in animation |
| `enableShadows` | `Boolean` | `false` | Draws a ring's shadow when it sets `shadowColor` and `shadowRadius` |
| `centerHoleRatio` | `Float` | `0f` | Untouched centre hole; `0f..0.5f` |
| `rotationEnabled` | `Boolean` | `false` | Continuous automatic rotation |
| `rotationDurationMs` | `Int` | `3000` | Period of one rotation; must be positive |
| `interactionEnabled` | `Boolean` | `true` | Enables the tap handling behind `onRingClick` |
| `showCenterText` | `Boolean` | `false` | Draws the outermost ring's percentage; ignored when `centerContent` is set |
| `paddingDp` | `Float` | `16f` | Padding around the whole indicator; non-negative |
| `centerTextStyle` | `TextStyle` | 24 sp, bold, black | Style of that centre text |

## Limitations

- Not a Cartesian chart: no `visibleWindow`, no `markers`, no `animateValueChanges`, no crosshair, no tooltip slot.
- Ring stroke width is derived from the size, the ring count, and `gapBetweenRings`; it is not directly configurable.
