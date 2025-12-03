# Multiline Chart

Display multiple data series on the same chart.

## Basic Usage

```kotlin
import com.himanshoe.charty.line.MultilineChart
import com.himanshoe.charty.line.data.LineGroup

MultilineChart(
    dataCollection = listOf(
        LineGroup(
            label = "Series 1",
            points = listOf(/* points */)
        ),
        LineGroup(
            label = "Series 2",
            points = listOf(/* points */)
        )
    )
)
```

---

For more details, see [Chart Overview](../overview.md).

