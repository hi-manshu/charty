# Radar Chart

Multi-axis spider/web chart for comparing multiple variables.

## Basic Usage

```kotlin
import com.himanshoe.charty.radar.RadarChart
import com.himanshoe.charty.radar.data.RadarData

RadarChart(
    dataCollection = listOf(
        RadarData(0.8f, "Speed"),
        RadarData(0.6f, "Power"),
        RadarData(0.9f, "Accuracy")
    )
)
```

---

For more details, see [Chart Overview](../overview.md).

