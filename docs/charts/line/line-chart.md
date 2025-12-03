# Line Chart

Classic line chart connecting data points to show trends.

## Basic Usage

```kotlin
import com.himanshoe.charty.line.LineChart
import com.himanshoe.charty.line.data.LineData

LineChart(
    dataCollection = listOf(
        LineData(10f, "Jan"),
        LineData(20f, "Feb"),
        LineData(15f, "Mar"),
        LineData(25f, "Apr")
    )
)
```

## Configuration

```kotlin
import com.himanshoe.charty.line.config.LineChartConfig

LineChart(
    dataCollection = data,
    config = LineChartConfig(
        lineWidth = 3.dp,
        showPoints = true,
        isCurved = true
    )
)
```

---

For more details, see [Chart Overview](../overview.md).

