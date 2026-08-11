# CircularProgressIndicator

Best for showing one or more concentric progress rings (activity rings, goal completion, multi-metric progress).

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
        rotationEnabled = false,
        showCenterText = true,
    ),
    onRingClick = { ringData -> println("Tapped ring: ${ringData.label}") },
    centerContent = {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "Activity", style = MaterialTheme.typography.labelMedium)
            Text(text = "Today",   style = MaterialTheme.typography.bodySmall)
        }
    },
    accessibilityDescription = "Daily activity rings: Steps 75%, Calories 70%, Active Minutes 58%",
)
```

Each `CircularRingData.progress` is clamped to `[0, maxValue]` automatically. Rings are drawn concentrically from outermost (index 0) to innermost (last index).

**Key config options:**
- `showCenterText` — when `true`, the innermost ring's label is displayed in the center area
- `rotationEnabled` — allows the user to rotate the entire indicator with a gesture
- `centerContent` — a composable slot rendered in the center of all rings (similar to donut center)
