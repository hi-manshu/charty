# Stacked Bar Chart
For more details, see [Chart Overview](../overview.md).

---

- Display cumulative values
- Compare part-to-whole relationships
- Show composition of totals across categories

## When to Use

```
)
    )
        )
            values = listOf(120f, 90f, 70f)
            label = "Q2",
        BarGroup(
        ),
            values = listOf(100f, 80f, 60f)
            label = "Q1",
        BarGroup(
    dataCollection = listOf(
StackedBarChart(

import com.himanshoe.charty.bar.data.BarGroup
import com.himanshoe.charty.bar.StackedBarChart
```kotlin

## Basic Usage

Display multiple data series stacked vertically.


