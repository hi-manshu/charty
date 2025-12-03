# Bubble Chart

Scatter plot with variable-sized bubbles for a third data dimension.

## Basic Usage

```kotlin
import com.himanshoe.charty.point.BubbleChart
import com.himanshoe.charty.point.data.BubbleData

BubbleChart(
    dataCollection = listOf(
        BubbleData(x = 10f, y = 20f, size = 50f),
        BubbleData(x = 15f, y = 35f, size = 80f)
    )
)
```

---

For more details, see [Chart Overview](../overview.md).

